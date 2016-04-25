package br.gov.controller;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import br.gov.dao.OrgaoDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.util.Util;

@ManagedBean(name = "redistribuirController")
@ViewScoped
public class RedistribuirController implements Serializable{

	private static final long serialVersionUID = 2269373683462370051L;

	private List<Servidor> servidores;
	private List<Servidor> servidoresSelecionados;

	private List<Orgao> orgaos;
	private Orgao orgaoDestino;

	private OrgaoDAO orgaoDao;
	private ServidorDAO servidorDao;

	@ManagedProperty(value = "#{loginController}")
	LoginController loginController;

	public RedistribuirController(){
		servidorDao = new ServidorDAO();
		orgaoDao = new OrgaoDAO();
	}

	/**
	 * prepara os dados para redistribuir servidores
	 * @return String pagina destino
	 * @author Rafael Hosaka
	 */
	@PostConstruct
	public void init() {
		preparaAmbiente();
	}

	/**
	 * Prepara o ambiente para trabalhar com a funcionalidade de redistribuir
	 * o servidor
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {
		loginController.atualizaServidorLogado();
		if(orgaoDao.findOrgaoEmEspera() == null){
			orgaoDao.criarEmEspera();
		}
		servidores = servidorDao.findServidoresByOrgao(orgaoDao.findOrgaoEmEspera());
		orgaos = new Util().recuperaListaOrgaoInferiores(loginController.getServidorLogado().getOrgao());
		servidoresSelecionados = null;
		orgaoDestino = null;
	}

	/**
	 * Salva a redistribuição
	 * @author Rafael Hosaka
	 */
	public void salvarRedistribuir(){

		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");

		new Util().settingServidoresCargoNull(servidoresSelecionados);
		List<Servidor> servidores = orgaoDestino.getServidores();
		servidores.addAll(servidoresSelecionados);

		servidorDao.atualizaRedistribuicao(servidoresSelecionados,
				orgaoDestino);
		preparaAmbiente();
		servidores = null;
		FacesContext faces = FacesContext.getCurrentInstance();
		faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Informação", "Servidor(es) redistribuido(s) com sucesso!"));

	}

	public LoginController getLoginController() {
		return loginController;
	}


	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public List<Servidor> getServidores() {
		return servidores;
	}

	public void setServidores(List<Servidor> servidores) {
		this.servidores = servidores;
	}

	public List<Servidor> getServidoresSelecionados() {
		return servidoresSelecionados;
	}

	public void setServidoresSelecionados(List<Servidor> servidoresSelecionados) {
		this.servidoresSelecionados = servidoresSelecionados;
	}

	public List<Orgao> getOrgaos() {
		return orgaos;
	}

	public void setOrgaos(List<Orgao> orgaos) {
		this.orgaos = orgaos;
	}

	public Orgao getOrgaoDestino() {
		return orgaoDestino;
	}

	public void setOrgaoDestino(Orgao orgaoDestino) {
		this.orgaoDestino = orgaoDestino;
	}


}

