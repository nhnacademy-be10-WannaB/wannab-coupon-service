package shop.wannab.couponservice.domain.couponpolicy.dto;

import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.DiscountType;

@Getter
@Setter
public class IssuableCouponPolicyDto {
    private Long couponPolicyId;
    private String name;
    private String discountInfo;

    public IssuableCouponPolicyDto(CouponPolicy policy) {
        this.couponPolicyId = policy.getCouponPolicyId();
        this.name = policy.getCouponPolicyName();

        if (DiscountType.FIXED == policy.getDiscountType()) {
            this.discountInfo = policy.getDiscountValue() + "원 할인";
        } else {
            this.discountInfo = policy.getDiscountValue() + "% 할인";
        }
    }
}
