package it.fi.itismeucci;

public class Messaggio {
    String mittente;
    String corpoMessaggio;    
    String destinatario;

    public Messaggio(String m, String c, String d){
        mittente =m;
        corpoMessaggio = c;
        destinatario = d;
    }

    public String getMittente() {
        return mittente;
    }

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public String getCorpoMessaggio() {
        return corpoMessaggio;
    }

    public void setCorpoMessaggio(String corpoMessaggio) {
        this.corpoMessaggio = corpoMessaggio;
    }

    public void setDestinatario(String destinatario){
        this.destinatario = destinatario;
    }
    
    public String getDestinatario(){
        return destinatario;
    }

    @Override
    public String toString() {
        return "Messaggio [mittente=" + mittente + ", corpoMessaggio=" + corpoMessaggio + ", destinatario="+ destinatario + "]";
    }
    
}
