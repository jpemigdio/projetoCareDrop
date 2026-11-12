package br.com.caredrop.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class EmailService {
    private EmailService() {}

    public static void enviarCodigo(String destinatario, String codigo, String linkVerificacao) throws Exception {
        String host = env("SMTP_HOST");
        String port = envOr("SMTP_PORT", "587");
        String username = env("SMTP_USER");
        String password = env("SMTP_PASSWORD");
        String from = envOr("SMTP_FROM", username);
        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new IllegalStateException("O envio de e-mail ainda não foi configurado. Configure SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD e SMTP_FROM no Windows e reinicie o Tomcat.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        message.setSubject("Código de verificação CareDrop", StandardCharsets.UTF_8.name());
        message.setText("Seu código de verificação do CareDrop é: " + codigo
                + "\n\nAbra este endereço para confirmar o e-mail: " + linkVerificacao
                + "\n\nO código expira em 10 minutos. Se você não solicitou este cadastro, ignore esta mensagem.",
                StandardCharsets.UTF_8.name());
        Transport.send(message);
    }

    private static String env(String name) { return System.getenv().getOrDefault(name, "").trim(); }
    private static String envOr(String name, String fallback) { String value = env(name); return value.isBlank() ? fallback : value; }
}
