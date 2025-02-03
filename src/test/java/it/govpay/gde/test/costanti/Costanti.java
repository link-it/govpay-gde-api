package it.govpay.gde.test.costanti;

import java.time.format.DateTimeFormatter;

public class Costanti {

	public static final String API_BASE_PATH = "";
	public static final String EVENTI_PATH = API_BASE_PATH + "/eventi";
	public static final String EVENTO_PATH = EVENTI_PATH + "/{id}";
	
	public static final String PATTERN_DATA_JSON_YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	public static final String PATTERN_DATA_JSON_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
	
	public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATA_JSON_YYYY_MM_DD_T_HH_MM_SS_SSS_Z); 
	
	public static final String ID_DOMINIO_1 = "12345678901";
	
	public static final String STRING_256= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnop";
}