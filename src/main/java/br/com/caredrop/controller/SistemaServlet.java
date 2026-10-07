package br.com.caredrop.controller;

import br.com.caredrop.dao.SistemaDAO;
import br.com.caredrop.util.Validacoes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

@WebServlet("/app")
public class SistemaServlet extends HttpServlet {
    private final SistemaDAO dao=new SistemaDAO();
    private static final List<String> ORDEM_TIPOS=List.of("A+","A-","B+","B-","AB+","AB-","O+","O-");
    private String campo(HttpServletRequest r,String k){String v=r.getParameter(k);return v==null?"":v.trim();}
    private int inteiro(HttpServletRequest r,String k){return Integer.parseInt(campo(r,k));}
    private LocalDate data(HttpServletRequest r,String k){return LocalDate.parse(campo(r,k));}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String view=campo(req,"view");if(view.isBlank())view="dashboard";
        Object ok=req.getSession().getAttribute("sucesso"), falha=req.getSession().getAttribute("erro");
        req.setAttribute("tipos",ORDEM_TIPOS);
        if(ok!=null){req.setAttribute("sucesso",ok);req.getSession().removeAttribute("sucesso");}
        if(falha!=null){req.setAttribute("erro",falha);req.getSession().removeAttribute("erro");}
        try {
            switch(view){
                case "dashboard" -> {req.setAttribute("idososTotal",dao.total("idosos"));req.setAttribute("medicamentosTotal",dao.total("medicamentos"));req.setAttribute("doacoesTotal",dao.total("doacoes"));req.setAttribute("estoque",dao.estoque());req.setAttribute("doacoes",dao.doacoes().stream().limit(5).toList());req.setAttribute("administracoes",dao.administracoes().stream().limit(5).toList());}
                case "idosos" -> req.setAttribute("idosos",dao.idosos());
                case "medicamentos" -> req.setAttribute("medicamentos",dao.medicamentos());
                case "administracoes" -> {req.setAttribute("administracoes",dao.administracoes());req.setAttribute("idosos",dao.idosos());req.setAttribute("medicamentos",dao.medicamentos());}
                case "doadores" -> req.setAttribute("doadores",dao.doadores());
                case "doacoes" -> {req.setAttribute("doacoes",dao.doacoes());req.setAttribute("doadores",dao.doadores());}
                case "estoque" -> {req.setAttribute("estoque",dao.estoque());req.setAttribute("consumos",dao.consumos());}
                case "previsao" -> {var historico=dao.previsao();req.setAttribute("historico",historico);double media=historico.stream().mapToInt(x->((Number)x.get("bolsas")).intValue()).average().orElse(0);req.setAttribute("media",Math.round(media*10.0)/10.0);}
                default -> {view="dashboard";req.setAttribute("idososTotal",dao.total("idosos"));req.setAttribute("medicamentosTotal",dao.total("medicamentos"));req.setAttribute("doacoesTotal",dao.total("doacoes"));req.setAttribute("estoque",dao.estoque());}
            }
        } catch(Exception e){req.setAttribute("erro",mensagem(e));}
        req.setAttribute("view",view);req.getRequestDispatcher("/WEB-INF/views/app.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        req.setCharacterEncoding("UTF-8");String acao=campo(req,"acao"),voltar=campo(req,"view");if(voltar.isBlank())voltar="dashboard";
        try {
            switch(acao){
                case "idoso" -> {obrigatorio(req,"nome","dataNascimento");dao.salvarIdoso(campo(req,"nome"),data(req,"dataNascimento"),campo(req,"quarto"),campo(req,"condicoes"));}
                case "medicamento" -> {obrigatorio(req,"nome");dao.salvarMedicamento(campo(req,"nome"),campo(req,"dosagem"),campo(req,"frequencia"));}
                case "doador" -> {obrigatorio(req,"nome","cpf","tipo");Validacoes.tipoSanguineo(campo(req,"tipo"));dao.salvarDoador(campo(req,"nome"),Validacoes.cpf(campo(req,"cpf")),campo(req,"tipo"),campo(req,"telefone"));}
                case "atualizarIdoso" -> {obrigatorio(req,"id","nome","dataNascimento");dao.atualizarIdoso(inteiro(req,"id"),campo(req,"nome"),data(req,"dataNascimento"),campo(req,"quarto"),campo(req,"condicoes"));}
                case "atualizarMedicamento" -> {obrigatorio(req,"id","nome");dao.atualizarMedicamento(inteiro(req,"id"),campo(req,"nome"),campo(req,"dosagem"),campo(req,"frequencia"));}
                case "atualizarDoador" -> {obrigatorio(req,"id","nome","cpf","tipo");Validacoes.tipoSanguineo(campo(req,"tipo"));dao.atualizarDoador(inteiro(req,"id"),campo(req,"nome"),Validacoes.cpf(campo(req,"cpf")),campo(req,"tipo"),campo(req,"telefone"));}
                case "arquivar" -> dao.arquivar(campo(req,"entidade"),inteiro(req,"id"));
                case "administracao" -> {obrigatorio(req,"idosoId","medicamentoId","dataHora","responsavel");dao.administrar(inteiro(req,"idosoId"),inteiro(req,"medicamentoId"),Timestamp.valueOf(LocalDateTime.parse(campo(req,"dataHora"))),campo(req,"responsavel"));}
                case "doacao" -> {obrigatorio(req,"doadorId","tipo","bolsas","data");Validacoes.tipoSanguineo(campo(req,"tipo"));Validacoes.quantidadePositiva(inteiro(req,"bolsas"));dao.doar(inteiro(req,"doadorId"),campo(req,"tipo"),inteiro(req,"bolsas"),data(req,"data"));}
                case "consumo" -> {obrigatorio(req,"tipo","bolsas","data");Validacoes.tipoSanguineo(campo(req,"tipo"));Validacoes.quantidadePositiva(inteiro(req,"bolsas"));dao.consumir(campo(req,"tipo"),inteiro(req,"bolsas"),data(req,"data"),campo(req,"motivo"));}
                default -> throw new IllegalArgumentException("Ação desconhecida.");
            }
            req.getSession().setAttribute("sucesso","Registro salvo com sucesso.");
        } catch(Exception e){req.getSession().setAttribute("erro",mensagem(e));}
        resp.sendRedirect(req.getContextPath()+"/app?view="+voltar);
    }
    private void obrigatorio(HttpServletRequest req,String... campos){for(String c:campos)if(campo(req,c).isBlank())throw new IllegalArgumentException("Preencha todos os campos obrigatórios.");}
    private String mensagem(Exception e){String m=e.getMessage();if(m==null||m.isBlank())return "Não foi possível concluir. Verifique a conexão com o banco e os dados informados.";if(m.contains("Duplicate entry"))return "Este CPF já está cadastrado.";return m;}
}
