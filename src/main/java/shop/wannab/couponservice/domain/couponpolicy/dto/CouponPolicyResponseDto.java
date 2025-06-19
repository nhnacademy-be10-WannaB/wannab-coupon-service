package shop.wannab.couponservice.domain.couponpolicy.dto;

import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.DiscountType;

@Getter
@Setter
public class CouponPolicyResponseDto {
    private Long id;
    private String name;
    private String discountType;
    private String purchaseTerm;
    private String discount;
    private String period;
    private boolean autoIssue;

    public static CouponPolicyResponseDto from(CouponPolicy policy,String bookName,String categoryName) {
        CouponPolicyResponseDto dto = new CouponPolicyResponseDto();

        dto.id = policy.getCouponPolicyId();
        dto.name = policy.getCouponPolicyName();
        dto.discountType = policy.getDiscountType() == DiscountType.FIXED ? "정액" : "정률";

        if (policy.getDiscountType() == DiscountType.FIXED) {
            dto.setDiscount(String.format("%,d원", policy.getDiscountValue()));
        } else {
            dto.setDiscount(String.format("%d%%", policy.getDiscountValue()));
        }

        if (policy.getValidDays() > 0) {
            dto.setPeriod(String.format("발급 후 %d일", policy.getValidDays()));
        } else {
            dto.setPeriod(String.format("%s ~ %s", policy.getFixedStartDate(), policy.getFixedEndDate()));
        }
        if(policy.getCouponType() == CouponType.BOOK){
            dto.purchaseTerm = bookName;
        } else if(policy.getCouponType() == CouponType.CATEGORY){
            dto.purchaseTerm = categoryName;
        } else{
            dto.purchaseTerm = String.format("%,d원 이상", policy.getMinPurchase());
            dto.autoIssue = true;
        }
        return dto;
    }
}
