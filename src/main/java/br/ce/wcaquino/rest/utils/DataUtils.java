package br.ce.wcaquino.rest.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataUtils {

	public static String getDataDiferencaDias(Integer qtdeDias) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, qtdeDias);
		//formatar em String
		return getDataFormatada(calendar.getTime());
		
	}
	
	public static String getDataFormatada(Date data) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		return format.format(data);
	}
	
}
