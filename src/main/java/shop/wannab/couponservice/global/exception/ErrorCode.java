package shop.wannab.couponservice.global.exception;

public interface ErrorCode {
    int getStatus();
    int getCode();
    String getMessage();
}
