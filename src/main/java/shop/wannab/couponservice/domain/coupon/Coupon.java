package shop.wannab.couponservice.domain.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @NotNull
    private Long userId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CouponPolicy couponPolicy;

    @NotNull
    private String couponCode;

    @NotNull
    private LocalDate issuedAt;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;


    private LocalDate usedAt;

    @NotNull
    private CouponStatus status;

    private Long orderId;

    private Long orderBookId;

    public static Coupon createNewCoupon(Long userId, CouponPolicy couponPolicy,String prefix){
        String couponCode = String.format("%s%s-%s",prefix,LocalDate.now().toString().replace("-",""),
                UUID.randomUUID().toString().substring(0,20).replace("-","").toUpperCase());

        return Coupon.builder()
                .userId(userId)
                .couponPolicy(couponPolicy)
                .couponCode(couponCode)
                .issuedAt(LocalDate.now())
                .startDate(LocalDate.now())
                .endDate(couponPolicy.getFixedEndDate())
                .status(CouponStatus.NOT_USED)
                .build();

    }
}
