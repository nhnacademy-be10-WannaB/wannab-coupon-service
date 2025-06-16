package shop.wannab.couponservice.domain.dto;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCouponPolicyDto {
    @Min(value = 0, message = "최소 구매 금액은 0 이상이어야 합니다.")
    private Integer minPurchase;
    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
    private Integer discountValue;

    private Integer maxDiscount;
    private String validityType;
    private Integer validForDays;
    private LocalDate endDate;
}
