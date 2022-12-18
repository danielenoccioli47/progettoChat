package it.fi.itismeucci;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Client{
    String nomeServer = "localHost";
    int portaServer = 9999;
    Socket socketServer;
    BufferedReader tastiera;
    DataOutputStream outVersoServer = new DataOutputStream(null);
    BufferedReader inDalServer;
    public static ArrayList<Messaggio> messaggiGruppo = new ArrayList<Messaggio>(); 
    public static ArrayList<Messaggio> messaggiSingoli = new ArrayList<Messaggio>();
    ObjectMapper invia = new ObjectMapper();
    ObjectMapper riceve = new ObjectMapper();
    String nome;

    public Client(){
        tastiera = new BufferedReader(new InputStreamReader(System.in));
    }

    public void connetti(){
        try{
            //creo il soket con ip (nel nostro caso 127.0.0.1 o localhost) e porta
            socketServer = new Socket(nomeServer, portaServer);
            outVersoServer = new DataOutputStream(socketServer.getOutputStream());
            inDalServer = new BufferedReader(new InputStreamReader(socketServer.getInputStream()));
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("errore durante la connessione");
            System.exit(1);
        }
    }

    public void inizia(){
        //manca la lista dei client on
        int tentativi = 0;
        do{
            tentativi++;
            try{
                //invio messaggio con nome e leggo la risposta(ok, non ok)
                if(tentativi == 1)
                    System.out.println("scrivi il tuo nome");

                String n = tastiera.readLine();
                //invio la stringa a clinetThread
                outVersoServer.writeBytes(n + "\n");
                //ora attendo la conferma
                String s = inDalServer.readLine();
                if(s.equals("ok")){
                    this.nome = n;
                    break;//esco dal while
                }
                else if(s.equals("non ok"))
                    System.out.println("nome non disponibile, inseriscine un altro");
                //potrei fare un controllo sui tentativi tipo se dopo 3 tentativi non riesco a connettermi allora chiudo la comunicazione
            } catch (IOException e){}
        } while (true);
        //creo il thread che gestisce la ricezione
        Ricezione ricevoMessaggi = new Ricezione(this);
        ricevoMessaggi.start();
        //
        boolean flag = true;
        String s;
        while(flag){
            System.out.println("1-invio messaggio singolo");
            System.out.println("2-invio messaggio gruppo");
            System.out.println("3-chiudi la comunicazione");
            try{
                int scelta = 0;
                s = tastiera.readLine();
                if(s.equals("1") || s.equals("2") || s.equals("3"))          
                    scelta = Integer.parseInt(s);                    

                switch(scelta){
                    case 0:
                        System.out.println("scegli un opzione tra quelle proposte(1,2,3)");
                        break;

                    case 1://invio messaggio ad un singolo client                        
                        System.out.println("inserisci il destinatario");
                        String dest = tastiera.readLine();
                        if(dest.equals(this.nome)){
                            System.out.println("non puoi inviare un messaggio a te stesso");
                            break;
                        }
                        pojoDatiClient objClient = new pojoDatiClient();
                        objClient.setCodiceOp(0);
                        objClient.setDestinatario(dest);
                        System.out.println(stampaMessaggi(dest));                        
                        System.out.println("inserisci il messaggio");
                        String messaggio = tastiera.readLine();
                        objClient.setCorpoMessaggio(messaggio);
                        objClient.setNomeClient(nome);
                        outVersoServer.writeBytes(invia.writeValueAsString(objClient) + "\n");
                    break;

                    case 2://invio messaggio a tutti
                        System.out.println(stampaMessaggiDiGruppo());
                        pojoDatiClient objClient2 = new pojoDatiClient();
                        objClient2.setCodiceOp(1);
                        objClient2.setDestinatario("all");
                        System.out.println("inserisci il messaggio");
                        String messaggio2 = tastiera.readLine();
                        objClient2.setCorpoMessaggio(messaggio2);
                        objClient2.setNomeClient(nome);
                        outVersoServer.writeBytes(invia.writeValueAsString(objClient2) + "\n");
                    break;

                    case 3://chiudo la comunicazione
                        pojoDatiClient objClient3 = new pojoDatiClient();
                        objClient3.setCodiceOp(2);
                        objClient3.setNomeClient(this.nome);
                        outVersoServer.writeBytes(invia.writeValueAsString(objClient3) + "\n");
                        System.out.println("grazie per aver usufruito del nostro servizio");
                        outVersoServer.close();
                        inDalServer.close();
                        tastiera.close();
                        ricevoMessaggi.stop();//da qua non ricevo piu i messaggi
                        socketServer.close();
                        flag = false;
                    break;
                } 
            }catch(IOException e){
                break;                
            }
        }
    }

    public String stampaMessaggi(String nome){
        String conversazione="";
        for(int i = 0 ; i < messaggiSingoli.size(); i++){
            if(messaggiSingoli.get(i).getDestinatario().equals(nome) || messaggiSingoli.get(i).getMittente().equals(nome)){
                conversazione = conversazione + messaggiSingoli.get(i).getMittente()+ ": "+ messaggiSingoli.get(i).getCorpoMessaggio()+ "\n";
            }
        }
        //se l'array e' vuoto
        if(conversazione.equals("")){
            conversazione ="NON CI SONO MESSAGGI";
        }
        return conversazione;
    }

    public String stampaMessaggiDiGruppo(){
        String conversazioneDiGruppo="";
        for(int j = 0 ; j < messaggiGruppo.size(); j++){
            conversazioneDiGruppo = conversazioneDiGruppo + messaggiGruppo.get(j).getMittente() + ": " + messaggiGruppo.get(j).getCorpoMessaggio() + "\n";
        }
        if(conversazioneDiGruppo.equals("")){
            conversazioneDiGruppo ="NON CI SONO MESSAGGI";
        }
        return conversazioneDiGruppo;
    }
}