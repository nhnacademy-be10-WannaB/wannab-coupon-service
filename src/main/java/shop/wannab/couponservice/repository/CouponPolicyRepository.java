package shop.wannab.couponservice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.PolicyStatus;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy,Long> {
    CouponPolicy findByCouponType(CouponType couponType);
    List<CouponPolicy> findByPolicyStatus(PolicyStatus status);
}
