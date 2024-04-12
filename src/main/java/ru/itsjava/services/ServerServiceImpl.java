package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.MessageDao;
import ru.itsjava.dao.MessageDaoImpl;
import ru.itsjava.dao.UserDao;
import ru.itsjava.dao.UserDaoImpl;
import ru.itsjava.utils.Props;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final static int PORT = 8081;
    private final List<Observer> observers = new ArrayList<>();
    private final UserDao userDao = new UserDaoImpl( new Props() );
    private final MessageDao messageDao = new MessageDaoImpl( new Props() );

    @SneakyThrows
    @Override
    public void start() {
        ServerSocket serverSocket = new ServerSocket( PORT );
        System.out.println( "= SERVER STARTS =" );


        while (true) {
            Socket socket = serverSocket.accept();

            if (socket != null) {
                Thread thread = new Thread( new ClientRunnable( socket, this, userDao, messageDao ) );
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
            if (!observer.equals( key ) && !message.split( ":" )[1].equals( "Exit" )) {
                key.notifyMe( message );
            } else if (!observer.equals( key ) && message.split( ":" )[1].equals( "Exit" )) {
                key.notifyMe( message.split( ":" )[0] + " вышел из чата." );
            }
        }
    }

    @Override
    public void notifyObserverOnlyMe(String message, Observer observer) {
        for (Observer key : observers) {
            if (observer.equals( key )) {
                key.notifyMe( message );
            }
        }
    }

    @Override
    public void notifyArchiveMessage(Observer observer) {
        for (Observer key : observers) {
            if (observer.equals( key )) {
                for (int i = 0; i < messageDao.printLastMessages().size(); i++) {
                    String message = messageDao.printLastMessages().get( i ).getFrom() + ":" + messageDao.printLastMessages().get( i ).getTo_text();
                    key.notifyMe( message );
                }
            }
        }
    }
}

