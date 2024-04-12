package ru.itsjava.Exception;

public class UserExit extends RuntimeException {
    public UserExit(String message) {
        super( message );
    }
}
