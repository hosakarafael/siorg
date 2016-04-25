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
import br.gov.entity.Servidor;
import br.gov.entity.TipoCargo;
import br.gov.factory.ConnectionFactory;

@Stateless
@Local(DaoInterface.class)
public class CargoDAO implements DaoInterface<Cargo, Integer> {

	private EntityManager em;
	private List<String> listMudancas;

	public CargoDAO() {
		em = ConnectionFactory.getEntityManager();
		listMudancas = new ArrayList<String>();
	}

	/**
	 * Cadastra o cargo
	 * @param entity Cargo que será cadastrado
	 * @param data Data que será cadastrado
	 * @return  true se sucesso
	 *         false se houve erro
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean create(Cargo entity, Date data) {
		try {
			configuraCargoCadastro(entity, data);
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
	 * Verifica se o registro na memoria está diferente do banco
	 * @param bd Cargo no banco de dados
	 * @param obj Cargo na memoria
	 * @return true se tiver alterado
	 *         false se nao tiver alterado
	 * @author Paulo Roberto        
	 */
	public boolean verificaMudanca(Cargo antigo, Cargo novo) {
		boolean retorno = false;
		if (!antigo.getNome().equalsIgnoreCase(novo.getNome())) {
			retorno = true;
			listMudancas.add("nome");
		}
		if (antigo.getTipoCargo() != null || novo.getTipoCargo() != null) {
			if ((antigo.getTipoCargo() == null && novo.getTipoCargo() != null)
					|| (antigo.getTipoCargo() != null && novo.getTipoCargo() == null)) {
				retorno = true;
				listMudancas.add("tipocargo");
			} else if (antigo.getTipoCargo() != null
					&& novo.getTipoCargo() != null) {
				if (antigo.getTipoCargo().getId() != novo.getTipoCargo()
						.getId() || antigo.getTipoCargo().getVersao() != novo.getTipoCargo().getVersao()) {
					retorno = true;
					listMudancas.add("tipocargo");
				}
			}
		}

		return retorno;
	}

	/**
	 * Configura o Cargo incrementado o id e prepara a versão dele
	 * @param cargo o Cargo que será cadastrado
	 * @param data data que será cadastrado o cargo
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author Paulo Roberto
	 */
	public void configuraCargoCadastro(Cargo cargo, Date date)
			throws ClassNotFoundException, SQLException {
		cargo.setId(findMaxId() + 1);
		preparaVersao(cargo, date);
	}

