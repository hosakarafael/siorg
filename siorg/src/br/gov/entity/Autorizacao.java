package br.gov.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="AUTORIZACAO")
@NamedQueries({
	@NamedQuery(name = Autorizacao.FINDADM, query = "select a from Autorizacao a where a.papel = 'adm'"),
	@NamedQuery(name = Autorizacao.FINDBYMAT, query = "select a from Autorizacao a where a.usuario.matricula =:mat")
})
public class Autorizacao implements Serializable {

	private static final long serialVersionUID = 6948487772835464164L;

	@Id	
	@OneToOne
	@JoinColumn(name="usuario",referencedColumnName="matricula")
	private Usuario usuario;
	
	@Column(name="PAPEL")
	private String papel;
	
	//Recupera autorizacao que é adm
	public static final String FINDADM = "Autorizacao.findAdms";
	//Recupera autorizacao por matricula
	public static final String FINDBYMAT = "Autorizacao.findByMatricula";
	
	public Autorizacao(){
		
	}
	
	public Autorizacao(Usuario usuario, String papel) {
		super();
		this.usuario = usuario;
		this.papel = papel;
	}

	public String getPapel() {
		return papel;
	}
	public void setPapel(String papel) {
		this.papel = papel;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	
}
