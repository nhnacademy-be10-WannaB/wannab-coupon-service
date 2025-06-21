package shop.wannab.couponservice.domain.couponpolicy.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.couponservice.domain.couponpolicy.CouponPolicy;
import shop.wannab.couponservice.domain.enums.CouponType;
import shop.wannab.couponservice.domain.enums.DiscountType;

@Getter
@Setter
public class CouponPolicyResponseDto {

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

    private Long id;
    private String name;
    private String discountType;
    private String purchaseTerm;
    private String discount;
    private String period;
    private boolean autoIssue;

    public static CouponPolicyResponseDto from(CouponPolicy policy,String bookName,String categoryName) {
        CouponPolicyResponseDto dto = new CouponPolicyResponseDto();

        dto.id = policy.getCouponPolicyId();
        dto.name = policy.getCouponPolicyName();
        dto.discountType = policy.getDiscountType() == DiscountType.FIXED ? "정액" : "정률";

        if (policy.getDiscountType() == DiscountType.FIXED) {
            dto.setDiscount(String.format("%,d원", policy.getDiscountValue()));
        } else {
            dto.setDiscount(String.format("%d%%", policy.getDiscountValue()));
        }

        if (policy.getValidDays() > 0) {
            dto.setPeriod(String.format("발급 후 %d일", policy.getValidDays()));
        } else {
            dto.setPeriod(String.format("%s ~ %s", policy.getFixedStartDate(), policy.getFixedEndDate()));
        }
        if(policy.getCouponType() == CouponType.BOOK){
            dto.purchaseTerm = String.format("%s 구매시", bookName);
        } else if(policy.getCouponType() == CouponType.CATEGORY){
            dto.purchaseTerm = String.format("%s 카테고리 도서 구매시", categoryName);
        } else{
            dto.purchaseTerm = String.format("%,d원 이상", policy.getMinPurchase());
            dto.autoIssue = true;
        }
        return dto;
    }
}
