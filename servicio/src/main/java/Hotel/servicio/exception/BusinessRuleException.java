package Hotel.servicio.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    public BusinessRuleException(String code, HttpStatus httpStatus, String mensaje) {
        super(mensaje);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessRuleException(String code, HttpStatus httpStatus, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
