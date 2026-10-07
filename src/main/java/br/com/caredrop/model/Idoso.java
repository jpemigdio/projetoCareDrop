package br.com.caredrop.model;
import java.time.LocalDate;
public class Idoso {
    private int id; private String nome, quarto, condicoes; private LocalDate dataNascimento;
    public Idoso() {}
    public Idoso(int id, String nome, LocalDate dataNascimento, String quarto, String condicoes) { this.id=id; this.nome=nome; this.dataNascimento=dataNascimento; this.quarto=quarto; this.condicoes=condicoes; }
    public int getId(){return id;} public void setId(int v){id=v;} public String getNome(){return nome;} public void setNome(String v){nome=v;} public LocalDate getDataNascimento(){return dataNascimento;} public void setDataNascimento(LocalDate v){dataNascimento=v;} public String getQuarto(){return quarto;} public void setQuarto(String v){quarto=v;} public String getCondicoes(){return condicoes;} public void setCondicoes(String v){condicoes=v;}
}
