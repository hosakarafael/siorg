package br.gov.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.entity.Autorizacao;
import br.gov.entity.Usuario;
import br.gov.factory.ConnectionFactory;

public class PerfilDAO{

	private EntityManager em;

	public PerfilDAO() {
		em = ConnectionFactory.getEntityManager();

	}

	/**
	 * Atualiza o usuario
	 * @param usuario 
	 * @return true se conseguiu atualizar
	 *         false se houve algum erro
	 * @author Rafael Hosaka
	 */
	public synchronized boolean update(Usuario usuario) {
		try{
			em.getTransaction().begin();
			this.em.merge(usuario);
			em.getTransaction().commit();
		}catch(Exception e){
			return false;
		}
		return true;
	}

	/**
	 * Cria um novo usuario e autorizacao (JAAS)
	 * @param usuario
	 * @param autorizacao
	 * @author Rafael Hosaka
	 */
	public synchronized void create(Usuario usuario,Autorizacao autorizacao) {
			em.getTransaction().begin();
		this.em.persist(usuario);
		this.em.persist(autorizacao);
		em.getTransaction().commit();
	}
	
	/**
	 * Atualiza o usuario e autorizacao (JAAS)
	 * @param usuario
	 * @param autorizacao
	 * @author Rafael Hosaka
	 */
	public synchronized void update(Usuario usuario,Autorizacao autorizacao) {
		em.getTransaction().begin();
		this.em.merge(usuario);
		this.em.merge(autorizacao);
		em.getTransaction().commit();
	}
	
	/**
	 * Remove o usuario e autorizacao (JAAS)
	 * @param usuario
	 * @param autorizacao
	 * @author Rafael Hosaka
	 */
	public void delete(Usuario usuario,Autorizacao autorizacao) {
		em.getTransaction().begin();
		em.remove(usuario);
		em.remove(autorizacao);
		em.getTransaction().commit();
	}
	
	/**
	 * Recupera o usuario por matricula
	 * @param matricula
	 * @return O usuario recuperado
	 * @author Rafael Hosaka
	 */
	public Usuario findUsuarioByMatricula(String matricula){
		Usuario usuario = null;
		try{
			Query q = em.createNamedQuery(Usuario.FINDBYMAT).setParameter("mat", matricula);
			usuario = (Usuario) q.getSingleResult();
		}catch(NoResultException e){
			System.out.println("Nenhum usuário encontrado com a matrícula "+matricula+"...");
			//e.printStackTrace();
			return usuario;
		}
		return usuario;
	}
	
	/**
	 * Recupera autorizacao por matricula
	 * @param matricula
	 * @return Autorizacao recuperada
	 * @author Rafael Hosaka
	 */
	public Autorizacao findAutorizacaoByMatricula(String matricula){
		Query q = em.createNamedQuery(Autorizacao.FINDBYMAT).setParameter("mat", matricula);
		return (Autorizacao) q.getSingleResult();
	}
	
	/**
	 * recupera autorizacaoes que são adm
	 * @return Lista de adms
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Autorizacao> findAutorizacoesAdm(){
		Query q = em.createNamedQuery(Autorizacao.FINDADM);
		return q.getResultList();
	}
}
