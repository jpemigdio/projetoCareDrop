package br.com.caredrop.model;
import java.time.LocalDate;
public class Doacao {
 private int id, doadorId, quantidadeBolsas; private String tipoSanguineo; private LocalDate dataDoacao;
 public int getId(){return id;} public void setId(int v){id=v;} public int getDoadorId(){return doadorId;} public void setDoadorId(int v){doadorId=v;} public int getQuantidadeBolsas(){return quantidadeBolsas;} public void setQuantidadeBolsas(int v){quantidadeBolsas=v;} public String getTipoSanguineo(){return tipoSanguineo;} public void setTipoSanguineo(String v){tipoSanguineo=v;} public LocalDate getDataDoacao(){return dataDoacao;} public void setDataDoacao(LocalDate v){dataDoacao=v;}
}
