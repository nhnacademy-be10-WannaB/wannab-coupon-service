package shop.wannab.couponservice.couponpolicy.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy,Long> {
    Optional<CouponPolicy> findByCouponTypeAndPolicyStatus(CouponType couponType, PolicyStatus policyStatus);
    List<CouponPolicy> findByPolicyStatus(PolicyStatus status);
}
