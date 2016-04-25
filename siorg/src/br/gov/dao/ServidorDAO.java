package br.gov.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.gov.entity.Cargo;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.factory.ConnectionFactory;

@Stateless
@Local(DaoInterface.class)
public class ServidorDAO implements DaoInterface<Servidor, Integer> {

	private EntityManager em;
	private List<String> listMudancas;

	public ServidorDAO() {
		em = ConnectionFactory.getEntityManager();

		listMudancas = new ArrayList<String>();
	}

	/**
	 * Cadastra o servidor
	 * @param entity Servidor que será cadastrado
	 * @param data Data que será cadastrado
	 * @return  true se sucesso
	 *         false se houve erro
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean create(Servidor entity, Date dataPesquisa) {
		try {

			configuraServidorCadastro(entity, dataPesquisa);
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
	 * Recupera Servidor por id e versao
	 * @param id 
	 * @param versao
	 * @return Servidor encontrado
	 * @author Paulo Roberto
	 */
	@Override
	public Servidor find(Integer id, Integer versao) {
		Servidor servidor = null;
		try {
			Query query = em.createNamedQuery(Servidor.FIND)
					.setParameter("id", id).setParameter("versao", versao);
			query.setHint("javax.persistence.cache.storeMode", "REFRESH");
			servidor = (Servidor) query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return servidor;
	}

	/**
	 * Fecha o periodo do servidor atribuindo uma data fim
	 * @param servidor
	 * @param data
	 * @return true se sucesso
	 *         false se houve erro
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean delete(Servidor entity, Date data) {
		try {
			entity.setDtFimServidor(data);
			merge(entity);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			return false;
		}
	}

	/**
	 * Realiza uma busca atravez da NamedQuery
	 * @param namedQueryName a NamedQuery que será invocada 
	 * @return List<Servidor> resultado da busca
	 * @author Rafael Hosaka
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Servidor> findWithNamedQuery(String namedQueryName) {
		Query query = this.em.createNamedQuery(namedQueryName);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Atualiza o servidor com suas devidas configuracoes
	 * @param novo Servidor que sera alterado
	 * @param data data que sera alterado
	 * @author Paulo Roberto
	 */
	@Override
	public synchronized boolean update(Servidor novo, Date dataPesquisa) {
		try{
			AlteracaoDAO altDao = new AlteracaoDAO();
			boolean alterado = false;
			detach(novo);
			Servidor servidor = find(novo.getId(), novo.getVersao());
			detach(servidor);
			if (!altDao.entityExists(novo, 'S')) {
				alterado = verificaMudanca(servidor, novo);
				if (alterado)
					configuraUpdate(novo, dataPesquisa);
			}
			merge(novo);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Configura o Servidor fechando o periodo do servidor antigo
	 * e abrindo novo periodo para o servidor
	 * @param cargo o Cargo que será cadastrado
	 * @param data data que será cadastrado o cargo
	 * @author Paulo Roberto
	 */
	@Override
	public void configuraUpdate(Servidor servidor, Date dataPesquisa) {
		preparaVersao(servidor, dataPesquisa);
		atualizaServidorAntigo(servidor, dataPesquisa);
	}

	/**
	 * Atualiza o servidor antigo inserindo a datafim
	 * @param servidorNovo o Servidor que sera atualizado
	 * @author Paulo Roberto
	 */
	public void atualizaServidorAntigo(Servidor servidorNovo, Date dataPesquisa) {
		Servidor antigo = find(servidorNovo.getId(), servidorNovo.getVersao()-1);
		if (antigo != null) {
			antigo.setDtFimServidor(dataPesquisa);
			merge(antigo);
		}
	}

	/**
	 * Recupera o maior id do Servidor cadastrado no banco
	 * @author Paulo Roberto
	 */
	@Override
	public Integer findMaxId() throws SQLException, ClassNotFoundException {
		int id;
		Query query = em.createNamedQuery(Servidor.MAXID);
		id = (query.getSingleResult() == null ? 0 : (int) query
				.getSingleResult());
		return id;
	}

	/**
	 * Configura o Servidor incrementado o id e prepara a versão dele
	 * @param servidor o Servidor que será cadastrado
	 * @param data data que será cadastrado o servidor
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author Paulo Roberto
	 */
	public void configuraServidorCadastro(Servidor servidor, Date data)
			throws ClassNotFoundException, SQLException {
		servidor.setId(findMaxId() + 1);
		preparaVersao(servidor, data);
	}

	/**
	 * Verifica se o registro na memoria está diferente do banco
	 * @param bd Servidor no banco de dados
	 * @param obj Servidor na memoria
	 * @return true se tiver alterado
	 *         false se nao tiver alterado
	 * @author Paulo Roberto        
	 */
	public boolean verificaMudanca(Servidor bd, Servidor obj) {
		boolean retorno = false;
		if (!bd.getNome().equalsIgnoreCase(obj.getNome())) {
			listMudancas.add("nome");
			retorno = true;
		}
		if (bd.getOrgao() != null || obj.getOrgao() != null) {
			if ((bd.getOrgao() == null && obj.getOrgao() != null)
					|| (bd.getOrgao() != null && obj.getOrgao() == null)) {
				listMudancas.add("orgao");
				retorno = true;
			} else if (bd.getOrgao() != null && obj.getOrgao() != null) {
				if (bd.getOrgao().getId() != obj.getOrgao().getId() || bd.getOrgao().getVersao() != obj.getOrgao().getVersao()) {
					retorno = true;
					listMudancas.add("orgao");
				}
			}
		}
		if(bd.getCargo()!=null || obj.getCargo()!=null){
			if((bd.getCargo() == null && obj.getCargo()!=null) || (bd.getCargo()!=null && obj.getCargo()==null)){
				listMudancas.add("cargo");
				retorno = true;
			}else if(bd.getCargo()!=null && obj.getCargo()!=null){
				if(bd.getCargo().getId()!=obj.getCargo().getId()|| bd.getCargo().getVersao()!=obj.getCargo().getVersao()){
					retorno = true;
					listMudancas.add("cargo");
				}
			}
		}
		if (bd.getOrgao() != null && obj.getOrgao() != null) {

			if (bd.getOrgao().getId() != obj.getOrgao().getId()) {
				listMudancas.add("orgao");
				retorno = true;
			}
		}

		return retorno;
	}

	/**
	 * Prepara a versao do servidor adicionando a data inicio
	 * e inserindo na tabela de alterado
	 * @param entity servidor que será preparado
	 * @param data Data que será iniciado
	 * @author Paulo Roberto
	 */
	@Override
	public void preparaVersao(Servidor entity, Date data) {
		entity.setDtFimServidor(null);
		entity.setDtInicioServidor(data);
		entity.setVersao(entity.getVersao() == null ? 1
				: entity.getVersao() + 1);
		new AlteracaoDAO().createAlterado(entity, 'S');
	}

	/**
	 * Recupera servidores por orgao
	 * @param orgao
	 * @return lista dos servidores encontrado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> findServidoresByOrgao(Orgao orgao) {
		Query q = em.createNamedQuery(Servidor.BY_ORGAO).setParameter("orgao",
				orgao);
		return q.getResultList();
	}

	/**
	 * Recupera servidor por matricula
	 * @param matricula
	 * @return Servidor encontrado
	 * @author Rafael Hosaka
	 */
	public Servidor findServidorByMatricula(String matricula) {
		Servidor servidor = null;
		try{
			Query q = em.createNamedQuery(Servidor.BYMATRICULA).setParameter("matricula", matricula);
			servidor = (Servidor) q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return servidor;
	}

	/**
	 * Atualiza os servidores com orgao superior novo
	 * @param servidores Servidores que deseja ser atualizado
	 * @param orgao Novo orgao superior
	 * @param data Data que sera finalziada o periodo do servidor
	 * @author Rafael Hosaka
	 */
	public void atualizaOrgaoSuperior(List<Servidor> servidores, Orgao orgao,
			Date data) {
		for (Servidor s : servidores) {
			s.setOrgao(orgao);
			update(s, data);
		}
	}

	/**
	 * Recupera o servidor por data
	 * @param data Data de pesquisa
	 * @return lista de servidores recuperado
	 * @author Rafael Hosaka 
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> searchServidorByDate(Date data) {
		Query query = em.createNamedQuery(Servidor.SEARCH).setParameter(
				"dtPesquisa", data);
		return query.getResultList();
	}

	/**
	 * Recupera servidor por data e parcialmente de acordo com parametro
	 * @param first
	 * @param max
	 * @param data
	 * @return lista do servidor encontrado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> searchServidorByDate(int first,int max,Date data) {
		Query query = em.createNamedQuery(Servidor.SEARCH).setParameter(
				"dtPesquisa", data);
		query.setFirstResult(first);
		query.setMaxResults(max);
		return query.getResultList();
	}

	/**
	 * Recupera servidores por cargo
	 * @param cargo
	 * @return lista de servidores recuperado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> findByCargo(Cargo cargo) {
		Query query = em.createNamedQuery(Servidor.FINDBYCARGO).setParameter(
				"id", cargo.getId()).setParameter("versao", cargo.getVersao());
		return query.getResultList();
	}

	/**
	 * Recupera todas matriculas
	 * @return Lista dos servidores recuperado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<String> recuperaTodasMatriculas() {
		Query query = this.em.createNamedQuery(Servidor.ALLMAT);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Realiza o merge do servidor
	 * @param servidor Servidor que será atualizado
	 * @author Paulo Roberto
	 */
	private void merge(Servidor servidor){
		em.getTransaction().begin();
		em.merge(servidor);
		em.getTransaction().commit();
	}

	/**
	 * Realiza o detach em servidor
	 * @param servidor
	 * @author Paulo Roberto
	 */
	private void detach(Servidor servidor){
		em.detach(servidor);
	}

	/**
	 * Atualiza o servidor independente de condições
	 * @param novo
	 * @param data
	 * @author Rafael Hosaka
	 */
	public void forceUpdate(Servidor novo, Date data) {
		detach(novo);
		configuraUpdate(novo, data);
		merge(novo);
	}

	public void forceUpdateFechandoPeriodo(Servidor novo, Date data) {
		detach(novo);
		configuraUpdate(novo, data);
		novo.setDtFimServidor(data);
		merge(novo);
	}

	/**
	 * Recupera servidor que não possui cargo por orgao e data
	 * @param orgao
	 * @param data
	 * @return Lista de servidores recuperado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> findByOrgaoDataSemCargo(Orgao orgao,Date data) {
		Query query = this.em.createNamedQuery(Servidor.BY_ORGAO_AND_DATA_SEM_CARGO).setParameter("orgao", orgao).setParameter("dtPesquisa", data);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Recupera servidor que possui cargo por orgao e data
	 * @param orgao
	 * @param data
	 * @return Lista de servidores recuperado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> findByOrgaoDataComCargo(Orgao orgao,Date data) {
		Query query = this.em.createNamedQuery(Servidor.BY_ORGAO_AND_DATA_COM_CARGO).setParameter("orgao", orgao).setParameter("dtPesquisa", data);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Recupera servidor por orgao e data
	 * @param orgao
	 * @param data
	 * @return Lista de servidores recuperado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> findServidoresByOrgaoAndDate(Orgao orgao,Date data) {
		Query q = em.createNamedQuery(Servidor.BY_ORGAO_AND_DATE).setParameter("orgao",
				orgao).setParameter("dtPesquisa", data);
		return q.getResultList();
	}

	/**
	 * Recupera servidores por id
	 * @param id
	 * @return Lista de servidores recuperados
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> findBYID(Integer id) {
		Query q = em.createNamedQuery(Servidor.BY_ID).setParameter("id", id);
		return q.getResultList();
	}

	/**
	 * Retorna a quantidade de servidor por periodo 
	 * @param data
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDate(Date data) {
		Query query = em.createNamedQuery(Servidor.COUNT_DATE)
				.setParameter("dtPesquisa", data);

		Number result = (Number) query.getSingleResult();

		return result.intValue();
	}

	/**
	 * Atualiza redistribuição do servidor, configurando o servidor corretamente
	 * @param servidores
	 * @param orgao
	 * @author Rafael Hosaka
	 */
	public void atualizaRedistribuicao(List<Servidor> servidores,Orgao orgao){
		for (Servidor servidor : servidores) {
			servidor.setDtInicioServidor(servidor.getDtInicioServidor());
			servidor.setDtFimServidor(null);
			servidor.setOrgao(orgao);

			merge(servidor);
		}

	}

	/**
	 * Retorna parcialmente servidor de acordo com parametro
	 * @param first o primeiro elemento
	 * @param max quantidade de elemento por busca
	 * @param data o periodo da busca
	 * @param filterValue o parametro que fitrará por matricula
	 * @return lista encontrada
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> lazyFilterMat(int first,int max,Date data,String filterValue) {
		Query q = em.createNamedQuery(Servidor.LAZY_FILTER_MAT).setParameter("dtPesquisa", data).setParameter("param", "%"+filterValue+"%");
		q.setFirstResult(first);
		q.setMaxResults(max);
		return q.getResultList();
	}

	/**
	 * Retorna a quantidade de elementos encontrado por filtragem matricula
	 * @param date periodo da busca
	 * @param valor valor da filtragem
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDateFilterMat(Date date, String valor) {
		Query q = em.createNamedQuery(Servidor.COUNT_DATE_FILTER_MAT).setParameter("dtPesquisa", date).setParameter("param", "%"+valor+"%");
		Number result = (Number) q.getSingleResult();
		return result.intValue();
	}	
	
	/**
	 * Retorna parcialmente servidor de acordo com parametro
	 * @param first o primeiro elemento
	 * @param max quantidade de elemento por busca
	 * @param data o periodo da busca
	 * @param filterValue o parametro que fitrará por matricula
	 * @return lista encontrada
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Servidor> lazyFilterNome(int first,int max,Date data,String filterValue) {
		Query q = em.createNamedQuery(Servidor.LAZY_FILTER_NOME).setParameter("dtPesquisa", data).setParameter("param", "%"+filterValue+"%");
		q.setFirstResult(first);
		q.setMaxResults(max);
		return q.getResultList();
	}

	/**
	 * Retorna a quantidade de elementos encontrado por filtragem matricula
	 * @param date periodo da busca
	 * @param valor valor da filtragem
	 * @return int quantidade
	 * @author Rafael Hosaka
	 */
	public int countByDateFilterNome(Date date, String valor) {
		Query q = em.createNamedQuery(Servidor.COUNT_DATE_FILTER_NOME).setParameter("dtPesquisa", date).setParameter("param", "%"+valor+"%");
		Number result = (Number) q.getSingleResult();
		return result.intValue();
	}	

//	public void insertJDBCBatchServidores(List<Servidor> servidores){
//		String sqlInsertServidor = "INSERT INTO SERVIDOR(ID,VERSAO,NOME,MATRICULA,EFETIVO,ORGAO_ID,VERSAO_ORGAO,CARGO_ID,VERSAO_CARGO,DT_INICIO_SERVIDOR,DT_FIM_SERVIDOR)" +
//				" VALUES(?,?,?,?,?,?,?,?,?,?,?)";
//		try {
//			Connection con = ConnectionFactory.getConnection();
//			con.setAutoCommit(false);
//			PreparedStatement st = con.prepareStatement(sqlInsertServidor);
//			int resto = 0;
//			for(int i=0;i<servidores.size();i++){
//				Servidor serv = servidores.get(i);
//				st.setInt(1, serv.getId());
//				st.setInt(2, serv.getVersao());
//				st.setString(3, serv.getNome());
//				st.setString(4, serv.getMatricula());
//				st.setBoolean(5, serv.getEfetivo());
//				st.setInt(6, serv.getOrgao().getId());
//				st.setInt(7, serv.getOrgao().getVersao() + 1);
//				st.setInt(8, serv.getCargo().getId());
//				st.setInt(9, serv.getCargo().getVersao());
//				st.setDate(10,new java.sql.Date(serv.getDtInicioServidor().getTime()));
//				st.setDate(11,(java.sql.Date)null);
//				st.addBatch();
//				resto = i%100;
//				if(resto==0)
//					st.executeBatch();
//			}
//			if(resto>0)
//				st.executeBatch();
//			con.commit();
//			ConnectionFactory.close(st, con);
//		} catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void insertJDBCBatchServidores(List<Servidor> servidores, Date dataFim){
//		String sqlInsertServidor = "INSERT INTO SERVIDOR(ID,VERSAO,NOME,MATRICULA,EFETIVO,ORGAO_ID,VERSAO_ORGAO,CARGO_ID,VERSAO_CARGO,DT_INICIO_SERVIDOR,DT_FIM_SERVIDOR)" +
//				" VALUES(?,?,?,?,?,?,?,?,?,?,?)";
//		try {
//			Connection con = ConnectionFactory.getConnection();
//			con.setAutoCommit(false);
//			PreparedStatement st = con.prepareStatement(sqlInsertServidor);
//			int resto = 0;
//			for(int i=0;i<servidores.size();i++){
//				Servidor serv = servidores.get(i);
//				st.setInt(1, serv.getId());
//				st.setInt(2, serv.getVersao() + 1);
//				st.setString(3, serv.getNome());
//				st.setString(4, serv.getMatricula());
//				st.setBoolean(5, serv.getEfetivo());
//				st.setInt(6, serv.getOrgao().getId());
//				st.setInt(7, serv.getOrgao().getVersao());
//				st.setInt(8, serv.getCargo().getId());
//				st.setInt(9, serv.getCargo().getVersao());
//				st.setDate(10,new java.sql.Date(serv.getDtInicioServidor().getTime()));
//				st.setDate(11,dataFim==null?null:new java.sql.Date(dataFim.getTime()));
//				st.addBatch();
//				resto = i%100;
//				if(resto==0)
//					st.executeBatch();
//			}
//			if(resto>0)
//				st.executeBatch();
//			con.commit();
//			ConnectionFactory.close(st, con);
//		} catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void updateJDBCBatchServidores(List<Servidor> servidores,Date dataFim){
//		String sqlInsertServidor = "UPDATE SERVIDOR SET DT_FIM_SERVIDOR = ? WHERE ID=? AND VERSAO=?";
//		int resto = 0;
//		try {
//			Connection con = ConnectionFactory.getConnection();
//			con.setAutoCommit(false);
//			PreparedStatement st = con.prepareStatement(sqlInsertServidor);
//			for(int i=0;i<servidores.size();i++){
//				Servidor serv = servidores.get(i);
//				st.setDate(1,(dataFim==null?null:new java.sql.Date(dataFim.getTime())));
//				st.setInt(2, serv.getId());
//				st.setInt(3, serv.getVersao());
//				st.addBatch();
//				resto = i%100; 
//				if(i%100==0)
//					st.executeBatch();
//			}
//			if(resto > 0)
//				st.executeBatch();
//			con.commit();
//			ConnectionFactory.close(st, con);
//		} catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//		}
//	}


}
