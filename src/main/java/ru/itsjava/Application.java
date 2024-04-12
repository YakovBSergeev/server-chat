package ru.itsjava;



import ru.itsjava.services.ServerService;
import ru.itsjava.services.ServerServiceImpl;


public class Application {



    public static void main(String[] args){

        ServerService serverService = new ServerServiceImpl();
        serverService.start();

    }
}
