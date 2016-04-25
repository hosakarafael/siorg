package br.gov.entity;

import java.io.Serializable;
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


@Entity
@IdClass(value=BaseEntity.class)
@Table(name = "CARGO")
@NamedQueries({
	@NamedQuery(name = Cargo.ALL, query = "SELECT c FROM Cargo c"),
	@NamedQuery(name=Cargo.MAXVERSAO,query = "select MAX(c.versao) from Cargo c where c.id=:idparam"),
	@NamedQuery(name=Cargo.MAXID,query="select MAX(c.id) from Cargo c"),
	@NamedQuery(name=Cargo.FIND,query="select c from Cargo c where c.id = :id and c.versao = :versao"),
	@NamedQuery(name= Cargo.SEARCH,query="SELECT c FROM Cargo c WHERE "
			+ "(c.dtInicioCargo <= :dtPesquisa and  c.dtFimCargo > :dtPesquisa)"
			+ " or (c.dtInicioCargo <= :dtPesquisa and c.dtFimCargo is null)"),
	@NamedQuery(name = Cargo.HEAD,query="SELECT c FROM Cargo c WHERE c.dtFimCargo is null"),
	@NamedQuery(name=Cargo.FINDBYTIPOCARGO,query="SELECT c from Cargo c where c.tipoCargo.id = :id and c.tipoCargo.versao=:versao and c.dtFimCargo is null"),
	@NamedQuery(name = Cargo.FIND_BY_NOME, query = "SELECT c FROM Cargo c WHERE c.nome =:nome "),
	@NamedQuery(name = Cargo.LAZY_FILTER_NOME, query = "SELECT c FROM Cargo c WHERE (c.dtInicioCargo <= :dtPesquisa and  c.dtFimCargo > :dtPesquisa) or (c.dtInicioCargo <= :dtPesquisa and c.dtFimCargo is null) and c.nome like :param "),
	@NamedQuery(name= Cargo.COUNT_DATE,query="SELECT COUNT(c) FROM Cargo c WHERE (c.dtInicioCargo <= :dtPesquisa and  c.dtFimCargo > :dtPesquisa) or (c.dtInicioCargo <= :dtPesquisa and c.dtFimCargo is null)"),
	@NamedQuery(name= Cargo.COUNT_DATE_FILTER_NOME,query="SELECT COUNT(c) FROM Cargo c WHERE (c.dtInicioCargo <= :dtPesquisa and  c.dtFimCargo > :dtPesquisa) or (c.dtInicioCargo <= :dtPesquisa and c.dtFimCargo is null) and c.nome like :param")

})
public class Cargo extends BaseEntity implements Serializable{
	private static final long serialVersionUID = -5322023693795006949L;

	@Column(name = "NOME")
	private String nome;

	@ManyToOne
	@JoinColumns({@JoinColumn(name="ID_TIPO_CARGO",referencedColumnName="ID"),@JoinColumn(name="VERSAO_TIPOCARGO",referencedColumnName="VERSAO")})
	private TipoCargo tipoCargo;

	@Temporal(TemporalType.DATE)
	@Column(name = "DT_INICIO_CARGO")
	private Date dtInicioCargo;

	@Temporal(TemporalType.DATE)
	@Column(name = "DT_FIM_CARGO")
	private Date dtFimCargo;

	@Transient
	private Boolean canEdit;

	//Recupera a maior versao de um cargo
	public final static String MAXVERSAO = "Cargo.findMaxVersao";
	//Recupera o maior id
	public final static String MAXID = "Cargo.findMaxId";
	//Recupera o Cargo por id e versao
	public final static String FIND = "Cargo.findById";
	//Recupera todos cargos (head e passado)
	public final static String ALL = "Cargo.listarTodos";
	//Recupera os cargos que esteja no periodo (dtinicio < dtpesquisa =< dtfim ou dtfim = null)
	public final static String SEARCH = "Cargo.search";
	//Recupera todos cargos que são head
	public final static String HEAD = "Cargo.head";
	//Recupera cargo por tipo de cargo
	public final static String FINDBYTIPOCARGO = "Cargo.findByTipoCargo";
	//Recupera Cargo por nome
	public static final String FIND_BY_NOME = "Cargo.findByNome";
	public final static String COUNT_DATE = "Cargo.countByDate";
	public final static String LAZY_FILTER_NOME = "Cargo.lazyFilterNome";
	public final static String COUNT_DATE_FILTER_NOME = "Cargo.countByDateAndFilterNome";
	
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public TipoCargo getTipoCargo() {
		return tipoCargo;
	}

	public void setTipoCargo(TipoCargo tipoCargo) {
		this.tipoCargo = tipoCargo;
	}

	public Date getDtInicioCargo() {
		return dtInicioCargo;
	}

	public void setDtInicioCargo(Date dtInicioCargo) {
		this.dtInicioCargo = dtInicioCargo;
	}

	public Date getDtFimCargo() {
		return dtFimCargo;
	}

	public void setDtFimCargo(Date dtFimCargo) {
		this.dtFimCargo = dtFimCargo;
	}

	public Boolean getCanEdit() {
		return canEdit;
	}

	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
	}

	@Override
	public String toXML() {

		StringBuilder sb = new StringBuilder();
		sb.append("<cargo>");
		sb.append("<nome>");
		sb.append(this.nome);
		sb.append("</nome>");
		sb.append("<tipoCargo>");
		sb.append(this.tipoCargo.getDescricao());
		sb.append("</tipoCargo>");
		sb.append("</cargo>");
		return sb.toString();
	}

}
