package shop.wannab.couponservice.global.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import shop.wannab.couponservice.global.exception.BaseException;
import shop.wannab.couponservice.global.exception.ErrorCode;
import shop.wannab.couponservice.global.exception.ErrorResponse;

public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.valueOf(errorCode.getStatus()));
    }
}
