package br.gov.controller;

import java.io.IOException;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.gov.dao.OrgaoDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.util.Util;

@ManagedBean(name = "orgaoController")
@ViewScoped
public class OrgaoController implements Serializable {
	private static final long serialVersionUID = -6246372188013078523L;

	private Orgao orgao;
	private OrgaoDAO orgaoDao;
	private ServidorDAO servidorDao;
	private List<Orgao> orgaosSelecionaveis;
	private List<Orgao> selectedOrgaos = new ArrayList<Orgao>();
	private List<Servidor> selectedServidores = new ArrayList<Servidor>();
	private List<Servidor> servidoresSelecionaveis;
	private Orgao orgTemp;
	private Orgao orgaoDestino;
	private TreeNode root;

	private Date dataPesquisa;

	private String filterOrg;

	//propriedade de outros managed beans
	//--------------------------------------------------------------------------
	@ManagedProperty(value = "#{loginController}")
	LoginController loginController;


	//--------------------------------------------------------------------------

	private String filterCargo;

	public OrgaoController() {
		this.orgaoDao = new OrgaoDAO();
		this.servidorDao = new ServidorDAO();
	}

	/**
	 * Inicializa a managed bean que trabalhará com
	 * a entidade Orgao
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
	 * Realiza o processo de cadastrar o orgao
	 * @author Rafael Hosaka
	 */
	public void cadastrar() {
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean flag = true;
		if(new Util().isOrgaoNomeRepetido(this.orgao)){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro", 
					"Órgão com esse nome já existe!"));
			flag = false;
		}
		if(flag){
			this.orgao.setSuperior(orgTemp);
			List<Orgao> lista = orgTemp.getOrgaos();
			lista.add(orgao);
			this.orgTemp.setOrgaos(lista);
			if(this.orgaoDao.create(this.orgao,getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Órgão cadastrado com sucesso!"));
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));
			}
		}

	}

	/**
	 * Reliza o processo de alterar o orgao
	 * @author Rafael Hosaka
	 */
	public void alterar() {
		FacesContext faces = FacesContext.getCurrentInstance();

		boolean flag = true;
		if(new Util().isOrgaoNomeRepetido(this.orgaoDestino)){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro", 
					"Órgão com esse nome já existe!"));
			flag = false;
		}
		if(flag){
			if (orgTemp != null && (orgTemp.getId() != orgaoDestino.getSuperior().getId())) {

				Orgao o = orgaoDestino.getSuperior();
				List<Orgao> list = o.getOrgaos();
				list.remove(orgaoDestino);
				o.setOrgaos(list);

				orgaoDestino.setSuperior(orgTemp);
				List<Orgao> lista = orgTemp.getOrgaos();
				lista.add(orgaoDestino);
				orgTemp.setOrgaos(lista);
			}

			if(this.orgaoDao.update(this.orgaoDestino, getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Informação",
						"Orgão alterado com sucesso!"));
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));
			}
		}
	}

	/**
	 * recupera os servidores do orgao e atribui
	 * @author Rafael Hosaka
	 */
	public void visualizar(ActionEvent evento) {
		this.orgTemp = (Orgao) evento.getComponent().getAttributes()
				.get("orgao");
		this.selectedServidores = servidorDao.findServidoresByOrgaoAndDate(orgTemp,getDataPesquisa());
		this.filterCargo = "todos";
	}

	/**
	 * prepara para adicionar orgao
	 * @author Rafael Hosaka
	 */
	public void preparaAdicionaOrgao(ActionEvent evento) {
		this.orgTemp = (Orgao) evento.getComponent().getAttributes()
				.get("orgao");
	}

	/**
	 * prepara para os dados para alterar o orgao
	 * @author Rafael Hosaka
	 */
	public void preparaAlteraOrgao(ActionEvent evento) {
		this.orgaoDestino = (Orgao) evento.getComponent().getAttributes()
				.get("orgao");
		orgTemp = orgaoDestino.getSuperior();

		orgaosSelecionaveis = new Util().recuperaListaOrgaoInferiores(orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(),dataPesquisa));	
		orgaosSelecionaveis.remove(this.orgaoDestino);
		
		//List<Orgao> listaOrg = this.orgaoDao
		//		.findOrgaosSelecionaveis(this.orgaoDestino);
		//setOrgaosSelecionaveis(listaOrg);

		setSelectedServidores(servidorDao.findServidoresByOrgao(orgaoDestino));
		List<Servidor> listaServ = servidorDao.findServidoresByOrgao(orgaoDestino);
		setServidoresSelecionaveis(listaServ);
	}

	/**
	 * prepara para excluir o orgao
	 * @author Rafael Hosaka
	 */
	public void preparaExcluirMovimentar(ActionEvent evento) {
		this.orgTemp = (Orgao) evento.getComponent().getAttributes()
				.get("orgao");
		this.setServidoresSelecionaveis(new Util()
		.recuperaListaServidoresInferiores(orgaoDao.findById(orgTemp
				.getId())));
		if(!new Util().isExisteOrgaoEmEspera()){
			orgaoDao.criarEmEspera();
		}
	}

	/**
	 * exclui o orgao e movimenta os servidores que estavam no orgao
	 * para um novo orgao
	 * @author Rafael Hosaka
	 */
	public void excluirMovimentar() {
		FacesContext faces = FacesContext.getCurrentInstance();

		//jogando para em_espera
		orgaoDao.moverServidoresEmEspera(servidoresSelecionaveis,getDataPesquisa());
		orgaoDao.excluirOrgaos(orgTemp,getDataPesquisa());

		faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Informação", "Orgão excluido com sucesso!"));
		preparaAmbiente();


	}



	/**
	 * prepara o ambiente para trabalhar com orgao
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {
		if(!loginController.atualizaServidorLogado()){
			FacesContext faces = FacesContext.getCurrentInstance();
			faces.getExternalContext().getFlash().setKeepMessages(true);
			faces.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, "Informação",
					"Seu perfil foi retirado pelo administrador superior"));
		}else{
			this.orgao = new Orgao();
			setDataPesquisa(new Date());
			Orgao orgao = orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(), dataPesquisa);
			if(orgao != null){
				inicializaRoot();

				setRoot(new Util().createOrgaoTreeNodeDate(
						root,
						orgao,dataPesquisa,new Util().recuperaListaOrgaoInferiores(getLoginController().getServidorLogado().getOrgao())));
			}else{
				FacesContext faces = FacesContext.getCurrentInstance();
				faces.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Informação",
						"Nenhum Orgão encontrado!"));
			}
		}
	}

	/**
	 * inicializa o nó da arvore
	 * @author Rafael Hosaka
	 */
	private void inicializaRoot() {
		this.root = null;
		this.root = new DefaultTreeNode(null, null);
	}

	/**
	 * pesquisa os orgoes de acorco com a data informada
	 * @author Rafael Hosaka
	 */
	public void pesquisar() {

		Orgao orgao = orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(), dataPesquisa);
		inicializaRoot();
		if (orgao != null) {
			this.setRoot(new Util().createOrgaoTreeNodeDate(root, orgao,
					dataPesquisa,new Util().recuperaListaOrgaoInferiores(getLoginController().getServidorLogado().getOrgao())));
		} else {

			FacesContext faces = FacesContext.getCurrentInstance();
			faces.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, "Informação",
					"Nenhum Orgão encontrado!"));


		}
	}

	/**
	 * filtra os servidores de um orgao de acordo com a opção de cargo
	 * todos - recupera todos servidores
	 * comCargo - recupera todos servidores que possuem cargo
	 * semCargo - recupera todos servidores que não possuem cargo
	 * @author Rafael Hosaka
	 */
	public void filtrarServidor(){
		if(filterCargo.equals("todos")){
			this.selectedServidores = servidorDao.findServidoresByOrgaoAndDate(orgTemp,getDataPesquisa());
		}else{
			if(filterCargo.equals("comCargo")){
				this.selectedServidores = servidorDao.findByOrgaoDataComCargo(orgTemp,getDataPesquisa());
			}else{
				this.selectedServidores = servidorDao.findByOrgaoDataSemCargo(orgTemp,getDataPesquisa());
			}
		}
	}

	/**
	 * Filtra o orgao por nome
	 * @author Rafael Hosaka
	 */
	public void filtrarOrgao(){
		if(!filterOrg.isEmpty()){
			List<Orgao> orgs = orgaoDao.filtrarPorNome(filterOrg,getDataPesquisa());
			if(orgs.size() > 500){
				FacesContext faces = FacesContext.getCurrentInstance();
				faces.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Informação",
						"O filtro recuperou muitos resultados, favor tentar com outro nome!"));
			}else{
				inicializaRoot();
				setRoot(new Util().montarArvoreSemHierarquia(root, orgs));
			}
		}else{
			pesquisar();
		}
	}

	/**
	 * Redireciona e cria o organograma
	 * @throws IOException
	 * @author Rafael Hosaka
	 */
	public void redirecionaOrganograma() throws IOException{
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		new Util().criarXml(FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/organograma.xml",orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(),getDataPesquisa()),getDataPesquisa());
		ec.redirect(ec.getRequestContextPath() + "/adm/organograma.html");
	}

	/**
	 * Expande toda a arvore
	 * @author Rafael Hosaka
	 */
	public void expandir(){
		new Util().expandirNode(root, true);
	}

	// getter setter
	public Orgao getOrgao() {
		return orgao;
	}

	public void setOrgao(Orgao orgao) {
		this.orgao = orgao;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public List<Orgao> getSelectedOrgaos() {
		return selectedOrgaos;
	}

	public void setSelectedOrgaos(List<Orgao> selectedOrgaos) {
		this.selectedOrgaos = selectedOrgaos;
	}

	public List<Orgao> getOrgaosSelecionaveis() {
		return orgaosSelecionaveis;
	}

	public void setOrgaosSelecionaveis(List<Orgao> orgaosSelecionaveis) {
		this.orgaosSelecionaveis = orgaosSelecionaveis;
	}

	public List<Servidor> getServidoresSelecionaveis() {
		return servidoresSelecionaveis;
	}

	public void setServidoresSelecionaveis(
			List<Servidor> servidoresSelecionaveis) {
		this.servidoresSelecionaveis = servidoresSelecionaveis;
	}

	public List<Servidor> getSelectedServidores() {
		return selectedServidores;
	}

	public void setSelectedServidores(List<Servidor> selectedServidores) {
		this.selectedServidores = selectedServidores;
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

	public String getFilterCargo() {
		return filterCargo;
	}

	public void setFilterCargo(String filterCargo) {
		this.filterCargo = filterCargo;
	}


	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
	}

	public String getDataPesquisaFormatada() {
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		return dataPesquisa == null?"":sd.format(this.dataPesquisa);
	}

	public void setDataPesquisaFormatada(String dataPesquisa) {
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.dataPesquisa = sd.parse(dataPesquisa);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getFilterOrg() {
		return filterOrg;
	}

	public void setFilterOrg(String filterOrg) {
		this.filterOrg = filterOrg;
	}

}
