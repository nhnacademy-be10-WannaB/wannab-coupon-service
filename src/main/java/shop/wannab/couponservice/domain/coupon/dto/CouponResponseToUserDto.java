package shop.wannab.couponservice.domain.coupon.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.coupon.Coupon;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.DiscountType;

@Getter
@Setter
public class CouponResponseToUserDto {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private String couponName;
    private String discountInfo;
    private String purchaseTerm;
    private String period;

    public CouponResponseToUserDto(Coupon coupon) {
        CouponPolicy policy = coupon.getCouponPolicy();
        this.couponName = policy.getCouponPolicyName();
        this.discountInfo = buildDiscountInfo(policy) + " 할인";
        this.purchaseTerm = buildPurchaseTerm(policy);
        this.period = buildPeriodString(coupon.getEndDate());
    }

    private String buildDiscountInfo(CouponPolicy policy) {
        if (DiscountType.FIXED == (policy.getDiscountType())) {
            return String.format("%d원", policy.getDiscountValue());
        } else {
            return policy.getDiscountValue() + "%";
        }
    }


    private String buildPeriodString(LocalDate endDate){
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            return "기간 만료(" + endDate + ")까지";
        }
        String formattedEndDate = endDate.format(DATE_FORMATTER);
        return String.format("%s 까지",formattedEndDate);
    }

    private String buildPurchaseTerm(CouponPolicy policy) {
        StringBuilder termBuilder = new StringBuilder();

        if (policy.getCouponType() == CouponType.CATEGORY) {
            termBuilder.append("지정 카테고리 전용");
        } else if (policy.getCouponType() == CouponType.BOOK) {
            termBuilder.append("특정 도서 전용");
        }

        if (policy.getMinPurchase() > 0) {
            if (!termBuilder.isEmpty()) {
                termBuilder.append(" / ");
            }
            termBuilder.append(String.format("%,d원 이상 구매 시", policy.getMinPurchase()));
        }

        if (termBuilder.isEmpty()) {
            return "총 주문 금액에서 할인";
        }

        return termBuilder.toString();
    }

}
