package hu.szavazzapp.controller.api;

import hu.szavazzapp.dto.ApiResponse;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException exception) {
                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Érvénytelen kérés.");

                return ResponseEntity
                                .badRequest()
                                .body(new ApiResponse(false, message));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException exception) {
                return ResponseEntity
                                .badRequest()
                                .body(new ApiResponse(false, "Érvénytelen kérésparaméter."));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException exception) {
                return ResponseEntity
                                .badRequest()
                                .body(new ApiResponse(false, exception.getMessage()));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException exception) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse(false, exception.getMessage()));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse> handleDataIntegrity() {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(new ApiResponse(false,
                                                "Az adatbázis nem engedélyezte a műveletet. Lehetséges, hogy már szavaztál."));
        }
}