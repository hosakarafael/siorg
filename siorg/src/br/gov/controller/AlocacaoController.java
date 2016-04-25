package br.gov.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DualListModel;

import br.gov.dao.OrgaoDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.util.Util;

@ManagedBean(name = "alocacaoController")
@ViewScoped
public class AlocacaoController implements Serializable{
	
	private static final long serialVersionUID = -3354700769646596481L;
	
	private DualListModel<Servidor> pickServidores;
	private List<Servidor> targetPickServidores;
	private List<Servidor> sourcePickServidores;
	private List<Orgao> orgaosSource;
	private List<Orgao> orgaosTarget;
	private Orgao orgTemp;
	private Orgao orgaoDestino;
	
	private OrgaoDAO orgaoDao;
	private ServidorDAO servidorDao;
	
	@ManagedProperty(value = "#{loginController}")
	LoginController loginController;
	
	public AlocacaoController() {
		servidorDao = new ServidorDAO();
		orgaoDao = new OrgaoDAO();
	}
	
	/**
	  * prepara os dados para alocar servidores
	  * @author Rafael Hosaka
	  */
	@PostConstruct
	public void init() {
		loginController.atualizaServidorLogado();
		orgTemp = null;
		orgaoDestino = null;
		orgaosSource = new Util().recuperaListaOrgaoInferiores(loginController.getServidorLogado().getOrgao());
		orgaosTarget = new Util().recuperaListaOrgaoInferiores(loginController.getServidorLogado().getOrgao());
		pickServidores = new DualListModel<Servidor>();
	}
	 
	/**
	  * Recupera os servidores de acordo com o orgao selecionado
	  * @author Rafael Hosaka
	  */
	public void atualizaSourceServidores() {
		if (orgTemp == null) {
			sourcePickServidores = new ArrayList<Servidor>();
		} else {
			sourcePickServidores = servidorDao.findServidoresByOrgaoAndDate(orgTemp,new Date());
		}
		orgaosTarget = new Util()
		.recuperaListaOrgaoInferiores(getLoginController()
				.getServidorLogado().getOrgao());
		orgaosTarget.remove(orgTemp);
		pickServidores.setSource(sourcePickServidores);
	}

	/**
	  * Recupera os servidores de acordo com o orgao selecionado
	  * @author Rafael Hosaka
	  */
	public void atualizaTargetServidores() {
		if (orgaoDestino == null) {
			targetPickServidores = new ArrayList<Servidor>();
		} else {
			targetPickServidores = servidorDao.findServidoresByOrgaoAndDate(orgaoDestino,new Date());
		}
		orgaosSource = new Util()
		.recuperaListaOrgaoInferiores(getLoginController()
				.getServidorLogado().getOrgao());
		orgaosSource.remove(orgaoDestino);
		pickServidores.setTarget(targetPickServidores);
	}

	/**
	  * Salva a alocação feita
	  * @author Rafael Hosaka
	  */
	public void salvarAlocacao() {
		FacesContext faces = FacesContext.getCurrentInstance();
		faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Informação", "Servidores alocados com sucesso!"));

		servidorDao.atualizaOrgaoSuperior(pickServidores.getSource(), orgTemp,
				new Date());
		servidorDao.atualizaOrgaoSuperior(pickServidores.getTarget(),
				orgaoDestino, new Date());

		orgTemp.setServidores(pickServidores.getSource());
		orgaoDestino.setServidores(pickServidores.getTarget());
		orgaoDao.update(orgTemp, new Date());
		orgaoDao.update(orgaoDestino, new Date());
	}
	
	
	public DualListModel<Servidor> getPickServidores() {
		return pickServidores;
	}
	public void setPickServidores(DualListModel<Servidor> pickServidores) {
		this.pickServidores = pickServidores;
	}
	public List<Servidor> getTargetPickServidores() {
		return targetPickServidores;
	}
	public void setTargetPickServidores(List<Servidor> targetPickServidores) {
		this.targetPickServidores = targetPickServidores;
	}
	public List<Servidor> getSourcePickServidores() {
		return sourcePickServidores;
	}
	public void setSourcePickServidores(List<Servidor> sourcePickServidores) {
		this.sourcePickServidores = sourcePickServidores;
	}
	public List<Orgao> getOrgaosSource() {
		return orgaosSource;
	}
	public void setOrgaosSource(List<Orgao> orgaosSource) {
		this.orgaosSource = orgaosSource;
	}
	public List<Orgao> getOrgaosTarget() {
		return orgaosTarget;
	}
	public void setOrgaosTarget(List<Orgao> orgaosTarget) {
		this.orgaosTarget = orgaosTarget;
	}
	public Orgao getOrgTemp() {
		return orgTemp;
	}
	public void setOrgTemp(Orgao orgTemp) {
		this.orgTemp = orgTemp;
	}
	public Orgao getOrgaoDestino() {
		return orgaoDestino;
	}
	public void setOrgaoDestino(Orgao orgaoDestino) {
		this.orgaoDestino = orgaoDestino;
	}
	public LoginController getLoginController() {
		return loginController;
	}
	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}
	
	
}

