package it.fi.itismeucci;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientThread extends Thread{
    Socket Socket_client;
    BufferedReader inDalClient;
    DataOutputStream outVersoClient;
    ObjectMapper inviaRiceve = new ObjectMapper();
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
        comunica();
    }

    public void comunica(){
        for(;;){
            try{
                inDalClient = new BufferedReader(new InputStreamReader(Socket_client.getInputStream()));
                stringaRicevuta=inDalClient.readLine();
                pojoDatiClient objClient =  inviaRiceve.readValue(stringaRicevuta, pojoDatiClient.class);

                //
                boolean ilClientEPresente = false;
                for(int i = 0;i < listaClientOn.size();i++){
                    if(listaClientOn.get(i).nomeClient.equals(objClient.getDestinatario())){
                        ilClientEPresente = true;
                    }
                }
                //
                if(objClient.getCodiceOp() == 2){
                    System.out.println(objClient.nomeClient + " vuole disconnettersi, gli invio la conferma");
                }
                else if(ilClientEPresente){
                    System.out.println("messaggio ricevuto da "+objClient.getNomeClient() + " lo inoltro a "+ objClient.getDestinatario()+ ", ");
                    System.out.println("contenuto del messaggio: "+objClient.getCorpoMessaggio()+"\n");
                }
                else if(!ilClientEPresente){
                    System.out.println("messaggio ricevuto da "+objClient.getNomeClient() + " non lo posso inoltrare a "+ objClient.getDestinatario()+ ", ");
                    System.out.println("perche il destinatario non esiste."+"\n");
                }
                switch(objClient.getCodiceOp()){
                    case 0://invio messaggio singolo
                        pojoDatiServer messaggiodainviare=new pojoDatiServer();
                        messaggiodainviare.setMessaggioSingolo(objClient.getCorpoMessaggio());
                        messaggiodainviare.setMessaggioGruppo("");
                        messaggiodainviare.setDestinatario(objClient.getDestinatario());
                        messaggiodainviare.setMittente(nomeClient);
                        String msg = inviaRiceve.writeValueAsString(messaggiodainviare);
                        //controllo che il client scelto dal mittente sia online
                        if(ilClientEPresente){
                            for(int i = 0;i < listaClientOn.size();i++){
                                if(listaClientOn.get(i).nomeClient.equals(messaggiodainviare.getDestinatario())){
                                    DataOutputStream out = new DataOutputStream(ClientThread.listaClientOn.get(i).Socket_client.getOutputStream());
                                    out.writeBytes(msg + "\n");
                                    outVersoClient.writeBytes(msg + "\n");
                                }
                            }
                        }
                        //il client destinatario non e' online
                        else if(ilClientEPresente == false){
                            pojoDatiServer messError = new pojoDatiServer();
                            messError.setDestinatario(nomeClient);
                            messError.setMittente("server");
                            messError.setMessaggioSingolo("messaggio non inviato con successo");
                            outVersoClient.writeBytes(inviaRiceve.writeValueAsString(messError) + "\n");
                        }
                            
                             
                        break;
                    case 1://invio messaggio gruppo
                        pojoDatiServer messaggiodainviare1=new pojoDatiServer();
                        messaggiodainviare1.setDestinatario("all");
                        messaggiodainviare1.setMittente(nomeClient);
                        messaggiodainviare1.setMessaggioGruppo(objClient.getCorpoMessaggio());
                        messaggiodainviare1.setMessaggioSingolo("");//lo ho aggiunto
                        String msg1 = inviaRiceve.writeValueAsString(messaggiodainviare1);
                        //controllo se ci sono altri client oltre al mittente
                        if(listaClientOn.size() < 1){
                            pojoDatiServer messError = new pojoDatiServer();
                            messError.setDestinatario(nomeClient);
                            messError.setMittente("server");
                            messError.setMessaggioSingolo("messaggio non inviato con successo: sei l'unico client ad essere connesso");
                            outVersoClient.writeBytes(inviaRiceve.writeValueAsString(messError) + "\n");
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
                        String msg2 = inviaRiceve.writeValueAsString(confermaChiusura);
                        outVersoClient.writeBytes(msg2 + "\n");
                        outVersoClient.close();
                        inDalClient.close();
                        Socket_client.close();
                        listaClientOn.remove(this);
                        this.stop();
                        break;    
                    }
            }catch (Exception e){
                System.out.println(this.nomeClient + " si è disconnesso.");
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