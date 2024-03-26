package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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


    @SneakyThrows
    @Override
    public void run() {
        System.out.println( "Client connected" );

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        String messageFromClient;
        if (authorization( bufferedReader )) {
            serverService.addObserver( this );

            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println( user.getName() + ":" + messageFromClient );
//                serverService.notifyObserver( user.getName() + ":" + messageFromClient );
                serverService.notifyObserverExceptMe( user.getName() + ":" + messageFromClient, this );
            }
        }

    }

    @SneakyThrows
    private boolean authorization(BufferedReader bufferedReader) {
        String authorizationMessage;
        while ((authorizationMessage = bufferedReader.readLine()) != null) {
//            !autho!login:password
            if (authorizationMessage.startsWith( "!autho!" )) {
                String login = authorizationMessage.substring( 7 ).split( ":" )[0];
                String password = authorizationMessage.substring( 7 ).split( ":" )[1];
                user = new User( login, password );
                return true;
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
}
