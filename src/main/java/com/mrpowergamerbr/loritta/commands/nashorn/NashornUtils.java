package com.mrpowergamerbr.loritta.commands.nashorn;

import com.github.kevinsawicki.http.HttpRequest;

/**
 * Classe de utilidades para comandos usando o Nashorn
 */
public class NashornUtils {
	public static String loritta() { // Método teste
		return "Loritta!";
	}

	public static String getURL(String url) {
		return HttpRequest.get(url).body();
	}
}