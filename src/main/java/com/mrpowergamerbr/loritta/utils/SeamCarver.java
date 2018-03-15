package com.mrpowergamerbr.loritta.utils; /**
 * SeamCarver.java
 * Frederik Roenn Stensaeth
 * Date: 12.17.15
 *
 * Java program to perform content-aware image resizing using seam carving.
 **/

import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

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
public class SeamCarver {
	public static void main(String[] args) {
		// Initialize and get the parameters from the command line.
		boolean showImages = false;
		String imageFilePath = null;
		String outputImageFilePath = null;
		Integer num  = null;
		String direction = null;
		String outputFormatName = ""; // e.g. "png" or "jpg"
		for (String arg: args) {
			if (arg.equals("--show")) {
				showImages = true;
			} else if (imageFilePath == null) {
				imageFilePath = arg;
			} else if (outputImageFilePath == null) {
				outputImageFilePath = arg;
				int index = outputImageFilePath.lastIndexOf('.');
				if (index >= 0) {
					outputFormatName = outputImageFilePath.substring(index + 1);
				}
			} else if (num == null) {
				num = Integer.parseInt(arg);
			} else if (direction == null) {
				direction = arg;
			}
		}

		// Make sure that the command line arguments given were valid.
		if (imageFilePath == null && outputImageFilePath == null) {
			System.err.println("Usage: java SeamCarver inputImage outputImage numOfSeams seamDirection [--show]");
			return;
		} else if (!direction.equals("vertical") && !direction.equals("horizontal")) {
			System.err.println("Usage: java SeamCarver inputImage outputImage numOfSeams seamDirection [--show]");
			System.err.println("Direction needs to be either 'horizontal' or 'vertical'.");
			return;
		} else if (num <= 0) {
			System.err.println("Usage: java SeamCarver inputImage outputImage numOfSeams seamDirection [--show]");
			System.err.println("numOfSeams needs to be a positive integer.");
			return;
		}

		// Open the input image
		BufferedImage image;
		try {
			image = ImageIO.read(new File(imageFilePath));
		} catch(IOException e) {
			System.err.println("Can't open " + imageFilePath);
			return;
		}

		// Remove num seams from the input image. We remove one at the time, as
		// we need to recompute the energy table each time.
		BufferedImage newImage = image;
		while (num > 0) {
			System.out.println(num);
			// Get the new image w/o one seam.
			newImage = carveSeam(newImage, direction);

			num = num - 1;
		}

		// Create the new image file.
		try {
			File outputfile = new File(outputImageFilePath);
			ImageIO.write(newImage, "png", outputfile);
		} catch (IOException e) {
			System.err.println("Trouble saving " + outputImageFilePath);
			return;
		}

		// Show the before and after images.
		if (showImages) {
			showImage(image);
			showImage(newImage);
		}
	}

	/**
	 * carveSeam() takes an image and removes a single seam from that image in the
	 * desired direction.
	 *
	 * @param image to be carved and direction of the seam (vertical / horizontal).
	 * @return carved image.
	 */
	public static BufferedImage carveSeam(BufferedImage image, String direction) {
		// We need to compute the energy table, find and remove a seam.
		BufferedImage newImage = null;
		double[][] energyTable = computeEnergy(image);
		int[][] seam = findSeam(energyTable, direction);
		newImage = removeSeam(image, seam, direction);

		return newImage;
	}

