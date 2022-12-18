package it.fi.itismeucci;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientThread extends Thread{
    //ServerSocket server;
    Socket Socket_client;
    BufferedReader inDalClient;
    DataOutputStream outVersoClient;
    ObjectMapper invia = new ObjectMapper();
    ObjectMapper riceve = new ObjectMapper();
    String stringaRicevuta;
    String stringaDaInviare;
    String nomeClient;
    public static ArrayList<ClientThread> listaClientOn = new ArrayList<>();
    
    public ClientThread(Socket socket){
        try{
            this.Socket_client = socket;
        } 
        catch (Exception e){
            System.out.println("ClientThread.costructor: " + e.getMessage());
        }
    }

    public void run(){
        try{
            outVersoClient = new DataOutputStream(Socket_client.getOutputStream());
        } 
        catch (Exception e){
            System.out.println("errore generazione outVersoClient: " + e.getMessage());
        }
        //login
        do {
            try {
                inDalClient = new BufferedReader(new InputStreamReader(Socket_client.getInputStream()));
                stringaRicevuta = inDalClient.readLine();
                if(this.cercaClient(stringaRicevuta)){
                    outVersoClient.writeBytes("ok" + "\n");
                    nomeClient = stringaRicevuta;
                    break;
                }else{
                    outVersoClient.writeBytes("non ok" + "\n");
                }
            }catch (Exception e){
                //questo serve quando un client si disconnette bruscamente
                listaClientOn.remove(this);
                this.stop();
            }
        }while(true);
        //inserimento nella lista dei clientON
        System.out.println("NUOVO CLIENT :"+this.nomeClient);
        listaClientOn.add(this);
        try{
            comunica();
        }catch(Exception e){
            System.out.println("start comunica: " + e.getMessage());
        }
    }

    public void comunica(){
        for(;;){
            try{
                inDalClient = new BufferedReader(new InputStreamReader(Socket_client.getInputStream()));
                stringaRicevuta=inDalClient.readLine();
                pojoDatiClient objClient =  riceve.readValue(stringaRicevuta, pojoDatiClient.class);
                if(objClient.getCodiceOp() == 2){
                    System.out.println(objClient.nomeClient + " vuole disconnettersi, gli invio la conferma");
                }
                else{
                    System.out.println("messaggio ricevuto da "+objClient.getNomeClient() + " lo inoltro a "+ objClient.getDestinatario()+ ", ");
                    System.out.println("contenuto del messaggio: "+objClient.getCorpoMessaggio()+"\n");
                }
                switch(objClient.getCodiceOp()){
                    case 0://invio messaggio singolo
                        pojoDatiServer messaggiodainviare=new pojoDatiServer();
                        messaggiodainviare.setMessaggioSingolo(objClient.getCorpoMessaggio());
                        messaggiodainviare.setMessaggioGruppo("");//lo ho aggiunto
                        messaggiodainviare.setDestinatario(objClient.getDestinatario());
                        messaggiodainviare.setMittente(nomeClient);
                        String msg = invia.writeValueAsString(messaggiodainviare);
                        //controllo che il client scelto dal mittente sia online
                        boolean ilClientEPresente = false;
                        for(int i = 0;i < ClientThread.listaClientOn.size();i++){
                            if(ClientThread.listaClientOn.get(i).nomeClient.equals(messaggiodainviare.getDestinatario())){
                                DataOutputStream out = new DataOutputStream(ClientThread.listaClientOn.get(i).Socket_client.getOutputStream());
                                out.writeBytes(msg + "\n");
                                ilClientEPresente = true;
                            }
                        }
                        //il client destinatario non e' online
                        if(ilClientEPresente == false){
                            pojoDatiServer messError = new pojoDatiServer();
                            messError.setDestinatario(nomeClient);
                            messError.setMittente("server");
                            messError.setMessaggioSingolo("messaggio non inviato con successo");
                            outVersoClient.writeBytes(invia.writeValueAsString(messError) + "\n");
                        }
                        else//riinoltro il messaggio al client in modo che lui possa salvarselo nella conversazione
                            outVersoClient.writeBytes(msg + "\n");
                             
                        break;
                    case 1://invio messaggio gruppo
                        pojoDatiServer messaggiodainviare1=new pojoDatiServer();
                        messaggiodainviare1.setDestinatario("all");
                        messaggiodainviare1.setMittente(nomeClient);
                        messaggiodainviare1.setMessaggioGruppo(objClient.getCorpoMessaggio());
                        messaggiodainviare1.setMessaggioSingolo("");//lo ho aggiunto
                        String msg1 = invia.writeValueAsString(messaggiodainviare1);
                        //controllo se ci sono altri client oltre al mittente
                        if(listaClientOn.size() < 1){
                            pojoDatiServer messError = new pojoDatiServer();
                            messError.setDestinatario(nomeClient);
                            messError.setMittente("server");
                            messError.setMessaggioSingolo("messaggio non inviato con successo: sei l'unico client ad essere connesso");
                            outVersoClient.writeBytes(invia.writeValueAsString(messError) + "\n");
                            break;
                        }
                        for(int j = 0; j < listaClientOn.size(); j++){
                            DataOutputStream out = new DataOutputStream(listaClientOn.get(j).outVersoClient);
                            out.writeBytes(msg1 + "\n");
                            System.out.println("messaggio inoltrato a: " + listaClientOn.get(j).nomeClient);
                        }
                        break;
                    case 2://Chiude la comunicazione ad un client
                        pojoDatiServer confermaChiusura=new pojoDatiServer();
                        confermaChiusura.setDestinatario(nomeClient);
                        confermaChiusura.setMessaggioSingolo("stai uscendo dalla conversazione:chiudo la comunicazione...");
                        String msg2 = invia.writeValueAsString(confermaChiusura);
                        outVersoClient.writeBytes(msg2 + "\n");
                        listaClientOn.remove(this);
                        Socket_client.close();
                        break;    
                    }
            }catch (IOException e){
                listaClientOn.remove(this);
                this.stop();
                break;
            }
        } 
    }
    private boolean cercaClient(String nomeClient){
        for (int i = 0; i < listaClientOn.size(); i++){
            if(nomeClient.equals(listaClientOn.get(i).nomeClient)){
                return false;
            }
        }
        return true;
    }
}