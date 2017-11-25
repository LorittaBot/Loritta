package com.mrpowergamerbr.loritta.commands.nashorn;

import com.github.kevinsawicki.http.HttpRequest;
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornImage;
import com.mrpowergamerbr.loritta.utils.Constants;
import com.mrpowergamerbr.loritta.utils.LorittaUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Classe de utilidades para comandos usando o Nashorn
 */
public final class NashornUtils {
	private NashornUtils() {
	}

	public static String loritta() { // Método teste
		return "Loritta!";
	}

	public static String getURL(String url) {
		return HttpRequest.get(url).userAgent(Constants.USER_AGENT).body();
	}

	public static NashornImage downloadImage(String url) throws IOException {
		BufferedImage image = LorittaUtils.downloadImage(url);
		NashornImage nashornImage = new NashornImage(image);
		return nashornImage;
	}

	public static Color createColor(int r, int g, int b) throws IOException {
		return new Color(r, g, b);
	}
}