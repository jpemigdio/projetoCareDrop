<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Acesso | Care Drop</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/auth.css"></head>
<body><main class="auth-shell"><section class="auth-card">
<a class="brand" href="${pageContext.request.contextPath}/auth"><span class="brand-mark">CD</span><span><b>Care Drop</b><small>Casa de Repouso</small></span></a>
<div class="auth-heading"><p class="eyebrow">CUIDADO CONECTADO</p>
<% if (Boolean.TRUE.equals(request.getAttribute("verificar"))) { %><h1>Confirme seu e-mail</h1><p>Digite o código de 6 dígitos enviado para concluir o cadastro.</p>
<% } else if (Boolean.TRUE.equals(request.getAttribute("primeiroAcesso"))) { %><h1>Configurar administrador</h1><p>Crie a primeira conta administrativa do Care Drop.</p>
<% } else if (Boolean.TRUE.equals(request.getAttribute("cadastro"))) { %><h1>Criar cadastro</h1><p>Informe seus dados para criar uma conta e confirmar seu e-mail.</p>
<% } else { %><h1>Entrar no Care Drop</h1><p>Acesse sua área de trabalho com seu perfil.</p><% } %>
</div>
<c:if test="${not empty authErro}"><div class="notice error"><c:out value="${authErro}"/></div></c:if>
<c:if test="${not empty authOk}"><div class="notice success"><c:out value="${authOk}"/></div></c:if>
<% if (Boolean.TRUE.equals(request.getAttribute("verificar"))) { %>
<form method="post" action="${pageContext.request.contextPath}/auth" class="auth-form">
 <input type="hidden" name="acao" value="verificar"><label>E-mail<input type="email" name="email" value="${email}" readonly required></label>
 <label>Código de verificação<input name="codigo" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" maxlength="6" placeholder="000000" required></label>
 <button class="primary">Confirmar e-mail</button><a class="back-link" href="${pageContext.request.contextPath}/auth">Voltar ao acesso</a>
</form>
<% } else if (Boolean.TRUE.equals(request.getAttribute("primeiroAcesso"))) { %>
<form method="post" action="${pageContext.request.contextPath}/auth" class="auth-form">
 <input type="hidden" name="acao" value="primeiroAdmin"><label>Nome completo<input name="nome" maxlength="100" autocomplete="name" required></label>
 <label>Telefone com DDD<input name="telefone" type="tel" autocomplete="tel" placeholder="(11) 99999-9999" required></label>
 <label>E-mail<input name="email" type="email" maxlength="254" autocomplete="email" required></label>
 <label>Senha<input name="senha" type="password" minlength="8" autocomplete="new-password" required><small>Use pelo menos 8 caracteres.</small></label>
 <button class="primary">Enviar código e criar administrador</button>
</form>
<% } else if (Boolean.TRUE.equals(request.getAttribute("cadastro"))) { %>
<form method="post" action="${pageContext.request.contextPath}/auth" class="auth-form">
 <input type="hidden" name="acao" value="cadastro">
 <label>Perfil<select name="perfil" required><option value="CUIDADOR">Cuidador</option><option value="ADMIN">Administrador da casa de repouso</option></select></label>
 <label>Nome completo<input name="nome" maxlength="100" autocomplete="name" required></label>
 <label>Telefone com DDD<input name="telefone" type="tel" autocomplete="tel" placeholder="(11) 99999-9999" required></label>
 <label>E-mail<input name="email" type="email" value="${email}" maxlength="254" autocomplete="email" required></label>
 <label>Senha<input name="senha" type="password" minlength="8" autocomplete="new-password" required><small>Use pelo menos 8 caracteres.</small></label>
 <label>Confirmar senha<input name="confirmarSenha" type="password" minlength="8" autocomplete="new-password" required></label>
 <button class="primary">Criar cadastro e enviar código</button>
 <a class="back-link" href="${pageContext.request.contextPath}/auth">Já tenho cadastro — voltar ao acesso</a>
</form>
<% } else { %>
<div class="role-tabs" role="tablist"><button type="button" class="role-tab active" data-role="ADMIN">Administrador</button><button type="button" class="role-tab" data-role="CUIDADOR">Cuidador</button></div>
<form method="post" action="${pageContext.request.contextPath}/auth" class="auth-form">
 <input type="hidden" name="acao" value="login"><input type="hidden" id="perfil" name="perfil" value="ADMIN">
 <label>E-mail<input name="email" type="email" value="${email}" autocomplete="username" maxlength="254" required></label>
 <label>Senha<input name="senha" type="password" autocomplete="current-password" required></label>
 <button class="primary">Entrar como administrador</button>
 <a class="signup-link" href="${pageContext.request.contextPath}/auth?view=cadastro">Não tem cadastro? <b>Criar cadastro</b></a>
</form><% } %>
<p class="auth-foot">Sistema de gestão de medicamentos</p>
</section><div class="auth-side"><div class="side-content"><span class="side-symbol">✚</span><h2>Mais cuidado em cada rotina.</h2><p>Informações de residentes, medicamentos e administrações em um só lugar.</p></div><small>Care Drop · Sistema Integrado de Gestão</small></div></main>
<script>
document.querySelectorAll('.role-tab').forEach(tab=>tab.addEventListener('click',()=>{
 document.querySelectorAll('.role-tab').forEach(t=>t.classList.remove('active'));tab.classList.add('active');
 const role=tab.dataset.role;document.getElementById('perfil').value=role;
 document.querySelector('.primary').textContent='Entrar como '+(role==='ADMIN'?'administrador':'cuidador');
}));
</script></body></html>