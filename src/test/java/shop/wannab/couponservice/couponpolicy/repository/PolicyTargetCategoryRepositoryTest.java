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
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.DiscountType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;
import shop.wannab.couponservice.global.config.AppConfig;

@DataJpaTest
@Import(AppConfig.class)
class PolicyTargetCategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PolicyTargetCategoryRepository policyTargetCategoryRepository;

    private CouponPolicy activePolicy;
    private CouponPolicy deletedPolicy;

    private PolicyTargetCategory targetCategory1;
    private PolicyTargetCategory targetCategory2;

    @Autowired(required = false)
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void setUp() {
        activePolicy = CouponPolicy.builder()
                .couponPolicyName("test_policy_active")
                .policyStatus(PolicyStatus.ACTIVE)
                .couponType(CouponType.CATEGORY)
                .discountType(DiscountType.FIXED)
                .discountValue(0)
                .maxDiscount(0)
                .minPurchase(0)
                .build();

        deletedPolicy = CouponPolicy.builder()
                .couponPolicyName("test_policy_deleted")
                .policyStatus(PolicyStatus.DELETED)
                .couponType(CouponType.CATEGORY)
                .discountType(DiscountType.FIXED)
                .discountValue(0)
                .maxDiscount(0)
                .minPurchase(0)
                .build();

        entityManager.persist(activePolicy);
        entityManager.persist(deletedPolicy);

        targetCategory1 = PolicyTargetCategory.builder()
                .categoryId(201L)
                .couponPolicy(activePolicy)
                .build();

        targetCategory2 = PolicyTargetCategory.builder()
                .categoryId(202L)
                .couponPolicy(deletedPolicy)
                .build();

        entityManager.persist(targetCategory1);
        entityManager.persist(targetCategory2);
    }

    @Test
    @DisplayName("categoryId와 활성(ACTIVE) 상태로 PolicyTargetCategory 조회")
    void findByCategoryIdAndCouponPolicy_PolicyStatus_Success() {
        Optional<PolicyTargetCategory> foundTarget = policyTargetCategoryRepository.findByCategoryIdAndCouponPolicy_PolicyStatus(201L, PolicyStatus.ACTIVE);

        assertThat(foundTarget).isPresent();
        assertThat(foundTarget.get().getCategoryId()).isEqualTo(201L);
        assertThat(foundTarget.get().getCouponPolicy().getPolicyStatus()).isEqualTo(PolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("categoryId는 일치하지만 CouponPolicy 상태가 달라 조회 실패")
    void findByCategoryIdAndCouponPolicy_PolicyStatus_StatusMismatch() {
        Optional<PolicyTargetCategory> foundTarget = policyTargetCategoryRepository.findByCategoryIdAndCouponPolicy_PolicyStatus(201L, PolicyStatus.DELETED);

        assertThat(foundTarget).isNotPresent();
    }

    @Test
    @DisplayName("CouponPolicy ID 목록으로 PolicyTargetCategory 목록 조회")
    void findAllByCouponPolicy_CouponPolicyIdIn() {
        List<Long> policyIdsToFind = List.of(activePolicy.getCouponPolicyId(), deletedPolicy.getCouponPolicyId());

        List<PolicyTargetCategory> foundList = policyTargetCategoryRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIdsToFind);

        assertThat(foundList).hasSize(2);
        assertThat(foundList).extracting("categoryId").containsExactlyInAnyOrder(201L, 202L);
    }

    @Test
    @DisplayName("존재하지 않는 CouponPolicy ID 목록으로 조회 시 빈 목록 반환")
    void findAllByCouponPolicy_CouponPolicyIdIn_NotFound() {
        List<Long> policyIdsToFind = List.of(9998L, 9999L);
        List<PolicyTargetCategory> foundList = policyTargetCategoryRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIdsToFind);
        assertThat(foundList).isEmpty();
    }
}