package shop.wannab.couponservice.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.Coupon;
import shop.wannab.couponservice.domain.CouponPolicy;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findByUserIdAndCouponPolicy(@NotNull Long userId, CouponPolicy couponPolicy);
    boolean existsByUserIdAndCouponPolicy(@NotNull Long userId, CouponPolicy couponPolicy);
}
