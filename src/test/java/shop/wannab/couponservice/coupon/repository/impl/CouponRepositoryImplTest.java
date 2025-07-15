package shop.wannab.couponservice.coupon.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.coupon.entity.Coupon;
import shop.wannab.couponservice.coupon.entity.CouponStatus;
import shop.wannab.couponservice.coupon.repository.CouponRepository;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;
import shop.wannab.couponservice.couponpolicy.repository.CouponPolicyRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetBookRepository;
import shop.wannab.couponservice.couponpolicy.repository.PolicyTargetCategoryRepository;
import shop.wannab.couponservice.global.config.AppConfig;

@DataJpaTest
@Import({CouponRepositoryImpl.class, AppConfig.class})
class CouponRepositoryImplTest {

    @Autowired
    private CouponRepositoryImpl couponRepositoryImpl;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private PolicyTargetBookRepository policyTargetBookRepository;

    @Autowired
    private PolicyTargetCategoryRepository policyTargetCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Long userId;
    private Long bookId1, bookId2;
    private Long categoryId1, categoryId2;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        couponRepository.deleteAll();
        policyTargetBookRepository.deleteAll();
        policyTargetCategoryRepository.deleteAll();
        couponPolicyRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        userId = 1L;
        bookId1 = 101L;
        bookId2 = 102L;
        categoryId1 = 201L;
        categoryId2 = 202L;
    }

    @Test
    @DisplayName("사용자 ID와 상품/카테고리 ID로 적용 가능한 쿠폰을 조회한다.")
    void findApplicableCoupons_shouldReturnApplicableCoupons() {
        CouponPolicy normalPolicy = CouponPolicy.builder()
            .couponPolicyName("Normal Coupon Policy")
            .couponType(CouponType.CUSTOM)
            .discountType(DiscountType.PERCENT)
            .discountValue(10)
            .minPurchase(10000)
            .maxDiscount(5000)
            .policyStatus(PolicyStatus.ACTIVE)
            .fixedStartDate(LocalDate.now().minusDays(1))
            .fixedEndDate(LocalDate.now().plusDays(1))
            .build();
        couponPolicyRepository.save(normalPolicy);

        Coupon normalCoupon = Coupon.builder()
            .couponPolicy(normalPolicy)
            .userId(userId)
            .status(CouponStatus.NOT_USED)
            .issuedAt(LocalDate.now())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(1))
            .couponCode("NORMAL_CODE_1")
            .build();
        couponRepository.save(normalCoupon);

        CouponPolicy bookPolicy = CouponPolicy.builder()
            .couponPolicyName("Book Coupon Policy")
            .couponType(CouponType.BOOK)
            .discountType(DiscountType.FIXED)
            .discountValue(1000)
            .minPurchase(5000)
            .maxDiscount(1000)
            .policyStatus(PolicyStatus.ACTIVE)
            .fixedStartDate(LocalDate.now().minusDays(1))
            .fixedEndDate(LocalDate.now().plusDays(1))
            .build();
        couponPolicyRepository.save(bookPolicy);

        PolicyTargetBook targetBook = PolicyTargetBook.builder()
            .couponPolicy(bookPolicy)
            .bookId(bookId1)
            .build();
        policyTargetBookRepository.save(targetBook);

        Coupon bookCoupon = Coupon.builder()
            .couponPolicy(bookPolicy)
            .userId(userId)
            .status(CouponStatus.NOT_USED)
            .issuedAt(LocalDate.now())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(1))
            .couponCode("BOOK_CODE_1")
            .build();
        couponRepository.save(bookCoupon);

        CouponPolicy categoryPolicy = CouponPolicy.builder()
            .couponPolicyName("Category Coupon Policy")
            .couponType(CouponType.CATEGORY)
            .discountType(DiscountType.PERCENT)
            .discountValue(5)
            .minPurchase(20000)
            .maxDiscount(3000)
            .policyStatus(PolicyStatus.ACTIVE)
            .fixedStartDate(LocalDate.now().minusDays(1))
            .fixedEndDate(LocalDate.now().plusDays(1))
            .build();
        couponPolicyRepository.save(categoryPolicy);

        PolicyTargetCategory targetCategory = PolicyTargetCategory.builder()
            .couponPolicy(categoryPolicy)
            .categoryId(categoryId1)
            .build();
        policyTargetCategoryRepository.save(targetCategory);

        Coupon categoryCoupon = Coupon.builder()
            .couponPolicy(categoryPolicy)
            .userId(userId)
            .status(CouponStatus.NOT_USED)
            .issuedAt(LocalDate.now())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(1))
            .couponCode("CATEGORY_CODE_1")
            .build();
        couponRepository.save(categoryCoupon);

        CouponPolicy expiredPolicy = CouponPolicy.builder()
            .couponPolicyName("Expired Coupon Policy")
            .couponType(CouponType.CUSTOM)
            .discountType(DiscountType.PERCENT)
            .discountValue(20)
            .minPurchase(1000)
            .maxDiscount(500)
            .policyStatus(PolicyStatus.ACTIVE)
            .fixedStartDate(LocalDate.now().minusDays(10))
            .fixedEndDate(LocalDate.now().minusDays(5))
            .build();
        couponPolicyRepository.save(expiredPolicy);

        Coupon expiredCoupon = Coupon.builder()
            .couponPolicy(expiredPolicy)
            .userId(userId)
            .status(CouponStatus.NOT_USED)
            .issuedAt(LocalDate.now().minusDays(6))
            .startDate(LocalDate.now().minusDays(6))
            .endDate(LocalDate.now().minusDays(5))
            .couponCode("EXPIRED_CODE_1")
            .build();
        couponRepository.save(expiredCoupon);

        CouponPolicy usedPolicy = CouponPolicy.builder()
            .couponPolicyName("Used Coupon Policy")
            .couponType(CouponType.CUSTOM)
            .discountType(DiscountType.PERCENT)
            .discountValue(20)
            .minPurchase(1000)
            .maxDiscount(500)
            .policyStatus(PolicyStatus.ACTIVE)
            .fixedStartDate(LocalDate.now().minusDays(1))
            .fixedEndDate(LocalDate.now().plusDays(1))
            .build();
        couponPolicyRepository.save(usedPolicy);

        Coupon usedCoupon = Coupon.builder()
            .couponPolicy(usedPolicy)
            .userId(userId)
            .status(CouponStatus.USED)
            .issuedAt(LocalDate.now().minusDays(1))
            .startDate(LocalDate.now().minusDays(1))
            .endDate(LocalDate.now().plusDays(1))
            .usedAt(LocalDate.now())
            .couponCode("USED_CODE_1")
            .build();
        couponRepository.save(usedCoupon);

        entityManager.flush();
        entityManager.clear();

        Map<Long, Set<Long>> bookIdToCategoryIdsMap = Map.of(
            bookId1, Set.of(categoryId1),
            bookId2, Set.of(categoryId2)
        );
        List<ApplicableCouponInfo> result = couponRepositoryImpl.findApplicableCouponsForOrder(userId, bookIdToCategoryIdsMap);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);

        assertThat(result).anyMatch(info -> info.coupon().getCouponId().equals(normalCoupon.getCouponId()) && info.coupon().getCouponPolicy().getCouponPolicyId().equals(normalPolicy.getCouponPolicyId()));
        assertThat(result).anyMatch(info -> info.coupon().getCouponId().equals(bookCoupon.getCouponId()) && info.coupon().getCouponPolicy().getCouponPolicyId().equals(bookPolicy.getCouponPolicyId()) && info.targetBookId().equals(bookId1));
        assertThat(result).anyMatch(info -> info.coupon().getCouponId().equals(categoryCoupon.getCouponId()) && info.coupon().getCouponPolicy().getCouponPolicyId().equals(categoryPolicy.getCouponPolicyId()) && info.targetBookId().equals(bookId1));
    }
}