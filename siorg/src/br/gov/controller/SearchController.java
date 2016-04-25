package br.gov.controller;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
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

@ManagedBean(name = "searchController")
@ViewScoped
public class SearchController implements Serializable{

	private static final long serialVersionUID = -1196615705650821684L;

	private Date dataPesquisa;

	private TreeNode root;
	private OrgaoDAO orgaoDao;
	private Orgao orgao;
	private Orgao orgTemp;
	private List<Servidor> selectedServidores;
	private String filterCargo;
	private ServidorDAO servidorDao;

	private String filterOrg;
	
	public SearchController() {
		orgaoDao = new OrgaoDAO();
		servidorDao = new ServidorDAO();
	}

	@PostConstruct
	public void init(){
		preparaAmbiente();
	}



	/**
	 * Redireciona e cria o organograma
	 * @throws IOException
	 * @author Rafael Hosaka
	 */
	public void redirecionaOrganogramaPublico() throws IOException{

		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		new Util().criarXml(FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/organograma.xml",orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(),getDataPesquisa()),getDataPesquisa());
		ec.redirect(ec.getRequestContextPath() + "/organograma.html");
	}

	/**
	 * prepara o ambiente para trabalhar com orgao
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {
		this.setOrgao(new Orgao());
		inicializaRoot();
		setDataPesquisa(new Date());
		Orgao orgao = orgaoDao.findByIdAndData(orgaoDao.findOrgaoSuperior().getId(), dataPesquisa);
		if(orgao != null){
			setRoot(new Util().createOrgaoTreeNodeDate(
					root,
					orgao,dataPesquisa));
		}else{
			FacesContext faces = FacesContext.getCurrentInstance();
			faces.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, "Informação",
					"Nenhum Orgão encontrado!"));
		}
	}

	/**
	 * recupera os servidores do orgao e atribui
	 * @author Rafael Hosaka
	 */
	public void visualizar(ActionEvent evento) {
		this.orgTemp = (Orgao) evento.getComponent().getAttributes()
				.get("orgao");
		this.setSelectedServidores(servidorDao.findServidoresByOrgaoAndDate(orgTemp,getDataPesquisa()));
		this.setFilterCargo("todos");
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
					dataPesquisa));
		} else {
			FacesContext faces = FacesContext.getCurrentInstance();
			faces.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, "Informação",
					"Nenhum Orgão encontrado!"));
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

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
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

	public Orgao getOrgao() {
		return orgao;
	}

	public void setOrgao(Orgao orgao) {
		this.orgao = orgao;
	}

	public List<Servidor> getSelectedServidores() {
		return selectedServidores;
	}

	public void setSelectedServidores(List<Servidor> selectedServidores) {
		this.selectedServidores = selectedServidores;
	}

	public String getFilterCargo() {
		return filterCargo;
	}

	public void setFilterCargo(String filterCargo) {
		this.filterCargo = filterCargo;
	}


	public Orgao getOrgTemp() {
		return orgTemp;
	}

	public void setOrgTemp(Orgao orgTemp) {
		this.orgTemp = orgTemp;
	}

	public String getFilterOrg() {
		return filterOrg;
	}

	public void setFilterOrg(String filterOrg) {
		this.filterOrg = filterOrg;
	}

	

}
