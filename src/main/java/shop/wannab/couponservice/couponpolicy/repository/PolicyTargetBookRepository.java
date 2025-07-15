package shop.wannab.couponservice.couponpolicy.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;

public interface PolicyTargetBookRepository extends JpaRepository<PolicyTargetBook, Long> {
    List<PolicyTargetBook> findByBookId(Long bookId);
    List<PolicyTargetBook> findAllByCouponPolicy_CouponPolicyIdIn(List<Long> policyIds);
    Optional<PolicyTargetBook> findByBookIdAndCouponPolicy_PolicyStatus(Long bookId, PolicyStatus status);

}
