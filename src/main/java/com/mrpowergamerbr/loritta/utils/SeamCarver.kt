package com.mrpowergamerbr.loritta.utils

/**
 * SeamCarver.java
 * Frederik Roenn Stensaeth
 * Date: 12.17.15
 *
 * Java program to perform content-aware image resizing using seam carving.
 */

import java.awt.image.BufferedImage

/**
 * SeamCarver() is a class for content aware image resizing.
 * Takes arguments from the command line:
 * - inputImage
 * - outputImage
 * - number of seams
 * - seam direction
 * - [--show]
 *
 * @param inputImage, outputImage, numOfSeams, seamDirection, [--show]
 * @return n/a. Stores the resized image as the desired filename.
 */
object SeamCarver {
	/**
	 * carveSeam() takes an image and removes a single seam from that image in the
	 * desired direction.
	 *
	 * @param image to be carved and direction of the seam (vertical / horizontal).
	 * @return carved image.
	 */
	fun carveSeam(image: BufferedImage?, direction: CarveDirection): BufferedImage {
		// We need to compute the energy table, find and remove a seam.
		var newImage: BufferedImage? = null
		val energyTable = computeEnergy(image!!)
		val seam = findSeam(energyTable, direction)
		newImage = removeSeam(image, seam, direction)

		return newImage
	}

	/**
	 * computeEnergy() takes an image and computes the energy table for that image.
	 * The energy of a pixel is the difference in the color of the pixels next to it
	 * (vertical and horizontal). If the pixel is at the edge the pixel itself replaces
	 * the pixel that is 'missing'.
	 *
	 * @param image
	 * @return energy table (Int[][]).
	 */
	private fun computeEnergy(image: BufferedImage): Array<IntArray> {
		val width = image.width
		val height = image.height
		val colors = Array(width) { IntArray(height) { -1 } }

		val raster = image.raster

		// Fazer cache de valores para evitar getRGBs desnecessários
		fun getColor(x: Int, y: Int): Int {
			if (colors[x][y] != -1)
				return colors[x][y]

			val result = (raster.getDataElements(x, y, null))
			val rgb = if (result is IntArray) {
				result[0]
			} else {
				// I tried, okay?
				image.getRGB(x, y)
			}

			colors[x][y] = rgb
			return rgb
		}

		val energyTable = Array(width) { IntArray(height) }

		// Loop over every pixel in the image and compute its energy.
		// O loop é x -> y para evitar cache misses, assim fica mais rápido!
		for (x in 0 until width) {
			for (y in 0 until height) {
				val x1Pixel: Int
				val x2Pixel: Int
				val y1Pixel: Int
				val y2Pixel: Int

				if (x == 0) {
					// leftmost column
					x1Pixel = getColor(x, y)
					x2Pixel = getColor(x + 1, y)
				} else if (x == width - 1) {
					// rightmost column
					x1Pixel = getColor(x - 1, y)
					x2Pixel = getColor(x, y)
				} else {
					// middle columns
					x1Pixel = getColor(x - 1, y)
					x2Pixel = getColor(x + 1, y)
				}

				if (y == 0) {
					// bottom row
					y1Pixel = getColor(x, y)
					y2Pixel = getColor(x, y + 1)
				} else if (y == height - 1) {
					// top row
					y1Pixel = getColor(x, y - 1)
					y2Pixel = getColor(x, y)
				} else {
					// middle rows
					y1Pixel = getColor(x, y - 1)
					y2Pixel = getColor(x, y + 1)
				}

				// we now have all the pixels we need, so we find the
				// differences between them.
				// By doing the bitwise operations we get at each individual
				// part of the color and can compare them. Each expression
				// should be close to 0 if the colors are similar.
				// Colors that are not similar will have a higher value.
				val xRed = Math.abs((x1Pixel and 0x00ff0000 shr 16) - (x2Pixel and 0x00ff0000 shr 16))
				val xGreen = Math.abs((x1Pixel and 0x0000ff00 shr 8) - (x2Pixel and 0x0000ff00 shr 8))
				val xBlue = Math.abs((x1Pixel and 0x000000ff) - (x2Pixel and 0x000000ff))

				val yRed = Math.abs((y1Pixel and 0x00ff0000 shr 16) - (y2Pixel and 0x00ff0000 shr 16))
				val yGreen = Math.abs((y1Pixel and 0x0000ff00 shr 8) - (y2Pixel and 0x0000ff00 shr 8))
				val yBlue = Math.abs((y1Pixel and 0x000000ff) - (y2Pixel and 0x000000ff))

				// We add up all the differences and call that our energy.
				val energy = xRed + xGreen + xBlue + yRed + yGreen + yBlue

				energyTable[x][y] = energy
			}
		}

		return energyTable
	}

