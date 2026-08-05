package it.govpay.gde.utils;

import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import jakarta.servlet.http.HttpServletRequest;

public class ListaUtils {

	private ListaUtils() {	}

	public static String createLocation(HttpServletRequest request, Long id) {
		
		
		UriBuilder builder = new DefaultUriBuilderFactory().builder()
				.scheme(request.getScheme())
				.host(request.getServerName())
				.port(request.getServerPort())
				.path(request.getRequestURI())
				.path("/{id}");
		
		return builder.build(id).toString();
	}
	
}
