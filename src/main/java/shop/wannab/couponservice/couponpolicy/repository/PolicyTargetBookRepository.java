package shop.wannab.couponservice.couponpolicy.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.PolicyTargetBook;

public interface PolicyTargetBookRepository extends JpaRepository<PolicyTargetBook, Long> {
    Optional<PolicyTargetBook> findByBookId(Long bookId);
    Optional<Long> findBookIdByCouponPolicy(CouponPolicy couponPolicy);
}
