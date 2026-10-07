package br.com.caredrop.model;
import java.time.LocalDateTime;
public class AdministracaoMedicamento {
 private int id, idosoId, medicamentoId; private LocalDateTime dataHora; private String responsavel;
 public int getId(){return id;} public void setId(int v){id=v;} public int getIdosoId(){return idosoId;} public void setIdosoId(int v){idosoId=v;} public int getMedicamentoId(){return medicamentoId;} public void setMedicamentoId(int v){medicamentoId=v;} public LocalDateTime getDataHora(){return dataHora;} public void setDataHora(LocalDateTime v){dataHora=v;} public String getResponsavel(){return responsavel;} public void setResponsavel(String v){responsavel=v;}
}
