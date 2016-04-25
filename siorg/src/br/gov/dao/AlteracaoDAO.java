package br.gov.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.entity.Alterado;
import br.gov.entity.BaseEntity;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.factory.ConnectionFactory;

public class AlteracaoDAO {

	private EntityManager em;

	public AlteracaoDAO() {
		this.em = ConnectionFactory.getEntityManager();
	}

	public void persistAlterado(Alterado alterado) {
		em.getTransaction().begin();
		em.persist(alterado);
		em.getTransaction().commit();
	}

	public boolean entityExists(BaseEntity entity, char tipo) {
		return (findEntity(entity, tipo) == null ? false : true);
	}

	public Alterado findEntity(BaseEntity entity, char tipo) {
		Alterado alterado = null;
		try {
			Query query = em.createNamedQuery(Alterado.FINDENTITY)
					.setParameter("idEntity", entity.getId())
					.setParameter("versaoEntity", entity.getVersao())
					.setParameter("tipo", tipo);
			alterado = (Alterado) query.getSingleResult();
		} catch (NoResultException e) {

		}
		return alterado;
	}

	public Alterado createAlterado(BaseEntity entity, char tipoEntity) {
		Alterado alterado = new Alterado(entity.getId(), entity.getVersao(),
				tipoEntity);
		return persist(alterado);
	}
	
	public Alterado createAlteradoServidor(Servidor entity) {
		Alterado alterado = new Alterado(entity.getId(), entity.getVersao(),
				'S');
		return alterado;
	}

	public Alterado createAlteradoOrgao(Orgao entity) {
		Alterado alterado = new Alterado(entity.getId(), entity.getVersao(),
				'O');
		return alterado;
	}
	
	public Alterado persist(Alterado entity){
		em.getTransaction().begin();
		em.persist(entity);
		em.getTransaction().commit();
		return entity;
	}

}
