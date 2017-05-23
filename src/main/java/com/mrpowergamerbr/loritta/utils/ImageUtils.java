package com.mrpowergamerbr.loritta.utils;

import java.awt.FontMetrics;
import java.awt.Graphics;

public class ImageUtils {
	/**
	 * Escreve um texto em um Graphics, fazendo wrap caso necessário
	 * @param text Texto
	 * @param startX X inicial
	 * @param startY Y inicial
	 * @param endX X máximo, caso o texto ultrapasse o endX, ele automaticamente irá fazer wrap para a próxima linha
	 * @param endY Y máximo, atualmente unused
	 * @param fontMetrics Metrics da fonte
	 * @param graphics Graphics usado para escrever a imagem
	 * @return Y final
	 */
	public static int drawTextWrap(String text, int startX, int startY, int endX, int endY, FontMetrics fontMetrics, Graphics graphics) {
		int lineHeight = fontMetrics.getHeight(); // Aqui é a altura da nossa fonte

		int currentX = startX; // X atual
		int currentY = startY; // Y atual

		for (char c : text.toCharArray()) {
			int width = fontMetrics.charWidth(c); // Width do char (normalmente é 16)
			if ((currentX + width) > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
				currentX = startX; // Nós iremos fazer wrapping do texto
				currentY = currentY + lineHeight;
			}
			graphics.drawString(String.valueOf(c), currentX, currentY); // Escreva o char na imagem
			currentX = currentX + width; // E adicione o width no nosso currentX
		}
		return currentY;
	}
}
