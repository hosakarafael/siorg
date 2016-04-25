package br.gov.lazymodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.gov.dao.TipoCargoDAO;
import br.gov.entity.TipoCargo;

/**
 * Classe utilizada para realizar o lazyload do primefaces com a entidade tipo cargo
 * @author Rafael Hosaka
 *
 */
public class LazyTipoCargoModel extends LazyDataModel<TipoCargo>{
	
	private static final long serialVersionUID = -7120123274171856356L;
	
	private List<TipoCargo> datasource;
	private Date date;

	public LazyTipoCargoModel(Date data) {
		date = data;
	}

	@Override
	public TipoCargo getRowData(String rowKey) {
		for(TipoCargo tipo : datasource) {
			if(tipo.getId().equals(rowKey))
				return tipo;
		}

		return null;
	}

	@Override
	public Object getRowKey(TipoCargo tipo) {
		return tipo.getId();
	}

	@Override
	public List<TipoCargo> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, String> filters) {
		List<TipoCargo> data = new ArrayList<TipoCargo>();
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

					data = new TipoCargoDAO().lazyFilterDesc(first,pageSize,date,valor);
					this.setRowCount(new TipoCargoDAO().countByDateFilterDesc(date,valor));
				}else{
					data = new TipoCargoDAO().searchTipoCargoByDate(first,pageSize,this.date);
					this.setRowCount(new TipoCargoDAO().countByDate(date));
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

	private void setCanEdit(List<TipoCargo> list){
		for (TipoCargo tipo : list) {
			tipo.setCanEdit(tipo.getDtFimTipoCargo() == null );
		}
	}
}
