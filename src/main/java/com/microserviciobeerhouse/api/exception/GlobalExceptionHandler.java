package com.microserviciobeerhouse.api.exception;

import com.microserviciobeerhouse.api.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Manejo de Códigos de Estado 400 - Solicitud Incorrecta (Bad Request)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.warn("Excepción 400 Bad Request en URI: {}. Detalle: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Manejo de 400 para tipos de datos inválidos en parámetros (ej: enviar texto en un ID numérico)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensaje = String.format("El parámetro '%s' con valor '%s' no es válido. Tipo esperado: %s",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "entero");
        log.warn("Excepción 400 Type Mismatch en URI: {}. Mensaje: {}", request.getRequestURI(), mensaje);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request - Tipo de parámetro inválido",
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Manejo de 400 para parámetros obligatorios no suministrados
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String mensaje = String.format("El parámetro obligatorio '%s' no ha sido proporcionado", ex.getParameterName());
        log.warn("Excepción 400 Missing Parameter en URI: {}. Mensaje: {}", request.getRequestURI(), mensaje);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request - Parámetro faltante",
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Manejo de 400 para argumentos ilegales generales
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Excepción 400 IllegalArgumentException en URI: {}. Mensaje: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Manejo de recurso no encontrado (mapeado a 400 conforme al requerimiento de control de estados 400/500)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Excepción 400 Recurso no encontrado en URI: {}. Detalle: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Recurso no encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Manejo de Códigos de Estado 500 - Error Interno del Servidor (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Excepción 500 Internal Server Error no controlada en URI: {}. Error: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrió un error interno en el servidor: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
