package shop.wannab.couponservice.couponpolicy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //동시성 제어 고민해볼것
    private Long couponPolicyId;

    @NotNull
    private String couponPolicyName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @NotNull
    private Integer discountValue;

    @NotNull
    private Integer maxDiscount;

    @NotNull
    private Integer minPurchase;

    private Integer validDays;

    private LocalDate fixedStartDate;

    private LocalDate fixedEndDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PolicyStatus policyStatus;
}
