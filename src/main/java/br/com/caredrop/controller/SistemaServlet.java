package br.com.caredrop.controller;

import br.com.caredrop.dao.AuthDAO;
import br.com.caredrop.dao.SistemaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@WebServlet("/app")
public class SistemaServlet extends HttpServlet {
    private final SistemaDAO dao=new SistemaDAO();
    private final AuthDAO authDAO=new AuthDAO();
    private String campo(HttpServletRequest r,String k){String v=r.getParameter(k);return v==null?"":v.trim();}
    private int inteiro(HttpServletRequest r,String k){return Integer.parseInt(campo(r,k));}
    private LocalDate data(HttpServletRequest r,String k){return LocalDate.parse(campo(r,k));}
    private boolean podeGerenciar(HttpServletRequest req){HttpSession sessao=req.getSession(false);return sessao!=null && sessao.getAttribute("userId")!=null;}

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String view=campo(req,"view");if(view.isBlank())view=podeGerenciar(req)?"dashboard":"agenda";
        if(!podeGerenciar(req)&&!Set.of("agenda","idosos","perfil").contains(view))view="agenda";
        Object ok=req.getSession().getAttribute("sucesso"),falha=req.getSession().getAttribute("erro"),authErr=req.getSession().getAttribute("authErro"),authOk=req.getSession().getAttribute("authOk");
        req.setAttribute("nomeUsuario",req.getSession().getAttribute("nome"));req.setAttribute("perfilUsuario",req.getSession().getAttribute("perfil"));
        if(ok!=null){req.setAttribute("sucesso",ok);req.getSession().removeAttribute("sucesso");}
        if(falha!=null){req.setAttribute("erro",falha);req.getSession().removeAttribute("erro");}
        if(authErr!=null){req.setAttribute("erro",authErr);req.getSession().removeAttribute("authErro");}
        if(authOk!=null){req.setAttribute("sucesso",authOk);req.getSession().removeAttribute("authOk");}
        try{
            switch(view){
                case "dashboard" -> {req.setAttribute("idososTotal",dao.total("idosos"));req.setAttribute("medicamentosTotal",dao.total("medicamentos"));req.setAttribute("administracoes",dao.administracoes().stream().limit(5).toList());}
                case "agenda" -> {LocalDate dia;try{dia=campo(req,"data").isBlank()?LocalDate.now():data(req,"data");}catch(Exception ex){dia=LocalDate.now();}String idoso=campo(req,"idosoId");req.setAttribute("agenda",dao.agenda(dia,idoso.isBlank()?null:Integer.valueOf(idoso),campo(req,"turno"),campo(req,"status")));req.setAttribute("idosos",dao.idosos());req.setAttribute("dataAgenda",dia);req.setAttribute("filtroIdoso",idoso);req.setAttribute("filtroTurno",campo(req,"turno"));req.setAttribute("filtroStatus",campo(req,"status"));}
                case "idosos" -> req.setAttribute("idosos",dao.idosos());
                case "perfil" -> {int id=inteiro(req,"id");Map<String,Object> perfil=dao.perfilIdoso(id);if(perfil==null){req.setAttribute("erro","Residente não encontrado.");view="idosos";req.setAttribute("idosos",dao.idosos());}else{req.setAttribute("idoso",perfil);req.setAttribute("prescricoesIdoso",dao.prescricoesDoIdoso(id));}}
                case "medicamentos" -> req.setAttribute("medicamentos",dao.medicamentos());
                case "administracoes" -> {req.setAttribute("administracoes",dao.administracoes());req.setAttribute("idosos",dao.idosos());req.setAttribute("medicamentos",dao.medicamentos());}
                case "prescricoes" -> {req.setAttribute("prescricoes",dao.prescricoes());req.setAttribute("idosos",dao.idosos());req.setAttribute("medicamentos",dao.medicamentos());req.setAttribute("hoje",LocalDate.now());}
                case "usuarios" -> req.setAttribute("usuarios",authDAO.listarUsuarios());
                default -> {view="dashboard";req.setAttribute("idososTotal",dao.total("idosos"));req.setAttribute("medicamentosTotal",dao.total("medicamentos"));req.setAttribute("administracoes",dao.administracoes().stream().limit(5).toList());}
            }
        }catch(Exception e){req.setAttribute("erro",mensagem(e));}
        req.setAttribute("view",view);req.getRequestDispatcher("/WEB-INF/views/app.jsp").forward(req,resp);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        req.setCharacterEncoding("UTF-8");String acao=campo(req,"acao"),voltar=campo(req,"view");if(voltar.isBlank())voltar="dashboard";
        boolean isAdmin=podeGerenciar(req);if(!isAdmin&&!acao.equals("marcarDose")){resp.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        try{
            switch(acao){
                case "idoso" -> {obrigatorio(req,"nome","dataNascimento");dao.salvarIdoso(campo(req,"nome"),data(req,"dataNascimento"),campo(req,"quarto"),campo(req,"condicoes"));}
                case "medicamento" -> {obrigatorio(req,"nome");dao.salvarMedicamento(campo(req,"nome"),campo(req,"dosagem"),campo(req,"frequencia"));}
                case "atualizarIdoso" -> {obrigatorio(req,"id","nome","dataNascimento");dao.atualizarIdoso(inteiro(req,"id"),campo(req,"nome"),data(req,"dataNascimento"),campo(req,"quarto"),campo(req,"condicoes"));}
                case "atualizarMedicamento" -> {obrigatorio(req,"id","nome");dao.atualizarMedicamento(inteiro(req,"id"),campo(req,"nome"),campo(req,"dosagem"),campo(req,"frequencia"));}
                case "arquivar" -> dao.arquivar(campo(req,"entidade"),inteiro(req,"id"));
                case "administracao" -> {obrigatorio(req,"idosoId","medicamentoId","dataHora","responsavel");dao.administrar(inteiro(req,"idosoId"),inteiro(req,"medicamentoId"),Timestamp.valueOf(LocalDateTime.parse(campo(req,"dataHora"))),campo(req,"responsavel"));}
                case "prescricao" -> {obrigatorio(req,"idosoId","medicamentoId","dosagem","horario","dataInicio");LocalDate fim=campo(req,"dataFim").isBlank()?null:data(req,"dataFim");dao.salvarPrescricao(inteiro(req,"idosoId"),inteiro(req,"medicamentoId"),campo(req,"dosagem"),LocalTime.parse(campo(req,"horario")),data(req,"dataInicio"),fim,campo(req,"instrucoes"),(Integer)req.getSession().getAttribute("userId"));voltar="prescricoes";}
                case "arquivarPrescricao" -> {dao.arquivarPrescricao(inteiro(req,"id"));voltar="prescricoes";}
                case "marcarDose" -> {obrigatorio(req,"prescricaoId","data");dao.marcarDoseAdministrada(inteiro(req,"prescricaoId"),data(req,"data"),(String)req.getSession().getAttribute("nome"));voltar="agenda";}
                default -> throw new IllegalArgumentException("Ação desconhecida.");
            }
            req.getSession().setAttribute("sucesso",acao.equals("marcarDose")?"Dose registrada como administrada.":"Registro salvo com sucesso.");
        }catch(Exception e){req.getSession().setAttribute("erro",mensagem(e));}
        String query="/app?view="+voltar;if(voltar.equals("agenda")&&!campo(req,"data").isBlank())query+="&data="+campo(req,"data");
        resp.sendRedirect(req.getContextPath()+query);
    }
    private void obrigatorio(HttpServletRequest req,String... campos){for(String c:campos)if(campo(req,c).isBlank())throw new IllegalArgumentException("Preencha todos os campos obrigatórios.");}
    private String mensagem(Exception e){String m=e.getMessage();if(m==null||m.isBlank())return "Não foi possível concluir. Verifique a conexão com o banco e os dados informados.";if(m.contains("Duplicate entry"))return "Este registro já existe.";return m;}
}

