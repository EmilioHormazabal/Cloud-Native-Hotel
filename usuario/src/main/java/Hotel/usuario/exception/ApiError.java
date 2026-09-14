package Hotel.usuario.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private Map<String, String> validations;
    private String path;

    public ApiError(LocalDateTime timestamp, int status, String error, String message, Map<String, String> validations) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = null;
        this.message = message;
        this.validations = validations;
        this.path = null;
    }
}
