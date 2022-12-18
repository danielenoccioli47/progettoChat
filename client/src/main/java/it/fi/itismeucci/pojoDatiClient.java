package it.fi.itismeucci;

public class pojoDatiClient {
    int codiceOp;
    String nomeClient;
    String corpoMessaggio;
    String destinatario;
    public pojoDatiClient() {
    }
    public int getCodiceOp() {
        return codiceOp;
    }
    public void setCodiceOp(int codiceOp) {
        this.codiceOp = codiceOp;
    }
    public String getNomeClient() {
        return nomeClient;
    }
    public void setNomeClient(String nomeClient) {
        this.nomeClient = nomeClient;
    }
    public String getCorpoMessaggio() {
        return corpoMessaggio;
    }
    public void setCorpoMessaggio(String corpoMessaggio) {
        this.corpoMessaggio = corpoMessaggio;
    }
    public String getDestinatario() {
        return destinatario;
    }
    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }
    
}
