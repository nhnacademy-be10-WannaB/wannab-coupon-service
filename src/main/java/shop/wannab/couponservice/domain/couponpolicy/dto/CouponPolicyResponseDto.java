package shop.wannab.couponservice.domain.couponpolicy.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.enums.DiscountType;
import shop.wannab.couponservice.domain.enums.PolicyRule;

@Getter
@Setter
public class CouponPolicyResponseDto {
    private Long id;
    private String name;
    private String discountType;
    private String minPurchase;
    private String discount;
    private String period;
    private String autoIssue;

    public static CouponPolicyResponseDto from(CouponPolicy policy) {
        CouponPolicyResponseDto dto = new CouponPolicyResponseDto();

        dto.id = policy.getCouponPolicyId();
        dto.name = policy.getCouponPolicyName();
        dto.discountType = policy.getDiscountType() == DiscountType.FIXED ? "정액" : "정률";
        dto.minPurchase = String.format("%,d원 이상", policy.getMinPurchase());

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

        List<String> autoIssueTypes = new ArrayList<>();
        if (policy.getPolicyRule() == PolicyRule.WELCOME) {
            autoIssueTypes.add("회원가입");
        }
        if (policy.getPolicyRule() == PolicyRule.BIRTHDAY) {
            autoIssueTypes.add("생일");
        }
        dto.setAutoIssue(String.join(", ", autoIssueTypes));

        return dto;
    }
}
