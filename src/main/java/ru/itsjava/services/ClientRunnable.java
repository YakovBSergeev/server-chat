package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import ru.itsjava.Exception.UserNotFoundException;
import ru.itsjava.Exception.UserRegistrationException;
import ru.itsjava.dao.MessageDao;
import ru.itsjava.dao.UserDao;
import ru.itsjava.domain.User;

import java.io.*;
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
     * После прохождения авторизации/регистрации пользователя добавляем в коллекцию List<Observer> и Map<String, Observer>.
     * Выводим в консоль сообщение  "Client connected", отправляем это сообщение пользователю и
     * выводим пользователю последние десять сообщении чата из БД.
     * Обрабатываем последующие сообщения от пользователя: выводим сообщение в консоль, сохраняем в файл,
     * рассылаем собщение всем подключеннвм пользователям кроме автора, сохраняем сообщение в БД, рассылаем личные сообщения,
     * при выходе из чата рассылаем всем пользователям сообщение "вышел из чата".
     */
    @SneakyThrows
    @Override
    public void run() {

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        String messageFromClient;
        if (authorizationRegistration( bufferedReader )) {
            serverService.addObserver( this );
            serverService.putObserver( user.getName(), this );
            log.info( serverService );
            System.out.println( "Client connected" );
            serverService.notifyObserverOnlyMe( "Client connected.", this );
            serverService.notifyArchiveMessage( this );
            serverService.notifyObserverOnlyMe( "Для личной переписки введите имя::сообщение.", this );

            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println( user.getName() + ":" + messageFromClient );
                if (messageFromClient.contains( "::" )) {
                    if (userDao.nameIs( messageFromClient.split( "::" )[0] )) {
                        saveMessage( messageFromClient );
                        serverService.notifyPrivate( messageFromClient.split( "::" )[0], user.getName() + ":" +
                                messageFromClient.split( "::" )[1] );
                        messageDao.saveMessage( user.getName(), messageFromClient );
                    }
                } else {
                    if (!messageFromClient.equals( "Exit" )) {
                        saveMessage( messageFromClient );
                        serverService.notifyObserverExceptMe( user.getName() + ":" + messageFromClient, this );
                        messageDao.saveMessage( user.getName(), messageFromClient );
                    } else {
                        serverService.notifyObserverExceptMe( user.getName() + ":" + " вышел из чата.", this );
                        serverService.deleteObserver( this );
                        serverService.clearObserver( user.getName(), this );
                    }
                }
            }
        }
    }

    /**
     * Метод проверяет авторизацию пользователя или регистрирует нового пользователя в БД.
     * Авторизируем пользователя (true) или ловим ошибку UserNotFoundException и отправляем пользователю сообщение
     * "Такой пользователь не зарегистрирован. Авторизуйтесь повторно."
     * Регистриуем пользователя (true) или ловим ошибку UserRegistrationException и отправляем пользователю сообщение
     * "Такой пользователь уже зарегистрирован. Измените данные регистрации." Проверка на повторное подключение.
     *
     * @param bufferedReader входящий поток с данными о поьлзователе.
     * @return true/false
     */
    @SneakyThrows
    private boolean authorizationRegistration(BufferedReader bufferedReader) {
        String authorizationMessage;
        while ((authorizationMessage = bufferedReader.readLine()) != null) {
            String login = authorizationMessage.substring( 8 ).split( ":" )[0];
            String password = authorizationMessage.substring( 8 ).split( ":" )[1];
            try {
                if (authorizationMessage.startsWith( "1!autho!" ) && serverService.isNotConnect( login, password )) {
                    user = userDao.findByNameAndPassword( login, password );
                    return true;
                } else if (authorizationMessage.startsWith( "1!autho!" ) && !serverService.isNotConnect( login, password )) {
                    sendToClient( "Такой пользователь уже подключен." );
//                    throw new UserAlreadyRegistrationException( "UserAlreadyRegistration" );
                }
            } catch (UserNotFoundException userNotFoundException) {
//                userNotFoundException.printStackTrace();
                sendToClient( "Такой пользователь не зарегистрирован. Авторизуйтесь повторно." );
            }
            try {
                if (authorizationMessage.startsWith( "2!autho!" )) {
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
        if (!messageFromClient.equals( "Exit" )) {
            BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter( "src/main/resources/arсhiveMessage.txt", true ) );
            DateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
            Date date = new Date();
            bufferedWriter.write( dateFormat.format( date ) + "       " + user.getName() + ":" + messageFromClient );
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
    }

}

