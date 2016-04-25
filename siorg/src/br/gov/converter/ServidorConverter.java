package br.gov.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import br.gov.dao.ServidorDAO;
import br.gov.entity.Servidor;

@FacesConverter (value="ServidorConverter")
public class ServidorConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (value.equals("Nenhum"))
			return null;
		String chaves[] = value.split(" ");
		try {
			return new ServidorDAO().find(Integer.parseInt(chaves[0]),Integer.parseInt(chaves[1]));
		} catch (Exception e) {
			e.printStackTrace();
			return new Object();
		}
	}

	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null)
			return null;
		Servidor servidor = (Servidor) object;
		return (Integer.toString(servidor.getId())+" "+Integer.toString(servidor.getVersao()));
	}
}
