package br.com.caredrop.model;
public class Medicamento {
    private int id; private String nome, dosagem, frequencia;
    public Medicamento() {} public Medicamento(int id,String nome,String dosagem,String frequencia){this.id=id;this.nome=nome;this.dosagem=dosagem;this.frequencia=frequencia;}
    public int getId(){return id;} public void setId(int v){id=v;} public String getNome(){return nome;} public void setNome(String v){nome=v;} public String getDosagem(){return dosagem;} public void setDosagem(String v){dosagem=v;} public String getFrequencia(){return frequencia;} public void setFrequencia(String v){frequencia=v;}
}
