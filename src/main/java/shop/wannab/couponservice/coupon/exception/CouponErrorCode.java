package shop.wannab.couponservice.coupon.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import shop.wannab.couponservice.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    WELCOME_COUPON_POLICY_NOT_FOUND(404, "COUPON-2001", "활성화된 웰컴 쿠폰 정책이 존재하지 않습니다."),
    COUPON_ALREADY_ISSUED(409, "COUPON-2002", "이미 발급받은 쿠폰입니다."),
    BIRTHDAY_COUPON_POLICY_NOT_FOUND(404, "COUPON-2003", "활성화된 생일 쿠폰 정책이 존재하지 않습니다."),
    BIRTHDAY_COUPON_ISSUE_FAILED(500, "COUPON-2004", "생일 쿠폰 발급 중 오류가 발생했습니다."),
    COUPON_NOT_FOUND(404, "COUPON-2005", "존재하지 않는 쿠폰입니다."),
    COUPON_OWNER_NOT_MATCH(403, "COUPON-2006", "쿠폰 소유자가 일치하지 않습니다."),
    COUPON_ALREADY_USED_OR_EXPIRED(400, "COUPON-2007", "이미 사용되었거나 만료된 쿠폰입니다.");

    private final int status;
    private final String code;
    private final String message;
}
