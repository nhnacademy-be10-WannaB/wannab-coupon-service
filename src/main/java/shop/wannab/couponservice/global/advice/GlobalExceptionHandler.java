package shop.wannab.couponservice.global.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import shop.wannab.couponservice.global.exception.BaseException;
import shop.wannab.couponservice.global.exception.ErrorCode;
import shop.wannab.couponservice.global.exception.ErrorResponse;

@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode.getStatus(),errorCode.getCode(), errorCode.getMessage());
        log.warn("action=handleBaseException,"
                +"statusCode: {}, errorCode : {}, message : {}", errorCode.getStatus(), e.getErrorCode(), e.getMessage());
        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.valueOf(errorCode.getStatus()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        final int DEFAULT_ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
        final String DEFAULT_ERROR_MESSAGE = "알 수 없는 서버 오류가 발생했습니다.";
        final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(DEFAULT_ERROR_CODE,"INTERNAL_SERVER_ERROR",DEFAULT_ERROR_MESSAGE);

        log.error("action=handleRuntimeException,"
                        + "statusCode: {}, errorCode : {}, message : {}",
                DEFAULT_ERROR_CODE, DEFAULT_HTTP_STATUS, e.getMessage(), e);

        return new ResponseEntity<>(response, DEFAULT_HTTP_STATUS);
    }
}
