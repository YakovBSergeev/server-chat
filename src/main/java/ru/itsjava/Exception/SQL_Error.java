package ru.itsjava.Exception;

public class SQL_Error extends RuntimeException {
    public SQL_Error(String message) {
        super( message );
    }
}
