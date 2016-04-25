package br.gov.lazymodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.gov.dao.ServidorDAO;
import br.gov.entity.Orgao;
import br.gov.entity.Servidor;
import br.gov.util.Util;

/**
 * Classe utilizada para realizar o lazyload do primefaces com a entidade servidor
 * @author Rafael Hosaka
 *
 */
public class LazyServidorModel extends LazyDataModel<Servidor>{

	private static final long serialVersionUID = -9113970373890768745L;

	private List<Servidor> datasource;
	private Date date;
	private List<Orgao> orgaosInferiores;


	public LazyServidorModel(Date data, List<Orgao> orgaosInferiores) {
		date = data;
		this.orgaosInferiores = orgaosInferiores;
	}

	@Override
	public Servidor getRowData(String rowKey) {
		for(Servidor serv : datasource) {
			if(serv.getId().equals(rowKey))
				return serv;
		}

		return null;
	}

	@Override
	public Object getRowKey(Servidor serv) {
		return serv.getId();
	}

	@Override
	public List<Servidor> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, String> filters) {
		List<Servidor> data = new ArrayList<Servidor>();
		try{
			if (filters != null) {
				boolean flag = false;
				for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {

					it.next();
					flag = true;
				}
				if(flag){

					String valor = null;
					String atributo = null;
					for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
						try {

							atributo = it.next();
							valor = filters.get(atributo);

						} catch(Exception e) {
							e.printStackTrace();
						}
					}

					if("matricula".equals(atributo)){
						data = new ServidorDAO().lazyFilterMat(first,pageSize,date,valor);
						this.setRowCount(new ServidorDAO().countByDateFilterMat(date,valor));
					}
					if("nome".equals(atributo)){
						data = new ServidorDAO().lazyFilterNome(first,pageSize,date,valor);
						this.setRowCount(new ServidorDAO().countByDateFilterNome(date,valor));
					}
				}else{
					data = new ServidorDAO().searchServidorByDate(first,pageSize,this.date);
					this.setRowCount(new ServidorDAO().countByDate(date));
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		//rowCount
		setCanEdit(data);
		setPageSize(pageSize);

		return data;

	}

	private void setCanEdit(List<Servidor> list){
		for (Servidor servidor : list) {
			servidor.setCanEdit(servidor.getDtFimServidor() == null && new Util().isServidorExisteEmOrgaos(servidor,orgaosInferiores));
		}
	}

}
