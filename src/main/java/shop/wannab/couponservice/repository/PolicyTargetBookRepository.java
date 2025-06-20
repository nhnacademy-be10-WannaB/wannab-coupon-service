package shop.wannab.couponservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.PolicyTargetBook;

public interface PolicyTargetBookRepository extends JpaRepository<PolicyTargetBook, Long> {
    Optional<PolicyTargetBook> findByCouponPolicy(CouponPolicy couponPolicy);
}
