package br.gov.factory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ConnectionFactory implements Serializable{
	private static final long serialVersionUID = 1L;

	private static final String UNIT = "JPA_UNIT";
	private static EntityManagerFactory emF = null;
	private static EntityManager em = null;
	
	/**
	 * Cria a conexão com o banco
	 * @return EntityManager
	 * @author Rafael Hosaka
	 */
	public static EntityManager getEntityManager() {
		try {
			if (emF == null)
				emF = Persistence.createEntityManagerFactory(UNIT);
		
			if (em == null)
				em = emF.createEntityManager();
		} catch (Exception e){
			e.printStackTrace();
		}
		return em;
	}
	
	public void dispose(EntityManager em){
		em.close();
	}

	public static java.sql.Connection getConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");
		return DriverManager.getConnection("jdbc:postgresql://localhost:5432/siorg","postgres","root");
	}
	
	public static void close(PreparedStatement st,Connection con) throws SQLException{
		st.close();
		con.close();
	}
}