package shop.wannab.couponservice.coupon.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponRepositoryImpl couponRepositoryImpl;
    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;

    @Transactional
    public void issueWelcomeCouponForNewUser(Long userId) {
        log.info("action=issueWelcomeCouponForNewUser, userId={}, message=\"웰컴 쿠폰 발급 시도.\"", userId);
        CouponPolicy welcomePolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME,
                        PolicyStatus.ACTIVE)
                .orElseThrow(() -> new CouponException(CouponErrorCode.WELCOME_COUPON_POLICY_NOT_FOUND));

        if (welcomePolicy != null) {
            if (couponRepository.existsByUserIdAndCouponPolicy(userId, welcomePolicy)) {
                throw new CouponException(CouponErrorCode.COUPON_ALREADY_ISSUED);
            }
            saveNewCoupon(userId, welcomePolicy, "WC");
        }
        log.info("action=issueWelcomeCouponForNewUser, userId={}, message=\"웰컴 쿠폰 발급 완료.\"", userId);
    }

    @Transactional
    public void issueGeneralCoupon(Long userId, Long couponPolicyId) {
        log.info("action=issueGeneralCoupon, userId={}, couponPolicyId={}, message=\"일반 쿠폰 발급 시도.\"", userId, couponPolicyId);
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId)
                .orElseThrow(() -> new CouponPolicyException(CouponPolicyErrorCode.POLICY_NOT_FOUND));

        if (couponPolicy == null) {
            throw new CouponException(CouponErrorCode.COUPON_NOT_FOUND);
        }

        if (couponRepository.existsByUserIdAndCouponPolicy(userId, couponPolicy)) {
            throw new CouponException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }

        saveNewCoupon(userId, couponPolicy, "CST");
        log.info("action=issueGeneralCoupon, userId={}, couponPolicyId={}, message=\"일반 쿠폰 발급 완료.\"", userId, couponPolicyId);

    }

    @Transactional
    public void issueBirthdayCoupon(int month) {
        log.info("action=issueBirthdayCoupon, month={}, message=\"생일 쿠폰 발급 시작.\"", month);

        CouponPolicy birthdayPolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.BIRTHDAY,
                        PolicyStatus.ACTIVE)
                .orElseThrow(() -> new CouponException(CouponErrorCode.BIRTHDAY_COUPON_POLICY_NOT_FOUND));

        if (birthdayPolicy != null) {
            List<Long> birthdayUserIds;

            try {
                birthdayUserIds = userServiceClient.getBirthdayUserIds(month);
                log.info("action=issueBirthdayCoupon,"
                        + " month={}, fetchedUserCount={},"
                        + " message=\"생일 유저 ID 조회 성공.\"", month, birthdayUserIds.size());
            } catch (Exception e) {
                birthdayUserIds = List.of();
            }
            int issuedCount = 0;
            for (Long userId : birthdayUserIds) {
                try {
                    if (!couponRepository.existsByUserIdAndCouponPolicy(userId, birthdayPolicy)) {
                        saveNewCoupon(userId, birthdayPolicy, "BD");
                        issuedCount++;
                    }
                } catch (Exception e) {
                    log.error("생일 쿠폰 발급 실패 유저 아이디 : {}", userId, e);
                }
            }
            log.info("action=issueBirthdayCoupon, month={}, totalUsers={}, issuedCount={}, message=\"생일 쿠폰 발급 로직 완료.\"",
                    month, birthdayUserIds.size(), issuedCount);
        }
    }

    private void saveNewCoupon(Long userId, CouponPolicy couponPolicy, String prefix) {
        Coupon createdCoupon = Coupon.createNewCoupon(userId, couponPolicy, prefix);
        couponRepository.save(createdCoupon);
        log.debug("action=saveNewCoupon, userId={}, couponPolicyId={}, couponId={}, message=\"새 쿠폰 저장됨.\"",
                userId, couponPolicy.getCouponPolicyId(), createdCoupon.getCouponId());
    }

    @Transactional(readOnly = true)
    public PageResponseDto<CouponResponseToUserDto> getUserCoupons(Long userId, Pageable pageable) {
        log.info("action=getUserCoupons, userId={}, page={}, size={}, sort={}, message=\"사용자 쿠폰 목록 조회 시작.\"",
                userId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

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
        log.info("action=getUserCoupons, userId={}, totalElements={}, totalPages={}, message=\"사용자 쿠폰 목록 조회 완료.\"",
                userId, dtoPage.getTotalElements(), dtoPage.getTotalPages());
        return new PageResponseDto<>(dtoPage);
    }

    @Transactional(readOnly = true)
    public ApplicableCouponsDto getUserApplicableCoupons(
            Long userId,
            OrderCouponsRequestDto requestDto) {

        log.info("action=getUserApplicableCoupons, userId={}, bookIdsCount={}, message=\"주문 적용 가능 쿠폰 조회 시작.\"",
                userId, requestDto.getBookIds().size());

        log.debug("action=getUserApplicableCoupons, target=BookServiceClient, method=getBookToCategoryMap, bookIds={}", requestDto.getBookIds());
        Map<Long, Set<Long>> bookIdToCategoryIdsMap =
                bookServiceClient.getBookToCategoryMap(requestDto.getBookIds());
        log.debug("action=getUserApplicableCoupons, target=BookServiceClient, method=getBookToCategoryMap, mappedCategoriesCount={}, message=\"책-카테고리 맵 수신.\"", bookIdToCategoryIdsMap.size());



        List<ApplicableCouponInfo> applicableCouponsInfo =
                couponRepositoryImpl.findApplicableCouponsForOrder(userId, bookIdToCategoryIdsMap);

        log.info("action=getUserApplicableCoupons, userId={}, applicableCouponCount={}, message=\"주문 적용 가능 쿠폰 조회 완료.\"",
                userId, applicableCouponsInfo.size());

        Map<Boolean, List<ApplicableCouponInfo>> partitionedCoupons = applicableCouponsInfo.stream()
                .collect(Collectors.partitioningBy(info -> info.targetBookId() == null));

        List<OrderCouponDto> orderCoupons = partitionedCoupons.get(true).stream()
                .map(info -> {
                    CouponPolicy policy = info.coupon().getCouponPolicy();
                    return new OrderCouponDto(
                            info.coupon().getCouponId(),
                            policy.getCouponPolicyName(),
                            policy.getDiscountValue(),
                            policy.getDiscountType()
                    );
                })
                .toList();

        Map<Long, List<BookCouponDto>> itemCoupons = partitionedCoupons.get(false).stream()
                .collect(Collectors.groupingBy(
                        ApplicableCouponInfo::targetBookId,
                        Collectors.mapping(info -> {
                            CouponPolicy policy = info.coupon().getCouponPolicy();
                            return new BookCouponDto(
                                    info.coupon().getCouponId(),
                                    policy.getCouponPolicyName(),
                                    policy.getDiscountValue(),
                                    policy.getDiscountType()
                            );
                        }, Collectors.toList())
                ));

        return new ApplicableCouponsDto(itemCoupons, orderCoupons);
    }

    @Transactional
    public void processUsedCoupons(Long userId, CouponUsageRequestDto requestDto) {
        log.info("action=processUsedCoupons, userId={}, orderId={}, usedCouponsCount={}, message=\"사용된 쿠폰 처리 시작.\"",
                userId, requestDto.getOrderId(), requestDto.getUsedCoupons().size());

        List<CouponUsageRequestDto.UsedCouponInfo> usedCoupons = requestDto.getUsedCoupons();

        for (CouponUsageRequestDto.UsedCouponInfo usedCouponInfo : usedCoupons) {
            Coupon coupon = couponRepository.findById(usedCouponInfo.getCouponId())
                    .orElseThrow(() -> {
                        log.warn("action=processUsedCoupons, userId={}, couponId={}, message=\"사용하려는 쿠폰을 찾을 수 없음.\"", userId, usedCouponInfo.getCouponId());
                        return new CouponException(CouponErrorCode.COUPON_NOT_FOUND);
                    });

            if (!coupon.getUserId().equals(userId)) {
                log.warn("action=processUsedCoupons, userId={}, couponId={}, message=\"쿠폰 소유자 불일치.\"", userId, usedCouponInfo.getCouponId());

                throw new CouponException(CouponErrorCode.COUPON_OWNER_NOT_MATCH);
            }

            if (coupon.getStatus() != CouponStatus.NOT_USED) {
                log.warn("action=processUsedCoupons, userId={}, couponId={}, status={}, message=\"쿠폰이 이미 사용되었거나 만료됨.\"", userId, usedCouponInfo.getCouponId(), coupon.getStatus());

                throw new CouponException(CouponErrorCode.COUPON_ALREADY_USED_OR_EXPIRED);

            }

            coupon.setStatus(CouponStatus.USED);
            coupon.setUsedAt(LocalDate.now());
            coupon.setOrderId(requestDto.getOrderId());

            if (usedCouponInfo.getBookId() != null) {
                coupon.setOrderBookId(usedCouponInfo.getBookId());
            }
            log.debug("action=processUsedCoupons, userId={}, couponId={}, message=\"쿠폰 사용 처리됨.\"", userId, coupon.getCouponId());
        }
        log.info("action=processUsedCoupons, userId={}, orderId={}, message=\"사용된 쿠폰 처리 완료.\"", userId, requestDto.getOrderId());

    }

    @Transactional(readOnly = true)
    public List<TryApplyCouponsResponseDto> applyCoupons(Long userId, TryApplyCouponsRequestDto requestDto) {
        log.info("action=applyCoupons, userId={}, requestedCouponsCount={}, message=\"쿠폰 적용 시도 시작.\"",
                userId, requestDto.getCouponAndBookIds().size());
        List<TryApplyCouponsResponseDto> respCouponDtoList = new ArrayList<>();
        Map<Long, Long> couponIdToBookIdMap = requestDto.getCouponAndBookIds();
        List<Long> requestedCouponIds = new ArrayList<>(couponIdToBookIdMap.keySet());
        List<Coupon> coupons = couponRepository.findAllById(requestedCouponIds);

        if (coupons.size() != requestedCouponIds.size()) {
            log.warn("action=applyCoupons, userId={}, message=\"요청된 일부 쿠폰 ID를 찾을 수 없음. 요청: {}, 조회: {}\"",
                    userId, requestedCouponIds.size(), coupons.size());
        }

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
        log.info("action=applyCoupons,userId={},appliedCount={},message=\"쿠폰 적용 완료\"", userId,respCouponDtoList.size());
        return respCouponDtoList;
    }
}
