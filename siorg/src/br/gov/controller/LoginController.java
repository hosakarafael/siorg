package br.gov.controller;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import br.gov.dao.PerfilDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Servidor;

@ManagedBean(name = "loginController")
@SessionScoped
public class LoginController implements Serializable {
	private static final long serialVersionUID = 5817646097834090646L;
	private String matricula;
	private String password;
	
    private Servidor servidorLogado;

	public LoginController() {
	}

	/**
	  * Verifica se o servidor existe no mecanismo do JAAS
	  *    se existe coloca o servidor na sessão (atributo servidorLogado)
	  *    assim redireciona para a pagina
	  * @author Rafael Hosaka
	  */
	public void login(ActionEvent actionEvent) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            String navigateString = "";
            request.login(matricula, password);
            @SuppressWarnings("unused")
			Principal principal = request.getUserPrincipal();
            if (request.isUserInRole("adm")) {
                navigateString = "/adm/listagemOrgao.xhtml";
            }
            try {
            	this.servidorLogado = new ServidorDAO().findServidorByMatricula(matricula);
                HttpSession session = ( HttpSession ) FacesContext.getCurrentInstance().getExternalContext().getSession( true );  
                session.setAttribute("servidorLogado",servidorLogado);
            	context.getExternalContext().redirect(request.getContextPath() + navigateString);
            } catch (IOException ex) {
                context.addMessage(null, new FacesMessage("Erro!", "Ocorreu um erro no servidor, contate o administrador."));
            }
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage("Error!", "Matrícula ou senha incorretos!"));
        }
    }

	
	/**
	  * Termina a sessão e redireciona para página de login
	  * @author Rafael Hosaka
	  */
	public void logout() {

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);

		if (session != null) {
			session.invalidate();
		}
		FacesContext
				.getCurrentInstance()
				.getApplication()
				.getNavigationHandler()
				.handleNavigation(FacesContext.getCurrentInstance(), null,
						"/login.xhtml?faces-redirect=true");
	}

	/**
	 * Atualiza o servidor logado no sistema
	 * caso consiga atualizar retorna true,
	 * caso o servidor não possua mais perfil retorna false
	 * @return true se atualizou
	 *         false se não possui perfil
	 * @author Rafael Hosaka
	 */
	public boolean atualizaServidorLogado() {
		servidorLogado =  new ServidorDAO().findServidorByMatricula(matricula);
		if(new PerfilDAO().findUsuarioByMatricula(matricula) == null){
			return false;
		}else{
			return true;
		}
	}
	
	//Getters Setters================================================================

	
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public Servidor getServidorLogado() {
		return servidorLogado;
	}

	public void setServidorLogado(Servidor servidorLogado) {
		this.servidorLogado = servidorLogado;
	}

	


}
