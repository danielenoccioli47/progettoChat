package it.fi.itismeucci;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Ricezione extends Thread{
    Client ilClientDaGestire;
    String stringaRicevuta;
    
    public Ricezione(Client ilClientDaGestire){
        this.ilClientDaGestire = ilClientDaGestire;
        try {
            ilClientDaGestire.inDalServer = new BufferedReader(new InputStreamReader(ilClientDaGestire.socketServer.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        for(;;){
            //leggo il messaggio
            try{
                stringaRicevuta=ilClientDaGestire.inDalServer.readLine();
                pojoDatiServer objServer = ilClientDaGestire.riceve.readValue(stringaRicevuta, pojoDatiServer.class);                 
                if(objServer.getDestinatario().equals("all")){
                    //gestisco il messaggio di gruppo
                    Messaggio m = new Messaggio(objServer.getMittente(),objServer.getMessaggioGruppo(), objServer.getDestinatario());
                    Client.messaggiGruppo.add(m);   
                }
                else if(objServer.getMittente().equals("server")){
                    System.out.println(objServer.getMessaggioSingolo());
                }
                else{
                    //gestisco il messaggio in base al mittente
                    Messaggio m = new Messaggio(objServer.getMittente(),objServer.getMessaggioSingolo(), objServer.getDestinatario());
                    Client.messaggiSingoli.add(m);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
