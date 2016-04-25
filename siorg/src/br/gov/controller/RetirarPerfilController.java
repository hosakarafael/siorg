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

@ManagedBean(name = "retirarPerfilController")
@ViewScoped
public class RetirarPerfilController implements Serializable {
	
	private static final long serialVersionUID = 5817646097834090646L;

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

	public RetirarPerfilController(){
		orgaoDao = new OrgaoDAO();
		perfilDao = new PerfilDAO();
		servDao = new ServidorDAO();
	}

	/**
	 * Redireciona para pagina onde retirará perfil de um servidor
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
	 * Atualiza a lista de servidores que possui perfil de acordo com orgao
	 * @author Rafael Hosaka
	 */
	public void atualizaServidoresRetirar(){
		if(orgTemp == null){
			servidores = new ArrayList<Servidor>();
		}else{
			List<Servidor> serv = servDao.findServidoresByOrgao(orgTemp);
			servidores = new Util().recuperaAdm(serv);
		}
	}

	/**
	 * Retira o perfil de um servidor
	 * @author Rafael Hosaka
	 */
	public void retirarPerfil(){
		FacesContext faces = FacesContext.getCurrentInstance();

		Usuario u = perfilDao.findUsuarioByMatricula(servTemp.getMatricula());
		Autorizacao a = perfilDao.findAutorizacaoByMatricula(servTemp.getMatricula());
		perfilDao.delete(u,a);
		faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Perfil retirado com sucesso!"));
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
