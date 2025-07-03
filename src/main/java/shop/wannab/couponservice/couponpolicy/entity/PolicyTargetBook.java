package shop.wannab.couponservice.couponpolicy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
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
public class PolicyTargetBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyTargetBookId;

    @NotNull
    private Long bookId;

    @OneToOne
    @JoinColumn(name = "coupon_policy_id",nullable = false)
    private CouponPolicy couponPolicy;
}
