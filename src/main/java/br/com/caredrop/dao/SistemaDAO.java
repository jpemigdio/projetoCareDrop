package br.com.caredrop.dao;

import br.com.caredrop.util.ConnectionFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Acesso JDBC às telas do MVP. Todas as escritas financeiras usam transação. */
public class SistemaDAO {
    private List<Map<String,Object>> listar(String sql) throws SQLException {
        List<Map<String,Object>> rows = new ArrayList<>();
        try(Connection c=ConnectionFactory.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            ResultSetMetaData md=r.getMetaData(); int n=md.getColumnCount();
            while(r.next()){Map<String,Object> row=new LinkedHashMap<>(); for(int i=1;i<=n;i++) row.put(md.getColumnLabel(i),r.getObject(i)); rows.add(row);}
        } return rows;
    }
    private int inserir(String sql, Object... values) throws SQLException {
        try(Connection c=ConnectionFactory.getConnection(); PreparedStatement p=c.prepareStatement(sql)) { bind(p,values); return p.executeUpdate(); }
    }
    private void bind(PreparedStatement p,Object... values) throws SQLException { for(int i=0;i<values.length;i++) p.setObject(i+1,values[i]); }
    public List<Map<String,Object>> idosos() throws SQLException {return listar("SELECT id,nome,data_nascimento AS nascimento,quarto,condicoes FROM idosos WHERE ativo=1 ORDER BY nome");}
    public List<Map<String,Object>> medicamentos() throws SQLException {return listar("SELECT id,nome,dosagem,frequencia FROM medicamentos WHERE ativo=1 ORDER BY nome");}
    public List<Map<String,Object>> doadores() throws SQLException {return listar("SELECT id,nome,cpf,tipo_sanguineo AS tipo,telefone FROM doadores WHERE ativo=1 ORDER BY nome");}
    public List<Map<String,Object>> estoque() throws SQLException {return listar("SELECT tipo_sanguineo AS tipo,quantidade,ultima_atualizacao AS atualizado FROM estoque_sangue ORDER BY FIELD(tipo_sanguineo,'A+','A-','B+','B-','AB+','AB-','O+','O-')");}
    public List<Map<String,Object>> doacoes() throws SQLException {return listar("SELECT d.data_doacao AS data,d.tipo_sanguineo AS tipo,d.quantidade_bolsas AS bolsas,o.nome AS doador FROM doacoes d JOIN doadores o ON o.id=d.doador_id ORDER BY d.data_doacao DESC,d.id DESC LIMIT 100");}
    public List<Map<String,Object>> administracoes() throws SQLException {return listar("SELECT a.data_hora AS data,i.nome AS idoso,m.nome AS medicamento,m.dosagem,a.responsavel FROM administracoes_medicamentos a JOIN idosos i ON i.id=a.idoso_id JOIN medicamentos m ON m.id=a.medicamento_id ORDER BY a.data_hora DESC LIMIT 100");}
    public List<Map<String,Object>> consumos() throws SQLException {return listar("SELECT data_consumo AS data,tipo_sanguineo AS tipo,quantidade AS bolsas,motivo FROM consumos_sangue ORDER BY data_consumo DESC,id DESC LIMIT 100");}
    public List<Map<String,Object>> previsao() throws SQLException {return listar("SELECT DATE_FORMAT(data_consumo,'%Y-%m') AS periodo,SUM(quantidade) AS bolsas FROM consumos_sangue WHERE data_consumo >= DATE_SUB(CURDATE(),INTERVAL 6 MONTH) GROUP BY DATE_FORMAT(data_consumo,'%Y-%m') ORDER BY periodo");}
    public int total(String tabela) throws SQLException {Set<String> ok=Set.of("idosos","medicamentos","doacoes","doadores");if(!ok.contains(tabela))throw new IllegalArgumentException();List<Map<String,Object>> r=listar("SELECT COUNT(*) AS total FROM "+tabela+(Set.of("idosos","medicamentos","doadores").contains(tabela)?" WHERE ativo=1":""));return ((Number)r.get(0).get("total")).intValue();}
    public void salvarIdoso(String nome,LocalDate nascimento,String quarto,String condicoes)throws SQLException{inserir("INSERT INTO idosos(nome,data_nascimento,quarto,condicoes) VALUES(?,?,?,?)",nome,nascimento,quarto,condicoes);}
    public void atualizarIdoso(int id,String nome,LocalDate nascimento,String quarto,String condicoes)throws SQLException{inserir("UPDATE idosos SET nome=?,data_nascimento=?,quarto=?,condicoes=? WHERE id=?",nome,nascimento,quarto,condicoes,id);}
    public void salvarMedicamento(String nome,String dosagem,String frequencia)throws SQLException{inserir("INSERT INTO medicamentos(nome,dosagem,frequencia) VALUES(?,?,?)",nome,dosagem,frequencia);}
    public void atualizarMedicamento(int id,String nome,String dosagem,String frequencia)throws SQLException{inserir("UPDATE medicamentos SET nome=?,dosagem=?,frequencia=? WHERE id=?",nome,dosagem,frequencia,id);}
    public void salvarDoador(String nome,String cpf,String tipo,String telefone)throws SQLException{inserir("INSERT INTO doadores(nome,cpf,tipo_sanguineo,telefone) VALUES(?,?,?,?)",nome,cpf,tipo,telefone);}
    public void atualizarDoador(int id,String nome,String cpf,String tipo,String telefone)throws SQLException{inserir("UPDATE doadores SET nome=?,cpf=?,tipo_sanguineo=?,telefone=? WHERE id=?",nome,cpf,tipo,telefone,id);}
    public void arquivar(String tabela,int id)throws SQLException{if(!Set.of("idosos","medicamentos","doadores").contains(tabela))throw new IllegalArgumentException("Registro não pode ser removido.");inserir("UPDATE "+tabela+" SET ativo=0 WHERE id=?",id);}
    public void administrar(int idoso,int medicamento,Timestamp quando,String responsavel)throws SQLException{inserir("INSERT INTO administracoes_medicamentos(idoso_id,medicamento_id,data_hora,responsavel) VALUES(?,?,?,?)",idoso,medicamento,quando,responsavel);}
    public void doar(int doador,String tipo,int bolsas,LocalDate data)throws SQLException{
        if(bolsas<=0)throw new IllegalArgumentException("A quantidade de bolsas deve ser maior que zero.");
        try(Connection c=ConnectionFactory.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("INSERT INTO doacoes(doador_id,tipo_sanguineo,quantidade_bolsas,data_doacao) VALUES(?,?,?,?)")){bind(p,doador,tipo,bolsas,data);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("UPDATE estoque_sangue SET quantidade=quantidade+? WHERE tipo_sanguineo=?")){bind(p,bolsas,tipo);if(p.executeUpdate()!=1)throw new SQLException("Tipo sanguíneo não encontrado no estoque.");}
                c.commit();
            } catch(SQLException|RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public void consumir(String tipo,int bolsas,LocalDate data,String motivo)throws SQLException{
        if(bolsas<=0)throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        try(Connection c=ConnectionFactory.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("UPDATE estoque_sangue SET quantidade=quantidade-? WHERE tipo_sanguineo=? AND quantidade>=?")){bind(p,bolsas,tipo,bolsas);if(p.executeUpdate()!=1)throw new IllegalArgumentException("Estoque insuficiente para "+tipo+".");}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO consumos_sangue(tipo_sanguineo,quantidade,data_consumo,motivo) VALUES(?,?,?,?)")){bind(p,tipo,bolsas,data,motivo);p.executeUpdate();}
                c.commit();
            } catch(SQLException|RuntimeException e) { c.rollback(); throw e; }
        }
    }
}
