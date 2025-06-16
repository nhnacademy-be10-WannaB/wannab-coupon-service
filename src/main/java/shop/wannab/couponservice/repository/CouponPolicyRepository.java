package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.CouponPolicy;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy,Long> {
}
