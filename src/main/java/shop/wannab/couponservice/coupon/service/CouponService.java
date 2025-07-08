package shop.wannab.couponservice.coupon.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.couponservice.client.BookServiceClient;
import shop.wannab.couponservice.client.UserServiceClient;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponsDto;
import shop.wannab.couponservice.coupon.dto.BookCouponDto;
import shop.wannab.couponservice.coupon.dto.CouponResponseToUserDto;
import shop.wannab.couponservice.coupon.dto.CouponUsageRequestDto;
import shop.wannab.couponservice.coupon.dto.OrderCouponDto;
import shop.wannab.couponservice.coupon.dto.OrderCouponsRequestDto;
import shop.wannab.couponservice.coupon.dto.PageResponseDto;
import shop.wannab.couponservice.coupon.dto.TryApplyCouponsRequestDto;
import shop.wannab.couponservice.coupon.dto.TryApplyCouponsResponseDto;
import shop.wannab.couponservice.coupon.entity.Coupon;
import shop.wannab.couponservice.coupon.entity.CouponStatus;
import shop.wannab.couponservice.coupon.exception.CouponErrorCode;
import shop.wannab.couponservice.coupon.exception.CouponException;
import shop.wannab.couponservice.coupon.repository.CouponRepository;
import shop.wannab.couponservice.coupon.repository.impl.CouponRepositoryImpl;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;

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
        CouponPolicy welcomePolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME,
                        PolicyStatus.ACTIVE)
                .orElseThrow(() -> new CouponException(CouponErrorCode.WELCOME_COUPON_POLICY_NOT_FOUND));

        if (welcomePolicy == null) {
            throw new CouponException(CouponErrorCode.COUPON_NOT_FOUND);
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, welcomePolicy)) {
            throw new CouponException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        saveNewCoupon(userId, welcomePolicy, "WC");
    }

    @Transactional
    public void issueGeneralCoupon(Long userId, Long couponPolicyId) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId)
                .orElseThrow(() -> new CouponPolicyException(CouponPolicyErrorCode.POLICY_NOT_FOUND));

        if (couponPolicy == null) {
            throw new CouponException(CouponErrorCode.COUPON_NOT_FOUND);
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, couponPolicy)) {
            throw new CouponException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        saveNewCoupon(userId, couponPolicy, "CST");
    }

    @Transactional
    public void issueBirthdayCoupon(int month) {
        System.out.println("생일 쿠폰 발급 로직 시작 (월: " + month + ")");

        CouponPolicy birthdayPolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.BIRTHDAY,
                        PolicyStatus.ACTIVE)
                .orElseThrow(() -> new CouponPolicyException(CouponPolicyErrorCode.POLICY_NOT_FOUND));

        if (birthdayPolicy == null) {
            throw new CouponException(CouponErrorCode.BIRTHDAY_COUPON_POLICY_NOT_FOUND);
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
                throw new CouponException(CouponErrorCode.BIRTHDAY_COUPON_ISSUE_FAILED);
            }
        }
    }

    private void saveNewCoupon(Long userId, CouponPolicy couponPolicy, String prefix) {
        Coupon createdCoupon = Coupon.createNewCoupon(userId, couponPolicy, prefix);
        couponRepository.save(createdCoupon);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<CouponResponseToUserDto> getUserCoupons(Long userId, Pageable pageable) {
        Pageable pageableWithMultiSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("status"),
                        Sort.Order.asc("endDate"),
                        Sort.Order.desc("issuedAt")
                )
        );
        Page<Coupon> couponPage = couponRepository.findByUserId(userId, pageableWithMultiSort);
        Page<CouponResponseToUserDto> dtoPage = couponPage.map(CouponResponseToUserDto::new);
        return new PageResponseDto<>(dtoPage);
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
                    .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

            if (!coupon.getUserId().equals(userId)) {
                throw new CouponException(CouponErrorCode.COUPON_OWNER_NOT_MATCH);
            }

            if (coupon.getStatus() != CouponStatus.NOT_USED) {
                throw new CouponException(CouponErrorCode.COUPON_ALREADY_USED_OR_EXPIRED);

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
        for (Map.Entry<Long, Long> entry : couponIdToBookIdMap.entrySet()) {
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
