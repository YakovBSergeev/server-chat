package ru.itsjava.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@AllArgsConstructor
public class ClientRunnable implements Runnable, Observer {
    private final Socket socket;
    private final  ServerService serverService;


    @SneakyThrows
    @Override
    public void run() {
        System.out.println( "Client connected" );
        serverService.addObserver( this );
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        String messageFromClient;
        while ((messageFromClient = bufferedReader.readLine()) != null) {
            System.out.println( messageFromClient );
            serverService.notifyObserver( messageFromClient );
        }


    }

    @SneakyThrows
    @Override
    public void notifyMe(String message) {
        PrintWriter clientWrite = new PrintWriter( socket.getOutputStream() );
        clientWrite.println( message );
        clientWrite.flush();
    }
}
