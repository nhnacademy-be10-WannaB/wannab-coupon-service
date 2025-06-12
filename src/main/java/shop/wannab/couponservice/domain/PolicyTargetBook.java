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
public class PolicyTargetBook {
    @Id
    private long policyTargetBookId;

    @NotNull
    private long bookId;

    @OneToOne
    @JoinColumn(nullable = false)
    private CouponPolicy couponPolicy;
}
