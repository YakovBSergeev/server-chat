package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import ru.itsjava.Exception.UserNotFoundException;
import ru.itsjava.Exception.UserRegistrationException;
import ru.itsjava.dao.MessageDao;
import ru.itsjava.dao.UserDao;
import ru.itsjava.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
public class ClientRunnable implements Runnable, Observer {

    private final Socket socket;
    private final ServerService serverService;
    private User user;
    private final UserDao userDao;
    private final MessageDao messageDao;
    private static final Logger log = Logger.getLogger( ClientRunnable.class );

    /**
     * Этот метод работает в потоке.
     * После прохождения авторизации/регистрации пользователя добавляем в коллекцию List<Observer>.
     * Выводим в консоль сообщение  "Client connected", отправляем это сообщение пользователю и
     * выводим пользователю последние десять сообщении чата из БД.
     * Обрабатываем последующие сообщения от пользователя: выводим сообщение в консоль, сохраняем в файл (МЕТОД НЕ РАБОТАЕТ),
     * рассылаем собщение всем подключеннвм пользователям кроме автора, сохраняем сообщение в БД.
     */
    @SneakyThrows
    @Override
    public void run() {

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        String messageFromClient;
        if (authorizationRegistration( bufferedReader )) {
            serverService.addObserver( this );
            serverService.putObserver( user, this );
            log.info( serverService );
            System.out.println( "Client connected" );
            serverService.notifyObserverOnlyMe( "Client connected.", this );
            serverService.notifyArchiveMessage( this );
            serverService.printMap();
            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println( user.getName() + ":" + messageFromClient );
                if (userDao.nameIs( messageFromClient.split( ":" )[0] )) {
                    serverService.notifyPrivate( messageFromClient.split( ":" )[0], messageFromClient.split( ":" )[1] );
                    messageDao.saveMessage( user.getName(), user.getName() + ":" + messageFromClient );
                } else {
                    saveMessage( messageFromClient );
                    serverService.notifyObserverExceptMe( user.getName() + ":" + messageFromClient, this );
                    messageDao.saveMessage( user.getName(), messageFromClient );
                }
            }
        }
    }


    /**
     * Медот проверяет авторизацию пользователя или регистрирует нового пользователя в БД.
     * Авторизируем пользователя (true) или ловим ошибку UserNotFoundException и отправляем пользователю сообщение
     * "Такой пользователь не зарегистрирован. Авторизуйтесь повторно."
     * Регистриуем пользователя (true) или ловим ошибку UserRegistrationException и отправляем пользователю сообщение
     * "Такой пользователь уже зарегистрирован. Измените данные регистрации."
     *
     * @param bufferedReader входящий поток с данными о поьлзователе.
     * @return true/false
     */
    @SneakyThrows
    private boolean authorizationRegistration(BufferedReader bufferedReader) {
        String authorizationMessage;
        while ((authorizationMessage = bufferedReader.readLine()) != null) {
            try {
                if (authorizationMessage.startsWith( "1!autho!" )) {
                    String login = authorizationMessage.substring( 8 ).split( ":" )[0];
                    String password = authorizationMessage.substring( 8 ).split( ":" )[1];
                    user = userDao.findByNameAndPassword( login, password );
                    return true;
                }
            } catch (UserNotFoundException userNotFoundException) {
//                userNotFoundException.printStackTrace();
                sendToClient( "Такой пользователь не зарегистрирован. Авторизуйтесь повторно." );
            }
            try {
                if (authorizationMessage.startsWith( "2!autho!" )) {
                    String login = authorizationMessage.substring( 8 ).split( ":" )[0];
                    String password = authorizationMessage.substring( 8 ).split( ":" )[1];
                    user = userDao.addUser( login, password );
                    return true;
                }
            } catch (
                    UserRegistrationException userRegistrationException) {
//                userRegistrationException.printStackTrace();
                sendToClient( "Такой пользователь уже зарегистрирован. Измените данные регистрации." );
            }
        }
        return false;
    }

    /**
     * Рассылает сообщения с сервера.
     *
     * @param message
     */

    @SneakyThrows
    @Override
    public void notifyMe(String message) {
        PrintWriter clientWrite = new PrintWriter( socket.getOutputStream() );
        clientWrite.println( message );
        clientWrite.flush();
    }

    /**
     * Отправляет сообщение пользователю, если авторизация/регистрация не пройдена.
     *
     * @param message
     */

    private void sendToClient(String message) {
        ServerServiceImpl service = new ServerServiceImpl();
        service.addObserver( this );
        service.notifyObserverOnlyMe( message, this );
        service.deleteObserver( this );
        log.info( service );
    }

    /**
     * Сохраняет сообщение в файл.
     *
     * @param messageFromClient
     */

    @SneakyThrows
    private void saveMessage(String messageFromClient) {
        PrintWriter printWriter = new PrintWriter( "src/main/resources/arсhiveMessage.txt" );
        DateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
        Date date = new Date();
        printWriter.println( dateFormat.format( date ) + ":" + user.getName() + ":" + messageFromClient );
    }

}

