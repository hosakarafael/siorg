package br.gov.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import br.gov.util.Util;

@FacesValidator("MatriculaValidator")
public class MatriculaValidator implements Validator {

	/**
	 * Validator utilizado para validar se a matricula é repetida
	 * @author Rafael Hosaka
	 */
	@Override
	public void validate(FacesContext faces, UIComponent arg1, Object value)
			throws ValidatorException {
		
			if(new Util().isMatriculaRepetida((String)value)){
				FacesMessage msg = 
						new FacesMessage("Erro", 
								"Matrícula repetida! Já existe um servidor com essa matrícula.");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(msg);
			}
	}
}