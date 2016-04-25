package br.gov.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import br.gov.dao.TipoCargoDAO;
import br.gov.entity.TipoCargo;
@FacesConverter (value="TipoCargoConverter")
public class TipoCargoConverter implements Converter{

	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (value.equals("Nenhum"))
			return null;
		try {
			String chaves[] = value.split(" "); 
			return new TipoCargoDAO().find(Integer.parseInt(chaves[0]),Integer.parseInt(chaves[1]));
		} catch (Exception e) {
			e.printStackTrace();
			return new Object();
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value == null)
			return null;
		TipoCargo tipo = (TipoCargo) value;
		return (Integer.toString(tipo.getId())+" "+Integer.toString(tipo.getVersao()));
	}
}
