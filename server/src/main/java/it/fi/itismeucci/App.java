package it.fi.itismeucci;

import java.net.Socket;
import java.net.ServerSocket;

public class App 
{
    public static void main( String[] args )
    {
        try{
            ServerSocket serverSocket = new ServerSocket(9999);
            //ogni volta che che un client mi si connette, un nuovo thread si preoccupa di gestirlo
            for(int i=1; i > 0; i++){
                //in attesa che un client si connetta...
                Socket socket = serverSocket.accept();
                ClientThread serverThread = new ClientThread(socket);
                serverThread.start();
            }
            serverSocket.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("errore durante l'instanziamento del server");
            System.exit(1);
        }
    }
}
