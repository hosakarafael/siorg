package br.gov.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface DaoInterface<T,PK extends Serializable> {
	public boolean create(T entity,Date data);
	public T find(PK id,PK versao);
	public List<T> findWithNamedQuery(String namedQueryName);
	public boolean delete(T entity,Date data);
	public boolean update(T entity,Date data);
	public PK findMaxId() throws SQLException, ClassNotFoundException;
	public void preparaVersao(T entity,Date data);
	public void configuraUpdate(T entity,Date data);
	
}
