package shop.wannab.couponservice.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import shop.wannab.couponservice.client.BookServiceClient;
import shop.wannab.couponservice.client.UserServiceClient;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponsDto;
import shop.wannab.couponservice.coupon.dto.CouponUsageRequestDto;
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
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponPolicyRepository couponPolicyRepository;
    @Mock
    private CouponRepositoryImpl couponRepositoryImpl;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private BookServiceClient bookServiceClient;

    @InjectMocks
    private CouponService couponService;

    private CouponPolicy createMockPolicy(CouponType type, PolicyStatus status) {
        return CouponPolicy.builder()
            .couponPolicyName(type.name() + " 쿠폰")
            .couponType(type)
            .discountType(DiscountType.FIXED)
            .discountValue(1000)
            .maxDiscount(1000)
            .minPurchase(10000)
            .validDays(30)
            .policyStatus(status)
            .build();
    }

    private Coupon createMockCoupon(Long couponId, Long userId, CouponPolicy policy, CouponStatus status) {
        return Coupon.builder()
            .couponId(couponId)
            .userId(userId)
            .couponPolicy(policy)
            .status(status)
            .issuedAt(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();
    }

    @Test
    @DisplayName("신규 유저 웰컴 쿠폰 발급 성공")
    void issueWelcomeCoupon_Success() {
        Long userId = 1L;
        CouponPolicy welcomePolicy = createMockPolicy(CouponType.WELCOME, PolicyStatus.ACTIVE);

        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE))
            .thenReturn(Optional.of(welcomePolicy));
        when(couponRepository.existsByUserIdAndCouponPolicy(userId, welcomePolicy)).thenReturn(false);

        couponService.issueWelcomeCouponForNewUser(userId);

        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("신규 유저 웰컴 쿠폰 발급 실패 - 이미 발급된 경우")
    void issueWelcomeCoupon_Fail_AlreadyIssued() {
        Long userId = 1L;
        CouponPolicy welcomePolicy = createMockPolicy(CouponType.WELCOME, PolicyStatus.ACTIVE);

        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE))
            .thenReturn(Optional.of(welcomePolicy));
        when(couponRepository.existsByUserIdAndCouponPolicy(userId, welcomePolicy)).thenReturn(true);

        CouponException exception = assertThrows(CouponException.class, () ->
            couponService.issueWelcomeCouponForNewUser(userId));
        assertThat(exception.getErrorCode()).isEqualTo(CouponErrorCode.COUPON_ALREADY_ISSUED);
    }

    @Test
    @DisplayName("신규 유저 웰컴 쿠폰 발급 무시 - 정책이 없는 경우")
    void issueWelcomeCoupon_Ignored_PolicyNotFound() {
        Long userId = 1L;
        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> couponService.issueWelcomeCouponForNewUser(userId));
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    @DisplayName("일반 쿠폰 발급 성공")
    void issueGeneralCoupon_Success() {
        Long userId = 1L;
        Long policyId = 101L;
        CouponPolicy policy = createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(couponRepository.existsByUserIdAndCouponPolicy(userId, policy)).thenReturn(false);

        couponService.issueGeneralCoupon(userId, policyId);

        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("일반 쿠폰 발급 실패 - 정책이 없는 경우")
    void issueGeneralCoupon_Fail_PolicyNotFound() {
        Long userId = 1L;
        Long policyId = 101L;
        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        assertThrows(CouponPolicyException.class, () -> couponService.issueGeneralCoupon(userId, policyId));
    }

    @Test
    @DisplayName("일반 쿠폰 발급 실패 - 이미 발급된 쿠폰인 경우")
    void issueGeneralCoupon_Fail_AlreadyIssued() {
        Long userId = 1L;
        Long policyId = 101L;
        CouponPolicy policy = createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(couponRepository.existsByUserIdAndCouponPolicy(userId, policy)).thenReturn(true);

        CouponException exception = assertThrows(CouponException.class, () ->
            couponService.issueGeneralCoupon(userId, policyId));
        assertThat(exception.getErrorCode()).isEqualTo(CouponErrorCode.COUPON_ALREADY_ISSUED);
    }

    @Test
    @DisplayName("생일 쿠폰 발급 성공")
    void issueBirthdayCoupon_Success() {
        int month = 7;
        CouponPolicy birthdayPolicy = createMockPolicy(CouponType.BIRTHDAY, PolicyStatus.ACTIVE);
        List<Long> userIds = List.of(1L, 2L, 3L);

        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.BIRTHDAY, PolicyStatus.ACTIVE))
            .thenReturn(Optional.of(birthdayPolicy));
        when(userServiceClient.getBirthdayUserIds(month)).thenReturn(userIds);
        when(couponRepository.existsByUserIdAndCouponPolicy(anyLong(), any(CouponPolicy.class))).thenReturn(false);

        couponService.issueBirthdayCoupon(month);

        verify(couponRepository, times(userIds.size())).save(any(Coupon.class));
    }

    @Test
    @DisplayName("생일 쿠폰 발급 무시 - 정책이 없는 경우")
    void issueBirthdayCoupon_Ignored_PolicyNotFound() {
        int month = 7;
        when(couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.BIRTHDAY, PolicyStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> couponService.issueBirthdayCoupon(month));
        verify(userServiceClient, never()).getBirthdayUserIds(anyInt());
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    @DisplayName("쿠폰 사용 처리 성공")
    void processUsedCoupons_Success() {
        Long userId = 1L;
        Long couponId = 10L;
        Long orderId = 100L;
        CouponUsageRequestDto requestDto = new CouponUsageRequestDto();
        requestDto.setOrderId(orderId);
        requestDto.setUsedCoupons(List.of(new CouponUsageRequestDto.UsedCouponInfo(couponId, null)));
        Coupon coupon = createMockCoupon(couponId, userId, createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE), CouponStatus.NOT_USED);

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        couponService.processUsedCoupons(userId, requestDto);

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
        assertThat(coupon.getOrderId()).isEqualTo(orderId);
        assertThat(coupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 사용 처리 실패 - 쿠폰 소유자가 아닌 경우")
    void processUsedCoupons_Fail_OwnerNotMatch() {
        Long userId = 1L;
        Long otherUserId = 2L;
        Long couponId = 10L;
        CouponUsageRequestDto requestDto = new CouponUsageRequestDto();
        requestDto.setOrderId(100L);
        requestDto.setUsedCoupons(List.of(new CouponUsageRequestDto.UsedCouponInfo(couponId, null)));
        Coupon coupon = createMockCoupon(couponId, otherUserId, createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE), CouponStatus.NOT_USED);

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        CouponException exception = assertThrows(CouponException.class, () ->
            couponService.processUsedCoupons(userId, requestDto));

        assertThat(exception.getErrorCode()).isEqualTo(CouponErrorCode.COUPON_OWNER_NOT_MATCH);
    }

    @Test
    @DisplayName("쿠폰 사용 처리 실패 - 쿠폰이 없는 경우")
    void processUsedCoupons_Fail_CouponNotFound() {
        Long userId = 1L;
        Long couponId = 10L;
        CouponUsageRequestDto requestDto = new CouponUsageRequestDto();
        requestDto.setOrderId(100L);
        requestDto.setUsedCoupons(List.of(new CouponUsageRequestDto.UsedCouponInfo(couponId, null)));

        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        CouponException exception = assertThrows(CouponException.class, () ->
            couponService.processUsedCoupons(userId, requestDto));

        assertThat(exception.getErrorCode()).isEqualTo(CouponErrorCode.COUPON_NOT_FOUND);
    }

    @Test
    @DisplayName("쿠폰 사용 처리 실패 - 이미 사용된 쿠폰인 경우")
    void processUsedCoupons_Fail_CouponAlreadyUsed() {
        Long userId = 1L;
        Long couponId = 10L;
        CouponUsageRequestDto requestDto = new CouponUsageRequestDto();
        requestDto.setOrderId(100L);
        requestDto.setUsedCoupons(List.of(new CouponUsageRequestDto.UsedCouponInfo(couponId, null)));
        Coupon coupon = createMockCoupon(couponId, userId, createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE), CouponStatus.USED);

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        CouponException exception = assertThrows(CouponException.class, () ->
            couponService.processUsedCoupons(userId, requestDto));

        assertThat(exception.getErrorCode()).isEqualTo(CouponErrorCode.COUPON_ALREADY_USED_OR_EXPIRED);
    }

    @Test
    @DisplayName("유저의 쿠폰 목록을 페이징하여 조회한다")
    void getUserCoupons_Success() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Coupon coupon = createMockCoupon(1L, userId, createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE), CouponStatus.NOT_USED);
        Page<Coupon> couponPage = new PageImpl<>(List.of(coupon), pageable, 1);

        when(couponRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(couponPage);

        PageResponseDto<?> result = couponService.getUserCoupons(userId, pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(couponRepository).findByUserId(anyLong(), pageableCaptor.capture());
        Sort expectedSort = Sort.by(Sort.Order.asc("status"), Sort.Order.asc("endDate"), Sort.Order.desc("issuedAt"));

        assertThat(result.getContent()).hasSize(1);
        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(expectedSort);
    }

    @Test
    @DisplayName("유저가 주문에 적용 가능한 쿠폰 목록을 조회한다")
    void getUserApplicableCoupons_Success() {
        Long userId = 1L;
        Long bookIdForBookCoupon = 101L;
        OrderCouponsRequestDto requestDto = new OrderCouponsRequestDto();
        requestDto.setBookIds(List.of(bookIdForBookCoupon));

        CouponPolicy orderCouponPolicy = createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE);
        Coupon orderCoupon = createMockCoupon(1L, userId, orderCouponPolicy, CouponStatus.NOT_USED);

        CouponPolicy bookCouponPolicy = createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE);
        Coupon bookCoupon = createMockCoupon(2L, userId, bookCouponPolicy, CouponStatus.NOT_USED);

        ApplicableCouponInfo orderCouponInfo = new ApplicableCouponInfo(orderCoupon, null);
        ApplicableCouponInfo bookCouponInfo = new ApplicableCouponInfo(bookCoupon, bookIdForBookCoupon);
        List<ApplicableCouponInfo> mockCouponInfos = List.of(orderCouponInfo, bookCouponInfo);

        when(bookServiceClient.getBookToCategoryMap(any())).thenReturn(Collections.singletonMap(bookIdForBookCoupon, Set.of(1L)));
        when(couponRepositoryImpl.findApplicableCouponsForOrder(anyLong(), any(Map.class))).thenReturn(mockCouponInfos);

        ApplicableCouponsDto result = couponService.getUserApplicableCoupons(userId, requestDto);

        assertThat(result.getOrderCoupons()).hasSize(1);
        assertThat(result.getItemCoupons()).hasSize(1);
        assertThat(result.getItemCoupons().get(bookIdForBookCoupon)).hasSize(1);
        assertThat(result.getOrderCoupons().get(0).getCouponId()).isEqualTo(orderCoupon.getCouponId());
        assertThat(result.getItemCoupons().get(bookIdForBookCoupon).get(0).getCouponId()).isEqualTo(bookCoupon.getCouponId());
    }

    @Test
    @DisplayName("적용할 쿠폰들의 할인 정보를 조회한다")
    void applyCoupons_Success() {
        Long userId = 1L;
        Map<Long, Long> couponAndBookIds = Map.of(1L, 101L, 2L, 102L);
        TryApplyCouponsRequestDto requestDto = new TryApplyCouponsRequestDto();
        requestDto.setCouponAndBookIds(couponAndBookIds);

        CouponPolicy policy1 = createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE);
        policy1.setDiscountType(DiscountType.FIXED);
        policy1.setDiscountValue(1000);
        Coupon coupon1 = createMockCoupon(1L, userId, policy1, CouponStatus.NOT_USED);

        CouponPolicy policy2 = createMockPolicy(CouponType.CUSTOM, PolicyStatus.ACTIVE);
        policy2.setDiscountType(DiscountType.PERCENT);
        policy2.setDiscountValue(10);
        Coupon coupon2 = createMockCoupon(2L, userId, policy2, CouponStatus.NOT_USED);

        when(couponRepository.findAllById(any(List.class))).thenReturn(List.of(coupon1, coupon2));

        List<TryApplyCouponsResponseDto> result = couponService.applyCoupons(userId, requestDto);

        assertThat(result).hasSize(2);
        TryApplyCouponsResponseDto result1 = result.stream().filter(r -> r.getCouponId() == 1L).findFirst().orElseThrow();
        assertThat(result1.getDiscountValue()).isEqualTo(1000);
        assertThat(result1.getDiscountType()).isEqualTo(DiscountType.FIXED);
        assertThat(result1.getBookId()).isEqualTo(101L);

        TryApplyCouponsResponseDto result2 = result.stream().filter(r -> r.getCouponId() == 2L).findFirst().orElseThrow();
        assertThat(result2.getDiscountValue()).isEqualTo(10);
        assertThat(result2.getDiscountType()).isEqualTo(DiscountType.PERCENT);
        assertThat(result2.getBookId()).isEqualTo(102L);
    }
}
