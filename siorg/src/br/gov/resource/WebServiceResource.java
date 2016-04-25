package br.gov.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.gov.dao.CargoDAO;
import br.gov.dao.OrgaoDAO;
import br.gov.dao.ServidorDAO;
import br.gov.entity.Cargo;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;

@Path("/service")
public class WebServiceResource {

	private static final String CABECALHO_XML = "<?xml version=\"1.0\"?>";
	
	public WebServiceResource() {
	
	}
	
	@GET
	@Path("/completo")
	@Produces(MediaType.TEXT_XML)
	public String completeXML() {
		Orgao orgao = new OrgaoDAO()
				.findOrgaoSuperior();
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append("<orgaos>");
		sb.append(orgao.toXML());
		sb.append("</orgaos>");
		return sb.toString();
	}
	
	@GET
	@Path("/todosServidores")
	@Produces(MediaType.TEXT_XML)
	public String todosServidores() {
		List<Servidor> servidores = new ServidorDAO().findWithNamedQuery(Servidor.HEAD);
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append("<servidores>");
		for (Servidor servidor : servidores) {
			sb.append(servidor.toXML());
		}
		sb.append("</servidores>");
		return sb.toString();
	}
	
	@GET
	@Path("/todosOrgaos")
	@Produces(MediaType.TEXT_XML)
	public String todosOrgaos() {
		List<Orgao> orgaos = new OrgaoDAO().findWithNamedQuery(Orgao.HEAD);
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append("<orgaos>");
		for (Orgao orgao : orgaos) {
			sb.append(orgao.toXML(false));
		}
		sb.append("</orgaos>");
		return sb.toString();
	}
	
	@GET
	@Path("/todosCargos")
	@Produces(MediaType.TEXT_XML)
	public String todosCargos() {
		List<Cargo> cargos = new CargoDAO().findWithNamedQuery(Cargo.HEAD);
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append("<cargos>");
		for (Cargo cargo : cargos) {
			sb.append(cargo.toXML());
		}
		sb.append("</cargos>");
		return sb.toString();
	}
	
	@GET
	@Path("/servidoresPorCargo/{cargo}")
	@Produces(MediaType.TEXT_XML)
	public String servidoresPorCargo(@PathParam("cargo") String cargo) {
		Cargo c = new CargoDAO().findByNome(cargo);
		List<Servidor> servidores = new ServidorDAO().findByCargo(c);
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append("<servidores>");
		for (Servidor servidor : servidores) {
			sb.append(servidor.toXML());
		}
		sb.append("</servidores>");
		return sb.toString();
	
	}
	
	@GET
	@Path("/servidoresPorOrgao/{orgao}")
	@Produces(MediaType.TEXT_XML)
	public String servidoresPorOrgao(@PathParam("orgao") String orgao) {
		Orgao o = new OrgaoDAO().findByNome(orgao);
		List<Servidor> servidores = new ServidorDAO().findServidoresByOrgao(o);
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append("<servidores>");
		for (Servidor servidor : servidores) {
			sb.append(servidor.toXML());
		}
		sb.append("</servidores>");
		return sb.toString();
	}
	
	@GET
	@Path("/servidorPorMatricula/{matricula}")
	@Produces(MediaType.TEXT_XML)
	public String servidoresPorMatricula(@PathParam("matricula") String matricula) {
		Servidor servidor = new ServidorDAO().findServidorByMatricula(matricula);
		StringBuilder sb = new StringBuilder();
		sb.append(CABECALHO_XML);
		sb.append(servidor.toXML());
		return sb.toString();
	}
}