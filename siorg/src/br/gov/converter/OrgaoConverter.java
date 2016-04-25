package br.gov.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import br.gov.dao.OrgaoDAO;
import br.gov.entity.Orgao;

@FacesConverter (value="OrgaoConverter")
public class OrgaoConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (value.equals("Nenhum"))
			return null;
		try {
			String chaves[] = value.split(" "); 
			return new OrgaoDAO().find(Integer.parseInt(chaves[0]),Integer.parseInt(chaves[1]));
		} catch (Exception e) {
			e.printStackTrace();
			return new Object();
		}
	}

	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null)
			return null;
		Orgao orgao = (Orgao) object;
		return (Integer.toString(orgao.getId())+" "+Integer.toString(orgao.getVersao()));
	}
}
