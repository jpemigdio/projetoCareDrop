package br.com.caredrop.model;
import java.time.LocalDateTime;
public class EstoqueSangue {
 private int id, quantidade; private String tipoSanguineo; private LocalDateTime ultimaAtualizacao;
 public int getId(){return id;} public void setId(int v){id=v;} public int getQuantidade(){return quantidade;} public void setQuantidade(int v){quantidade=v;} public String getTipoSanguineo(){return tipoSanguineo;} public void setTipoSanguineo(String v){tipoSanguineo=v;} public LocalDateTime getUltimaAtualizacao(){return ultimaAtualizacao;} public void setUltimaAtualizacao(LocalDateTime v){ultimaAtualizacao=v;}
}
