package ru.itsjava.Exception;

public class UserFoundException extends RuntimeException {
    public UserFoundException(String message) {
        super( message );
    }
}
