package com.mrpowergamerbr.loritta.utils.misc

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
	// hey 🅱hantae
	// some pretty dank meme by garfunk: https://cdn.discordapp.com/attachments/268586574185365504/332981056074416128/shantaepost.png

	// Gerador de backgrounds para o website da Loritta
	// As vezes eu quero regenerar o bg do website da Loritta, então eu deixo aqui :)


	val folder = File("D://ServerIconsv2")
	val listOfFiles = folder.listFiles()

	var calculateY = 0;
	var numberOfImageFiles = 0;

	val bufferedImages = mutableListOf<BufferedImage>();

	for (file in listOfFiles) {
		if (file.isFile()) {
			if (file.extension == "png") {
				numberOfImageFiles += 1;
				bufferedImages.add(ImageIO.read(file));
			}
		}
	}

	println("Imagens: $numberOfImageFiles")

	calculateY = (numberOfImageFiles / 6) * 128

	var x = 0;
	var y = 0;
	val image = BufferedImage(768, calculateY, BufferedImage.TYPE_INT_ARGB)

	val graphics = image.graphics;

	for (icon in bufferedImages) {
		if (x > 768) {
			y += 128;
			x = 0;
		}
		graphics.drawImage(icon, x, y, null)
		x += 128;
	}

	ImageIO.write(image, "png", File("D://ServerIconsv2//servers.png"))
}