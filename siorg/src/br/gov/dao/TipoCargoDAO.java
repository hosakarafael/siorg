package br.gov.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.entity.Alterado;
import br.gov.entity.Cargo;
import br.gov.entity.TipoCargo;
import br.gov.factory.ConnectionFactory;

@Stateless
@Local(DaoInterface.class)
public class TipoCargoDAO implements DaoInterface<TipoCargo, Integer> {

	private EntityManager em;
	private List<String> listMudancas;

	public TipoCargoDAO() {
		em = ConnectionFactory.getEntityManager();
		listMudancas = new ArrayList<String>();
	}

	/**
	 * Configura o TipoCargo incrementado o id e prepara a versão dele
	 * @param tipoCargo o TipoCargo que será cadastrado
	 * @param data data que será cadastrado o orgao
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author Paulo Roberto
	 */
	public void configuraTipoCargoCadastro(TipoCargo tipoCargo, Date data)
			throws ClassNotFoundException, SQLException {
		tipoCargo.setId(findMaxId() + 1);
		preparaVersao(tipoCargo, data);
	}

	/**
	 * Cadastra o TipoCargo em uma Data
	 * @param entity tipocargo à ser cadastrado
	 * @param data data que o tipocargo será cadastrado 
	 * @return  true se sucesso
	 *         false se houve erro 
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean create(TipoCargo entity, Date data) {
		try {
			configuraTipoCargoCadastro(entity, data);
			em.getTransaction().begin();
			this.em.persist(entity);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			return false;
		} finally {
			em.getTransaction().commit();
		}
		return true;
	}
	/**
	 * Recupera TipoCargo por id e versao
	 * @param id 
	 * @param versao
	 * @return TipoCargo encontrado
	 * @author Paulo Roberto
	 */
	@Override
	public TipoCargo find(Integer id, Integer versao) {
		TipoCargo cargo = null;
		try {
			Query query = em.createNamedQuery(TipoCargo.FIND)
					.setParameter("id", id).setParameter("versao", versao);
			cargo = (TipoCargo) query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return cargo;
	}

	/**
	 * Realiza uma busca atravez da NamedQuery
	 * @param namedQueryName a NamedQuery que será invocada 
	 * @return List<TipoCargo> resultado da busca
	 * @author Rafael Hosaka
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TipoCargo> findWithNamedQuery(String namedQueryName) {
		Query query = this.em.createNamedQuery(namedQueryName);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Fecha o periodo do tipocargo atribuindo uma data fim
	 * @param tipoCargo
	 * @param data
	 * @return true se sucesso
	 *         false se houve erro
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean delete(TipoCargo tipoCargo, Date data) {
		try {
			tipoCargo.setDtFimTipoCargo(data);
			merge(tipoCargo);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Atualiza o tipocargo com suas devidas configuracoes
	 * @param tipo TipoCargo que sera alterado
	 * @param data data que sera alterado
	 * @author Paulo Roberto
	 */
	@Override
	public synchronized boolean update(TipoCargo tipo, Date data) {
		try{
			AlteracaoDAO altDao = new AlteracaoDAO();
			detach(tipo);
			TipoCargo tipoOld = find(tipo.getId(), tipo.getVersao());
			detach(tipoOld);
			boolean alterado = false;
			if (!altDao.entityExists(tipo, 'T')) {
				alterado = verificaMudanca(tipoOld, tipo);
				if (alterado)
					configuraUpdate(tipo, data);
			}
			merge(tipo);
			if(alterado)
				atualizaCargos(tipo,tipoOld, data);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Verifica se o registro na memoria está diferente do banco
	 * @param bd TipoCargo no banco de dados
	 * @param obj TipoCargo na memoria
	 * @return true se tiver alterado
	 *         false se nao tiver alterado
	 * @author Paulo Roberto        
	 */
	public boolean verificaMudanca(TipoCargo tipoCargo, TipoCargo head) {
		boolean retorno = false;
		if (!tipoCargo.getDescricao().equalsIgnoreCase(head.getDescricao())) {
			listMudancas.add("descricao");
			retorno = true;
		}
		return retorno;
	}

	/**
	 * Configura o TipoCargo fechando o periodo do cargo antigo
	 * e abrindo novo periodo para o tipocargo
	 * @param tipoCargo o TipoCargo que será cadastrado
	 * @param data data que será cadastrado o tipocargo
	 * @author Paulo Roberto
	 */
	@Override
	public void configuraUpdate(TipoCargo tipoCargo, Date data) {
		atualizaTipoCargoAntigo(tipoCargo, data);
		preparaVersao(tipoCargo, data);
	}

	/**
	 * Atualiza o tipocargo antigo inserindo a datafim
	 * @param tipoCargo o TipoCargo que sera atualizado
	 * @author Paulo Roberto
	 */
	public void atualizaTipoCargoAntigo(TipoCargo tipoCargo, Date data) {
		TipoCargo antigo = find(tipoCargo.getId(), tipoCargo.getVersao());
		antigo.setDtFimTipoCargo(data);
		merge(antigo);
	}

	/**
	 * Recupera o maior id do TipoCargo cadastrado no banco
	 * @author Paulo Roberto
	 */
	@Override
	public Integer findMaxId() throws SQLException, ClassNotFoundException {
		int id;
		Query query = em.createNamedQuery(TipoCargo.MAXID);
		id = (query.getSingleResult() == null ? 0 : (int) query
				.getSingleResult());
		return id;
	}

	/**
	 * Prepara a versao do cargo adicionando a data inicio
	 * e inserindo na tabela de alterado
	 * @param entity cargo que será preparado
	 * @param data Data que será iniciado
	 * @author Paulo Roberto
	 */
	@Override
	public void preparaVersao(TipoCargo entity, Date data) {
		AlteracaoDAO altdao = new AlteracaoDAO();
		entity.setVersao(entity.getVersao() == null ? 1
				: entity.getVersao() + 1);
		entity.setDtInicioTipoCargo(data);
		entity.setDtFimTipoCargo(null);
		altdao.persist(createAlterado(entity));
	}

	/**
	 * Cria um registro na tabela alterado
	 * @param tipo
	 * @return alterado
	 * @author Paulo Roberto
	 */
	public Alterado createAlterado(TipoCargo tipo) {
		Alterado alterado = new Alterado();
		alterado.setIdEntity(tipo.getId());
		alterado.setVersaoEntity(tipo.getVersao());
		alterado.setTipoEntity('T');
		return alterado;
	}

	/**
	 * Recupera o tipocargo por data
	 * @param data Data de pesquisa
	 * @return lista de tipocargos recuperado
	 * @author Rafael Hosaka 
	 */
	@SuppressWarnings("unchecked")
	public List<TipoCargo> searchTipoCargoByDate(Date data) {
		Query query = em.createNamedQuery(TipoCargo.SEARCH).setParameter(
				"dtPesquisa", data);
		return query.getResultList();
	}

	/**
	 * Recupera tipocargo por data e parcialmente de acordo com parametro
	 * @param first
	 * @param max
	 * @param data
	 * @return lista do tipo encontrado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<TipoCargo> searchTipoCargoByDate(int first,int max,Date data) {
		Query query = em.createNamedQuery(TipoCargo.SEARCH).setParameter(
				"dtPesquisa", data);
		query.setFirstResult(first);
		query.setMaxResults(max);
		return query.getResultList();
	}

	/**
	 * Atualiza cargos que possuem o tipocargo fechando
	 * e abrindo um novo periodo para o cargo com a data passada
	 * como parametro
	 * @param tipo
	 * @param data
	 * @author Paulo Roberto
	 */
	public void atualizaCargos(TipoCargo tipo,TipoCargo old,Date data){
		CargoDAO cargoDao = new CargoDAO();
		List<Cargo> cargos = cargoDao.findByTipoCargo(old);
		for(Cargo cargo:cargos){
			em.detach(cargo);
			cargo.setTipoCargo(tipo);
			cargoDao.update(cargo, data);
		}
	}

	/**
	 * Realiza o merge do tipocargo
	 * @param tipo TipoCargo que será atualizado
	 * @author Paulo Roberto
	 */
	private void merge(TipoCargo tipo){
		em.getTransaction().begin();
		em.merge(tipo);
		em.getTransaction().commit();
	}

	/**
	 * Realiza o detach em tipocargo
	 * @param tipo
	 * @author Paulo Roberto
	 */
	private void detach(TipoCargo tipo){
		em.detach(tipo);
	}

	/**
	 * Retorna a quantidade de tipo por periodo 
	 * @param data
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDate(Date data) {
		Query query = em.createNamedQuery(TipoCargo.COUNT_DATE)
				.setParameter("dtPesquisa", data);
		Number result = (Number) query.getSingleResult();
		return result.intValue();
	}

	/**
	 * Retorna parcialmente tipo de acordo com parametro
	 * @param first o primeiro elemento
	 * @param max quantidade de elemento por busca
	 * @param data o periodo da busca
	 * @param filterValue o parametro que fitrará por descrição
	 * @return lista encontrada
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<TipoCargo> lazyFilterDesc(int first,int max,Date data,String filterValue) {
		Query q = em.createNamedQuery(TipoCargo.LAZY_FILTER_DESC).setParameter("dtPesquisa", data).setParameter("param", "%"+filterValue+"%");
		q.setFirstResult(first);
		q.setMaxResults(max);
		return q.getResultList();
	}

	/**
	 * Retorna a quantidade de elementos encontrado por filtragem descrição
	 * @param date periodo da busca
	 * @param valor valor da filtragem
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDateFilterDesc(Date date, String valor) {
		Query q = em.createNamedQuery(TipoCargo.COUNT_DATE_FILTER_DESC).setParameter("dtPesquisa", date).setParameter("param", "%"+valor+"%");
		Number result = (Number) q.getSingleResult();

		return result.intValue();
	}

}
