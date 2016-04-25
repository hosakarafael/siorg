package br.gov.controller;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import br.gov.dao.CargoDAO;
import br.gov.dao.OrgaoDAO;
import br.gov.dao.PerfilDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Autorizacao;
import br.gov.entity.Cargo;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.entity.Usuario;
import br.gov.lazymodel.LazyServidorModel;
import br.gov.util.Util;

@ManagedBean(name = "servidorController")
@ViewScoped
public class ServidorController implements Serializable {
	private static final long serialVersionUID = -8951981606562457927L;

	private Servidor servidor;
	private ServidorDAO servidorDao;
	private OrgaoDAO orgaoDao;
	private CargoDAO cargoDao;
	private List<Servidor> servidores;
	private List<Orgao> orgaosSelecionaveis;
	private List<Cargo> cargosSelecionaveis;

	private Date dataPesquisa;

	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;

	private LazyServidorModel lazyServidores;

	public ServidorController() {
		this.servidorDao = new ServidorDAO();
		this.orgaoDao = new OrgaoDAO();
		this.cargoDao = new CargoDAO();
	}

	/**
	 * Inicializa a managed bean que trabalhará com
	 * a entidade Servidor
	 * @author Rafael Hosaka
	 */
	@PostConstruct
	public void init() {
		preparaAmbiente();
	}

	@PreDestroy  
	private void destroy(){  
	} 

	/**
	 * Realiza o processo de cadastrar o servidor
	 * @author Rafael Hosaka
	 */
	public void cadastrar() {
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean flag = true;
		if(servidor.getCargo() != null){
			if(new Util().isCargoRepetidoEmOrgao(servidor, servidor.getOrgao())){
				faces.addMessage("msgs", new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Erro", "Já existe um servidor com este cargo nesse órgão! Selecione outro cargo."));
				flag = false;
			}
		}
		if(flag){
			if(this.servidorDao.create(this.servidor,getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Informação", "Servidor inserido com sucesso!"));
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Erro", "Houve um erro na deleção!"));
			}
			preparaAmbiente();
		}
	}

	/**
	 * Realiza o processo de excluir o servidor
	 * @author Rafael Hosaka
	 */
	public void excluir(ActionEvent evento) {
		FacesContext faces = FacesContext.getCurrentInstance();
		this.servidor = (Servidor) evento.getComponent().getAttributes()
				.get("servidor");

		if(this.servidorDao.delete(this.servidor,getDataPesquisa())){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Informação", "Servidor excluido com sucesso!"));
			Usuario u = new PerfilDAO().findUsuarioByMatricula(servidor.getMatricula());
			if(u != null){
				Autorizacao a = new PerfilDAO().findAutorizacaoByMatricula(servidor.getMatricula());
				new PerfilDAO().delete(u,a);
			}
		}else{
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Erro", "Houve um erro na deleção!"));

		}

		preparaAmbiente();
	}

	/**
	 * Reliza o processo de alterar o servidor
	 * @author Rafael Hosaka
	 */
	public void alterar() {
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean flag = true;
		if(servidor.getCargo() != null){
			if(new Util().isCargoRepetidoEmOrgao(servidor, servidor.getOrgao())){
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Erro", "Já existe um servidor com este cargo nesse órgão! Selecione outro cargo."));
				flag = false;
			}
		}

		if(flag){
			if(this.servidorDao.update(this.servidor,this.getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Informação",
						"Servidor alterado com sucesso!"));
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));
			}
		}

	}

	/**
	 * prepara os dados para alterar o servidor
	 * @author Rafael Hosaka
	 */
	public void preparaAlterarServidor(ActionEvent evento) {
		this.servidor = (Servidor) evento.getComponent().getAttributes()
				.get("servidor");

	}

	/**
	 * pesquisa os servidores de acorco com a data informada
	 * @author Rafael Hosaka
	 */
	public void pesquisar(){
		configuraOrgaosSelecionariosEServidores(dataPesquisa);
		cargosSelecionaveis = cargoDao.searchCargoByDate(dataPesquisa);
		Orgao o = orgaoDao.findByIdAndData(getLoginController().getServidorLogado().getOrgao().getId(), getDataPesquisa());
		if(o == null){
			lazyServidores = null;
		}else{
			lazyServidores = new LazyServidorModel(getDataPesquisa(),new Util().recuperaListaOrgaoInferiores(o));
		}
	}

	/**
	 * prepara o ambiente para trabalhar com servidor
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {
		loginController.atualizaServidorLogado();
		setDataPesquisa(new Date());
		configuraOrgaosSelecionariosEServidores(new Date());
		lazyServidores = new LazyServidorModel(getDataPesquisa(),new Util().recuperaListaOrgaoInferiores(orgaoDao.findByIdAndData(getLoginController().getServidorLogado().getOrgao().getId(), getDataPesquisa()))); 
		this.setCargosSelecionaveis(cargoDao.findWithNamedQuery(Cargo.HEAD));
		this.servidor = new Servidor();
	}

	private void configuraOrgaosSelecionariosEServidores(Date dataPesquisa){
		if(orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(),dataPesquisa)==null){
			orgaosSelecionaveis = new ArrayList<Orgao>();
		}else{
			orgaosSelecionaveis = new Util().recuperaListaOrgaoInferiores(orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(),dataPesquisa));	
		}
	}

	/**
	 * Prepara o historico do servidor
	 * @author Rafael Hosaka
	 */
	public void preparaHistoricoServidor(ActionEvent evento){
		this.servidor = (Servidor) evento.getComponent().getAttributes()
				.get("servidor");
		servidores = servidorDao.findBYID(servidor.getId());

	}

	// Getter Setter
	public List<Servidor> getServidores() {
		return servidores;
	}

	public void setServidores(List<Servidor> servidores) {
		this.servidores = servidores;
	}

	public Servidor getServidor() {
		return servidor;
	}

	public void setServidor(Servidor servidor) {
		this.servidor = servidor;
	}

	public List<Orgao> getOrgaosSelecionaveis() {
		return orgaosSelecionaveis;
	}

	public void setOrgaosSelecionaveis(List<Orgao> orgaosSelecionaveis) {
		this.orgaosSelecionaveis = orgaosSelecionaveis;
	}

	public void setCanEdit(List<Servidor> servidoresInferiores){
		for(Servidor servidor:this.lazyServidores)
			servidor.setCanEdit(servidor.getDtFimServidor() == null && servidoresInferiores.contains(servidor));
	}

	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public List<Cargo> getCargosSelecionaveis() {
		return cargosSelecionaveis;
	}

	public void setCargosSelecionaveis(List<Cargo> cargosSelecionaveis) {
		this.cargosSelecionaveis = cargosSelecionaveis;
	}

	public LazyServidorModel getLazyServidores() {
		return lazyServidores;
	}

	public void setLazyServidores(LazyServidorModel lazyServidores) {
		this.lazyServidores = lazyServidores;
	}

	public String getDataPesquisaFormatada() {
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		return sd.format(dataPesquisa == null?"":this.dataPesquisa);
	}

	public void setDataPesquisaFormatada(String dataPesquisa) {
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.dataPesquisa = sd.parse(dataPesquisa);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
	}

}
