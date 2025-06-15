package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
