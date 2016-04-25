package br.gov.lazymodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.gov.dao.CargoDAO;
import br.gov.entity.Cargo;

/**
 * Classe utilizada para realizar o lazyload do primefaces com a entidade cargo
 * @author Rafael Hosaka
 *
 */
public class LazyCargoModel extends LazyDataModel<Cargo>{
	
	private static final long serialVersionUID = 1267761063510848424L;
	
	private List<Cargo> datasource;
	private Date date;


	public LazyCargoModel(Date data) {
		date = data;
	}

	@Override
	public Cargo getRowData(String rowKey) {
		for(Cargo cargo : datasource) {
			if(cargo.getId().equals(rowKey))
				return cargo;
		}

		return null;
	}

	@Override
	public Object getRowKey(Cargo cargo) {
		return cargo.getId();
	}

	@Override
	public List<Cargo> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, String> filters) {
		List<Cargo> data = new ArrayList<Cargo>();
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

					data = new CargoDAO().lazyFilterNome(first,pageSize,date,valor);
					this.setRowCount(new CargoDAO().countByDateFilterNome(date,valor));
				}else{
					data = new CargoDAO().searchCargoByDate(first,pageSize,this.date);
					this.setRowCount(new CargoDAO().countByDate(date));
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

	private void setCanEdit(List<Cargo> list){
		for (Cargo cargo : list) {
			cargo.setCanEdit(cargo.getDtFimCargo() == null);
		}
	}

}
	
