package it.govpay.gde.utils;

import java.lang.reflect.Method;

import org.springframework.data.domain.Page;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import it.govpay.gde.beans.PageInfo;
import jakarta.servlet.http.HttpServletRequest;

public class ListaUtils {
	
	private ListaUtils() {	}
	
	/** 
	 * Costruisce una lista paginata riempiendola con i riferimenti a 
	 * first, prev, next e last.
	 * 
	 * Mentre il limit non viene toccato.
	 * 
	 */
	public static final <T> T buildPaginatedList(
			Page<?> results,
			int limit,
			T destList)  {
		
		long startOffset = results.getNumber() * (long) limit;

		PageInfo pageInfo = new PageInfo(startOffset, limit);
		pageInfo.setTotal(results.getTotalElements());
		set(destList, "Page", pageInfo);
		
		return destList;
	}

	
	private static void set(Object obj, String field, Object value) {
		try {
			Method method = Class.forName(obj.getClass().getName()).getMethod("set"+field, value.getClass());
			method.invoke(obj, value);
		} catch(Exception e) {
			throw new InternalError(e);
		}
	}
	
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
