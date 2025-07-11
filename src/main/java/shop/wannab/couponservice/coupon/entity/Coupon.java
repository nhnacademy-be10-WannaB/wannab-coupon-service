package shop.wannab.couponservice.coupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import shop.wannab.couponservice.couponpolicy.entity.CouponPolicy;

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
    @JoinColumn(name = "coupon_policy_id",nullable = false)
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
    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private Long orderId;

    private Long orderBookId;

    public static Coupon createNewCoupon(Long userId, CouponPolicy couponPolicy,String prefix){
        if(userId < 0){
            return null;
        }

        String couponCode = String.format("%s%s-%s",prefix,LocalDate.now().toString().replace("-",""),
                UUID.randomUUID().toString().substring(0,20).replace("-","").toUpperCase());

        Coupon coupon = Coupon.builder()
                .userId(userId)
                .couponPolicy(couponPolicy)
                .couponCode(couponCode)
                .issuedAt(LocalDate.now())
                .startDate(LocalDate.now())
                .status(CouponStatus.NOT_USED)
                .build();

        if(couponPolicy.getValidDays() > 0){
            coupon.setEndDate(LocalDate.now().plusDays(couponPolicy.getValidDays()));
        }
        else{
            coupon.setEndDate(couponPolicy.getFixedEndDate());
        }
        return coupon;
    }
}