	/**
	 * computeEnergy() takes an image and computes the energy table for that image.
	 * The energy of a pixel is the difference in the color of the pixels next to it
	 * (vertical and horizontal). If the pixel is at the edge the pixel itself replaces
	 * the pixel that is 'missing'.
	 *
	 * @param image.
	 * @return energy table (double[][]).
	 */
	private static double[][] computeEnergy(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		double[][] energyTable = new double[width][height];

		// Loop over every pixel in the image and compute its energy.
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int x1Pixel;
				int x2Pixel;
				int y1Pixel;
				int y2Pixel;

				if (x == 0) {
					// leftmost column
					x1Pixel = image.getRGB(x, y);
					x2Pixel = image.getRGB(x + 1, y);
				} else if (x == width - 1) {
					// rightmost column
					x1Pixel = image.getRGB(x - 1, y);
					x2Pixel = image.getRGB(x, y);
				} else {
					// middle columns
					x1Pixel = image.getRGB(x - 1, y);
					x2Pixel = image.getRGB(x + 1, y);
				}

				if (y == 0) {
					// bottom row
					y1Pixel = image.getRGB(x, y);
					y2Pixel = image.getRGB(x, y + 1);
				} else if (y == height - 1) {
					// top row
					y1Pixel = image.getRGB(x, y - 1);
					y2Pixel = image.getRGB(x, y);
				} else {
					// middle rows
					y1Pixel = image.getRGB(x, y - 1);
					y2Pixel = image.getRGB(x, y + 1);
				}

				// we now have all the pixels we need, so we find the
				// differences between them.
				// By doing the bitwise operations we get at each individual
				// part of the color and can compare them. Each expression
				// should be close to 0 if the colors are similar.
				// Colors that are not similar will have a higher value.
				int xRed = Math.abs(((x1Pixel & 0x00ff0000) >> 16) - ((x2Pixel & 0x00ff0000) >> 16));
				int xGreen = Math.abs(((x1Pixel & 0x0000ff00) >> 8) - ((x2Pixel & 0x0000ff00) >> 8));
				int xBlue = Math.abs((x1Pixel & 0x000000ff) - (x2Pixel & 0x000000ff));

				int yRed = Math.abs(((y1Pixel & 0x00ff0000) >> 16) - ((y2Pixel & 0x00ff0000) >> 16));
				int yGreen = Math.abs(((y1Pixel & 0x0000ff00) >> 8) - ((y2Pixel & 0x0000ff00) >> 8));
				int yBlue = Math.abs((y1Pixel & 0x000000ff) - (y2Pixel & 0x000000ff));

				// We add up all the differences and call that our energy.
				double energy = xRed + xGreen + xBlue + yRed + yGreen + yBlue;

				energyTable[x][y] = energy;
			}
		}

		return energyTable;
	}

	/**
	 * findSeam() finds a seam given an energy table and a direction. The seam is
	 * the path from bottom to top or left to right with minimum total energy.
	 *
	 * @param energy table (double[][]) and direction (vertical / horizontal).
	 * @return seam (int[x or y][x, y]).
	 */
	private static int[][] findSeam(double[][] energyTable, String direction) {
		int[][] seam;
		int width = energyTable.length;
		int height = energyTable[0].length;
		// seamDynamic is the table we will use for dynamic programming.
		double[][] seamDynamic = new double[width][height];
		int[][] backtracker = new int[width][height];
		double minimum;
		if (direction.equals("vertical")) {
			// vertical seam.
			seam = new int[energyTable[0].length][2];

			// Loops over the energy table and finds the lowest energy path.
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (y == 0) {
						seamDynamic[x][y] = energyTable[x][y];
						backtracker[x][y] = -1;
					} else {
						// every other row.
						// need to special case the sides.
						if (x == 0) {
							minimum = Math.min(seamDynamic[x][y - 1], seamDynamic[x + 1][y - 1]);
							if (minimum == seamDynamic[x][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 1;
							} else { // x + 1
								// add backtracker.
								backtracker[x][y] = 2;
							}
						} else if (x == (width - 1)) {
							minimum = Math.min(seamDynamic[x][y - 1], seamDynamic[x - 1][y - 1]);
							if (minimum == seamDynamic[x][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 1;
							} else { // x - 1
								// add backtracker.
								backtracker[x][y] = 0;
							}
						} else {
							minimum = Math.min(seamDynamic[x - 1][y - 1], Math.min(seamDynamic[x][y - 1], seamDynamic[x + 1][y - 1]));
							if (minimum == seamDynamic[x - 1][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 0;
							} else if (minimum == seamDynamic[x][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 1;
							} else { // x + 1
								// add backtracker.
								backtracker[x][y] = 2;
							}
						}
						seamDynamic[x][y] = energyTable[x][y] + minimum;
					}
				}
			}

			// now that we have computed the paths, we need to backtrace the minimum one.
			// 0 --> x - 1.
			// 1 --> x.
			// 2 --> x + 1.
			// first we need to find the min at the end.
			double min_num = seamDynamic[0][height - 1];
			int min_index = 0;
			for (int x = 0; x < width; x++) {
				if (min_num > seamDynamic[x][height - 1]) {
					min_index = x;
					min_num = seamDynamic[x][height - 1];
				}
			}

			// now that we have the min we need to backtrace it.
			int y_index = height - 1;
			int x_index = min_index;
			seam[y_index][0] = x_index;
			seam[y_index][1] = y_index;
			int backtrack;
			while (y_index > 0) {
				backtrack = backtracker[x_index][y_index];
				if (backtrack != -1) {
					if (backtrack == 0) {
						x_index = x_index - 1;
					} else if (backtrack == 1) {
						x_index = x_index;
					} else { // = 2
						x_index = x_index + 1;
					}
				} else {
					x_index = x_index;
				}
				y_index = y_index - 1;

				seam[y_index][0] = x_index;
				seam[y_index][1] = y_index;
			}
		} else {
			// horizontal seam.
			seam = new int[energyTable.length][2];

			// Loops over the energy table and finds the lowest energy path.
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (x == 0) {
						seamDynamic[x][y] = energyTable[x][y];
						backtracker[x][y] = -1;
					} else {
						// every other column.
						// need to special case the top/bottom.
						if (y == 0) {
							minimum = Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y + 1]);
							if (minimum == seamDynamic[x - 1][y]) {
								// add backtracker.
								backtracker[x][y] = 1;
							} else { // y + 1
								// add backtracker.
								backtracker[x][y] = 2;
							}
						} else if (y == (height - 1)) {
							minimum = Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y - 1]);
							if (minimum == seamDynamic[x - 1][y]) {
								// add backtracker.
								backtracker[x][y] = 1;
							} else { // y - 1
								// add backtracker.
								backtracker[x][y] = 0;
							}
						} else {
							minimum = Math.min(seamDynamic[x - 1][y - 1], Math.min(seamDynamic[x - 1][y], seamDynamic[x - 1][y + 1]));
							if (minimum == seamDynamic[x - 1][y - 1]) {
								// add backtracker.
								backtracker[x][y] = 0;
							} else if (minimum == seamDynamic[x - 1][y]) {
								// add backtracker.
								backtracker[x][y] = 1;
							} else { // y + 1
								// add backtracker.
								backtracker[x][y] = 2;
							}
						}
						seamDynamic[x][y] = energyTable[x][y] + minimum;
					}
				}
			}

			// now that we have computed the paths, we need to backtrace the minimum one.
			// 0 --> y - 1.
			// 1 --> y.
			// 2 --> y + 1.
			// first we need to find the min at the end.
			double min_num = seamDynamic[width - 1][0];
			int min_index = 0;
			for (int y = 0; y < height; y++) {
				if (min_num > seamDynamic[width - 1][y]) {
					min_index = y;
					min_num = seamDynamic[width - 1][y];
				}
			}

			// now that we have the min we need to backtrace it.
			int y_index = min_index;
			int x_index = width - 1;
			seam[x_index][0] = x_index;
			seam[x_index][1] = y_index;
			int backtrack;
			while (x_index > 0) {
				backtrack = backtracker[x_index][y_index];
				if (backtrack != -1) {
					if (backtrack == 0) {
						y_index = y_index - 1;
					} else if (backtrack == 1) {
						y_index = y_index;
					} else { // = 2
						y_index = y_index + 1;
					}
				} else {
					y_index = y_index;
				}
				x_index = x_index - 1;

				seam[x_index][0] = x_index;
				seam[x_index][1] = y_index;
			}
		}

		return seam;
	}

	/**
	 * removeSeam() removes a given seam from an image.
	 *
	 * @param image, seam[][] and direction (vertical / horizontal).
	 * @return carved image.
	 */
	private static BufferedImage removeSeam(BufferedImage image, int[][] seam, String direction) {
		BufferedImage newImage;
		int width = image.getWidth();
		int height = image.getHeight();
		if (direction.equals("vertical")) {
			// vertical seam.
			newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_ARGB);
		} else {
			// horizontal seam.
			newImage = new BufferedImage(width, height - 1, BufferedImage.TYPE_INT_ARGB);
		}

		// Loops over ever pixel in the original image and copies them over.
		// Do not copy over the pixels in the seam.
		if (direction.equals("vertical")) {
			// vertical seam.
			for (int y = 0; y < height; y++) {
				boolean shift = false;
				for (int x = 0; x < width; x++) {
					// Simple loop to check if the pixel is part of the seam or not.
					boolean inSeam = false;
					if ((seam[y][0] == x) && (seam[y][1] == y)) {
						inSeam = true;
						shift = true;
					}

					if (!inSeam) {
						// pixel not part of the seam, so we add it.
						int color = image.getRGB(x, y);
						if (shift) {
							newImage.setRGB(x - 1, y, color);
						} else {
							newImage.setRGB(x, y, color);
						}
					}
				}
			}
		} else {
			// horizontal seam.
			for (int x = 0; x < width; x++) {
				boolean shift = false;
				for (int y = 0; y < height; y++) {
					// Simple loop to check if the pixel is part of the seam or not.
					boolean inSeam = false;
					if ((seam[x][0] == x) && (seam[x][1] == y)) {
						inSeam = true;
						shift = true;
					}

					// this does not work, as we might need to put it at either x-1 or y-1.
					if (!inSeam) {
						// pixel not part of the seam, so we add it.
						if (shift) {
							newImage.setRGB(x, y - 1, image.getRGB(x, y));
						} else {
							newImage.setRGB(x, y, image.getRGB(x, y));
						}
					}
				}
			}
		}

		return newImage;
	}

	/**
	 * showImage() displays the given image.
	 *
	 * @param image.
	 * @return n/a.
	 */
	private static void showImage(BufferedImage image) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
	}
}


