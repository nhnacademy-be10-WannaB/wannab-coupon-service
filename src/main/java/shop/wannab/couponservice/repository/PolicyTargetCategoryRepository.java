package shop.wannab.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.couponservice.domain.PolicyTargetCategory;

public interface PolicyTargetCategoryRepository extends JpaRepository<PolicyTargetCategory, Long> {
}
