package shop.wannab.couponservice.domain.dto;

import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.CouponPolicy;

@Getter
@Setter
public class CouponPolicyDetailResponseDto {
    private long couponPolicyId;
    private String couponPolicyName;
    private String policyRule;
    private String discountType;
    private int discountValue;
    private int maxDiscount;
    private int minPurchase;

    private int validDays;
    private String startDate;
    private String endDate;

    public static CouponPolicyDetailResponseDto convertToDto(CouponPolicy couponPolicy) {
        CouponPolicyDetailResponseDto dto = new CouponPolicyDetailResponseDto();
        dto.couponPolicyId = couponPolicy.getCouponPolicyId();
        dto.couponPolicyName = couponPolicy.getCouponPolicyName();
        dto.policyRule = couponPolicy.getPolicyRule().toString();
        dto.discountType = couponPolicy.getDiscountType().toString();
        dto.discountValue = couponPolicy.getDiscountValue();
        dto.maxDiscount = couponPolicy.getMaxDiscount();
        dto.minPurchase = couponPolicy.getMinPurchase();

        if(couponPolicy.getValidDays() != null){
            dto.validDays = couponPolicy.getValidDays();
        }

        else{
            dto.startDate = couponPolicy.getFixedStartDate().toString();
            dto.endDate = couponPolicy.getFixedEndDate().toString();
        }
        return dto;
    }
}
