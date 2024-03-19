package ru.itsjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Application {
    public final static int PORT = 8081;


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket( PORT );

        System.out.println( "= SERVER STARTS =" );
        while (true) {
            Socket socket = serverSocket.accept();

            if (socket != null) {
                System.out.println( "Client connected" );
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                System.out.println( bufferedReader.readLine() );
            }
            int g=0;


        }


    }
}
