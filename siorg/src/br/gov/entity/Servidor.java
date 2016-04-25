package br.gov.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement
@XmlSeeAlso({BaseEntity.class})
@Entity 
@IdClass(value = BaseEntity.class)
@Table(name="SERVIDOR")
@NamedQueries({
	@NamedQuery(name = Servidor.ALL, query = "SELECT s FROM Servidor s "),
	@NamedQuery(name=Servidor.MAXVERSAO,query = "select MAX(s.versao) from Servidor s where s.id=:idparam"),
	@NamedQuery(name=Servidor.MAXID,query="select MAX(s.id) from Servidor s"),
	@NamedQuery(name=Servidor.FIND,query="select s from Servidor s where s.id = :id and s.versao = :versao"),
	@NamedQuery(name=Servidor.HEAD,query="SELECT s FROM Servidor s WHERE s.dtFimServidor is null"),
	@NamedQuery(name=Servidor.BY_ORGAO,query="SELECT s FROM Servidor s WHERE  s.orgao=:orgao"),
	@NamedQuery(name=Servidor.ONLYSELECTIONABLE,query="SELECT s FROM Servidor s WHERE s.dtFimServidor is null and s.orgao=:orgao"),
	@NamedQuery(name= Servidor.SEARCH,query="SELECT s FROM Servidor s WHERE "
			+ "(s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa) "
			+ "or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null)"),
	@NamedQuery(name=Servidor.BYMATRICULA,query="SELECT s FROM Servidor s WHERE s.matricula=:matricula and s.dtFimServidor is null"),
	@NamedQuery(name=Servidor.FINDBYCARGO,query="SELECT s FROM Servidor s WHERE s.cargo.id=:id and s.cargo.versao=:versao and s.dtFimServidor is null"),
	@NamedQuery(name=Servidor.ALLMAT,query="SELECT s.matricula FROM Servidor s"),
	@NamedQuery(name=Servidor.BY_ORGAO_AND_DATA_COM_CARGO,query="SELECT s FROM Servidor s WHERE s.orgao =:orgao and s.cargo is not null and ((s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa)"
			+ " or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null))"),
	@NamedQuery(name=Servidor.BY_ORGAO_AND_DATA_SEM_CARGO,query="SELECT s FROM Servidor s WHERE s.orgao =:orgao and s.cargo is null and ((s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa)"
			+ " or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null))"),
	@NamedQuery(name=Servidor.BY_ORGAO_AND_DATE,query="SELECT s FROM Servidor s WHERE  s.orgao=:orgao and "
			+ "((s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa)"
			+ " or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null))"),
	@NamedQuery(name = Servidor.BY_ID, query = "SELECT s FROM Servidor s WHERE s.id = :id"),
	@NamedQuery(name = Servidor.LAZY_FILTER_MAT, query = "SELECT s FROM Servidor s WHERE (s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa) or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null) and s.matricula like :param "),
	@NamedQuery(name = Servidor.LAZY_FILTER_NOME, query = "SELECT s FROM Servidor s WHERE (s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa) or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null) and s.nome like :param "),
	@NamedQuery(name= Servidor.COUNT_DATE,query="SELECT COUNT(s) FROM Servidor s WHERE ((s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa) or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null))"),
	@NamedQuery(name= Servidor.COUNT_DATE_FILTER_MAT,query="SELECT COUNT(s) FROM Servidor s WHERE (s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa) or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null) and s.matricula like :param"),
	@NamedQuery(name= Servidor.COUNT_DATE_FILTER_NOME,query="SELECT COUNT(s) FROM Servidor s WHERE (s.dtInicioServidor <= :dtPesquisa and  s.dtFimServidor > :dtPesquisa) or (s.dtInicioServidor <= :dtPesquisa and s.dtFimServidor is null) and s.nome like :param")

})
public class Servidor extends BaseEntity implements Serializable{

	private static final long serialVersionUID = -2156105732787243698L;

	@Column(name="NOME")
	private String nome;

	@Column(name="MATRICULA")
	private String matricula;
	
	@Column(name="EFETIVO")
	private Boolean efetivo;
	
