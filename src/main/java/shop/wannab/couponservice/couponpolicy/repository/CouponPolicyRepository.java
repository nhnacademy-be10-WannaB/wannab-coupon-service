package shop.wannab.couponservice.couponpolicy.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;
import shop.wannab.couponservice.couponpolicy.entity.CouponType;
import shop.wannab.couponservice.couponpolicy.entity.PolicyStatus;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy,Long> {
    Optional<CouponPolicy> findByCouponTypeAndPolicyStatus(CouponType couponType, PolicyStatus policyStatus);
    List<CouponPolicy> findByPolicyStatus(PolicyStatus status);

    @Query("SELECT cp FROM CouponPolicy cp " +
            "JOIN PolicyTargetCategory ptc ON cp.couponPolicyId = ptc.couponPolicy.couponPolicyId " +
            "WHERE ptc.categoryId IN :categoryIds " +
            "AND cp.policyStatus = :status")
    List<CouponPolicy> findActivePoliciesForCategoryIds(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("status") PolicyStatus status
    );
}
