package shop.wannab.couponservice.domain.couponpolicy.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;

@Getter
@Setter
public class CouponPolicyDetailResponseDto {

    //실제 구동시에는 도서 api로 요청을 보내든 접근을 보내든 하기
    public static final Map<Long, String> DUMMY_BOOKS = Map.ofEntries(
            Map.entry(1L, "Do it! 자바 프로그래밍 입문"),
            Map.entry(2L, "이것이 자바다"),
            Map.entry(3L, "객체지향의 사실과 오해"),
            Map.entry(4L, "모던 자바스크립트 Deep Dive"),
            Map.entry(5L, "스프링 부트와 AWS로 혼자 구현하는 웹 서비스"),
            Map.entry(6L, "코틀린 인 액션 (Kotlin in Action)"),
            Map.entry(7L, "Clean Code(클린 코드)"),
            Map.entry(8L, "실용주의 프로그래머"),
            Map.entry(9L, "데이터베이스 개론"),
            Map.entry(10L, "만들면서 배우는 클린 아키텍처")
    );

    private long couponPolicyId;
    private String couponPolicyName;
    private String couponType;
    private String discountType;
    private int discountValue;
    private int maxDiscount;
    private int minPurchase;

    private int validDays;
    private String startDate;
    private String endDate;

    private String bookName;
    private String categoryName;

    public static CouponPolicyDetailResponseDto from(CouponPolicy couponPolicy,String bookName,
                                                     String categoryName) {
        CouponPolicyDetailResponseDto dto = new CouponPolicyDetailResponseDto();
        dto.couponPolicyId = couponPolicy.getCouponPolicyId();
        dto.couponPolicyName = couponPolicy.getCouponPolicyName();
        dto.couponType = couponPolicy.getCouponType().toString();
        dto.discountType = couponPolicy.getDiscountType().toString();
        dto.discountValue = couponPolicy.getDiscountValue();
        dto.maxDiscount = couponPolicy.getMaxDiscount();
        dto.minPurchase = couponPolicy.getMinPurchase();

        if(couponPolicy.getValidDays() != null){
            dto.validDays = couponPolicy.getValidDays();
        }

        else{
            dto.startDate = couponPolicy.getFixedStartDate().toString();
            dto.endDate = couponPolicy.getFixedEndDate().toString();
        }

        if(couponPolicy.getCouponType() == CouponType.BOOK){
            dto.bookName = bookName;
        }

        return dto;
    }


}
