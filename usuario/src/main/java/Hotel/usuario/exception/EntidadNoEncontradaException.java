package Hotel.usuario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntidadNoEncontradaException extends RuntimeException {

    private final String code;

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
        this.code = "ENTITY-001";
    }

    public EntidadNoEncontradaException(String code, String mensaje) {
        super(mensaje);
        this.code = code;
    }

    public EntidadNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.code = "ENTITY-001";
    }

    public String getCode() {
        return code;
    }
}
