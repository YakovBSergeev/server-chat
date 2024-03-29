package ru.itsjava.Exception;

public class UserRegistrationException extends RuntimeException {
    public UserRegistrationException(String message) {
        super( message );
    }
}
