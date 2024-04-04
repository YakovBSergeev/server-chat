package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.Exception.UserNotFoundException;
import ru.itsjava.Exception.UserRegistrationException;
import ru.itsjava.dao.UserDao;
import ru.itsjava.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RequiredArgsConstructor
public class ClientRunnable implements Runnable, Observer {
    private final Socket socket;
    private final ServerService serverService;
    private User user;
    private final UserDao userDao;

    @SneakyThrows
    @Override
    public void run() {

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        String messageFromClient;
        if (authorizationRegistration( bufferedReader )) {
            serverService.addObserver( this );
            System.out.println( "Client connected" );
            sendToClient( "Client connected." );
            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println( user.getName() + ":" + messageFromClient );
//                serverService.notifyObserver( user.getName() + ":" + messageFromClient );
                serverService.notifyObserverExceptMe( user.getName() + ":" + messageFromClient, this );
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
                } else if (authorizationMessage.startsWith( "2!autho!" )) {
                    String login = authorizationMessage.substring( 8 ).split( ":" )[0];
                    String password = authorizationMessage.substring( 8 ).split( ":" )[1];
                    user = userDao.addUser( login, password );
                    return true;
                }
            } catch (UserNotFoundException userNotFoundException) {
//                userNotFoundException.printStackTrace();
                sendToClient( "Такой пользователь не зарегистрирован. Авторизуйтесь повторно." );//
            } catch (UserRegistrationException userRegistrationException) {
//                userRegistrationException.printStackTrace();
                sendToClient( "Такой пользователь уже зарегистрирован. Измените данные регистрации." );//
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
    }
}

