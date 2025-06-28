package shop.wannab.couponservice.repository;

import java.util.List;
import java.util.Map;
import shop.wannab.couponservice.domain.coupon.dto.ApplicableCouponInfo;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;

public interface CouponRepositoryCustom {
    List<ApplicableCouponInfo> findApplicableCouponsForOrder(Long userId, Map<Long, Long> bookIdToCategoryIdMap);
    List<CouponPolicy> findActiveCouponPolicies(List<Long> ancestorCategoryIds);
}
