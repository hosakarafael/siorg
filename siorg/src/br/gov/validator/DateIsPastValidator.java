package br.gov.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("DateIsPastValidator")
public class DateIsPastValidator implements Validator {

	/**
	 * Validator utilizado para validar se a data é passada
	 * @author Rafael Hosaka
	 */
	@Override
	public void validate(FacesContext faces, UIComponent arg1, Object value)
			throws ValidatorException {
		SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
		Date today = new Date();
		Date dtPesq = null;
		try {
			if(value.toString().isEmpty()){
				FacesMessage msg = 
						new FacesMessage("Erro", 
								"A data não pode vazia!");
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(msg);
			}else{

				if(!isPadraoValido(value)){
					FacesMessage msg = 
							new FacesMessage("Erro", 
									"Data Inválida!Por favor informe novamente!");
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					throw new ValidatorException(msg);
				}else{
					dtPesq = sd.parse(value.toString());
					if(!isDataValida(dtPesq)){
						FacesMessage msg = 
								new FacesMessage("Erro", 
										"Data Inválida!Por favor informe novamente!");
						msg.setSeverity(FacesMessage.SEVERITY_ERROR);
						throw new ValidatorException(msg);
					}else{
						if(today.before(dtPesq)){
							FacesMessage msg = 
									new FacesMessage("Erro", 
											"A data não pode ser do futuro!");
							msg.setSeverity(FacesMessage.SEVERITY_ERROR);
							throw new ValidatorException(msg);
						}
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}


	}

	/**
	 * Verifica posteriormente se o valor está com dia válido e mes válido
	 * pois ao atribuir valores muito alto no Date, irá atribuir com o seu limite
	 * assim mudando sua natureza
	 * @param value
	 * @return true se é valido
	 *         false se é invalido
	 * @author Rafael Hosaka
	 */
	private boolean isPadraoValido(Object value) {
		String[] valores = value.toString().split("/");
		int dia = Integer.valueOf(valores[0]);
		int mes = Integer.valueOf(valores[1]);

		if(dia < 1 || dia > 31){
			return false;
		}
		if(mes<1 || mes > 12){
			return false;
		}

		return true;
	}

	/**
	 * Verifica se a data é valida
	 * @param data
	 * @return true se é valida
	 *         false se é invalida
	 * @author Rafael Hosaka
	 */
	private boolean isDataValida(Date data){
		boolean flag = true;

		Calendar calendar = Calendar.getInstance();  
		calendar.setTime(data);

		int dia = calendar.get(Calendar.DAY_OF_MONTH);
		int mes = calendar.get(Calendar.MONTH);
		int ano = calendar.get(Calendar.YEAR);

		switch (mes+1) {
		case 2:	
			if(ano % 400 == 0 || ano % 100 == 0 || ano % 4 == 0){
				if(dia>29 || dia < 1){
					flag = false; 
				}

			}else{
				if(dia>28 || dia < 1){
					flag = false; 
				}

			}
			break;
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			if(dia>31 || dia < 1){
				flag = false; 
			}
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			if(dia>30 || dia < 1){
				flag = false; 
			}
			break;
		default:
			flag = false;
		}

		return flag;
	}

}
