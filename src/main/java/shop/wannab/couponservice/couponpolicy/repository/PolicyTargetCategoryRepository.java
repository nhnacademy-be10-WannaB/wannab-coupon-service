package shop.wannab.couponservice.couponpolicy.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetCategory;

public interface PolicyTargetCategoryRepository extends JpaRepository<PolicyTargetCategory, Long> {
    Optional<PolicyTargetCategory> findByCategoryId(Long categoryId);
    Optional<Long> findCategoryIdByCouponPolicy(CouponPolicy couponPolicy);
}
