package it.govpay.gde.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AttributeConverterException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AttributeConverterException(Throwable e) {
		super("Errore durante la conversione dell'attributo JPA: " + e.getLocalizedMessage() , e);
	}

}
