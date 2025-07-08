package shop.wannab.couponservice.couponpolicy.service.couponcreator;

import shop.wannab.couponservice.couponpolicy.dto.CreateCouponPolicyDto;

public interface CouponPolicyCreator {

    boolean supports(String couponType);

    void createCouponPolicy(CreateCouponPolicyDto createCouponPolicyDto);
}