	/**
	 * findSeam() finds a seam given an energy table and a direction. The seam is
	 * the path from bottom to top or left to right with minimum total energy.
	 *
	 * @param energy table (Int[][]) and direction (vertical / horizontal).
	 * @return seam (int[x or y][x, y]).
	 */
	private fun findSeam(energyTable: Array<IntArray>, direction: CarveDirection): Array<IntArray> {
		val seam: Array<IntArray>
		val width = energyTable.size
		val height = energyTable[0].size
		// seamDynamic is the table we will use for dynamic programming.
		val seamDynamic = Array(width) { IntArray(height) }
		val backtracker = Array(width) { IntArray(height) }
		var minimum: Int
		if (direction == CarveDirection.VERTICAL) {
			// vertical seam.
			seam = Array(energyTable[0].size) { IntArray(2) }

			// Loops over the energy table and finds the lowest energy path.
			for (y in 0 until height) {
				for (x in 0 until width) {
					if (y == 0) {
						seamDynamic[x][y] = energyTable[x][y]
						backtracker[x][y] = -1
					} else {
						// every other row.
						// need to special case the sides.
						if (x == 0) {
							minimum = Math.min(seamDynamic[x][y - 1], seamDynamic[x + 1][y - 1])
							if (minimum == seamDynamic[x][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 1
							} else { // x + 1
								// add backtracker.
								backtracker[x][y] = 2
							}
						} else if (x == width - 1) {
							minimum = Math.min(seamDynamic[x][y - 1], seamDynamic[x - 1][y - 1])
							if (minimum == seamDynamic[x][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 1
							} else { // x - 1
								// add backtracker.
								backtracker[x][y] = 0
							}
						} else {
							minimum = Math.min(seamDynamic[x - 1][y - 1], Math.min(seamDynamic[x][y - 1], seamDynamic[x + 1][y - 1]))
							if (minimum == seamDynamic[x - 1][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 0
							} else if (minimum == seamDynamic[x][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 1
							} else { // x + 1
								// add backtracker.
								backtracker[x][y] = 2
							}
						}
						seamDynamic[x][y] = energyTable[x][y] + minimum
					}
				}
			}

			// now that we have computed the paths, we need to backtrace the minimum one.
			// 0 --> x - 1.
			// 1 --> x.
			// 2 --> x + 1.
			// first we need to find the min at the end.
			var min_num = seamDynamic[0][height - 1]
			var min_index = 0
			for (x in 0 until width) {
				if (min_num > seamDynamic[x][height - 1]) {
					min_index = x
					min_num = seamDynamic[x][height - 1]
				}
			}

			// now that we have the min we need to backtrace it.
			var y_index = height - 1
			var x_index = min_index
			seam[y_index][0] = x_index
			seam[y_index][1] = y_index
			var backtrack: Int
			while (y_index > 0) {
				backtrack = backtracker[x_index][y_index]
				if (backtrack != -1) {
					if (backtrack == 0) {
						x_index = x_index - 1
					} else if (backtrack == 1) {
						x_index = x_index
					} else { // = 2
						x_index = x_index + 1
					}
				} else {
					x_index = x_index
				}
				y_index = y_index - 1

				seam[y_index][0] = x_index
				seam[y_index][1] = y_index
			}
		} else {
			// horizontal seam.
			seam = Array(energyTable.size) { IntArray(2) }

			// Loops over the energy table and finds the lowest energy path.
			for (x in 0 until width) {
				for (y in 0 until height) {
					if (x == 0) {
						seamDynamic[x][y] = energyTable[x][y]
						backtracker[x][y] = -1
					} else {
						// every other column.
						// need to special case the top/bottom.
						if (y == 0) {
							minimum = Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y + 1])
							if (minimum == seamDynamic[x - 1][y]) {
								// add backtracker.
								backtracker[x][y] = 1
							} else { // y + 1
								// add backtracker.
								backtracker[x][y] = 2
							}
						} else if (y == height - 1) {
							minimum = Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y - 1])
							if (minimum == seamDynamic[x - 1][y]) {
								// add backtracker.
								backtracker[x][y] = 1
							} else { // y - 1
								// add backtracker.
								backtracker[x][y] = 0
							}
						} else {
							minimum = Math.min(seamDynamic[x - 1][y - 1], Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y + 1]))
							if (minimum == seamDynamic[x - 1][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 0
							} else if (minimum == seamDynamic[x - 1][y]) {
								// add backtracker.
								backtracker[x][y] = 1
							} else { // y + 1
								// add backtracker.
								backtracker[x][y] = 2
							}
						}
						seamDynamic[x][y] = energyTable[x][y] + minimum
					}
				}
			}

			// now that we have computed the paths, we need to backtrace the minimum one.
			// 0 --> y - 1.
			// 1 --> y.
			// 2 --> y + 1.
			// first we need to find the min at the end.
			var min_num = seamDynamic[width - 1][0]
			var min_index = 0
			for (y in 0 until height) {
				if (min_num > seamDynamic[width - 1][y]) {
					min_index = y
					min_num = seamDynamic[width - 1][y]
				}
			}

			// now that we have the min we need to backtrace it.
			var y_index = min_index
			var x_index = width - 1
			seam[x_index][0] = x_index
			seam[x_index][1] = y_index
			var backtrack: Int
			while (x_index > 0) {
				backtrack = backtracker[x_index][y_index]
				if (backtrack != -1) {
					if (backtrack == 0) {
						y_index = y_index - 1
					} else if (backtrack == 1) {
						y_index = y_index
					} else { // = 2
						y_index = y_index + 1
					}
				} else {
					y_index = y_index
				}
				x_index = x_index - 1

				seam[x_index][0] = x_index
				seam[x_index][1] = y_index
			}
		}

