package ru.yandex.practicum.exception;

public class NotFoundShoppingCartException extends RuntimeException {
    public NotFoundShoppingCartException(String message) {
        super(message);
    }
}
