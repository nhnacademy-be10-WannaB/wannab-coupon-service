package shop.wannab.couponservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.couponservice.domain.enums.DiscountType;
import shop.wannab.couponservice.domain.enums.PolicyRule;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CouponPolicy {
    @Id
    private long couponPolicyId;

    @NotNull
    private String couponPolicyName;

    @NotNull
    private PolicyRule policyRule;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private float discountValue;

    @NotNull
    private int maxDiscount;

    @NotNull
    private int minPurchase;

    @NotNull
    private int validDays;

    @NotNull
    private LocalDate fixedStartDate;

    @NotNull
    private LocalDate fixedEndDate;

    @NotNull
    private boolean state;
}