		return seam
	}

	/**
	 * removeSeam() removes a given seam from an image.
	 *
	 * @param image, seam[][] and direction (vertical / horizontal).
	 * @return carved image.
	 */
	private fun removeSeam(image: BufferedImage, seam: Array<IntArray>, direction: CarveDirection): BufferedImage {
		val newImage: BufferedImage
		val width = image.width
		val height = image.height
		if (direction == CarveDirection.VERTICAL) {
			// vertical seam.
			newImage = BufferedImage(width - 1, height, BufferedImage.TYPE_INT_ARGB)
		} else {
			// horizontal seam.
			newImage = BufferedImage(width, height - 1, BufferedImage.TYPE_INT_ARGB)
		}

		val raster = image.raster

		// Loops over ever pixel in the original image and copies them over.
		// Do not copy over the pixels in the seam.
		if (direction == CarveDirection.VERTICAL) {
			// vertical seam.
			for (y in 0 until height) {
				var shift = false
				for (x in 0 until width) {
					// Simple loop to check if the pixel is part of the seam or not.
					var inSeam = false
					if (seam[y][0] == x && seam[y][1] == y) {
						inSeam = true
						shift = true
					}

					if (!inSeam) {
						// pixel not part of the seam, so we add it.
						val result = (raster.getDataElements(x, y, null))
						val color = if (result is IntArray) {
							result[0]
						} else {
							// I tried, okay?
							image.getRGB(x, y)
						}

						if (shift) {
							newImage.setRGB(x - 1, y, color)
						} else {
							newImage.setRGB(x, y, color)
						}
					}
				}
			}
		} else {
			// horizontal seam.
			for (x in 0 until width) {
				var shift = false
				for (y in 0 until height) {
					// Simple loop to check if the pixel is part of the seam or not.
					var inSeam = false
					if (seam[x][0] == x && seam[x][1] == y) {
						inSeam = true
						shift = true
					}

					// this does not work, as we might need to put it at either x-1 or y-1.
					if (!inSeam) {
						// pixel not part of the seam, so we add it.
						val result = (raster.getDataElements(x, y, null))
						val color = if (result is IntArray) {
							result[0]
						} else {
							// I tried, okay?
							image.getRGB(x, y)
						}

						if (shift) {
							newImage.setRGB(x, y - 1, color)
						} else {
							newImage.setRGB(x, y, color)
						}
					}
				}
			}
		}

		return newImage
	}

	enum class CarveDirection {
		HORIZONTAL, VERTICAL
	}
}


