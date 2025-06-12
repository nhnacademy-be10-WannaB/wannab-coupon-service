package shop.wannab.couponservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PolicyTargetCategory {
    @Id
    private long policyTargetCategoryId;

    @NotNull
    private long categoryId;

    @OneToOne
    @JoinColumn(nullable = false)
    private CouponPolicy couponPolicy;
}
