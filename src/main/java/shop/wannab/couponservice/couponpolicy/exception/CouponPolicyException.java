package shop.wannab.couponservice.couponpolicy.exception;

import shop.wannab.couponservice.global.exception.BaseException;
import shop.wannab.couponservice.global.exception.ErrorCode;

public class CouponPolicyException extends BaseException {

    public CouponPolicyException(ErrorCode errorCode){
        super(errorCode);
    }
}
