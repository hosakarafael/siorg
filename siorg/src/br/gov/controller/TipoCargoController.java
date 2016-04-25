package br.gov.controller;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import br.gov.dao.TipoCargoDAO;
import br.gov.entity.TipoCargo;
import br.gov.lazymodel.LazyTipoCargoModel;
import br.gov.util.Util;

@ManagedBean(name = "tipoCargoController")
@ViewScoped
public class TipoCargoController implements Serializable{

	private static final long serialVersionUID = -7982464157859697999L;

	private TipoCargo tipoCargo;	
	private TipoCargoDAO tipoCargoDAO;
	private List<TipoCargo> tiposCargos = null;
	private LazyTipoCargoModel lazyTiposCargos;

	private Date dataPesquisa;

	public TipoCargoController(){
		this.tipoCargo = new TipoCargo();
		this.tipoCargoDAO = new TipoCargoDAO();
		this.tiposCargos = this.tipoCargoDAO.findWithNamedQuery(TipoCargo.HEAD);
	}

	/**
	 * Inicializa a managed bean que trabalhará com
	 * a entidade TipoCargo
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
	 * Realiza o processo de cadastrar o tipo cargo
	 * @author Rafael Hosaka
	 */
	public void cadastrar(){
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean flag = true;
		if(new Util().isTipoCargoDescRepetido(this.tipoCargo)){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","tipo com essa descrição já existe!"));				
			flag = false;
		}
		if(flag){
			if(this.tipoCargoDAO.create(this.tipoCargo,getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Tipo de cargo inserido com sucesso!"));				
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));				
			}
		}


	}

	/**
	 * Realiza o processo de excluir o tipo cargo
	 * @author Rafael Hosaka
	 */
	public void excluir(ActionEvent evento) {
		FacesContext faces = FacesContext.getCurrentInstance();
		this.tipoCargo = (TipoCargo) evento.getComponent().getAttributes().get("tipo");
		if(this.tipoCargoDAO.delete(this.tipoCargo,getDataPesquisa())){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Tipo de cargo excluido com sucesso!"));
			preparaAmbiente();
		}else{
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));
		}

	}

	/**
	 * Reliza o processo de alterar o tipo cargo
	 * @author Rafael Hosaka
	 */
	public void alterar() {
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean flag = true;
		if(new Util().isTipoCargoDescRepetido(this.tipoCargo)){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Tipo com essa descrição já existe!"));				
			flag = false;
		}
		if(flag){
			if(this.tipoCargoDAO.update(this.tipoCargo,getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Informação",
						"Tipo de cargo alterado com sucesso!"));
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));
			}
		}
	}
	/**
	 * redireciona para pagina onde será informado
	 * os dados para alterar o tipo cargo
	 * @author Rafael Hosaka
	 */
	public void preparaAlterarTipoCargo(ActionEvent evento){
		this.tipoCargo = (TipoCargo) evento.getComponent().getAttributes().get("tipo");
	}

	/**
	 * pesquisa os tipos cargo de acorco com a data informada
	 * @author Rafael Hosaka
	 */
	public void pesquisar(){
		lazyTiposCargos = new LazyTipoCargoModel(getDataPesquisa());
	}

	/**
	 * prepara o ambiente para trabalhar com tipo cargo
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {	
		setDataPesquisa(new Date());
		lazyTiposCargos = new LazyTipoCargoModel(getDataPesquisa());
		setCanEdit();
		this.tipoCargo = new TipoCargo();
	}

	//Getter Setter
	public TipoCargo getTipoCargo() {
		return tipoCargo;
	}

	public void setTipoCargo(TipoCargo tipoCargo) {
		this.tipoCargo = tipoCargo;
	}

	public void setCanEdit(){
		for(TipoCargo tipo:lazyTiposCargos)
			tipo.setCanEdit(tipo.getDtFimTipoCargo()==null);
	}

	public List<TipoCargo> getTiposCargos() {
		return tiposCargos;
	}

	public void setTiposCargos(List<TipoCargo> tiposCargos) {
		this.tiposCargos = tiposCargos;
	}

	public LazyTipoCargoModel getLazyTiposCargos() {
		return lazyTiposCargos;
	}

	public void setLazyTiposCargos(LazyTipoCargoModel lazyTiposCargos) {
		this.lazyTiposCargos = lazyTiposCargos;
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
