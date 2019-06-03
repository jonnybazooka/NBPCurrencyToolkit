package sample.exceptions;

public class NoSuchCurrencyException extends Throwable {
    public NoSuchCurrencyException() {
    }

    public NoSuchCurrencyException(String message) {
        super(message);
    }
}
