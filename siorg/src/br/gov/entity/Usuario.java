package br.gov.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="USUARIO")
@NamedQueries({
	@NamedQuery(name = Usuario.FINDBYMAT, query = "select u from Usuario u where u.matricula =:mat")
})
public class Usuario implements Serializable{

	private static final long serialVersionUID = -2517909653892323925L;
	
	@Id
	@Column(name="MATRICULA")
	private String matricula;
	@Column(name="SENHA")
	private String senha;
	
	@OneToOne
	@JoinColumns({@JoinColumn(name="id_servidor",referencedColumnName="id"),@JoinColumn(name="versao_servidor",referencedColumnName="versao")})
	private Servidor servidor;
	
	public Usuario(){
		
	}
	
	//senha padrão
	public static final String DEFAULT_PW = "1234";
	public static final String ADM = "adm";
	//Recupera usuario por matricula
	public static final String FINDBYMAT = "Usuario.findByMatricula";
	
	public Usuario(String matricula, String senha,Servidor servidor){
		setMatricula(matricula);
		setSenha(senha);
		setServidor(servidor);
	}
	
	public String getMatricula() {
		return matricula;
	}
	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}

	public Servidor getServidor() {
		return servidor;
	}

	public void setServidor(Servidor servidor) {
		this.servidor = servidor;
	}

}
