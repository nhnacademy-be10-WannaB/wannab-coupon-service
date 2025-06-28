package shop.wannab.couponservice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.client.BookServiceClient;
import shop.wannab.couponservice.client.UserServiceClient;
import shop.wannab.couponservice.domain.coupon.Coupon;
import shop.wannab.couponservice.domain.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.domain.coupon.dto.ApplicableCouponsDto;
import shop.wannab.couponservice.domain.coupon.dto.BookCouponDto;
import shop.wannab.couponservice.domain.coupon.dto.CouponResponseToUserDto;
import shop.wannab.couponservice.domain.coupon.dto.CouponUsageRequestDto;
import shop.wannab.couponservice.domain.coupon.dto.OrderCouponDto;
import shop.wannab.couponservice.domain.coupon.dto.OrderCouponsRequestDto;
import shop.wannab.couponservice.domain.coupon.dto.TryApplyCouponsRequestDto;
import shop.wannab.couponservice.domain.coupon.dto.TryApplyCouponsResponseDto;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponStatus;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.PolicyStatus;
import shop.wannab.couponservice.repository.CouponPolicyRepository;
import shop.wannab.couponservice.repository.CouponRepository;
import shop.wannab.couponservice.repository.impl.CouponRepositoryImpl;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponRepositoryImpl couponRepositoryImpl;
    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;
    public CouponService(CouponRepository couponRepository,
                         CouponPolicyRepository couponPolicyRepository,
                         CouponRepositoryImpl couponRepositoryImpl,
                         UserServiceClient userServiceClient,
                         BookServiceClient bookServiceClient
                         ) {

        this.couponRepository = couponRepository;
        this.couponPolicyRepository = couponPolicyRepository;
        this.couponRepositoryImpl = couponRepositoryImpl;
        this.userServiceClient = userServiceClient;
        this.bookServiceClient = bookServiceClient;
    }

    @Transactional
    public void issueWelcomeCouponForNewUser(Long userId) {
        CouponPolicy welcomePolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE).orElse(null);

        if (welcomePolicy == null) {
            throw new IllegalArgumentException("웰컴 쿠폰이 없습니다.");
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, welcomePolicy)) {
            throw new IllegalArgumentException("이미 웰컴 쿠폰을 발급받았습니다.");
        }

        saveNewCoupon(userId, welcomePolicy, "WC");
    }

    @Transactional
    public void issueGeneralCoupon(Long userId, Long couponPolicyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId).orElse(null);

        if (couponPolicy == null) {
            throw new IllegalArgumentException("해당 쿠폰이 없습니다.");
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, couponPolicy)) {
            throw new IllegalArgumentException("이미 해당 쿠폰을 발급받았습니다.");
        }

        saveNewCoupon(userId, couponPolicy, "CST");
    }

    //TODO: FeignClient로 변환 후 처리
    @Transactional
    public void issueBirthdayCoupon(int month) {
        System.out.println("생일 쿠폰 발급 로직 시작 (월: " + month + ")");

        CouponPolicy birthdayPolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.BIRTHDAY,PolicyStatus.ACTIVE).orElse(null);
        if (birthdayPolicy == null) {
            throw new IllegalArgumentException("해당 쿠폰이 없습니다.");
        }

        List<Long> birthdayUserIds;

        try {
            birthdayUserIds = userServiceClient.getBirthdayUserIds(month);
        } catch (Exception e) {
            birthdayUserIds = List.of();
        }
        for (Long userId : birthdayUserIds) {
            try {
                saveNewCoupon(userId, birthdayPolicy, "BD");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("생일 쿠폰 발급 실패");
            }
        }
    }

    private void saveNewCoupon(Long userId, CouponPolicy couponPolicy, String prefix) {
        Coupon createdCoupon = Coupon.createNewCoupon(userId, couponPolicy, prefix);
        couponRepository.save(createdCoupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResponseToUserDto> getUserCoupons(Long userId) {
        List<Coupon> coupons = couponRepository.findByUserId(userId);
        List<CouponResponseToUserDto> respCouponDtoList = new ArrayList<>();
        for (Coupon coupon : coupons) {
            respCouponDtoList.add(new CouponResponseToUserDto(coupon));
        }
        return respCouponDtoList;
    }

    @Transactional(readOnly = true)
    public ApplicableCouponsDto getUserApplicableCoupons(
            Long userId,
            OrderCouponsRequestDto requestDto) {

        Map<Long, Long> bookIdToCategoryIdMap = new HashMap<>();
        List<Long> bookIds = requestDto.getBookIds();
        List<Long> categoryIds = bookServiceClient.getCategoryIds(bookIds);
        if (bookIds != null && categoryIds != null && bookIds.size() == categoryIds.size()) {
            for (int i = 0; i < bookIds.size(); i++) {
                bookIdToCategoryIdMap.put(bookIds.get(i), categoryIds.get(i));
            }
        }

        List<ApplicableCouponInfo> applicableCouponsInfo =
                couponRepositoryImpl.findApplicableCouponsForOrder(userId, bookIdToCategoryIdMap);

        Map<Long, List<BookCouponDto>> itemCoupons = new HashMap<>();
        List<OrderCouponDto> orderCoupons = new ArrayList<>();

        for (ApplicableCouponInfo info : applicableCouponsInfo) {
            Coupon coupon = info.coupon();
            CouponPolicy policy = coupon.getCouponPolicy();
            Long targetBookId = info.targetBookId();

            if (targetBookId != null) {
                BookCouponDto bookCouponDto = new BookCouponDto(
                        coupon.getCouponId(),
                        policy.getCouponPolicyName(),
                        policy.getDiscountValue(),
                        policy.getDiscountType()
                );
                itemCoupons.computeIfAbsent(targetBookId, k -> new ArrayList<>()).add(bookCouponDto);

            } else {
                OrderCouponDto orderCouponDto = new OrderCouponDto(
                        coupon.getCouponId(),
                        policy.getCouponPolicyName(),
                        policy.getDiscountValue(),
                        policy.getDiscountType()
                );
                orderCoupons.add(orderCouponDto);
            }
        }

        return new ApplicableCouponsDto(itemCoupons, orderCoupons);
    }

    @Transactional
    public void processUsedCoupons(Long userId, CouponUsageRequestDto requestDto) {

        List<CouponUsageRequestDto.UsedCouponInfo> usedCoupons = requestDto.getUsedCoupons();

        for (CouponUsageRequestDto.UsedCouponInfo usedCouponInfo : usedCoupons) {
            Coupon coupon = couponRepository.findById(usedCouponInfo.getCouponId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다. ID: " + usedCouponInfo.getCouponId()));


            if (!coupon.getUserId().equals(userId)) {
                throw new IllegalStateException("쿠폰의 소유자가 일치하지 않습니다.");
            }

            if (coupon.getStatus() != CouponStatus.NOT_USED) {
                throw new IllegalStateException("이미 사용되었거나 만료된 쿠폰입니다.");
            }


            coupon.setStatus(CouponStatus.USED);
            coupon.setUsedAt(LocalDate.now());
            coupon.setOrderId(requestDto.getOrderId());


            if (usedCouponInfo.getBookId() != null) {
                coupon.setOrderBookId(usedCouponInfo.getBookId());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TryApplyCouponsResponseDto> applyCoupons(Long userId, TryApplyCouponsRequestDto requestDto) {
        List<TryApplyCouponsResponseDto> respCouponDtoList = new ArrayList<>();
        Map<Long, Long> couponIdToBookIdMap = requestDto.getCouponAndBookIds();
        List<Long> requestedCouponIds = new ArrayList<>(couponIdToBookIdMap.keySet());
        List<Coupon> coupons = couponRepository.findAllById(requestedCouponIds);

        Map<Long, Coupon> couponMap = coupons.stream()
                .collect(Collectors.toMap(Coupon::getCouponId, coupon -> coupon));
        for(Map.Entry<Long, Long> entry : couponIdToBookIdMap.entrySet()) {
            Coupon coupon = couponMap.get(entry.getKey());
            Long targetBookId = entry.getValue();
            respCouponDtoList.add(new TryApplyCouponsResponseDto(
                    entry.getKey(),
                    coupon.getCouponPolicy().getDiscountValue(),
                    coupon.getCouponPolicy().getDiscountType(),
                    targetBookId));
        }
        return respCouponDtoList;
    }
}
