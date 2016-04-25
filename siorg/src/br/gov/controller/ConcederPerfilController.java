package br.gov.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import br.gov.dao.OrgaoDAO;
import br.gov.dao.PerfilDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Autorizacao;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.entity.Usuario;
import br.gov.util.Util;

@ManagedBean(name = "concederPerfilController")
@ViewScoped
public class ConcederPerfilController implements Serializable {
	
	private static final long serialVersionUID = -2246075201448521357L;
	
	private Servidor servTemp;
	private Orgao orgTemp;
	private List<Orgao> orgaos;
	private List<Servidor> servidores;

	private Usuario usuario;

	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;

	private OrgaoDAO orgaoDao;
	private PerfilDAO perfilDao;
	private ServidorDAO servDao;

	public ConcederPerfilController(){
		orgaoDao = new OrgaoDAO();
		perfilDao = new PerfilDAO();
		servDao = new ServidorDAO();
	}

	/**
	 * prepara os dados para a página onde será informado
	 * os dados para conceder perfil
	 * @author Rafael Hosaka
	 */
	@PostConstruct
	public void init(){
		preparaAmbiente();
		orgaos = new Util().recuperaListaOrgaoInferiores(orgaoDao.findById(getLoginController().getServidorLogado().getOrgao().getId()));
	}

	@PreDestroy  
	private void destroy(){  
	} 

	/**
	 * atualiza a lista de servidores que não possuir perfil de acordo com o orgao
	 * @author Rafael Hosaka
	 */
	public void atualizaListServidores(){
		if(orgTemp == null){
			servidores = new ArrayList<Servidor>();
		}else{
			List<Servidor> serv = servDao.findServidoresByOrgao(orgTemp);
			servidores = new Util().retiraServidoresAdm(serv);
		}
	}

	/**
	 * Concede perfil para um servidor, criando novo registro na tabela de Usuario e Autorizacao (JAAS)
	 * @author Rafael Hosaka
	 */
	public void conceder(){
		FacesContext faces = FacesContext.getCurrentInstance();
		Usuario u = new Usuario(servTemp.getMatricula(),new Util().encriptPassword(Usuario.DEFAULT_PW),servTemp);
		Autorizacao a = new Autorizacao(u,Usuario.ADM);
		perfilDao.create(u, a);
		faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Perfil concedido com sucesso!"));
		preparaAmbiente();
	}

	/**
	 * prepara o ambiente para trabalhar com perfil
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {
		loginController.atualizaServidorLogado();
		servidores = null;
		orgTemp = null;
		servTemp = null;
	}

	//Getter Setter
	public List<Servidor> getServidores() {
		return servidores;
	}

	public void setServidores(List<Servidor> servidores) {
		this.servidores = servidores;
	}

	public List<Orgao> getOrgaos() {
		return orgaos;
	}

	public void setOrgaos(List<Orgao> orgaos) {
		this.orgaos = orgaos;
	}

	public Orgao getOrgTemp() {
		return orgTemp;
	}

	public void setOrgTemp(Orgao orgTemp) {
		this.orgTemp = orgTemp;
	}

	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public Servidor getServTemp() {
		return servTemp;
	}

	public void setServTemp(Servidor servTemp) {
		this.servTemp = servTemp;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}
