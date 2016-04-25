package br.gov.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.gov.dao.CargoDAO;
import br.gov.dao.OrgaoDAO;
import br.gov.dao.PerfilDAO;
import br.gov.dao.ServidorDAO;
import br.gov.dao.TipoCargoDAO;
import br.gov.entity.Autorizacao;
import br.gov.entity.BaseEntity;
import br.gov.entity.Cargo;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.entity.TipoCargo;

public class Util {
	
	private static long inicio;
	/**
	 * Monta a estrutura de arvore para ser apresentada
	 * @param root O nó da arvore inicial
	 * @param org Orgão superior inicial
	 * @param data Data que fará a restrição da montagem
	 * @return O nó da arvore
	 * @author Rafael Hosaka
	 */
	public TreeNode createOrgaoTreeNodeDate(TreeNode root, Orgao org, Date data, List<Orgao> orgaosInferiores) {
		TreeNode node;
		if ((org.getDtInicioOrgao().compareTo(data) < 0 || org
				.getDtInicioOrgao().compareTo(data) == 0)
				&& (org.getDtFimOrgao() == null || org.getDtFimOrgao()
				.compareTo(data) > 0)) {

			org.setCanEdit(org.getDtFimOrgao() == null && orgaosInferiores.contains(org));
			node = new DefaultTreeNode(org, root);
			for (Orgao o : org.getOrgaos()) {
				o.setCanEdit(o.getDtFimOrgao() == null && orgaosInferiores.contains(o));

				@SuppressWarnings("unused")
				TreeNode temp = createOrgaoTreeNodeDate(node, o, data, orgaosInferiores);

			}
		}
		return root;
	}

	/**
	 * Monta a estrutura de arvore para ser apresentada
	 * @param root O nó da arvore inicial
	 * @param org Orgão superior inicial
	 * @param data Data que fará a restrição da montagem
	 * @return O nó da arvore
	 * @author Rafael Hosaka
	 */
	public TreeNode createOrgaoTreeNodeDate(TreeNode root, Orgao org, Date data) {
		TreeNode node;
		if ((org.getDtInicioOrgao().compareTo(data) < 0 || org
				.getDtInicioOrgao().compareTo(data) == 0)
				&& (org.getDtFimOrgao() == null || org.getDtFimOrgao()
				.compareTo(data) > 0)) {

			node = new DefaultTreeNode(org, root);
			for (Orgao o : org.getOrgaos()) {

				TreeNode temp = createOrgaoTreeNodeDate(node, o, data);

			}
		}
		return root;
	}


	/**
	 * Expande a arvore para apresentar
	 * @param n o nó da arvore
	 * @param opcao true se quer expandir a arvore false se nã oquer expandir na apresentação
	 * @author Rafael Hosaka
	 */
	public void expandirNode(TreeNode n, boolean opcao) {

		if (n.getChildren().size() == 0) {
			n.setSelected(false);
		} else {
			for (TreeNode s : n.getChildren()) {
				expandirNode(s, opcao);
			}
			n.setExpanded(opcao);
		}
	}

	/**
	 * Recupera os órgãos inferiores apartir de um órgão
	 * @param orgao orgao superior inicial
	 * @return List<Orgao> lista dos orgaos inferiores
	 * @author Rafael Hosaka
	 */
	public List<Orgao> recuperaListaOrgaoInferiores(Orgao orgao) {
		List<Orgao> lista = new ArrayList<Orgao>();
		//orgao.setCanEdit(orgao.getDtFimOrgao() == null);
		if (orgao.getDtFimOrgao() == null) {
			lista.add(orgao);
		}
		for (Orgao org : orgao.getOrgaos()) {
			lista.addAll(recuperaListaOrgaoInferiores(org));
		}
		return lista;
	}

	/**
	 * Recupera todos servidores de todos os orgaos abaixo
	 * @param orgao superior inicial
	 * @return List<Servidor> lista dos servidores inferiores
	 * @author Rafael Hosaka
	 */
	public List<Servidor> recuperaListaServidoresInferiores(Orgao orgao) {
		List<Servidor> lista = new ArrayList<Servidor>();
		for (Servidor serv : orgao.getServidores()) {
			if (serv.getDtFimServidor() == null) {
				lista.add(serv);
			}
		}

		for (Orgao org : orgao.getOrgaos()) {
			lista.addAll(recuperaListaServidoresInferiores(org));
		}
		return lista;
	}