	/**
	 * Fecha o periodo do cargo atribuindo uma data fim
	 * @param cargo
	 * @param data
	 * @return true se sucesso
	 *         false se houve erro
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean delete(Cargo cargo, Date data) {
		try {
			cargo.setDtFimCargo(data);
			merge(cargo);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Atualiza o cargo com suas devidas configuracoes
	 * @param novo Cargo que sera alterado
	 * @param data data que sera alterado
	 * @author Paulo Roberto
	 */
	@Override
	public synchronized boolean update(Cargo novo, Date dataPesquisa) {
		try{
			AlteracaoDAO altDao = new AlteracaoDAO();
			boolean alterado = false;
			detach(novo);
			Cargo cargo = find(novo.getId(), novo.getVersao());
			detach(cargo);
			if (!altDao.entityExists(novo, 'C')) {
				alterado = verificaMudanca(cargo, novo);
				if (alterado){
					configuraUpdate(novo, dataPesquisa);
					atualizaServidores(novo,cargo, dataPesquisa);
				} 
			}
			merge(novo);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Recupera o maior id do Cargo cadastrado no banco
	 * @author Paulo Roberto
	 */
	@Override
	public Integer findMaxId() throws SQLException, ClassNotFoundException {
		int id;
		Query query = em.createNamedQuery(Cargo.MAXID);
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
	public void preparaVersao(Cargo entity, Date date) {
		AlteracaoDAO altdao = new AlteracaoDAO();
		entity.setVersao(entity.getVersao() == null ? 1
				: entity.getVersao() + 1);
		entity.setDtInicioCargo(date);
		entity.setDtFimCargo(null);
		altdao.persist(createAlterado(entity));
	}

	/**
	 * Configura o Cargo fechando o periodo do cargo antigo
	 * e abrindo novo periodo para o cargo
	 * @param cargo o Cargo que será cadastrado
	 * @param data data que será cadastrado o cargo
	 * @author Paulo Roberto
	 */
	@Override
	public void configuraUpdate(Cargo cargo, Date data) {
		atualizaCargoAntigo(cargo, data);
		preparaVersao(cargo, data);
	}

	/**
	 * Atualiza o cargo antigo inserindo a datafim
	 * @param cargoNovo o Cargo que sera atualizado
	 * @author Paulo Roberto
	 */
	public void atualizaCargoAntigo(Cargo cargoNovo, Date data) {
		Cargo antigo = find(cargoNovo.getId(), cargoNovo.getVersao());
		if (antigo != null) {
			antigo.setDtFimCargo(data);
			merge(antigo);
		}
	}

	/**
	 * Recupera Cargo por id e versao
	 * @param id 
	 * @param versao
	 * @return Cargo encontrado
	 * @author Paulo Roberto
	 */
	@Override
	public Cargo find(Integer id, Integer versao) {
		Cargo cargo = null;
		Query query = em.createNamedQuery(Cargo.FIND).setParameter("id", id)
				.setParameter("versao", versao);
		try {
			cargo = (Cargo) query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return cargo;
	}

	/**
	 * Realiza uma busca atravez da NamedQuery
	 * @param namedQueryName a NamedQuery que será invocada 
	 * @return List<Cargo> resultado da busca
	 * @author Rafael Hosaka
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Cargo> findWithNamedQuery(String namedQueryName) {
		Query query = this.em.createNamedQuery(namedQueryName);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Recupera o cargo por data
	 * @param data Data de pesquisa
	 * @return lista de cargos recuperado
	 * @author Rafael Hosaka 
	 */
	@SuppressWarnings("unchecked")
	public List<Cargo> searchCargoByDate(Date data) {
		Query query = em.createNamedQuery(Cargo.SEARCH).setParameter(
				"dtPesquisa", data);
		return query.getResultList();
	}

	/**
	 * Recupera cargo por data e parcialmente de acordo com parametro
	 * @param first
	 * @param max
	 * @param data
	 * @return lista do cargo encontrado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Cargo> searchCargoByDate(int first, int max,Date data) {
		Query query = em.createNamedQuery(Cargo.SEARCH).setParameter(
				"dtPesquisa", data);
		query.setFirstResult(first);
		query.setMaxResults(max);
		return query.getResultList();
	}

	/**
	 * Cria um registro na tabela alterado
	 * @param cargo
	 * @return alterado
	 */
	public synchronized Alterado createAlterado(Cargo cargo) {
		Alterado alterado = new Alterado();
		alterado.setIdEntity(cargo.getId());
		alterado.setVersaoEntity(cargo.getVersao());
		alterado.setTipoEntity('C');
		return alterado;
	}

	/**
	 * Recupera os Cargos por Tipo de Cargo
	 * @param tipo Tipo do Cargo que está buscando
	 * @return Lista dos cargos encontrados
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Cargo> findByTipoCargo(TipoCargo tipo) {
		Query query = em.createNamedQuery(Cargo.FINDBYTIPOCARGO).setParameter(
				"id", tipo.getId()).setParameter("versao", tipo.getVersao());
		return query.getResultList();
	}

	/**
	 * Atualiza servidores que possuem o cargo fechando
	 * e abrindo um novo periodo para o servidor com a data passada
	 * como parametro
	 * @param cargo
	 * @param data
	 * @author Paulo Roberto
	 */
	public void atualizaServidores(Cargo cargo, Cargo antigo,Date data) {
		ServidorDAO servDao = new ServidorDAO();
		List<Servidor> servidores = servDao.findByCargo(antigo);
		for (Servidor servidor : servidores){
			servidor.setCargo(cargo);
			servDao.update(servidor, data);
		}
	}


	/**
	 * Realiza o merge do cargo
	 * @param cargo Cargo que será atualizado
	 * @author Paulo Roberto
	 */
	private void merge(Cargo cargo){
		em.getTransaction().begin();
		em.merge(cargo);
		em.getTransaction().commit();	
	}

	/**
	 * Realiza o detach em cargo
	 * @param cargo
	 * @author Paulo Roberto
	 */
	private void detach(Cargo cargo){
		em.detach(cargo);
	}

	/**
	 * atualiza todos servidores que possuiam o cargo, mas que o cargo
	 * fechou o periodo e nao existe mais
	 * @param cargo
	 * @param data
	 * @author Rafael Hosaka
	 */
	public void atualizaRelacaoExclusaoServidorCargo(Cargo cargo, Date data) {
		List<Servidor> servidores = new ServidorDAO().findByCargo(cargo);
		for (Servidor servidor : servidores) {
			try {
				atualizaRelacaoExclusaoServidorCargo(servidor,data);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Atualiza o servidor, fechando e abrindo um periodo,
	 * retirando o cargo do servidor
	 * @param servidor
	 * @param data
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author Rafael Hosaka
	 */
	private void atualizaRelacaoExclusaoServidorCargo(Servidor servidor,
			Date data) throws ClassNotFoundException, SQLException {
		servidor.setDtFimServidor(data);
		em.getTransaction().begin();
		em.merge(servidor);
		em.getTransaction().commit();

		em.detach(servidor);

		servidor.setDtFimServidor(null);
		servidor.setDtInicioServidor(data);
		servidor.setVersao(servidor.getVersao() == null ? 1
				: servidor.getVersao() + 1);
		servidor.setCargo(null);
		em.getTransaction().begin();
		em.merge(servidor);
		em.getTransaction().commit();
	}

	/**
	 * Recupera cargo por um nome
	 * @param nome O nome do cargo que está procurando
	 * @return cargo que foi encontrado
	 */
	public Cargo findByNome(String nome) {
		Query query = this.em.createNamedQuery(Cargo.FIND_BY_NOME).setParameter("nome", nome);
		return (Cargo) query.getSingleResult();
	}

	/**
	 * Retorna a quantidade de cargo por periodo 
	 * @param data
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDate(Date data) {
		Query query = em.createNamedQuery(Cargo.COUNT_DATE).setParameter("dtPesquisa", data);
		Number result = (Number) query.getSingleResult();
		return result.intValue();
	}

	/**
	 * Retorna parcialmente cargo de acordo com parametro
	 * @param first o primeiro elemento
	 * @param max quantidade de elemento por busca
	 * @param data o periodo da busca
	 * @param filterValue o parametro que fitrará por nome
	 * @return lista encontrada
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Cargo> lazyFilterNome(int first,int max,Date data,String filterValue) {
		Query q = em.createNamedQuery(Cargo.LAZY_FILTER_NOME).setParameter("dtPesquisa", data).setParameter("param", "%"+filterValue+"%");
		q.setFirstResult(first);
		q.setMaxResults(max);
		return q.getResultList();
	}

	/**
	 * Retorna a quantidade de elementos encontrado por filtragem nome
	 * @param date periodo da busca
	 * @param valor valor da filtragem
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDateFilterNome(Date date, String valor) {
		Query q = em.createNamedQuery(Cargo.COUNT_DATE_FILTER_NOME).setParameter("dtPesquisa", date).setParameter("param", "%"+valor+"%");
		Number result = (Number) q.getSingleResult();

		return result.intValue();
	}


	//Getter Setter
	public List<String> getListMudancas() {
		return listMudancas;
	}

	public void setListMudancas(List<String> listMudancas) {
		this.listMudancas = listMudancas;
	}


}
