package br.com.caredrop.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/app")
public class AuthFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse resp=(HttpServletResponse)response;
        HttpSession session=req.getSession(false);
        if(session==null || session.getAttribute("userId")==null){
            resp.sendRedirect(req.getContextPath()+"/auth"); return;
        }
        chain.doFilter(request,response);
    }
}
