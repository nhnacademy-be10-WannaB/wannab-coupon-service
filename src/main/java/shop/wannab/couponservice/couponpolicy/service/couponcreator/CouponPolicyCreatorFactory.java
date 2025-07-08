package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import java.util.List;
import org.springframework.stereotype.Component;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyErrorCode;
import shop.wannab.couponservice.couponpolicy.exception.CouponPolicyException;

@Component
public class CouponPolicyCreatorFactory {
    private final List<CouponPolicyCreator> creators;

    public CouponPolicyCreatorFactory(List<CouponPolicyCreator> creators) {
        this.creators = creators;
    }

    public CouponPolicyCreator findCreator(String couponType){
        return creators.stream()
                .filter(creator -> creator.supports(couponType))
                .findFirst()
                .orElseThrow(() -> new CouponPolicyException(CouponPolicyErrorCode.INVALID_COUPON_TYPE));
    }
}
