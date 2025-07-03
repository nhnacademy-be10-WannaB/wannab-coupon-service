package shop.wannab.couponservice.coupon.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.coupon.entity.Coupon;
import shop.wannab.couponservice.coupon.entity.CouponStatus;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Coupon findByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
    boolean existsByUserIdAndCouponPolicy(Long userId, CouponPolicy couponPolicy);
    List<Coupon> findByUserId(Long userId);
    List<Coupon> findByUserIdAndStatus(Long userId, CouponStatus status);
    Page<Coupon> findByUserId(Long userId, Pageable pageable);

}