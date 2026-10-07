package br.com.caredrop.model;
import java.time.LocalDate;
public class ConsumoSangue {
 private int id, quantidade; private String tipoSanguineo, motivo; private LocalDate dataConsumo;
 public int getId(){return id;} public void setId(int v){id=v;} public int getQuantidade(){return quantidade;} public void setQuantidade(int v){quantidade=v;} public String getTipoSanguineo(){return tipoSanguineo;} public void setTipoSanguineo(String v){tipoSanguineo=v;} public String getMotivo(){return motivo;} public void setMotivo(String v){motivo=v;} public LocalDate getDataConsumo(){return dataConsumo;} public void setDataConsumo(LocalDate v){dataConsumo=v;}
}
