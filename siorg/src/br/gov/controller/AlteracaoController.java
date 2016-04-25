package br.gov.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import br.gov.dao.AlteracaoDAO;
import br.gov.entity.Alterado;
import br.gov.entity.BaseEntity;

@ManagedBean(name = "alteracaoController")
@ViewScoped
public class AlteracaoController implements Serializable {
	private static final long serialVersionUID = 5817646097834090646L;

	private AlteracaoDAO dao;

	public AlteracaoController() {
		this.dao = new AlteracaoDAO();

	}

	public Alterado findEntity(BaseEntity entity,char tipo) {
		return dao.findEntity(entity, tipo);
	}

	public AlteracaoDAO getDao() {
		return dao;
	}

	public void setDao(AlteracaoDAO dao) {
		this.dao = dao;
	}

}
