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

	public NashornImage(BufferedImage image) {
		this.bufferedImage = image;
		graphics = bufferedImage.getGraphics();
	}

	public NashornImage escrever(String texto, int x, int y) {
		return escrever(texto, new Color(0, 0,0), x, y);
	}

	public NashornImage escrever(String texto, Color cor, int x, int y) {
		graphics.setColor(cor);
		ImageUtils.drawTextWrap(texto, x, y, 9999, 9999, graphics.getFontMetrics(), graphics);
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

	public NashornImage colar(NashornImage imagem, int x, int y) {
		graphics.drawImage(imagem.bufferedImage, x, y, null);
		return this;
	}

	public NashornImage cortar(int x, int y, int h, int w) {
		bufferedImage = bufferedImage.getSubimage(x, y, h, w);
		this.graphics = bufferedImage.getGraphics();
		return this;
	}

	public NashornImage preencherRetângulo(Color color, int x, int y, int h, int w) {
		graphics.setColor(color);
		graphics.fillRect(x, y, h, w);
		return this;
	}
}
