package com.mrpowergamerbr.loritta.utils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

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
			if (!graphics.getFont().canDisplay(c)) {
                continue;
            }
			graphics.drawString(String.valueOf(c), currentX, currentY); // Escreva o char na imagem
			currentX = currentX + width; // E adicione o width no nosso currentX
		}
		return currentY;
	}

    public static int drawTextWrapUndertale(String text, int startX, int startY, int endX, int endY, FontMetrics fontMetrics, Graphics graphics) {
        BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	    int lineHeight = fontMetrics.getHeight(); // Aqui é a altura da nossa fonte
        Font font = graphics.getFont(); // Font original
        int currentX = startX; // X atual
        int currentY = startY; // Y atual

        for (char c : text.toCharArray()) {
            int width = fontMetrics.charWidth(c); // Width do char (normalmente é 16)
            if ((currentX + width) > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
                currentX = startX; // Nós iremos fazer wrapping do texto
                currentY = currentY + lineHeight;
            }
            if (font.canDisplay(c)) {
                graphics.drawString(String.valueOf(c), currentX, currentY); // Escreva o char na imagem
            } else {
                graphics.setFont(temp.getGraphics().getFont());
                graphics.drawString(String.valueOf(c), currentX, currentY); // Escreva o char na imagem
                graphics.setFont(font);
            }
            currentX = currentX + width; // E adicione o width no nosso currentX
        }
        return currentY;
    }

	public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = output.createGraphics();

		// This is what we want, but it only does hard-clipping, i.e. aliasing
		// g2.setClip(new RoundRectangle2D ...)

		// so instead fake soft-clipping by first drawing the desired clip shape
		// in fully opaque white with antialiasing enabled...
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

		// ... then compositing the image on top,
		// using the white shape from above as alpha source
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, null);

		g2.dispose();

		return output;
	}

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param graphics The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics graphics, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = graphics.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Draw the String
        graphics.drawString(text, x, y);
    }
}
