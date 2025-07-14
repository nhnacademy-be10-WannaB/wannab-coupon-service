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
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;
import shop.wannab.couponservice.global.config.AppConfig;

@DataJpaTest
@Import(AppConfig.class)
class PolicyTargetBookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PolicyTargetBookRepository policyTargetBookRepository;

    private CouponPolicy activePolicy;
    private CouponPolicy inactivePolicy;

    @Autowired(required = false)
    private JPAQueryFactory queryFactory;

    @BeforeEach
    void setUp() {
        activePolicy = CouponPolicy.builder()
                .couponPolicyName("test_policy_active")
                .policyStatus(PolicyStatus.ACTIVE)
                .couponType(CouponType.BOOK)
                .discountType(DiscountType.FIXED)
                .discountValue(0)
                .maxDiscount(0)
                .minPurchase(0)
                .build();

        inactivePolicy = CouponPolicy.builder()
                .couponPolicyName("test_policy_inactive")
                .policyStatus(PolicyStatus.DELETED)
                .couponType(CouponType.BOOK)
                .discountType(DiscountType.FIXED)
                .discountValue(0)
                .maxDiscount(0)
                .minPurchase(0)
                .build();

        entityManager.persist(activePolicy);
        entityManager.persist(inactivePolicy);

        PolicyTargetBook targetBook1 = PolicyTargetBook.builder()
                .bookId(101L)
                .couponPolicy(activePolicy)
                .build();

        PolicyTargetBook targetBook2 = PolicyTargetBook.builder()
                .bookId(102L)
                .couponPolicy(inactivePolicy)
                .build();

        entityManager.persist(targetBook1);
        entityManager.persist(targetBook2);
    }

    @Test
    @DisplayName("bookId로 PolicyTargetBook 조회 성공")
    void findByBookId_Success() {
        List<PolicyTargetBook> foundTargetBooks = policyTargetBookRepository.findByBookId(101L);
        assertThat(foundTargetBooks).hasSize(1);
        assertThat(foundTargetBooks.get(0).getBookId()).isEqualTo(101L);
        assertThat(foundTargetBooks.get(0).getCouponPolicy().getCouponPolicyId()).isEqualTo(activePolicy.getCouponPolicyId());
    }

    @Test
    @DisplayName("CouponPolicy ID 목록으로 PolicyTargetBook 목록 조회")
    void findAllByCouponPolicy_CouponPolicyIdIn() {
        List<Long> policyIdsToFind = List.of(activePolicy.getCouponPolicyId());

        List<PolicyTargetBook> foundList = policyTargetBookRepository.findAllByCouponPolicy_CouponPolicyIdIn(policyIdsToFind);

        assertThat(foundList).hasSize(1);
        assertThat(foundList.get(0).getBookId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("bookId와 활성(ACTIVE) 상태로 PolicyTargetBook 조회")
    void findByBookIdAndCouponPolicy_PolicyStatus_Active() {
        Optional<PolicyTargetBook> foundTargetBook = policyTargetBookRepository.findByBookIdAndCouponPolicy_PolicyStatus(101L, PolicyStatus.ACTIVE);

        assertThat(foundTargetBook).isPresent();
        assertThat(foundTargetBook.get().getBookId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("bookId는 일치하지만 CouponPolicy 상태가 달라 조회 실패")
    void findByBookIdAndCouponPolicy_PolicyStatus_StatusMismatch() {
        Optional<PolicyTargetBook> foundTargetBook = policyTargetBookRepository.findByBookIdAndCouponPolicy_PolicyStatus(101L, PolicyStatus.DELETED);

        assertThat(foundTargetBook).isNotPresent();
    }
}