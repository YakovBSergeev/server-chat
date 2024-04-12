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

    @SneakyThrows
    @Override
    public void run() {

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        String messageFromClient;
        if (authorizationRegistration( bufferedReader )) {
            serverService.addObserver( this );
            log.info( serverService );
            System.out.println( "Client connected" );
            serverService.notifyObserverOnlyMe( "Client connected.", this );
            serverService.notifyArchiveMessage( this );
            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println( user.getName() + ":" + messageFromClient );
                saveMessage( messageFromClient );
//                serverService.notifyObserver( user.getName() + ":" + messageFromClient );
                serverService.notifyObserverExceptMe( user.getName() + ":" + messageFromClient, this );
                messageDao.saveMessage( user.getName(), messageFromClient );
            }
        }
    }

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

    @SneakyThrows
    @Override
    public void notifyMe(String message) {
        PrintWriter clientWrite = new PrintWriter( socket.getOutputStream() );
        clientWrite.println( message );
        clientWrite.flush();
    }

    private void sendToClient(String message) {
        ServerServiceImpl service = new ServerServiceImpl();
        service.addObserver( this );
        service.notifyObserverOnlyMe( message, this );
        service.deleteObserver( this );
        log.info( service );
    }

    @SneakyThrows
    private void saveMessage(String messageFromClient) {
        PrintWriter printWriter = new PrintWriter( "src/main/resources/arсhiveMessage.txt" );
        DateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
        Date date = new Date();
        printWriter.println( dateFormat.format( date ) + ":" + user.getName() + ":" + messageFromClient );
    }
}

