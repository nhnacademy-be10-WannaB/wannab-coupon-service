package shop.wannab.couponservice.couponpolicy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.global.config.AppConfig;

@ActiveProfiles("ci")
@DataJpaTest
@Import(AppConfig.class)
class CouponPolicyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    private CouponPolicy activeAmountPolicy;
    private CouponPolicy inactiveRatePolicy;

    @Autowired(required = false)
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void setUp() {
        activeAmountPolicy = CouponPolicy.builder()
                .couponPolicyName("활성 정액 할인 정책")
                .couponType(CouponType.WELCOME)
                .discountType(DiscountType.FIXED)
                .discountValue(1000)
                .maxDiscount(1000)
                .minPurchase(10000)
                .policyStatus(PolicyStatus.ACTIVE)
                .build();

        inactiveRatePolicy = CouponPolicy.builder()
                .couponPolicyName("비활성 정률 할인 정책")
                .couponType(CouponType.BIRTHDAY)
                .discountType(DiscountType.PERCENT)
                .discountValue(10)
                .maxDiscount(5000)
                .minPurchase(20000)
                .policyStatus(PolicyStatus.DELETED)
                .build();

        entityManager.persist(activeAmountPolicy);
        entityManager.persist(inactiveRatePolicy);
    }

    @Test
    @DisplayName("쿠폰 타입과 정책 상태로 쿠폰 정책 조회 성공")
    void findByCouponTypeAndPolicyStatus_Success() {
        Optional<CouponPolicy> foundPolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.ACTIVE);

        assertThat(foundPolicy).isPresent();
        assertThat(foundPolicy.get().getCouponPolicyId()).isEqualTo(activeAmountPolicy.getCouponPolicyId());
        assertThat(foundPolicy.get().getCouponPolicyName()).isEqualTo("활성 정액 할인 정책");
    }

    @Test
    @DisplayName("쿠폰 타입과 정책 상태로 조회 시 결과 없음")
    void findByCouponTypeAndPolicyStatus_NotFound() {
        Optional<CouponPolicy> foundPolicy = couponPolicyRepository.findByCouponTypeAndPolicyStatus(CouponType.WELCOME, PolicyStatus.DELETED);

        assertThat(foundPolicy).isNotPresent();
    }

    @Test
    @DisplayName("정책 상태로 쿠폰 정책 목록 조회")
    void findByPolicyStatus() {
        CouponPolicy anotherActivePolicy = CouponPolicy.builder()
                .couponPolicyName("추가 활성 정책")
                .couponType(CouponType.BIRTHDAY)
                .discountType(DiscountType.FIXED)
                .discountValue(3000)
                .maxDiscount(3000)
                .minPurchase(30000)
                .policyStatus(PolicyStatus.ACTIVE)
                .build();
        entityManager.persist(anotherActivePolicy);

        List<CouponPolicy> activePolicies = couponPolicyRepository.findByPolicyStatus(PolicyStatus.ACTIVE);
        List<CouponPolicy> inactivePolicies = couponPolicyRepository.findByPolicyStatus(PolicyStatus.DELETED);

        assertThat(activePolicies).hasSize(2);
        assertThat(activePolicies).extracting("couponPolicyName").containsExactlyInAnyOrder("활성 정액 할인 정책", "추가 활성 정책");

        assertThat(inactivePolicies).hasSize(1);
        assertThat(inactivePolicies.get(0).getCouponPolicyName()).isEqualTo("비활성 정률 할인 정책");
    }

    @Test
    @DisplayName("카테고리 ID 목록으로 활성 상태인 쿠폰 정책 조회")
    void findActivePoliciesForCategoryIds() {
        long electronicsId = 1L;
        CouponPolicy activeCategoryPolicy = CouponPolicy.builder()
                .couponPolicyName("카테고리 활성 정책")
                .couponType(CouponType.WELCOME)
                .discountType(DiscountType.FIXED)
                .discountValue(5000)
                .maxDiscount(5000)
                .minPurchase(50000)
                .policyStatus(PolicyStatus.ACTIVE)
                .build();
        entityManager.persist(activeCategoryPolicy);
        List<Long> searchCategoryIds = List.of(electronicsId, 999L);
        List<CouponPolicy> foundPolicies = couponPolicyRepository.findActivePoliciesForCategoryIds(searchCategoryIds, PolicyStatus.ACTIVE);

        assertThat(foundPolicies).isEmpty();
    }
}