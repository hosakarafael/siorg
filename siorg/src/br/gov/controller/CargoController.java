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

import br.gov.dao.CargoDAO;
import br.gov.dao.TipoCargoDAO;
import br.gov.entity.Cargo;
import br.gov.entity.TipoCargo;
import br.gov.lazymodel.LazyCargoModel;
import br.gov.util.Util;

@ManagedBean(name = "cargoController")
@ViewScoped
public class CargoController implements Serializable{

	private static final long serialVersionUID = 9116440992602983256L;

	private Cargo cargo;
	private List<Cargo> cargos = null;
	private CargoDAO cargoDao;
	private List<TipoCargo> tiposCargo = null;
	private TipoCargoDAO tipoCargoDao;
	private LazyCargoModel lazyCargos;

	private Date dataPesquisa;

	public CargoController(){
		this.cargo = new Cargo();
		this.cargoDao = new CargoDAO();
		this.tipoCargoDao = new TipoCargoDAO();
	}

	/**
	 * Inicializa a managed bean que trabalhará com
	 * a entidade Cargo
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
	 * Realiza o processo de cadastrar o cargo
	 * @author Rafael Hosaka
	 */
	public void cadastrar(){
		FacesContext faces = FacesContext.getCurrentInstance();		
		boolean flag =true;
		if(new Util().isCargoNomeRepetido(this.cargo)){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Erro", "Cargo com esse nome já existe!"));	
			flag = false;
		}
		if(flag){
			if(this.cargoDao.create(this.cargo,this.getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Informação", "Cargo inserido com sucesso!"));		
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Erro", "Houve erro no cadastro!"));		
			}
		}

	}

	/**
	 * Realiza o processo de excluir o cargo
	 * @author Rafael Hosaka
	 */
	public void excluir(ActionEvent evento) {
		FacesContext faces = FacesContext.getCurrentInstance();
		this.cargo = (Cargo) evento.getComponent().getAttributes().get("cargo");
		this.cargoDao.atualizaRelacaoExclusaoServidorCargo(this.cargo,this.getDataPesquisa());
		if(this.cargoDao.delete(this.cargo,this.getDataPesquisa())){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Cargo excluido com sucesso!"));
			preparaAmbiente();
		}else{
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Cargo excluido com sucesso!"));

		}

	}

	/**
	 * Reliza o processo de alterar o cargo
	 * @author Rafael Hosaka
	 */
	public void alterar(){
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean flag =true;
		if(new Util().isCargoNomeRepetido(this.cargo)){
			faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Erro", "Cargo com esse nome já existe!"));	
			flag = false;
		}
		if(flag){
			if(this.cargoDao.update(this.cargo,getDataPesquisa())){
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Informação","Cargo alterado com sucesso!"));
				preparaAmbiente();
			}else{
				faces.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro","Ocorreu um erro no servidor, contate o administrador."));
			}
		}

	}


	/**
	 * redireciona para pagina onde será informado
	 * os dados para alterar o cargo
	 * @author Rafael Hosaka
	 */
	public void preparaAlterarCargo(ActionEvent evento){
		this.cargo = (Cargo) evento.getComponent().getAttributes().get("cargo");
	}

	/**
	 * pesquisa os cargos de acorco com a data informada
	 * @author Rafael Hosaka
	 */
	public void pesquisar(){
		lazyCargos = new LazyCargoModel(getDataPesquisa());
	}

	/**
	 * prepara o ambiente para trabalhar com cargo
	 * @author Rafael Hosaka
	 */
	private void preparaAmbiente() {
		this.cargo = new Cargo();
		setDataPesquisa(new Date());
		lazyCargos = new LazyCargoModel(getDataPesquisa());
		this.setTiposCargo(this.tipoCargoDao.findWithNamedQuery(TipoCargo.HEAD));
	}

	//Getters Setters
	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public List<Cargo> getCargos() {
		return cargos;
	}

	public void setCargos(List<Cargo> cargos) {
		this.cargos = cargos;
	}

	public List<TipoCargo> getTiposCargo() {
		return tiposCargo;
	}

	public void setTiposCargo(List<TipoCargo> tiposCargo) {
		this.tiposCargo = tiposCargo;
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

	public LazyCargoModel getLazyCargos() {
		return lazyCargos;
	}

	public void setLazyCargos(LazyCargoModel lazyCargos) {
		this.lazyCargos = lazyCargos;
	}

	public Date getDataPesquisa() {
		return dataPesquisa;
	}

	public void setDataPesquisa(Date dataPesquisa) {
		this.dataPesquisa = dataPesquisa;
	}	

}
