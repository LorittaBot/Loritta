package com.mrpowergamerbr.loritta.commands.nashorn;

import com.github.kevinsawicki.http.HttpRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Classe de utilidades para comandos usando o Nashorn
 */
public class NashornUtils {
	public static String loritta() { // Método teste
		return "Loritta!";
	}

	public static String getURL(String url) {
		return HttpRequest.get(url).userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0").body();
	}

	public static NashornImage downloadImage(String url) throws IOException {
		URL imageUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
		connection.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
		BufferedImage image = ImageIO.read(connection.getInputStream());
		NashornImage nashornImage = new NashornImage(image);
		return nashornImage;
	}

	public static Color createColor(int r, int g, int b) throws IOException {
		return new Color(r, g, b);
	}
}