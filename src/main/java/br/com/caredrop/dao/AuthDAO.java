package br.com.caredrop.dao;

import br.com.caredrop.util.ConnectionFactory;
import br.com.caredrop.util.PasswordUtil;
import java.sql.*;
import java.util.*;

public class AuthDAO {
    public record User(int id, String nome, String telefone, String email, String senhaHash, String perfil, boolean ativo) {}

    public boolean existeUsuario() throws SQLException {
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM usuarios")) { r.next(); return r.getInt(1) > 0; }
    }

    public Optional<User> buscarPorEmail(String email) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT id,nome,telefone,email,senha_hash,perfil,ativo FROM usuarios WHERE email=?")) {
            p.setString(1, email);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(user(r)) : Optional.empty(); }
        }
    }

    public List<Map<String,Object>> listarUsuarios() throws SQLException {
        List<Map<String,Object>> rows = new ArrayList<>();
        String sql = "SELECT id,nome,telefone,email,perfil,ativo FROM usuarios ORDER BY perfil,nome";
        try (Connection c=ConnectionFactory.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while (r.next()) {
                Map<String,Object> m=new LinkedHashMap<>();
                m.put("id",r.getInt("id")); m.put("nome",r.getString("nome")); m.put("telefone",r.getString("telefone"));
                m.put("email",r.getString("email")); m.put("perfil",r.getString("perfil")); m.put("ativo",r.getBoolean("ativo")); rows.add(m);
            }
        }
        return rows;
    }

    public void iniciarCadastro(String nome, String telefone, String email, String senhaHash, String perfil,
                                String codigoHash, Timestamp expira) throws SQLException {
        String sql="INSERT INTO cadastros_pendentes(nome,telefone,email,senha_hash,perfil,codigo_hash,expira_em,tentativas) VALUES(?,?,?,?,?,?,?,0) "
                + "ON DUPLICATE KEY UPDATE nome=VALUES(nome),telefone=VALUES(telefone),senha_hash=VALUES(senha_hash),perfil=VALUES(perfil),codigo_hash=VALUES(codigo_hash),expira_em=VALUES(expira_em),tentativas=0";
        try (Connection c=ConnectionFactory.getConnection()) {
            try (PreparedStatement check=c.prepareStatement("SELECT id FROM usuarios WHERE email=?")) {
                check.setString(1,email); try(ResultSet r=check.executeQuery()){if(r.next())throw new IllegalArgumentException("Este e-mail já está cadastrado.");}
            }
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setString(1,nome);p.setString(2,telefone);p.setString(3,email);p.setString(4,senhaHash);p.setString(5,perfil);p.setString(6,codigoHash);p.setTimestamp(7,expira);p.executeUpdate();
            }
        }
    }

    public boolean verificarCadastro(String email, String codigo, String hashEsperado) throws SQLException {
        try (Connection c=ConnectionFactory.getConnection()) {
            c.setAutoCommit(false);
            try {
                String sql="SELECT nome,telefone,email,senha_hash,perfil,codigo_hash,expira_em,tentativas FROM cadastros_pendentes WHERE email=? FOR UPDATE";
                try(PreparedStatement p=c.prepareStatement(sql)){
                    p.setString(1,email);
                    try(ResultSet r=p.executeQuery()){
                        if(!r.next()) { c.rollback(); return false; }
                        String stored=r.getString("codigo_hash"); Timestamp exp=r.getTimestamp("expira_em"); int tries=r.getInt("tentativas");
                        if(tries>=5 || exp.before(new Timestamp(System.currentTimeMillis())) || !java.security.MessageDigest.isEqual(stored.getBytes(java.nio.charset.StandardCharsets.UTF_8), hashEsperado.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                            try(PreparedStatement u=c.prepareStatement("UPDATE cadastros_pendentes SET tentativas=tentativas+1 WHERE email=?")){u.setString(1,email);u.executeUpdate();}
                            c.commit(); return false;
                        }
                        try(PreparedStatement ins=c.prepareStatement("INSERT INTO usuarios(nome,telefone,email,senha_hash,perfil,ativo) VALUES(?,?,?,?,?,TRUE)")){
                            ins.setString(1,r.getString("nome"));ins.setString(2,r.getString("telefone"));ins.setString(3,email);ins.setString(4,r.getString("senha_hash"));ins.setString(5,r.getString("perfil"));ins.executeUpdate();
                        }
                        try(PreparedStatement del=c.prepareStatement("DELETE FROM cadastros_pendentes WHERE email=?")){del.setString(1,email);del.executeUpdate();}
                        c.commit();return true;
                    }
                }
            } catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
        }
    }

    public void iniciarRecuperacao(String email, String codigoHash, Timestamp expira) throws SQLException {
        String sql="INSERT INTO recuperacoes_senha(email,codigo_hash,expira_em,tentativas) "
                + "SELECT ?,?,?,0 FROM usuarios WHERE email=? AND ativo=TRUE "
                + "ON DUPLICATE KEY UPDATE codigo_hash=VALUES(codigo_hash),expira_em=VALUES(expira_em),tentativas=0";
        try(Connection c=ConnectionFactory.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,email);p.setString(2,codigoHash);p.setTimestamp(3,expira);p.setString(4,email);p.executeUpdate();
        }
    }

    public boolean redefinirSenha(String email, String hashEsperado, String novoHash) throws SQLException {
        try(Connection c=ConnectionFactory.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement("SELECT codigo_hash,expira_em,tentativas FROM recuperacoes_senha WHERE email=? FOR UPDATE")){
                p.setString(1,email);
                try(ResultSet r=p.executeQuery()){
                    if(!r.next()){c.rollback();return false;}
                    String armazenado=r.getString("codigo_hash");Timestamp expira=r.getTimestamp("expira_em");int tentativas=r.getInt("tentativas");
                    boolean valido=tentativas<5 && expira!=null && expira.after(new Timestamp(System.currentTimeMillis()))
                            && java.security.MessageDigest.isEqual(armazenado.getBytes(java.nio.charset.StandardCharsets.UTF_8),hashEsperado.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    if(!valido){
                        try(PreparedStatement u=c.prepareStatement("UPDATE recuperacoes_senha SET tentativas=tentativas+1 WHERE email=?")){u.setString(1,email);u.executeUpdate();}
                        c.commit();return false;
                    }
                    try(PreparedStatement u=c.prepareStatement("UPDATE usuarios SET senha_hash=? WHERE email=? AND ativo=TRUE")){
                        u.setString(1,novoHash);u.setString(2,email);
                        if(u.executeUpdate()!=1){c.rollback();return false;}
                    }
                    try(PreparedStatement d=c.prepareStatement("DELETE FROM recuperacoes_senha WHERE email=?")){d.setString(1,email);d.executeUpdate();}
                    c.commit();return true;
                }
            }catch(SQLException|RuntimeException ex){c.rollback();throw ex;}
        }
    }

    public boolean senhaConfere(User user, char[] senha) { return PasswordUtil.verify(senha,user.senhaHash()); }

    private User user(ResultSet r) throws SQLException {
        return new User(r.getInt("id"),r.getString("nome"),r.getString("telefone"),r.getString("email"),r.getString("senha_hash"),r.getString("perfil"),r.getBoolean("ativo"));
    }
}
