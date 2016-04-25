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

import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.factory.ConnectionFactory;
import br.gov.util.Util;

@Stateless
@Local(DaoInterface.class)
public class OrgaoDAO implements DaoInterface<Orgao, Integer> {

	private EntityManager em;
	private List<String> listMudancas;

	public OrgaoDAO() {
		em = ConnectionFactory.getEntityManager();
		listMudancas = new ArrayList<String>();
	}

	/**
	 * Verifica se o registro na memoria está diferente do banco
	 * @param bd Orgao no banco de dados
	 * @param obj Orgao na memoria
	 * @return true se tiver alterado
	 *         false se nao tiver alterado
	 * @author Paulo Roberto       
	 */
	public boolean verificaMudanca(Orgao bd, Orgao obj) {
		boolean retorno = false;
		if (!bd.getNome().equalsIgnoreCase(obj.getNome())) {
			listMudancas.add("nome");
			retorno = true;
		}
		if (bd.getSuperior() != null || obj.getSuperior() != null) {
			if ((bd.getSuperior() == null && obj.getSuperior() != null)
					|| (bd.getSuperior() != null && obj.getSuperior() == null)) {
				retorno = true;
				listMudancas.add("superior");
			} else if (bd.getSuperior() != null && obj.getSuperior() != null) {
				if (bd.getSuperior().getId() != obj.getSuperior().getId()
						|| bd.getSuperior().getVersao() != obj.getSuperior()
						.getVersao()) {
					retorno = true;
					listMudancas.add("superior");
				}
			}
		}
		return retorno;
	}

