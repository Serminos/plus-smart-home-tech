package ru.yandex.practicum.exception;

public class NotAssembledOrderException extends RuntimeException {
    public NotAssembledOrderException(String message) {
        super(message);
    }
}