	@ManyToOne
	@JoinColumns({@JoinColumn(name="CARGO_ID",referencedColumnName="ID"),@JoinColumn(name="VERSAO_CARGO",referencedColumnName="VERSAO")})
	private Cargo cargo;

	@ManyToOne
	@JoinColumns({@JoinColumn(name="ORGAO_ID",referencedColumnName="id"),@JoinColumn(name="VERSAO_ORGAO",referencedColumnName="VERSAO")})
	private Orgao orgao;
	
	@Temporal(TemporalType.DATE)
	@Column(name="DT_INICIO_SERVIDOR")
	private Date dtInicioServidor;

	@Temporal(TemporalType.DATE)
	@Column(name="DT_FIM_SERVIDOR")
	private Date dtFimServidor;
	
	@Transient
	private Boolean canEdit;
	
	//Recupera todos servidores (head e passado)
	public final static String ALL = "Servidor.listarTodos";
	//Recupera a maior versao de um servidor
	public final static String MAXVERSAO = "Servidor.findMaxVersao";
	//Recupera o maior id
	public final static String MAXID = "Servidor.findMaxId";
	//Recupera o Servidor por id e versao
	public final static String FIND = "Servidor.findById";
	//Recupera todos servidores que são head
	public final static String HEAD = "Servidor.getHeadVersions";
	//Recupera Servidor por orgao
	public final static String BY_ORGAO = "Servidor.findServidorByOrgao";
	public final static String ONLYSELECTIONABLE = "Servidor.listaSelecionaveis";
	//Recupera os servidores que esteja no periodo (dtinicio < dtpesquisa =< dtfim ou dtfim = null)
	public final static String SEARCH = "Servidor.search";
	//Recupera servidor por matricula head
	public final static String BYMATRICULA = "Servidor.findByMatricula";
	//Recupera servidor por cargo
	public final static String FINDBYCARGO = "Servidor.findByCargo";
	//Recupera todas matriculas
	public final static String ALLMAT = "Servidor.todasMatriculas";
	//Recupera servidores por orgao e data, e que não possui cargo
	public final static String BY_ORGAO_AND_DATA_SEM_CARGO = "Servidor.byOrgaoAndDataSemCargo";
	//Recupera servidores por orgao data, e que possui cargo
	public final static String BY_ORGAO_AND_DATA_COM_CARGO = "Servidor.byOrgaoAndDataComCargo";
	//Recupera servidores por orgao e data
	public final static String BY_ORGAO_AND_DATE = "Servidor.findServidorByOrgaoAndDate";
	//Recupera servidores por id (recupera todas versoes)
	public final static String BY_ID = "Servidor.findByID";
	public final static String COUNT_DATE = "Servidor.countByDate";
	public final static String LAZY_FILTER_MAT = "Servidor.lazyFilterMatricula";
	public final static String COUNT_DATE_FILTER_MAT = "Servidor.countByDateAndFilterMat";
	
	public final static String LAZY_FILTER_NOME = "Servidor.lazyFilterNome";
	public final static String COUNT_DATE_FILTER_NOME = "Servidor.countByDateAndFilterNome";
	
	public Servidor(){
		
	}
	
