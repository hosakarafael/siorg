package br.gov.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import br.gov.dao.PerfilDAO;
import br.gov.entity.Usuario;
import br.gov.util.Util;

@ManagedBean(name = "alteraSenhaController")
@ViewScoped
public class AlteraSenhaController implements Serializable{
	
	private static final long serialVersionUID = -1784421459975212715L;
	
	private PerfilDAO perfilDao;
	private Usuario usuario;
	
	private String senha;
	private String novaSenha1;
	private String novaSenha2;
	
	@ManagedProperty(value = "#{loginController}")
	LoginController loginController;
	
	public AlteraSenhaController(){
		perfilDao = new PerfilDAO();
	}
	
	/**
	 * Atribui o servidor que está logado para mudar a senha
	 * @author Rafael Hosaka
	 */
	@PostConstruct
	public void init(){
		loginController.atualizaServidorLogado();
		setUsuario(perfilDao.findUsuarioByMatricula(getLoginController().getServidorLogado().getMatricula()));
	}

	/**
	 * Altera a senha do servidor logado
	 * @author Rafael Hosaka
	 */
	public void alterarSenha(){
		FacesContext faces = FacesContext.getCurrentInstance();
		senha = new Util().encriptPassword(senha);
		if(senha.equals(usuario.getSenha())){
			if(novaSenha1.equals(novaSenha2)){
				usuario.setSenha(new Util().encriptPassword(novaSenha1));
				perfilDao.update(usuario);
				this.senha = null;
				this.novaSenha1 = null;
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Senha alterada com sucesso!"));
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","A nova senha e a confirmação da nova senha não correspondem!"));
			}
		}else{
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","A senha atual está incorreta!"));
		}

	}
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getNovaSenha1() {
		return novaSenha1;
	}

	public void setNovaSenha1(String novaSenha1) {
		this.novaSenha1 = novaSenha1;
	}

	public String getNovaSenha2() {
		return novaSenha2;
	}

	public void setNovaSenha2(String novaSenha2) {
		this.novaSenha2 = novaSenha2;
	}
	
	
}
