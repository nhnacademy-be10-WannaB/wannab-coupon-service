package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.PolicyTargetBook;

public interface PolicyTargetBookRepository extends JpaRepository<PolicyTargetBook, Long> {
}