	/**
	 * Cadastra o Orgao em uma Data
	 * @param entity orgao à ser cadastrado
	 * @param data data que o orgao será cadastrado 
	 * @return  true se sucesso
	 *          false se houve erro
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean create(Orgao entity, Date data) {
		try {
			configuraOrgaoCadastro(entity, data);
			persist(entity);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			return false;
		}
		return true;
	}

	/**
	 * Recupera Orgao por id e versao
	 * @param id 
	 * @param versao
	 * @return Orgao encontrado
	 * @author Paulo Roberto
	 */
	@Override
	public Orgao find(Integer id, Integer versao) {
		Orgao orgao = null;
		try {
			Query query = em.createNamedQuery(Orgao.FIND)
					.setParameter("id", id).setParameter("versao", versao);
			orgao = (Orgao) query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return orgao;
	}

	/**
	 * Atualiza o orgao colocando a data fim
	 * @param entity orgao à ser atualizado
	 * @param data data fim do orgao 
	 * @return boolean true se alterou com sucesso
	 *                 false se falhou
	 * @author Rafael Hosaka
	 */
	@Override
	public synchronized boolean delete(Orgao entity, Date data) {
		try {
			entity.setDtFimOrgao(data);
			merge(entity);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Realiza uma busca atravez da NamedQuery
	 * @param namedQueryName a NamedQuery que será invocada 
	 * @return List<Orgao> resultado da busca
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Orgao> findWithNamedQuery(String namedQueryName) {
		Query query = this.em.createNamedQuery(namedQueryName);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Realiza uma busca de todos orgaos que existem ainda e que não é o orgao passado como parametro
	 * @param o Orgao que será excluida da busca
	 * @return List<Orgao> resultado da busca
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Orgao> findOrgaosSelecionaveis(Orgao o) {
		Query query = this.em.createNamedQuery(Orgao.ONLYSELECTIONABLE)
				.setParameter("id", o.getId());
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return query.getResultList();
	}

	/**
	 * Atualiza o orgao com suas devidas configuracoes
	 * @param novo Orgao que sera alterado
	 * @param data data que sera alterado
	 * @author Paulo Roberto
	 */
	@Override
	public synchronized boolean update(Orgao novo, Date data) {
		try{
			AlteracaoDAO altDao = new AlteracaoDAO();
			boolean alterado = false;
			detach(novo);
			Orgao orgao = find(novo.getId(), novo.getVersao());
			detach(orgao);
			if (!altDao.entityExists(novo, 'O')) {
				alterado = verificaMudanca(orgao, novo);
				if (alterado) {
					configuraUpdate(novo, data);
					atualizaOrgaos(novo, orgao, data);
				}
			}
			merge(novo);
			if(alterado)
				atualizaServidoresRecursivo(novo,orgao, data);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Atualiza o orgao forçadamente sem restrições
	 * @param novo Orgao que será alterado
	 * @param data data que sera alterado
	 * @author Rafael Hosaka
	 */
	public void forceUpdate(Orgao novo,Orgao antigo,Date data) {
		detach(novo);
		configuraUpdate(novo, data);
		atualizaOrgaos(novo,antigo, data);	
		merge(novo);
	}


	/**
	 * Recupera o maior id do Orgao cadastrado no banco
	 * @author Paulo Roberto
	 */
	@Override
	public Integer findMaxId() throws SQLException, ClassNotFoundException {
		int id;
		Query query = em.createNamedQuery(Orgao.MAXID);
		id = (query.getSingleResult() == null ? 0 : (int) query
				.getSingleResult());
		return id;
	}

	/**
	 * Configura o Orgao incrementado o id e prepara a versão dele
	 * @param orgao o Orgao que será cadastrado
	 * @param data data que será cadastrado o orgao
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author Paulo Roberto
	 */
	public void configuraOrgaoCadastro(Orgao orgao, Date data)
			throws ClassNotFoundException, SQLException {
		orgao.setId(findMaxId() + 1);
		preparaVersao(orgao, data);
	}

	/**
	 * Configura o Orgao fechando o periodo do orgao antigo
	 * e abrindo novo periodo para o orgao
	 * @param orgao o Orgao que será cadastrado
	 * @param data data que será cadastrado o orgão
	 * @author Paulo Roberto
	 */
	@Override
	public void configuraUpdate(Orgao orgao, Date data) {
		detach(orgao);
		preparaVersao(orgao, data);
		atualizaOrgaoAntigo(orgao);
	}

	/**
	 * Atualiza o orgao antigo inserindo a datafim
	 * @param orgaoNovo o Orgao que sera atualizado
	 * @author Paulo Roberto
	 */
	public void atualizaOrgaoAntigo(Orgao orgaoNovo) {
		Orgao antigo = find(orgaoNovo.getId(), (orgaoNovo.getVersao()-1));
		if (antigo != null) {
			antigo.setDtFimOrgao(new Date());
			merge(antigo);
		}
	}

	/**
	 * Recupera o orgao por data 
	 * @param data Data de pesquisa
	 * @return lista de orgaos recuperado
	 * @author Rafael Hosaka 
	 */
	@SuppressWarnings("unchecked")
	public List<Orgao> searchOrgaoByDate(Date data) {
		Query query = em.createNamedQuery(Orgao.SEARCH).setParameter(
				"dtPesquisa", data);
		return query.getResultList();
	}

	/**
	 * Prepara a versao do orgao adicionando a data inicio
	 * e inserindo na tabela de alterado
	 * @param entity orgao que será preparado
	 * @param data Data que será iniciado
	 * @author Paulo Roberto
	 */
	@Override
	public void preparaVersao(Orgao entity, Date data) {
		entity.setVersao(entity.getVersao() == null ? 1
				: entity.getVersao() + 1);
		entity.setDtInicioOrgao(data);
		entity.setDtFimOrgao(null);
		new AlteracaoDAO().createAlterado(entity, 'O');
	}

	/**
	 * Recupera os orgaos inferiores apartir de um orgao
	 * @param orgao Orgao superior dos que deseja recuperar
	 * @return lista de orgao que foi recuperado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Orgao> findOrgaosInferioresHeadByOrgao(Orgao orgao) {
		Query q = em.createNamedQuery(Orgao.BYORGAO).setParameter("orgao",
				orgao);
		return q.getResultList();
	}

	/**
	 * Recupera um Orgao atravez do id
	 * @param id id do orgao à ser procurado
	 * @return Orgao
	 * @author Rafael Hosaka
	 */
	public Orgao findById(Integer id) {
		Query q = em.createNamedQuery(Orgao.FINDBYID).setParameter("id", id);
		q.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return (Orgao) q.getSingleResult();
	}

	/**
	 * Recupera orgao por id e data
	 * @param id
	 * @param data
	 * @return Orgao recuperado
	 * @author Rafael Hosaka
	 */
	public Orgao findByIdAndData(Integer id, Date data) {
		Orgao orgao = null;
		try {
			
			Query q = em.createNamedQuery(Orgao.FINDBYIDANDDATA)
					.setParameter("id", id).setParameter("dtPesquisa", data);
			orgao = (Orgao) q.getSingleResult();
			
		} catch (NoResultException e) {
			System.out.println("Orgao não encontrado");
		}
		return orgao;
	}

	/**
	 * Atualiza recrusivamente todos servidores de um orgao
	 * @param orgao Orgao que conterá os servidores que serao atualizados
	 * @param data data que sera alterado
	 * @author Paulo Roberto
	 */
	public void atualizaServidoresRecursivo(Orgao orgao,Orgao antigo,Date data){
		ServidorDAO servDao = new ServidorDAO();
		List<Servidor> servidores = servDao.findServidoresByOrgao(antigo);
		for (Servidor servidor : servidores) {
			servidor.setOrgao(orgao);
			servDao.forceUpdate(servidor, data);
		}
		if(orgao.getOrgaos()!=null)
			for(Orgao org:orgao.getOrgaos()){
				atualizaServidoresRecursivo(org, data);
			}
	}
	
	public void atualizaServidoresRecursivo(Orgao orgao,Date data){
		ServidorDAO servDao = new ServidorDAO();
		Orgao antigo = find(orgao.getId(),orgao.getVersao()-1);
		List<Servidor> servidores = servDao.findServidoresByOrgao(antigo);
		for (Servidor servidor : servidores) {
			servidor.setOrgao(orgao);
			servDao.forceUpdate(servidor, data);
		}
		if(orgao.getOrgaos()!=null)
			for(Orgao org:orgao.getOrgaos()){
				atualizaServidoresRecursivo(org, data);
			}
	}

	/**
	 * Recupera orgao superior
	 * @return Orgao superior
	 * @author Rafael Hosaka
	 */
	public Orgao findOrgaoSuperior() {
		Query query = this.em.createNamedQuery(Orgao.ORGAOSUPERIOR);
		query.setHint("javax.persistence.cache.storeMode", "REFRESH");
		return (Orgao) query.getSingleResult();
	}

	/**
	 * Atualiza todos orgaos inferiores
	 * @param orgao Orgao superior
	 * @param data Data que sera atualziada
	 * @author Rafael Hosaka
	 */
	public void atualizaOrgaos(Orgao novo, Orgao antigo, Date data) {
		List<Orgao> orgaosInferiores = findOrgaosInferioresHeadByOrgao(antigo);
		for (Orgao org : orgaosInferiores) {
			//att anterior
			detach(org);
			Orgao ant = find(org.getId(), org.getVersao());
			detach(ant);
			atualizaOrgaos(org, ant, data);
			ant.setDtFimOrgao(data);
			merge(ant);
			//insertnovo
			
			org.setVersao(org.getVersao() + 1);
			org.setSuperior(novo);
			org.setDtFimOrgao(null);
			merge(org);
			new AlteracaoDAO().createAlterado(org, 'O');
			
		//	detach(org);
		//	Orgao ant = find(org.getId(), org.getVersao());
		//	detach(ant);
		//	org.setSuperior(novo);
		//	forceUpdate(org,ant, data);
		}
	}

	/**
	 * Realiza o persist do orgao
	 * @param orgao Orgao que será cadastrado
	 * @author Paulo Roberto
	 */
	private void persist(Orgao orgao){
		em.getTransaction().begin();
		em.persist(orgao);
		em.getTransaction().commit();
	}

	/**
	 * Realiza o merge do orgao
	 * @param orgao Orgao que será atualizado
	 * @author Paulo Roberto
	 */
	private void merge(Orgao orgao){
		em.getTransaction().begin();
		em.merge(orgao);
		em.getTransaction().commit();
	}

	/**
	 * Realiza o detach em orgao
	 * @param orgao
	 * @author Paulo Roberto
	 */
	private void detach(Orgao orgao){
		em.detach(orgao);
	}

	/**
	 * Recupera orgao apartir de um nome
	 * @param nome O nome do orgao que está procurando
	 * @return Orgao orgao que foi encontrado
	 */
	public Orgao findByNome(String nome) {
		Query query = this.em.createNamedQuery(Orgao.FIND_BY_NOME).setParameter("nome", nome);

		return (Orgao) query.getSingleResult();
	}

	/**
	 * Cria o orgao "em espera"
	 * @author Rafael Hosaka
	 */
	public void criarEmEspera(){
		Orgao emEspera = new Orgao(0,0,"Em Espera",null, null,null, null, null, false);
		persist(emEspera);
	}

	/**
	 * Recupera orgao "em espera"
	 * @return Orgao "em espera"
	 * @author Rafael Hosaka
	 */
	public Orgao findOrgaoEmEspera(){
		Orgao orgao = null;
		try{
			Query query = this.em.createNamedQuery(Orgao.EM_ESPERA);
			orgao = (Orgao) query.getSingleResult();
		} catch (NoResultException e) {

		}
		return orgao;
	}

	/**
	 * Move os servidores para o orgao "em espera"
	 * @param servidoresSelecionaveis
	 * @param data
	 * @author Rafael Hosaka
	 */
	public void moverServidoresEmEspera(List<Servidor> servidoresSelecionaveis,Date data) {
		ServidorDAO servDao = new ServidorDAO();
		Orgao emEspera = findOrgaoEmEspera();
		for (Servidor servidor : servidoresSelecionaveis) {
			servDao.delete(servidor, data);
			moverServidorEmEspera(servidor,data,emEspera);
		}
	}

	/**
	 * move o servidor para o orgao "em espera"
	 * @param servidor
	 * @param data
	 * @param emEspera
	 * @author Rafael Hosaka
	 */
	public void moverServidorEmEspera(Servidor servidor,Date data,Orgao emEspera){
		servidor.setOrgao(emEspera);
		new ServidorDAO().forceUpdateFechandoPeriodo(servidor,data);
	}

	/**
	 * Filtra o orgao por nome
	 * @param filterOrg
	 * @param data
	 * @return lista de orgaos encontrado
	 * @author Rafael Hosaka
	 */
	@SuppressWarnings("unchecked")
	public List<Orgao> filtrarPorNome(String filterOrg,Date data) {
		Query q = em.createNamedQuery(Orgao.FILTER_NOME).setParameter("dtPesquisa", data).setParameter("param", "%"+filterOrg+"%");
		return (List<Orgao>) q.getResultList();
	}
	
	//Getter Setter
	public List<String> getListMudancas() {
		return listMudancas;
	}

	public void setListMudancas(List<String> listMudancas) {
		this.listMudancas = listMudancas;
	}

	public void excluirOrgaos(Orgao orgTemp, Date dataPesquisa) {
		for (Orgao o : new Util().recuperaListaOrgaoInferiores(orgTemp)) {
			delete(o, dataPesquisa);
		}
	}


	public void pdateJDBCBatchOrgaos(List<Orgao> orgaos,Date dataFim){
		String sqlInsertServidor = "UPDATE ORGAO SET DT_FIM_ORGAO = ? WHERE ID=? AND VERSAO=?";
		int resto = 0;
		try {
			Connection con = ConnectionFactory.getConnection();
			con.setAutoCommit(false);
			PreparedStatement st = con.prepareStatement(sqlInsertServidor);
			for(int i=0;i<orgaos.size();i++){
				Orgao org = orgaos.get(i);
				st.setDate(1,(dataFim==null?null:new java.sql.Date(dataFim.getTime())));
				st.setInt(2, org.getId());
				st.setInt(3, org.getVersao());
				st.addBatch();
				resto = i%100; 
				if(i%100==0)
					st.executeBatch();
			}
			if(resto > 0)
				st.executeBatch();
			con.commit();
			ConnectionFactory.close(st, con);
			em.getTransaction().begin();
			em.clear();
			em.getTransaction().commit();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void nsertJDBCBatchOrgaos(List<Orgao> orgaos){
		String sqlInsertServidor = "INSERT INTO ORGAO(ID,VERSAO,NOME,ID_SUPERIOR,VERSAO_SUPERIOR,DT_INICIO_ORGAO,DT_FIM_ORGAO)" +
				" VALUES(?,?,?,?,?,?,?)";
		try {
			Connection con = ConnectionFactory.getConnection();
			con.setAutoCommit(false);
			PreparedStatement st = con.prepareStatement(sqlInsertServidor);
			int resto = 0;
			for(int i=0;i<orgaos.size();i++){
				Orgao org = orgaos.get(i);
				st.setInt(1, org.getId());
				st.setInt(2, org.getVersao());
				st.setString(3, org.getNome());
				if(org.getSuperior()==null){
					st.setNull(4, java.sql.Types.INTEGER);
					st.setNull(5, java.sql.Types.INTEGER);
				}else{
					st.setInt(4,org.getSuperior().getId());
					st.setInt(5,org.getSuperior().getVersao());
				}
				st.setDate(6,new java.sql.Date(org.getDtInicioOrgao().getTime()));
				st.setDate(7,(java.sql.Date)null);
				st.addBatch();
				resto = i%100;
				if(resto==0)
					st.executeBatch();
			}
			if(resto>0)
				st.executeBatch();
			con.commit();
			ConnectionFactory.close(st, con);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

}
