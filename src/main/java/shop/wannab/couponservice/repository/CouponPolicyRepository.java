package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.enums.PolicyRule;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy,Long> {
    CouponPolicy findByPolicyRule(PolicyRule policyRule);
}
