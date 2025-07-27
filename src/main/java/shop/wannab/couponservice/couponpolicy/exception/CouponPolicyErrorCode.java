package shop.wannab.couponservice.couponpolicy.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import shop.wannab.couponservice.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CouponPolicyErrorCode implements ErrorCode {
    POLICY_ALREADY_EXISTS(409, "COUPON-1001", "해당 타입의 쿠폰 정책은 이미 존재합니다. 단 하나의 정책만 허용됩니다."),
    INVALID_BOOK_ID(400, "COUPON-1002", "BOOK 타입 쿠폰 정책 생성 시 유효한 도서 ID가 필요합니다."),
    BOOK_POLICY_ALREADY_EXISTS(409, "COUPON-1003", "해당 도서 ID에 대한 BOOK 타입 쿠폰 정책은 이미 존재합니다."),
    INVALID_CATEGORY_ID(400, "COUPON-1004", "CATEGORY 타입 쿠폰 정책 생성 시 유효한 카테고리 ID가 필요합니다."),
    CATEGORY_POLICY_ALREADY_EXISTS(409, "COUPON-1005", "해당 카테고리 ID에 대한 CATEGORY 타입 쿠폰 정책은 이미 존재합니다."),
    POLICY_NOT_FOUND(404, "COUPON-1006", "존재하지 않는 쿠폰 정책입니다."),
    INVALID_COUPON_TYPE(400, "COUPON-1007", "지원하지 않는 쿠폰 타입입니다.");

    private final int status;
    private final String code;
    private final String message;
}
