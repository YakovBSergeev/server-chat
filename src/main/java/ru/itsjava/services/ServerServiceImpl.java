package ru.itsjava.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.UserDao;
import ru.itsjava.dao.UserDaoImpl;
import ru.itsjava.utils.Props;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final static int PORT = 8081;
    private final List<Observer> observers = new ArrayList<>();
    private final UserDao userDao = new UserDaoImpl( new Props() );

    @SneakyThrows
    @Override
    public void start() {
        ServerSocket serverSocket = new ServerSocket( PORT );
        System.out.println( "= SERVER STARTS =" );


        while (true) {
            Socket socket = serverSocket.accept();

            if (socket != null) {
                Thread thread = new Thread( new ClientRunnable( socket, this , userDao ));
                thread.start();

            }
        }
    }


    @Override
    public void addObserver(Observer observer) {
        observers.add( observer );
    }

    @Override
    public void deleteObserver(Observer observer) {
        observers.remove( observer );
    }

    @Override
    public void notifyObserver(String message) {

        for (Observer observer : observers) {
            observer.notifyMe( message );
        }
    }

    @Override
    public void notifyObserverExceptMe(String message, Observer observer) {
//        for (int i = 0; i < observers.size(); i++) {
//            if (!observer.equals( observers.get( i ) )) {
//                observers.get( i ).notifyMe( message );
//                System.out.println( observers.get( i ).hashCode() );
//            }
//                }

        for (Observer key : observers) {
            if (!observer.equals( key )) {
                key.notifyMe( message );
            }
        }

    }
}

