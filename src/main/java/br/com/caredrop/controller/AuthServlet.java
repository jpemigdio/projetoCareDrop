package br.com.caredrop.controller;

import br.com.caredrop.dao.AuthDAO;
import br.com.caredrop.dao.AuthDAO.User;
import br.com.caredrop.util.EmailService;
import br.com.caredrop.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private final AuthDAO dao = new AuthDAO();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9() .-]{10,20}$");

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        boolean verificar="verificar".equals(req.getParameter("view"));
        boolean cadastro="cadastro".equals(req.getParameter("view"));
        if (!verificar && session != null && session.getAttribute("userId") != null) {
            redirectHome(req, resp, (String)session.getAttribute("perfil")); return;
        }
        req.setCharacterEncoding("UTF-8");
        String ok=(String)req.getSession().getAttribute("authOk"), err=(String)req.getSession().getAttribute("authErro");
        if(ok!=null){req.setAttribute("authOk",ok);req.getSession().removeAttribute("authOk");}
        if(err!=null){req.setAttribute("authErro",err);req.getSession().removeAttribute("authErro");}
        try { req.setAttribute("primeiroAcesso", !dao.existeUsuario()); }
        catch (SQLException ex) { throw new ServletException("Não foi possível consultar os usuários. Importe a migração de login do CareDrop.",ex); }
        req.setAttribute("verificar", verificar);
        req.setAttribute("cadastro", cadastro);
        req.setAttribute("email", req.getParameter("email") == null ? "" : req.getParameter("email"));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req,resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String acao=val(req,"acao"), email=val(req,"email").toLowerCase(Locale.ROOT);
        try {
            switch(acao){
                case "login" -> login(req,resp,email);
                case "primeiroAdmin" -> iniciar(req,resp,email,"ADMIN",true,true);
                case "cadastro" -> {
                    String perfil=val(req,"perfil"), senha=raw(req,"senha"), confirmar=raw(req,"confirmarSenha");
                    if(!"ADMIN".equals(perfil) && !"CUIDADOR".equals(perfil)) throw new IllegalArgumentException("Selecione cuidador ou administrador.");
                    if(senha==null || !senha.equals(confirmar)) throw new IllegalArgumentException("As senhas não coincidem.");
                    boolean primeiroAdmin=!dao.existeUsuario();
                    if(primeiroAdmin && !"ADMIN".equals(perfil)) throw new IllegalArgumentException("O primeiro cadastro precisa ser de administrador.");
                    iniciar(req,resp,email,perfil,primeiroAdmin,true);
                }                case "criarUsuario" -> {
                    HttpSession s=req.getSession(false);
                    if(s==null || s.getAttribute("userId")==null) throw new SecurityException("É necessário estar conectado para cadastrar usuários.");
                    String perfil=val(req,"perfil");
                    if(!"ADMIN".equals(perfil) && !"CUIDADOR".equals(perfil)) throw new IllegalArgumentException("Selecione cuidador ou administrador.");
                    iniciar(req,resp,email,perfil,false,false);
                }
                case "criarCuidador" -> {
                    HttpSession s=req.getSession(false);
                    if(s==null || s.getAttribute("userId")==null) throw new SecurityException("É necessário estar conectado para cadastrar cuidadores.");
                    iniciar(req,resp,email,"CUIDADOR",false,false);
                }
                case "verificar" -> verificar(req,resp,email);
                case "sair" -> { HttpSession s=req.getSession(false); if(s!=null)s.invalidate(); resp.sendRedirect(req.getContextPath()+"/auth"); }
                default -> throw new IllegalArgumentException("Ação inválida.");
            }
        } catch (SecurityException ex) { falhar(req,resp,ex.getMessage(),email); }
          catch (Exception ex) { falhar(req,resp,mensagem(ex),email); }
    }

    private void login(HttpServletRequest req,HttpServletResponse resp,String email)throws Exception{
        validarEmail(email);
        String senha=raw(req,"senha");
        if(senha==null)senha="";
        User user=dao.buscarPorEmail(email).orElseThrow(()->new IllegalArgumentException("E-mail ou senha incorretos."));
        String perfilPedido=val(req,"perfil");
        if(!user.ativo() || !user.perfil().equals(perfilPedido) || !dao.senhaConfere(user,senha.toCharArray())) throw new IllegalArgumentException("E-mail, senha ou perfil incorretos.");
        HttpSession antiga=req.getSession(false);if(antiga!=null)req.changeSessionId();HttpSession s=req.getSession();
        s.setAttribute("userId",user.id());s.setAttribute("nome",user.nome());s.setAttribute("email",user.email());s.setAttribute("perfil",user.perfil());
        redirectHome(req,resp,user.perfil());
    }

    private void iniciar(HttpServletRequest req,HttpServletResponse resp,String email,String perfil,boolean primeiroAdmin,boolean cadastroPublico)throws Exception{
        if(primeiroAdmin && dao.existeUsuario()) throw new IllegalArgumentException("O administrador inicial já foi criado. Entre com sua conta.");
        if(!primeiroAdmin && dao.existeUsuario()==false) throw new IllegalArgumentException("Crie primeiro a conta do administrador.");
        String nome=val(req,"nome"), telefone=val(req,"telefone"), senha=raw(req,"senha");
        if(senha==null)senha="";
        validarEmail(email);
        if(nome.length()<3 || nome.length()>100) throw new IllegalArgumentException("Informe o nome completo.");
        if(!PHONE.matcher(telefone).matches()) throw new IllegalArgumentException("Informe um telefone válido com DDD.");
        if(senha.length()<8) throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        String codigo=String.format(Locale.ROOT,"%06d",RANDOM.nextInt(1_000_000));
        Timestamp expira=Timestamp.from(Instant.now().plus(10,ChronoUnit.MINUTES));
        dao.iniciarCadastro(nome,telefone,email,PasswordUtil.hash(senha.toCharArray()),perfil,PasswordUtil.hashCode(email,codigo),expira);
        String link=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath()+"/auth?view=verificar&email="+URLEncoder.encode(email,StandardCharsets.UTF_8);
        EmailService.enviarCodigo(email,codigo,link);
        if(!cadastroPublico){
            req.getSession().setAttribute("authOk","Convite enviado para "+email+". A pessoa precisa confirmar o e-mail antes do primeiro acesso.");
            resp.sendRedirect(req.getContextPath()+"/app?view=usuarios");return;
        }
        req.getSession().setAttribute("authOk","Enviamos um código de 6 dígitos para " + email + ". Ele expira em 10 minutos.");
        resp.sendRedirect(req.getContextPath()+"/auth?view=verificar&email="+URLEncoder.encode(email,StandardCharsets.UTF_8));
    }

    private void verificar(HttpServletRequest req,HttpServletResponse resp,String email)throws Exception{
        validarEmail(email); String codigo=val(req,"codigo");
        if(!codigo.matches("[0-9]{6}")) throw new IllegalArgumentException("Digite o código de 6 dígitos recebido por e-mail.");
        if(!dao.verificarCadastro(email,codigo,PasswordUtil.hashCode(email,codigo))) throw new IllegalArgumentException("Código incorreto ou expirado. Peça um novo código ao administrador.");
        req.getSession().setAttribute("authOk","E-mail confirmado. Agora entre com seu e-mail e senha.");
        resp.sendRedirect(req.getContextPath()+"/auth");
    }

    private void falhar(HttpServletRequest req,HttpServletResponse resp,String mensagem,String email)throws IOException{
        req.getSession().setAttribute("authErro",mensagem==null?"Não foi possível concluir o pedido.":mensagem);
        String path="/auth";
        if("verificar".equals(val(req,"acao")))path+="?view=verificar&email="+URLEncoder.encode(email,StandardCharsets.UTF_8);
        else if("cadastro".equals(val(req,"acao")))path+="?view=cadastro";
        else if("criarCuidador".equals(val(req,"acao")) || "criarUsuario".equals(val(req,"acao")))path="/app?view=usuarios";
        resp.sendRedirect(req.getContextPath()+path);
    }

    private String mensagem(Exception ex){
        if(ex instanceof SQLException && ex.getMessage()!=null && ex.getMessage().contains("cadastros_pendentes")) return "As tabelas de login ainda não foram criadas. Importe o arquivo sprint1_sprint2_login_migration.sql pelo phpMyAdmin.";
        String m=ex.getMessage(); return m==null?"Não foi possível concluir. Confira o log do Tomcat.":m;
    }
    private void validarEmail(String email){if(email.length()>254 || !EMAIL.matcher(email).matches())throw new IllegalArgumentException("Digite um e-mail válido.");}
    private String val(HttpServletRequest req,String nome){String v=req.getParameter(nome);return v==null?"":v.trim();}
    private String raw(HttpServletRequest req,String nome){return req.getParameter(nome);}
    private void redirectHome(HttpServletRequest req,HttpServletResponse resp,String perfil)throws IOException{
        resp.sendRedirect(req.getContextPath()+"/app?view="+("ADMIN".equals(perfil)?"dashboard":"agenda"));
    }
}

