package br.gov.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@IdClass(value=BaseEntity.class)
@Table(name = "ORGAO")
@NamedQueries({
	@NamedQuery(name = Orgao.ALL, query = "SELECT o FROM Orgao o"),
	@NamedQuery(name = Orgao.ONLYSELECTIONABLE, query = "SELECT o FROM Orgao o where  o.dtFimOrgao is null and o.id <> :id"),
	@NamedQuery(name=Orgao.ORGAOSVAGOS,query = "SELECT o FROM Orgao o where o.id <> :id and o.dtFimOrgao is null"),
	@NamedQuery(name=Orgao.MAXVERSAO,query = "select MAX(o.versao) from Orgao o where o.id=:idparam"),
	@NamedQuery(name=Orgao.MAXID,query="select MAX(o.id) from Orgao o"),
	@NamedQuery(name=Orgao.FIND,query="select o from Orgao o where o.id = :id and o.versao = :versao"),
	@NamedQuery(name= Orgao.SEARCH,query="SELECT o FROM Orgao o "
			+ "WHERE (o.dtInicioOrgao <= :dtPesquisa and  o.dtFimOrgao is not null and  o.dtFimOrgao >= :dtPesquisa) "
			+ "or (o.dtInicioOrgao <= :dtPesquisa and o.dtFimOrgao is null)"),
			@NamedQuery(name=Orgao.HEAD,query="SELECT o FROM Orgao o WHERE o.dtFimOrgao is null"),
			@NamedQuery(name=Orgao.BYORGAO,query="SELECT o FROM Orgao o WHERE o.dtFimOrgao is null and o.superior =:orgao"),
			@NamedQuery(name = Orgao.FINDBYID, query = "SELECT o FROM Orgao o WHERE o.dtFimOrgao is null and o.id =:id"),
			@NamedQuery(name= Orgao.FINDBYIDANDDATA,query="SELECT o FROM Orgao o WHERE ((o.dtInicioOrgao <= :dtPesquisa and  o.dtFimOrgao is not null and  o.dtFimOrgao > :dtPesquisa) or (o.dtInicioOrgao <= :dtPesquisa and o.dtFimOrgao is null)) and o.id = :id"),
			@NamedQuery(name = Orgao.ORGAOSUPERIOR, query = "SELECT o FROM Orgao o WHERE o.dtFimOrgao is null and o.dtInicioOrgao is not null and o.superior is null"),
			@NamedQuery(name = Orgao.FIND_BY_NOME, query = "SELECT o FROM Orgao o WHERE o.nome =:nome "),
			@NamedQuery(name = Orgao.EM_ESPERA, query = "SELECT o FROM Orgao o WHERE o.id = 0 "),
			@NamedQuery(name = Orgao.FILTER_NOME, query = "SELECT o FROM Orgao o WHERE ((o.dtInicioOrgao <= :dtPesquisa and  o.dtFimOrgao is not null and  o.dtFimOrgao > :dtPesquisa) or (o.dtInicioOrgao <= :dtPesquisa and o.dtFimOrgao is null)) and o.nome like :param"),


})
public class Orgao extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1268200731273751676L;

	@Column(name = "NOME")
	private String nome;

	@OneToMany(mappedBy="orgao")
	private List<Servidor> servidores;

	@ManyToOne (cascade=CascadeType.PERSIST)
	@JoinColumns({@JoinColumn(name = "ID_SUPERIOR",referencedColumnName="ID"),@JoinColumn(name = "VERSAO_SUPERIOR",referencedColumnName="VERSAO")})
	private Orgao superior;

	@OneToMany (cascade=CascadeType.PERSIST)
	@JoinColumns({@JoinColumn(name = "ID_SUPERIOR",referencedColumnName="ID"),@JoinColumn(name = "VERSAO_SUPERIOR",referencedColumnName="VERSAO")})
	private List<Orgao> orgaos;

	@Temporal(TemporalType.DATE)
	@Column(name="DT_INICIO_ORGAO")
	private Date dtInicioOrgao;

	@Temporal(TemporalType.DATE)
	@Column(name="DT_FIM_ORGAO")
	private Date dtFimOrgao;

	@Transient
	private Boolean canEdit;

	//Nome das NamedQuery==================================

	//Recupera todos orgaos (Head e Passado)
	public final static String ALL = "Orgao.listarTodos";
	//Recupera todos orgaos que não seja exceto o orgao passado como id
	public final static String ONLYSELECTIONABLE = "Orgao.listaSelecionaveis";
	//Recupera a maior versao
	public final static String MAXVERSAO = "Orgao.findMaxVersao";
	//Recupera o maior id
	public final static String MAXID = "Orgao.findMaxId";
	//Recupera orgao por id e versao
	public final static String FIND = "Orgao.find";
	//Recupera os orgaos que são Head
	public final static String HEAD = "Orgao.getHeadVersions";
	//Recupera os orgaos que esteja no periodo (dtinicio < dtpesquisa =< dtfim ou dtfim = null)
	public final static String SEARCH = "Orgao.search";
	//Recupera orgaos por orgaosuperior
	public final static String BYORGAO = "Orgao.findOrgaoInferioresByOrgao";
	//Recupera orgaos que possui dtfim = null e não é o orgao passado como id
	public final static String ORGAOSVAGOS = "Orgao.findOrgaosVagos";
	//Recupera orgao por id
	public static final String FINDBYID = "Orgao.findById";
	//Recupera orgao por id e data
	public static final String FINDBYIDANDDATA = "Orgao.findByIdAndData";
	//Recupera o superior de todos orgaos
	public static final String ORGAOSUPERIOR = "Orgao.findSuperior";
	//Recupera orgao por nome
	public static final String FIND_BY_NOME = "Orgao.findByNome";
	//Recupera o orgao em_espera
	public static final String EM_ESPERA = "Orgao.emEspera";

	public static final String FILTER_NOME = "Orgao.byFilterNome";


	public Orgao(){
		servidores = new ArrayList<Servidor>();
	}



	public Orgao(Integer id,Integer versao,String nome, List<Servidor> servidores, Orgao superior,
			List<Orgao> orgaos, Date dtInicioOrgao, Date dtFimOrgao,
			Boolean canEdit) {
		setId(id);
		setVersao(versao);
		this.nome = nome;
		this.servidores = servidores;
		this.superior = superior;
		this.orgaos = orgaos;
		this.dtInicioOrgao = dtInicioOrgao;
		this.dtFimOrgao = dtFimOrgao;
		this.canEdit = canEdit;
	}



	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}

	public Orgao getSuperior() {
		return superior;
	}
	public void setSuperior(Orgao superior) {
		this.superior = superior;
	}
	public List<Orgao> getOrgaos() {
		return orgaos;
	}
	public void setOrgaos(List<Orgao> orgaos) {
		this.orgaos = orgaos;
	}
	public List<Servidor> getServidores() {
		return servidores;
	}

	public Date getDtInicioOrgao() {
		return dtInicioOrgao;
	}

	public void setDtInicioOrgao(Date dtInicioOrgao) {
		this.dtInicioOrgao = dtInicioOrgao;
	}

	public Date getDtFimOrgao() {
		return dtFimOrgao;
	}

	public void setDtFimOrgao(Date dtFimOrgao) {
		this.dtFimOrgao = dtFimOrgao;
	}

	public void setServidores(List<Servidor> servidores) {
		this.servidores = servidores;
	}

	public Boolean getCanEdit() {
		return canEdit;
	}

	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((dtFimOrgao == null) ? 0 : dtFimOrgao.hashCode());
		result = prime * result
				+ ((dtInicioOrgao == null) ? 0 : dtInicioOrgao.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Orgao other = (Orgao) obj;
		if (dtFimOrgao == null) {
			if (other.dtFimOrgao != null)
				return false;
		} else if (!dtFimOrgao.equals(other.dtFimOrgao))
			return false;
		if (dtInicioOrgao == null) {
			if (other.dtInicioOrgao != null)
				return false;
		} else if (!dtInicioOrgao.equals(other.dtInicioOrgao))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (orgaos == null) {
			if (other.orgaos != null)
				return false;
		} else if (!orgaos.equals(other.orgaos))
			return false;
		if (servidores == null) {
			if (other.servidores != null)
				return false;
		} else if (!servidores.equals(other.servidores))
			return false;
		if (superior == null) {
			if (other.superior != null)
				return false;
		} else if (!superior.equals(other.superior))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Orgao [nome=" + nome + ", servidores=" + servidores
				+ ", superior=" + superior.getNome() +", dtInicioOrgao=" + dtInicioOrgao + ", dtFimOrgao="
				+ dtFimOrgao + "]";
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<orgao>");
		sb.append("<nome>");
		sb.append(this.nome);
		sb.append("</nome>");
		sb.append("<servidores>");
		for (Servidor s : this.servidores) {
			sb.append(s.toXML(true));
		}
		sb.append("</servidores>");
		sb.append("<orgaos>");
		for (Orgao o : this.orgaos) {
			sb.append(o.toXML());
		}
		sb.append("</orgaos>");
		sb.append("</orgao>");
		return sb.toString();
	}

	public String toXML(boolean comServidor) {
		StringBuilder sb = new StringBuilder();
		sb.append("<orgao>");
		sb.append("<nome>");
		sb.append(this.nome);
		sb.append("</nome>");
		if(comServidor){
			sb.append("<servidores>");
			for (Servidor s : this.servidores) {
				sb.append(s.toXML());
			}
			sb.append("</servidores>");
		}
		if(!orgaos.isEmpty()){
			sb.append("<orgaos>");
			for (Orgao o : this.orgaos) {
				sb.append(o.toXML(comServidor));
			}
			sb.append("</orgaos>");
		}
		sb.append("</orgao>");
		return sb.toString();
	}





}
