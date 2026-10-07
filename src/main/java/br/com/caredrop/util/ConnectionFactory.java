package br.com.caredrop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private ConnectionFactory() {}
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/caredrop?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (ClassNotFoundException e){
            throw new SQLException("Driver JDBC do MySQL não encontrado.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}
