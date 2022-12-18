package it.fi.itismeucci;

public class pojoDatiServer {
    String messaggioSingolo;
    String messaggioGruppo;
    String Destinatario;
    String Mittente;
    public pojoDatiServer() {
    }
    public String getMessaggioSingolo() {
        return messaggioSingolo;
    }
    public void setMessaggioSingolo(String messaggioSingolo) {
        this.messaggioSingolo = messaggioSingolo;
    }
    public String getMessaggioGruppo() {
        return messaggioGruppo;
    }
    public void setMessaggioGruppo(String messaggioGruppo) {
        this.messaggioGruppo = messaggioGruppo;
    }
    public String getDestinatario() {
        return Destinatario;
    }
    public void setDestinatario(String destinatario) {
        this.Destinatario = destinatario;
    }
    public String getMittente() {
        return Mittente;
    }
    public void setMittente(String mittente) {
        this.Mittente = mittente;
    }
}
