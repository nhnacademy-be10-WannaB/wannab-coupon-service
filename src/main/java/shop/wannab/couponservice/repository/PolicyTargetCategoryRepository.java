package shop.wannab.couponservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.couponpolicy.PolicyTargetCategory;

public interface PolicyTargetCategoryRepository extends JpaRepository<PolicyTargetCategory, Long> {
    Optional<PolicyTargetCategory> findByCouponPolicy(CouponPolicy couponPolicy);

}
