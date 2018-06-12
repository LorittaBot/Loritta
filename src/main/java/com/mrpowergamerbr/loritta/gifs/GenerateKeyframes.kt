package com.mrpowergamerbr.loritta.gifs

import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
	for (i in 0..70) {
		var image = ImageIO.read(File("D:\\TavaresBot\\trumptest\\frame_${i}.png"))

		var x0 = 0;
		var y0 = 0;
		var x1 = 0;
		var y1 = 0;
		var x2 = 0;
		var y2 = 0;
		var x3 = 0;
		var y3 = 0;

		for (x in 0..image.width - 1) {
			for (y in 0..image.height - 1) {
				var rgb = image.getRGB(x, y)

				var red = (rgb shr 16) and 0xFF;
				var green = (rgb shr 8) and 0xFF;
				var blue = rgb and 0xFF;


				if (red == 255 && green == 255 && blue == 0) { // amarelo
					x1 = x;
					y1 = y;
					continue;
				}

				if (green == 255 && blue == 255 && red == 0) { // azul
					x3 = x;
					y3 = y;
					continue;
				}

				if (red == 255 && green == 0 && blue == 0) { // vermelho
					x0 = x;
					y0 = y;
					continue;
				}

				if (green == 255 && red == 0 && blue == 0) { // verde
					x2 = x;
					y2 = y;
					continue;
				}
			}
		}

		println("\t\t\tSkewCorners(${x0}F, ${y0}F, // UL\n" +
				"\t\t\t${x1}F, ${y1}F, // UR\n" +
				"\t\t\t${x2}F, ${y2}F, // LR\n" +
				"\t\t\t${x3}F, ${y3}F), // LL");
	}
}