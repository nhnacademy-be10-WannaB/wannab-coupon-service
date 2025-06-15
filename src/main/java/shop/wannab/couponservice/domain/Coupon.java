package shop.wannab.couponservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.couponservice.domain.enums.CouponStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @NotNull
    private String userId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CouponPolicy couponPolicy;

    @NotNull
    private String couponPolicyName;

    @NotNull
    private LocalDate issuedAt;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private LocalDate usedAt;

    @NotNull
    private CouponStatus status;

    @NotNull
    private Long orderId;

    @NotNull
    private Long orderBookId;
}
