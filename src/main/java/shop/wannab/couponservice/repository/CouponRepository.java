package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.Coupon;
import shop.wannab.couponservice.domain.CouponPolicy;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
    boolean existsByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
}