	/**
	 * Criptografa uma string
	 * @param original valor a ser criptografado
	 * @return o valor criptografado
	 * @author Rafael Hosaka
	 */
	public String encriptPassword(String original) {
		String senha = null;
		MessageDigest algorithm;
		try {
			algorithm = MessageDigest.getInstance("SHA-256");

			byte messageDigest[] = algorithm.digest(original.getBytes("UTF-8"));

			StringBuilder hexString = new StringBuilder();
			for (byte b : messageDigest) {
				hexString.append(String.format("%02X", 0xFF & b));
			}

			senha = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return senha;
	}

	/**
	 * Verifica a instancia do Objeto
	 * @param entity o objeto
	 * @return 'C' se é Cargo, 'O' se é Orgao, 'S' se é Servidor e 'T' se é TipoCargo
	 * @author Paulo Roberto
	 */
	@SuppressWarnings("rawtypes")
	public static char getEntityTipo(BaseEntity entity) {
		char tipo = ' ';
		Class classe = entity.getClass();
		if (classe == Cargo.class)
			tipo = 'C';
		else if (classe == Orgao.class)
			tipo = 'O';
		else if (classe == Servidor.class)
			tipo = 'S';
		else if (classe == TipoCargo.class)
			tipo = 'T';
		return tipo;
	}

	/**
	 * Converte a string em uma Date
	 * @param strDate String a ser convertida em Date
	 * @param padrao padrao da data após ser convertida 
	 * @return Date data convertida
	 * @throws ParseException
	 * @author Paulo Roberto
	 */
	public static Date parseDate(String strDate,String padrao) throws ParseException {
		SimpleDateFormat sd = new SimpleDateFormat(padrao);
		return sd.parse(strDate);
	}

	/**
	 * Retira o servidores que são ADM
	 * @param servidores lista de servidores que fará a verificação
	 * @return lista de servidores que não são adm
	 * @author Rafael Hosaka
	 */
	public List<Servidor> retiraServidoresAdm(List<Servidor> servidores) {
		PerfilDAO dao = new PerfilDAO();
		List<Autorizacao> list = dao.findAutorizacoesAdm();
		List<Servidor> servTemp = new ArrayList<Servidor>();
		servTemp.addAll(servidores);
		for (Autorizacao autorizacao : list) {
			for (Servidor serv : servidores) {
				if ((serv.getMatricula().equals(autorizacao.getUsuario().getMatricula()))
						|| (serv.getDtFimServidor() != null)) {
					servTemp.remove(serv);
				}
			}

		}
		return servTemp;
	}

	/**
	 * Recupera todos usuario que são ADMs
	 * @param servidores lista de servidores que fará a verificação
	 * @return lsita de servidores que são ADM
	 * @author Rafael Hosaka
	 */
	public List<Servidor> recuperaAdm(List<Servidor> servidores) {
		PerfilDAO dao = new PerfilDAO();
		List<Autorizacao> list = dao.findAutorizacoesAdm();
		List<Servidor> servTemp = new ArrayList<Servidor>();
		for (Autorizacao autorizacao : list) {
			for (Servidor serv : servidores) {
				if ((serv.getMatricula().equals(autorizacao.getUsuario().getMatricula()))) {
					servTemp.add(serv);
				}
			}

		}
		return servTemp;
	}

	/**
	 * Cria um arquivo xml como uma estrura de arvore 
	 * @param nomeArq Nome do arquivo a ser gerado
	 * @param org Orgao superior inicial
	 * @throws IOException
	 * @author Rafael Hosaka
	 */
	public void criarXml(String nomeArq, Orgao org) throws IOException {
		Document doc = new Document();

		Element root = new Element("orgaos");

		Element orgElem = new Element("orgao");
		Attribute nome = new Attribute("nome", org.getNome());
		orgElem.setAttribute(nome);
		for (Orgao o : org.getOrgaos()) {
			if (o.getDtFimOrgao() == null) {
				Element orgElemTemp = new Element("orgao");
				Attribute a1 = new Attribute("nome", o.getNome());
				orgElemTemp.setAttribute(a1);
				orgElem.addContent(orgElemTemp);
				if (o.getOrgaos() != null) {
					criarXml(o, orgElemTemp);
				}
			}
		}
		root.addContent(orgElem);

		doc.setRootElement(root);

		XMLOutputter xout = new XMLOutputter();
		OutputStream out = new FileOutputStream(new File(nomeArq));
		xout.output(doc, out);

	}

	/**
	 * Cria recursivamente as estruturas de um orgao interna
	 * @param org Orgao que fará criação da estrutura
	 * @param orgElem Elemento xml do orgao
	 * @author Rafael Hosaka
	 */
	private void criarXml(Orgao org, Element orgElem) {
		Element orgElemTemp = null;
		for (Orgao o : org.getOrgaos()) {
			if (o.getDtFimOrgao() == null) {
				orgElemTemp = new Element("orgao");
				Attribute a1 = new Attribute("nome", o.getNome());
				orgElemTemp.setAttribute(a1);
				orgElem.addContent(orgElemTemp);
				if (o.getOrgaos() != null) {
					criarXml(o, orgElemTemp);
				}
			}
		}
	}


	/**
	 * Cria um arquivo xml como uma estrura de arvore 
	 * @param nomeArq Nome do arquivo a ser gerado
	 * @param org Orgao superior inicial
	 * @param data Data que será a referencia da criação
	 * @throws IOException
	 * @author Rafael Hosaka
	 */
	public void criarXml(String nomeArq, Orgao org,Date data) throws IOException {
		Document doc = new Document();

		Element root = new Element("orgaos");

		Element orgElem = new Element("orgao");
		Attribute nome = new Attribute("nome", org.getNome());
		orgElem.setAttribute(nome);
		for (Orgao o : org.getOrgaos()) {
			if ((o.getDtInicioOrgao().compareTo(data) < 0 || o
					.getDtInicioOrgao().compareTo(data) == 0)
					&& (o.getDtFimOrgao() == null || o.getDtFimOrgao()
					.compareTo(data) > 0)) {
				Element orgElemTemp = new Element("orgao");
				Attribute a1 = new Attribute("nome", o.getNome());
				orgElemTemp.setAttribute(a1);
				orgElem.addContent(orgElemTemp);
				if (o.getOrgaos() != null) {
					criarXml(o, orgElemTemp,data);
				}
			}
		}
		root.addContent(orgElem);

		doc.setRootElement(root);

		XMLOutputter xout = new XMLOutputter();
		OutputStream out = new FileOutputStream(new File(nomeArq));
		xout.output(doc, out);

	}

	/**
	 * Cria recursivamente as estruturas de um orgao interna
	 * @param org Orgao que fará criação da estrutura
	 * @param orgElem Elemento xml do orgao
	 * @param data Data que será a referencia da criação
	 * @author Rafael Hosaka
	 */
	private void criarXml(Orgao org, Element orgElem,Date data) {
		Element orgElemTemp = null;
		for (Orgao o : org.getOrgaos()) {
			if ((o.getDtInicioOrgao().compareTo(data) < 0 || o
					.getDtInicioOrgao().compareTo(data) == 0)
					&& (o.getDtFimOrgao() == null || o.getDtFimOrgao()
					.compareTo(data) > 0)) {
				orgElemTemp = new Element("orgao");
				Attribute a1 = new Attribute("nome", o.getNome());
				orgElemTemp.setAttribute(a1);
				orgElem.addContent(orgElemTemp);
				if (o.getOrgaos() != null) {
					criarXml(o, orgElemTemp,data);
				}
			}
		}
	}

	/**
	 * Verifica se a matricula já existe
	 * @param matricula Matricula que fará a verificação
	 * @return true se a matricula existe, false se não existe
	 * @author Rafael Hosaka
	 */
	public boolean isMatriculaRepetida(String matricula) {
		List<String> matriculas = new ServidorDAO().recuperaTodasMatriculas();
		for (String m : matriculas) {
			if (m.equals(matricula)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se o cargo já exste no orgao
	 * @param servidor Servidor que será verificado
	 * @param orgao Orgao que será analisado
	 * @return true se existe, false se não existe
	 * @author Rafael Hosaka
	 */
	public boolean isCargoRepetidoEmOrgao(Servidor servidor, Orgao orgao) {
		List<Servidor> servidores = new ServidorDAO().findServidoresByOrgaoAndDate(orgao,new Date());
		for (Servidor serv : servidores) {
			if(servidor.getCargo() != null && !servidor.equals(serv) && serv.getCargo() != null){
				if(serv.getCargo().equals(servidor.getCargo())){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Atribui todos os cargos dos servidores como null
	 * @param servidoresSelecionaveis lista de servidores que será atribuido como null
	 * @author Rafael Hosaka
	 */
	public void settingServidoresCargoNull(List<Servidor> servidoresSelecionaveis) {
		for (Servidor servidor : servidoresSelecionaveis) {
			servidor.setCargo(null);
		}
	}

	/**
	 * Remove recursivamente todos orgaos inferiores
	 * @param lista
	 * @param orgao que será a referencia para retirar abaixo
	 * @return Lista com os orgaos retirados
	 * @author Paulo Roberto
	 */
	public List<Orgao> removeOrgaosLista(List<Orgao> lista, Orgao orgao){
		for (Orgao o : orgao.getOrgaos()) {
			removeOrgaosLista(lista, o);
			lista.remove(o);
		}
		return lista;
	}

	/**
	 * Verifica se existe o orgao "em espera" no banco
	 * @return true se existe
	 *         false se não existe
	 * @author Rafael Hosaka
	 */
	public boolean isExisteOrgaoEmEspera(){
		if(new OrgaoDAO().findOrgaoEmEspera() == null){
			return false;
		}
		return true;
	}

	/**
	 * Verifica se já existe orgao com o nome informado
	 * @param org
	 * @return true se existe
	 *         false se não existe
	 * @author Rafael Hosaka
	 */
	public boolean isOrgaoNomeRepetido(Orgao org) {
		for (Orgao o : new OrgaoDAO().searchOrgaoByDate(new Date())) {
			if(o.getNome().equals(org.getNome().trim()) && o.getId() != org.getId()){
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se já existe cargo com o nome informado 
	 * @param cargo
	 * @return true se existe
	 *         false se não existe
	 * @author Rafael Hosaka        
	 */
	public boolean isCargoNomeRepetido(Cargo cargo) {
		for (Cargo c : new CargoDAO().searchCargoByDate(new Date())) {
			if(c.getNome().equals(cargo.getNome().trim()) && c.getId() != cargo.getId()){
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se já existe tipo cargo com a descrição informada
	 * @param tipo
	 * @return true se existe 
	 *         false se não existe
	 * @author Rafael Hosaka
	 */
	public boolean isTipoCargoDescRepetido(TipoCargo tipo) {
		for (TipoCargo t : new TipoCargoDAO().searchTipoCargoByDate(new Date())) {
			if(t.getDescricao().equals(tipo.getDescricao().trim()) && t.getId() != tipo.getId()){
				return true;
			}
		}
		return false;
	}

	/**
	 * Monta a treenode sem hierarquia
	 * @param root nó da arvore
	 * @param list 
	 * @return root
	 * @author Rafael Hosaka
	 */
	public TreeNode montarArvoreSemHierarquia(TreeNode root,List<Orgao> list){
		for (Orgao orgao : list) {
			@SuppressWarnings("unused")
			TreeNode node = new DefaultTreeNode(orgao, root);
		}
		return root;
	}

	/**
	 * Verifica se o servidor existe em uma lista de orgaos
	 * @param servidor
	 * @param orgaosInferiores
	 * @return true se existe
	 *         false se não existe
	 * @author Rafael Hosaka
	 */
	public boolean isServidorExisteEmOrgaos(Servidor servidor,List<Orgao> orgaosInferiores) {
		for (Orgao orgao : orgaosInferiores) {
			if(servidor.getOrgao().equals(orgao)){
				return true;
			}
		}
		return false;
	}

}
