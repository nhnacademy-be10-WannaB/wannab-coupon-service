package shop.wannab.couponservice.coupon.repository;

import java.util.List;
import java.util.Map;
import shop.wannab.couponservice.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;

public interface CouponRepositoryCustom {
    List<ApplicableCouponInfo> findApplicableCouponsForOrder(Long userId, Map<Long, Long> bookIdToCategoryIdMap);
    List<CouponPolicy> findActiveCouponPolicies(List<Long> ancestorCategoryIds);
}
