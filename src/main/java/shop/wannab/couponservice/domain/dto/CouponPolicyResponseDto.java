package shop.wannab.couponservice.domain.dto;

import lombok.Getter;
import lombok.Setter;

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
}
