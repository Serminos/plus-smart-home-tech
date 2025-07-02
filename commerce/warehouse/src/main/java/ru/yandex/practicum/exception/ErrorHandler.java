package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    public record ErrorResponse(String message) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            IllegalArgumentException.class,
            SpecifiedProductAlreadyInWarehouseException.class,
            ProductInShoppingCartLowQuantityInWarehouse.class,
            NoSpecifiedProductInWarehouseException.class,
            MethodArgumentNotValidException.class
    })
    public ErrorResponse handleIBadRequestException(final Exception e) {
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Throwable.class})
    public ErrorResponse handleAnyException(final Exception e) {
        return new ErrorResponse(e.getMessage());
    }
}