package shop.wannab.couponservice.domain;

import jakarta.persistence.Entity;
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
import shop.wannab.couponservice.domain.enums.DiscountType;
import shop.wannab.couponservice.domain.enums.PolicyRule;

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
    private PolicyRule policyRule;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private int discountValue;

    @NotNull
    private int maxDiscount;

    @NotNull
    private Integer minPurchase;

    //프론트 엔드에 코드 추가 예정
    private Integer validDays;

    private LocalDate fixedStartDate;

    private LocalDate fixedEndDate;

}
