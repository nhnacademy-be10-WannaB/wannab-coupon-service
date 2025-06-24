package shop.wannab.couponservice.repository;

import java.util.List;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;

public interface CouponRepositoryCustom {
    //List<Coupon> findAllPotentiallyApplicableCoupons(Long userId, List<Long> bookIds, List<Long> categoryIds);
    //List<ApplicableCouponInfo> findApplicableCouponsForOrder(Long userId, Map<Long, Long> bookIdToCategoryIdMap);
    List<CouponPolicy> findActiveCouponPolicies(List<Long> ancestorCategoryIds);
}
