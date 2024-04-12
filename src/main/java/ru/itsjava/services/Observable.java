package ru.itsjava.services;

public interface Observable {
    void addObserver(Observer observer);

    void deleteObserver(Observer observer);
    void notifyArchiveMessage(Observer observer);

    void notifyObserver(String message);

    void notifyObserverExceptMe(String message, Observer observer);

    void notifyObserverOnlyMe(String message, Observer observer);
}
