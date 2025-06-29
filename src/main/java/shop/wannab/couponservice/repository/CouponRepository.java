package shop.wannab.couponservice.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.coupon.Coupon;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponStatus;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
    boolean existsByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
    List<Coupon> findByUserId(Long userId);
    List<Coupon> findByUserIdAndStatus(Long userId, CouponStatus status);
    Page<Coupon> findByUserId(Long userId, Pageable pageable);

}