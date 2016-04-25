package br.gov.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@IdClass(value = BaseEntity.class)
@Table(name = "TIPO_CARGO")
@NamedQueries({
		@NamedQuery(name = TipoCargo.MAXID, query = "select MAX(t.id) from TipoCargo t"),
		@NamedQuery(name = TipoCargo.ALL, query = "SELECT t FROM TipoCargo t "),
		@NamedQuery(name = TipoCargo.HEAD,query="SELECT t FROM TipoCargo t WHERE t.dtFimTipoCargo is null"),
		@NamedQuery(name = TipoCargo.FIND, query = "Select t from TipoCargo t where t.id=:id and t.versao=:versao"),
		@NamedQuery(name= TipoCargo.SEARCH,query="SELECT t FROM TipoCargo t WHERE (t.dtInicioTipoCargo <= :dtPesquisa and  t.dtFimTipoCargo is not null and  t.dtFimTipoCargo > :dtPesquisa) or (t.dtInicioTipoCargo <= :dtPesquisa and t.dtFimTipoCargo is null)"),
		@NamedQuery(name = TipoCargo.LAZY_FILTER_DESC, query = "SELECT t FROM TipoCargo t WHERE (t.dtInicioTipoCargo <= :dtPesquisa and  t.dtFimTipoCargo > :dtPesquisa) or (t.dtInicioTipoCargo <= :dtPesquisa and t.dtFimTipoCargo is null) and t.descricao like :param "),
		@NamedQuery(name= TipoCargo.COUNT_DATE,query="SELECT COUNT(t) FROM TipoCargo t WHERE ((t.dtInicioTipoCargo <= :dtPesquisa and  t.dtFimTipoCargo > :dtPesquisa) or (t.dtInicioTipoCargo <= :dtPesquisa and t.dtFimTipoCargo is null))"),
		@NamedQuery(name= TipoCargo.COUNT_DATE_FILTER_DESC,query="SELECT COUNT(t) FROM TipoCargo t WHERE (t.dtInicioTipoCargo <= :dtPesquisa and  t.dtFimTipoCargo > :dtPesquisa) or (t.dtInicioTipoCargo <= :dtPesquisa and t.dtFimTipoCargo is null) and t.descricao like :param")

})
public class TipoCargo extends BaseEntity implements Serializable {
	private static final long serialVersionUID = -39392571405396614L;

	@Column(name = "DESCRICAO")
	private String descricao;

	@Temporal(TemporalType.DATE)
	@Column(name = "DT_INICIO_TIPOCARGO")
	private Date dtInicioTipoCargo;

	@Temporal(TemporalType.DATE)
	@Column(name = "DT_FIM_TIPOCARGO")
	private Date dtFimTipoCargo;

	@Transient
	private Boolean canEdit;
	
	//Recupera todos tipocargos (head e passado)
	public final static String ALL = "TipoCargo.listarTodos";
	//Recupera o maior id
	public final static String MAXID = "TipoCargo.findMaxId";
	//Recupera o TipoCargo por id e versao
	public final static String FIND = "TipoCargo.find";
	//Recupera todos cargos que são head
	public final static String HEAD = "TipoCargo.head";
	//Recupera os cargos que esteja no periodo (dtinicio < dtpesquisa =< dtfim ou dtfim = null)
	public final static String SEARCH = "TipoCargo.search";
	public final static String COUNT_DATE = "TipoCargo.countByDate";
	public final static String LAZY_FILTER_DESC = "TipoCargo.lazyFilterDescricao";
	public final static String COUNT_DATE_FILTER_DESC = "TipoCargo.countByDateAndFilterDesc";
	
	
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getDtInicioTipoCargo() {
		return dtInicioTipoCargo;
	}

	public void setDtInicioTipoCargo(Date dtInicioTipoCargo) {
		this.dtInicioTipoCargo = dtInicioTipoCargo;
	}

	public Date getDtFimTipoCargo() {
		return dtFimTipoCargo;
	}

	public void setDtFimTipoCargo(Date dtFimTipoCargo) {
		this.dtFimTipoCargo = dtFimTipoCargo;
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
				+ ((descricao == null) ? 0 : descricao.hashCode());
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
		TipoCargo other = (TipoCargo) obj;
		if (descricao == null) {
			if (other.descricao != null)
				return false;
		} else if (!descricao.equals(other.descricao))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ID = " + getId() + "___VERSÃO = " + getVersao()
				+ "___DESCRIÇÃO = " + getDescricao();
	}

	@Override
	public String toXML() {
		return null;
	}


}