	public Servidor(String nome, String matricula,Boolean efetivo,Cargo cargo, Orgao orgao, Date dtInicioServidor,Date dtFimServidor){
		setNome(nome);
		setMatricula(matricula);
		setEfetivo(efetivo);
		setCargo(cargo);
		setOrgao(orgao);
		setDtInicioServidor(dtInicioServidor);
		setDtFimServidor(dtFimServidor);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public Boolean getEfetivo() {
		return efetivo;
	}

	public void setEfetivo(Boolean efetivo) {
		this.efetivo = efetivo;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public Orgao getOrgao() {
		return orgao;
	}

	public void setOrgao(Orgao orgao) {
		this.orgao = orgao;
	}

	public Date getDtInicioServidor() {
		return dtInicioServidor;
	}

	public void setDtInicioServidor(Date dtInicioServidor) {
		this.dtInicioServidor = dtInicioServidor;
	}

	public Date getDtFimServidor() {
		return dtFimServidor;
	}

	public void setDtFimServidor(Date dtFimServidor) {
		this.dtFimServidor = dtFimServidor;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Boolean getCanEdit() {
		return canEdit;
	}

	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
	}
	
	public String getDataInicioFormatada(){
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		return sd.format(dtInicioServidor);
	}
	
	public String getDataFimFormatada(){
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		return dtFimServidor==null?"":sd.format(dtFimServidor);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cargo == null) ? 0 : cargo.hashCode());
	
		result = prime * result
				+ ((dtFimServidor == null) ? 0 : dtFimServidor.hashCode());
		result = prime
				* result
				+ ((dtInicioServidor == null) ? 0 : dtInicioServidor.hashCode());
		result = prime * result + ((efetivo == null) ? 0 : efetivo.hashCode());
		result = prime * result
				+ ((matricula == null) ? 0 : matricula.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result + ((orgao == null) ? 0 : orgao.hashCode());
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
		Servidor other = (Servidor) obj;
		if (cargo == null) {
			if (other.cargo != null)
				return false;
		} else if (!cargo.equals(other.cargo))
			return false;
		if (dtFimServidor == null) {
			if (other.dtFimServidor != null)
				return false;
		} else if (!dtFimServidor.equals(other.dtFimServidor))
			return false;
		if (dtInicioServidor == null) {
			if (other.dtInicioServidor != null)
				return false;
		} else if (!dtInicioServidor.equals(other.dtInicioServidor))
			return false;
		if (efetivo == null) {
			if (other.efetivo != null)
				return false;
		} else if (!efetivo.equals(other.efetivo))
			return false;
		if (matricula == null) {
			if (other.matricula != null)
				return false;
		} else if (!matricula.equals(other.matricula))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (orgao == null) {
			if (other.orgao != null)
				return false;
		} else if (!orgao.equals(other.orgao))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Servidor [nome=" + nome + ", matricula=" + matricula
				+ ", efetivo=" + efetivo + ", cargo=" + cargo 
				+ ", dtInicioServidor=" + dtInicioServidor + ", dtFimServidor="
				+ dtFimServidor + "]";
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<servidor>");
			sb.append("<nome>");
				sb.append(this.nome);
			sb.append("</nome>");
			sb.append("<efetivo>");
				sb.append(this.efetivo);
			sb.append("</efetivo>");
			if(this.getOrgao()!=null){
				sb.append("<orgao>");
					sb.append(this.orgao.getNome());
				sb.append("</orgao>");	
			}
			sb.append("<matricula>");
				sb.append(this.matricula);
			sb.append("</matricula>");
			sb.append("<dtInicioServidor>");
				sb.append(this.getDataInicioFormatada());
			sb.append("</dtInicioServidor>");
			if(this.dtFimServidor!=null){
				sb.append("<dtFimServidor>");
				sb.append(this.getDataFimFormatada());
				sb.append("</dtFimServidor>");
			}
			if(this.cargo!=null){
				sb.append("<cargo>");
				sb.append(this.cargo.getNome());
				sb.append("</cargo>");
			}
		sb.append("</servidor>");
		return sb.toString();
	}

	public Object toXML(boolean comCargo) {
		StringBuilder sb = new StringBuilder();
		sb.append("<servidor>");
			sb.append("<nome>");
				sb.append(this.nome);
			sb.append("</nome>");
			sb.append("<efetivo>");
				sb.append(this.efetivo);
			sb.append("</efetivo>");
			if(this.getOrgao()!=null){
				sb.append("<orgao>");
					sb.append(this.orgao.getNome());
				sb.append("</orgao>");	
			}
			sb.append("<matricula>");
				sb.append(this.matricula);
			sb.append("</matricula>");
			sb.append("<dtInicioServidor>");
				sb.append(this.getDataInicioFormatada());
			sb.append("</dtInicioServidor>");
			if(this.dtFimServidor!=null){
				sb.append("<dtFimServidor>");
				sb.append(this.getDataFimFormatada());
				sb.append("</dtFimServidor>");
			}
			if(this.cargo!=null){
				sb.append("<cargo>");
				sb.append(this.cargo.getNome());
				sb.append("</cargo>");
			}
		sb.append("</servidor>");
		return sb.toString();
	}

	

	
}
