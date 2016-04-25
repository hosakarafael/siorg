package br.gov.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "alterado")
@NamedQueries({ @NamedQuery(name = Alterado.FINDENTITY, query = "Select a from Alterado a where a.idEntity = :idEntity and a.versaoEntity=:versaoEntity and a.tipoEntity=:tipo") })
public class Alterado implements Serializable{

	private static final long serialVersionUID = 4255037233749620011L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "tipo_entity")
	private char tipoEntity;

	@Column(name = "id_entity")
	private Integer idEntity;

	@Column(name = "versao_entity")
	private Integer versaoEntity;

	public static final String FINDENTITY = "Alterado.findEntity";

	public Alterado() {

	}

	public Alterado(Integer idEntity, Integer versaoEntity,
			char tipoEntity) {
		this.idEntity = idEntity;
		this.versaoEntity = versaoEntity;
		this.tipoEntity = tipoEntity;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public char getTipoEntity() {
		return tipoEntity;
	}

	public void setTipoEntity(char tipoEntity) {
		this.tipoEntity = tipoEntity;
	}

	public Integer getIdEntity() {
		return idEntity;
	}

	public void setIdEntity(Integer idEntity) {
		this.idEntity = idEntity;
	}

	public Integer getVersaoEntity() {
		return versaoEntity;
	}

	public void setVersaoEntity(Integer versaoEntity) {
		this.versaoEntity = versaoEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tipoEntity;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Alterado other = (Alterado) obj;
		if (tipoEntity != other.tipoEntity)
			return false;
		return true;
	}

}
