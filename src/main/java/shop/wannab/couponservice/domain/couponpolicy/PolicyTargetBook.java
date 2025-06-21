package shop.wannab.couponservice.domain.couponpolicy;

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
    @JoinColumn(nullable = false)
    private CouponPolicy couponPolicy;
}
