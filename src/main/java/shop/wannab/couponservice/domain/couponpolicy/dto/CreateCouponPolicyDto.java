package shop.wannab.couponservice.domain.couponpolicy.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCouponPolicyDto {
    //쿠폰 등록 모달 참조
    @NotNull
    private String couponType;

    private long targetBookId;

    private long targetCategoryId;

    @NotNull
    private String name;

    @NotNull
    private String discountType;

    private int minPurchase;

    @NotNull
    private int discountValue;

    private int maxDiscount;

    private int validDays;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isBirthday;

    private boolean isWelcome;

}
