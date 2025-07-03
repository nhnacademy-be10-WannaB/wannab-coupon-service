package shop.wannab.couponservice.coupon.exception;

import lombok.Getter;
import shop.wannab.couponservice.global.exception.BaseException;
import shop.wannab.couponservice.global.exception.ErrorCode;

@Getter
public class CouponException extends BaseException {

    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }
}
