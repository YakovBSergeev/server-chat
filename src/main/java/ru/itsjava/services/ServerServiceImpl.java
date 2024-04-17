package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.MessageDao;
import ru.itsjava.dao.MessageDaoImpl;
import ru.itsjava.dao.UserDao;
import ru.itsjava.dao.UserDaoImpl;
import ru.itsjava.domain.User;
import ru.itsjava.utils.Props;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final static int PORT = 8081;
    private final List<Observer> observers = new ArrayList<>();
    private final Map<String, Observer> userObserver = new HashMap<>();

    private final UserDao userDao = new UserDaoImpl( new Props() );
    private final MessageDao messageDao = new MessageDaoImpl( new Props() );


    /**
     * Запускаем сервер.
     */

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

    /**
     * Добавляем пользователя в коллекцию Лист
     *
     * @param observer
     */
    @Override
    public void addObserver(Observer observer) {
        observers.add( observer );
    }

    /**
     * Удаляем пользователя из коллекции
     *
     * @param observer
     */

    @Override
    public void deleteObserver(Observer observer) {
        observers.remove( observer );
    }

    /**
     * Рассылаем сообщение пользователям из коллекции Лист
     *
     * @param message
     */

    @Override
    public void notifyObserver(String message) {
        for (Observer observer : observers) {
            observer.notifyMe( message );
        }
    }

    /**
     * Отправка сообщения всем кроме сябя
     *
     * @param message
     * @param observer
     */

    @Override
    public void notifyObserverExceptMe(String message, Observer observer) {
        for (Observer key : observers) {
            if (!observer.equals( key )) {
                key.notifyMe( message );
            }
        }
//        for (int i = 0; i < observers.size(); i++) {
//            if (!observer.equals( observers.get( i ) )) {
//                observers.get( i ).notifyMe( message );//
//            }
    }

    /**
     * Отправка сообщение только себе
     *
     * @param message
     * @param observer
     */
    @Override
    public void notifyObserverOnlyMe(String message, Observer observer) {
        for (Observer key : observers) {
            if (observer.equals( key )) {
                key.notifyMe( message );
            }
        }
    }

    /**
     * Добавление пользователя в коллекцию МАП (имя пользователя, пользователь)
     *
     * @param user
     * @param observer
     */
    @Override
    public void putObserver(String user, Observer observer) {
        userObserver.put( user, observer );
    }

    /**
     * Удаление пользователя из коллекции МАП (имя пользователя, пользователь)
     *
     * @param user
     * @param observer
     */
    @Override
    public void clearObserver(String user, Observer observer) {
        userObserver.remove( user, observer );
    }

    /**
     * Отправка приватного сообщения
     *
     * @param user
     * @param message
     */
    @Override
    public void notifyPrivate(String user, String message) {
        for (Map.Entry<String, Observer> key : userObserver.entrySet()
        )
            if (key.getKey().equals( user )) {
                key.getValue().notifyMe( message );
            }
    }

    /**
     * Печать МАПы
     */
    @Override
    public void printMap() {
        for (Map.Entry<String, Observer> key : userObserver.entrySet()
        ) {
            System.out.println( key.getKey() + ":" + key.getValue() );
        }
    }

    /**
     *
     * Проверка на повторный коннект уже подключенного к чату пользователя.
     * @param login
     * @param password
     * @return
     */
    @Override
    public boolean isNotConnect(String login, String password) {
        for (Map.Entry<String, Observer> key : userObserver.entrySet()
        )
            if (key.getKey().equals( login ) &&
                    userDao.findByNameAndPassword( login, password ).getName().equals( login )) {
                return false;
            }
        return true;
    }

    /**
     * Печать Лист коллекции
     */
    @Override
    public void printList() {
        for (Observer key : observers
        ) {
            System.out.println( key );
        }
    }

    /**
     * Вывод последних 10 сообщений из БД
     *
     * @param observer
     */
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

