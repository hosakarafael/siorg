package br.gov.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Filtro para redirecionar o usuario para página de login, caso já esteja logado
 * @author Rafael Hosaka
 */
public class LoginPageFilter implements Filter{
   @Override
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,   FilterChain filterChain) throws IOException, ServletException{
       HttpServletRequest request = (HttpServletRequest) servletRequest;
       HttpServletResponse response = (HttpServletResponse) servletResponse;
       if(request.getUserPrincipal() != null){
                String navigateString = "";
                if(request.isUserInRole("adm")){
                        navigateString = "/adm/listagemOrgao.xhtml";
                }
                response.sendRedirect(request.getContextPath()+navigateString);
       } else{
           filterChain.doFilter(servletRequest, servletResponse);
       }
   }

   @Override
   public void destroy(){
   }
   
   @Override
   public void init(FilterConfig filterConfig) throws ServletException{
   }
}