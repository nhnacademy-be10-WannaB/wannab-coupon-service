package shop.wannab.couponservice.couponpolicy.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;

public interface PolicyTargetCategoryRepository extends JpaRepository<PolicyTargetCategory, Long> {
    Optional<PolicyTargetCategory> findByCategoryIdAndCouponPolicy_PolicyStatus(Long categoryId, PolicyStatus status);
    List<PolicyTargetCategory> findAllByCouponPolicy_CouponPolicyIdIn(List<Long> policyIds);
}
