package com.mrpowergamerbr.loritta.commands.nashorn;

import com.mrpowergamerbr.loritta.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Wrapper para o BufferedImage, usado para imagens de comandos Nashorn
 */
public class NashornImage {
	private BufferedImage bufferedImage;
	private Graphics graphics;

	public NashornImage(int x, int y) {
		if (x > 1024 || y > 1024) {
			throw new LorittaNashornException("Imagem grande demais!");
		}
		bufferedImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		graphics = bufferedImage.getGraphics();
	}

	public NashornImage escrever(String texto, int x, int y) {
		return escrever(texto, x, y, 0, 0, 0);
	}

	public NashornImage escrever(String texto, int x, int y, int r, int g, int b) {
		graphics.setColor(new Color(r, g, b));
		graphics.drawString(texto, x, y);
		return this;
	}

	public NashornImage redimensionar(int x, int y) {
		if (x > 1024 || y > 1024) {
			throw new LorittaNashornException("Imagem grande demais!");
		}
		bufferedImage = ImageUtils.toBufferedImage(bufferedImage.getScaledInstance(x, y, BufferedImage.SCALE_SMOOTH));
		graphics = bufferedImage.getGraphics();
		return this;
	}
}
