package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.coupon.Coupon;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
    boolean existsByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
}