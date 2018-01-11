package com.mrpowergamerbr.loritta.utils;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/*
 * Isto é a mesma classe do javaxt "Image", mas com uma correção no setCorners para não mover a imagem para o canto da tela caso seja transparente
 */
//Imports for JPEG
//import com.sun.image.codec.jpeg.*; //<-- Not always available in newer versions

//Imports for JP2
//import javax.media.jai.RenderedOp;
//import com.sun.media.imageio.plugins.jpeg2000.J2KImageReadParam;


//******************************************************************************
//**  Image Utilities - By Peter Borissow
//******************************************************************************
/**
 *   Used to open, resize, rotate, crop and save images.
 *
 ******************************************************************************/

public class LorittaImage {

	private BufferedImage bufferedImage = null;
	private java.util.ArrayList corners = null;

	private float outputQuality = 1f; //0.9f; //0.5f;

	private static final boolean useSunCodec = getSunCodec();
	private static Class JPEGCodec;
	private static Class JPEGEncodeParam;


	private Graphics2D g2d = null;

	public static String[] InputFormats = getFormats(ImageIO.getReaderFormatNames());
	public static String[] OutputFormats = getFormats(ImageIO.getWriterFormatNames());


	private IIOMetadata metadata;
	private HashMap<Integer, Object> exif;
	private HashMap<Integer, Object> iptc;
	private HashMap<Integer, Object> gps;


	//**************************************************************************
	//** Constructor
	//**************************************************************************
	/**  Creates a new instance of this class using an existing image */

	public LorittaImage(String PathToImageFile){
		this(new java.io.File(PathToImageFile));
	}

	public LorittaImage(java.io.File file){
		try{ createBufferedImage(new FileInputStream(file)); }
		catch(Exception e){}
	}

	public LorittaImage(java.io.InputStream InputStream){
		createBufferedImage(InputStream);
	}

	public LorittaImage(byte[] byteArray){
		this(new ByteArrayInputStream(byteArray));
	}

	public LorittaImage(int width, int height){
		this.bufferedImage =
				new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		this.g2d = getGraphics();
	}

	public LorittaImage(BufferedImage bufferedImage){
		this.bufferedImage = bufferedImage;
	}


	public LorittaImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			this.bufferedImage = (BufferedImage) img;
		}
		else{
			java.awt.image.ColorModel cm = img.getColorModel();
			java.awt.image.WritableRaster raster =
					cm.createCompatibleWritableRaster(img.getWidth(), img.getHeight());
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			java.util.Hashtable properties = new java.util.Hashtable();
			String[] keys = img.getPropertyNames();
			if (keys!=null) {
				for (int i = 0; i < keys.length; i++) {
					properties.put(keys[i], img.getProperty(keys[i]));
				}
			}
			BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
			img.copyData(raster);
			this.bufferedImage = result;
		}
	}


	//**************************************************************************
	//** Constructor
	//**************************************************************************
	/** Creates a new instance of this class using a block of text.
	 *  @param fontName Name of the font you with to use. Note that you can get
	 *  a list of available fonts like this:
	<pre>
	for (String fontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()){
	System.out.println(fontName);
	}
	</pre>
	 */
	public LorittaImage(String text, String fontName, int fontSize, int r, int g, int b){
		this(text, new Font(fontName, Font.TRUETYPE_FONT, fontSize), r,g,b);
	}


	public LorittaImage(String text, Font font, int r, int g, int b){

		//Get Font Metrics
		Graphics2D t = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		t.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		FontMetrics fm = t.getFontMetrics(font);
		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		int descent = fm.getDescent();

		t.dispose();


		//Create Image
		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g2d = bufferedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		//Add Text
		float alpha = 1.0f; //Set alpha.  0.0f is 100% transparent and 1.0f is 100% opaque.
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.setColor(new Color(r, g, b));
		g2d.setFont(font);
		g2d.drawString(text, 0, height-descent);
	}


	//**************************************************************************
	//** setBackgroundColor
	//**************************************************************************
	/** Used to set the background color. Creates an image layer and inserts it
	 *  under the existing graphic. This method should only be called once.
	 */
	public void setBackgroundColor(int r, int g, int b){
        /*
        Color org = g2d.getColor();
        g2d.setColor(new Color(r,g,b));
        g2d.fillRect(1,1,width-2,height-2); //g2d.fillRect(0,0,width,height);
        g2d.setColor(org);
        */

		int imageType = bufferedImage.getType();
		if (imageType == 0) {
			imageType = BufferedImage.TYPE_INT_ARGB;
		}

		int width = this.getWidth();
		int height = this.getHeight();

		BufferedImage bi = new BufferedImage(width, height, imageType);
		Graphics2D g2d = bi.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2d.setColor(new Color(r,g,b));
		g2d.fillRect(0,0,width,height);

		java.awt.Image img = bufferedImage;
		g2d.drawImage(img, 0, 0, null);

		this.bufferedImage = bi;

		g2d.dispose();
	}


	//**************************************************************************
	//** getInputFormats
	//**************************************************************************
	/**  Used to retrieve a list of supported input (read) formats. */

	public String[] getInputFormats(){
		return getFormats(ImageIO.getReaderFormatNames());
	}


	//**************************************************************************
	//** getOutputFormats
	//**************************************************************************
	/**  Used to retrieve a list of supported output (write) formats. */

	public String[] getOutputFormats(){
		return getFormats(ImageIO.getWriterFormatNames());
	}


	//**************************************************************************
	//** getFormats
	//**************************************************************************
	/**  Used to trim the list of formats. */

	private static String[] getFormats(String[] inputFormats){

		//Build a unique list of file formats
		HashSet<String> formats = new HashSet<String> ();
		for (int i=0; i<inputFormats.length; i++){
			String format = inputFormats[i].toUpperCase();
			if (format.contains("JPEG") && format.contains("2000")){
				formats.add("JP2");
				formats.add("J2C");
				formats.add("J2K");
				formats.add("JPX");
			}
			else if (format.equals("JPEG") || format.equals("JPG")){
				formats.add("JPE");
				formats.add("JFF");
				formats.add(format);
			}
			else{
				formats.add(format);
			}
		}

		//Sort and return the hashset as an array
		inputFormats = formats.toArray(new String[formats.size()]);
		java.util.Collections.sort(java.util.Arrays.asList(inputFormats));
		return inputFormats;
	}


	//**************************************************************************
	//** getSunCodec
	//**************************************************************************
	/** Attempts to load classes from the com.sun.image.codec.jpeg package used
	 *  to compress jpeg images. These classes are marked as deprecated in Java
	 *  1.7 and several distributions of Java no longer include these classes
	 *  (e.g.  "IcedTea" OpenJDK 7). Returns true of the classes are available.
	 */
	private static boolean getSunCodec(){
		try{
			JPEGCodec = Class.forName("com.sun.image.codec.jpeg.JPEGCodec");
			JPEGEncodeParam = Class.forName("com.sun.image.codec.jpeg.JPEGEncodeParam");
			return true;
		}
		catch(Exception e){
			return false;
		}
	}


	//**************************************************************************
	//** getWidth
	//**************************************************************************
	/**  Returns the width of the image, in pixels. */

	public int getWidth(){
		return bufferedImage.getWidth();
	}


	//**************************************************************************
	//** getHeight
	//**************************************************************************
	/**  Returns the height of the image, in pixels. */

	public int getHeight(){
		return bufferedImage.getHeight();
	}


	//**************************************************************************
	//** getGraphics
	//**************************************************************************

	private Graphics2D getGraphics(){
		if (g2d==null){
			g2d = this.bufferedImage.createGraphics();

			//Enable anti-alias
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		return g2d;
	}


	//**************************************************************************
	//** addText
	//**************************************************************************
	/**  Used to add text to the image at a given position.
	 *  @param x Lower left coordinate of the text
	 *  @param y Lower left coordinate of the text
	 */
	public void addText(String text, int x, int y){
		addText(text, x, y, new Font ("SansSerif",Font.TRUETYPE_FONT,12), 0, 0, 0);
	}


	//**************************************************************************
	//** addText
	//**************************************************************************
	/**  Used to add text to the image at a given position.
	 *  @param x Lower left coordinate of the text
	 *  @param y Lower left coordinate of the text
	 *  @param fontName Name of the font face (e.g. "Tahoma", "Helvetica", etc.)
	 *  @param fontSize Size of the font
	 *  @param r Value for the red channel (0-255)
	 *  @param g Value for the green channel (0-255)
	 *  @param b Value for the blue channel (0-255)
	 */
	public void addText(String text, int x, int y, String fontName, int fontSize, int r, int g, int b){
		addText(text, x, y, new Font(fontName, Font.TRUETYPE_FONT, fontSize), r,g,b);
	}


	//**************************************************************************
	//** addText
	//**************************************************************************
	/**  Used to add text to the image at a given position.
	 *  @param x Lower left coordinate of the text
	 *  @param y Lower left coordinate of the text
	 *  @param font Font
	 *  @param r Value for the red channel (0-255)
	 *  @param g Value for the green channel (0-255)
	 *  @param b Value for the blue channel (0-255)
	 */
	public void addText(String text, int x, int y, Font font, int r, int g, int b){
		g2d = getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);


		g2d.setColor(new Color(r, g, b));
		g2d.setFont(font);
		g2d.drawString(text, x, y);
	}


	//**************************************************************************
	//** addPoint
	//**************************************************************************
	/**  Simple drawing function used to set color of a specific pixel in the
	 *   image.
	 */
	public void addPoint(int x, int y, int r, int g, int b){
		setColor(x,y,new Color(r,g,b));
	}


	//**************************************************************************
	//** setColor
	//**************************************************************************
	/**  Used to set the color (ARGB value) for a specific pixel in the image.
	 *   Note that input x,y values are relative to the upper left corner of the
	 *   image, starting at 0,0.
	 */
	public void setColor(int x, int y, Color color){
		g2d = getGraphics();
		Color org = g2d.getColor();
		g2d.setColor(color);
		g2d.fillRect(x,y,1,1);
		g2d.setColor(org);
	}


	//**************************************************************************
	//** getColor
	//**************************************************************************
	/**  Used to retrieve the color (ARGB) values for a specific pixel in the
	 *   image. Returns a java.awt.Color object. Note that input x,y values are
	 *   relative to the upper left corner of the image, starting at 0,0.
	 */
	public Color getColor(int x, int y){
		//return new Color(bufferedImage.getRGB(x, y)); //<--This will return an incorrect alpha value

		int pixel = bufferedImage.getRGB(x, y);
		int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;
		return new java.awt.Color(red, green, blue, alpha);
	}


	//**************************************************************************
	//** getHistogram
	//**************************************************************************
	/** Returns an array with 4 histograms: red, green, blue, and average
	 <pre>
	 ArrayList<int[]> histogram = image.getHistogram();
	 int[] red = histogram.get(0);
	 int[] green = histogram.get(1);
	 int[] blue = histogram.get(2);
	 int[] average = histogram.get(3);
	 </pre>
	 */
	public java.util.ArrayList<int[]> getHistogram(){

		//Create empty histograms
		int[] red = new int[256];
		int[] green = new int[256];
		int[] blue = new int[256];
		int[] average = new int[256];

		for (int i=0; i<red.length; i++) red[i] = 0;
		for (int i=0; i<green.length; i++) green[i] = 0;
		for (int i=0; i<blue.length; i++) blue[i] = 0;
		for (int i=0; i<average.length; i++) average[i] = 0;


		//Populate the histograms
		for (int i=0; i<this.getWidth(); i++){
			for (int j=0; j<this.getHeight(); j++){
				Color color = this.getColor(i, j);
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();

				red[r] = red[r]+1;
				green[g] = green[g]+1;
				blue[b] = blue[r]+1;

				int avg = Math.round((r+g+b)/3);
				average[avg] = average[avg]+1;
			}
		}

		java.util.ArrayList<int[]> hist = new java.util.ArrayList<int[]>();
		hist.add(red);
		hist.add(green);
		hist.add(blue);
		hist.add(average);
		return hist;
	}


	//**************************************************************************
	//** addImage
	//**************************************************************************
	/**  Used to add an image "overlay" to the existing image at a given
	 *   position. This method can also be used to create image mosiacs.
	 */
	public void addImage(BufferedImage in, int x, int y, boolean expand){

		int x2 = 0;
		int y2 = 0;
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();

		if (expand){

			//Update Width and Horizontal Position of the Original Image
			if (x<0) {
				w = w + -x;
				if (in.getWidth()>w){
					w = w + (in.getWidth()-w);
				}
				x2 = -x;
				x = 0;
			}
			else if (x>w) {
				w = (w + (x-w)) + in.getWidth();
			}
			else{
				if ((x+in.getWidth())>w){
					w = w + ((x+in.getWidth())-w);
				}
			}

			//Update Height and Vertical Position of the Original Image
			if (y<0){
				h = h + -y;
				if (in.getHeight()>h){
					h = h + (in.getHeight()-h);
				}
				y2 = -y;
				y = 0;
			}
			else if(y>h){
				h = (h + (y-h)) + in.getHeight();
			}
			else{
				if ((y+in.getHeight())>h){
					h = h + ((y+in.getHeight())-h);
				}
			}

		}


		//Create new image "collage"
		if (w>bufferedImage.getWidth() || h>bufferedImage.getHeight()){
			int imageType = bufferedImage.getType();
			if (imageType == 0 || imageType == 12) {
				imageType = BufferedImage.TYPE_INT_ARGB;
			}
			BufferedImage bi = new BufferedImage(w, h, imageType);
			Graphics2D g2d = bi.createGraphics();
			java.awt.Image img = bufferedImage;
			g2d.drawImage(img, x2, y2, null);
			img = in;
			g2d.drawImage(img, x, y, null);
			g2d.dispose();
			bufferedImage = bi;
		}
		else{
			Graphics2D g2d = bufferedImage.createGraphics();
			java.awt.Image img = in;
			g2d.drawImage(img, x, y, null);
			g2d.dispose();
		}

	}


	//**************************************************************************
	//** addImage
	//**************************************************************************
	/**  Used to add an image "overlay" to the existing image at a given
	 *   position. This method can also be used to create image mosiacs.
	 */
	public void addImage(LorittaImage in, int x, int y, boolean expand){
		addImage(in.getBufferedImage(),x,y,expand);
	}


	//**************************************************************************
	//** createBufferedImage
	//**************************************************************************
	/** Used to create a BufferedImage from a InputStream */

	private void createBufferedImage(java.io.InputStream input) {
		try{
			//bufferedImage = ImageIO.read(input);

			javax.imageio.stream.ImageInputStream stream = ImageIO.createImageInputStream(input);

			Iterator iter = ImageIO.getImageReaders(stream);
			if (!iter.hasNext()) {
				return;
			}

			ImageReader reader = (ImageReader)iter.next();
			ImageReadParam param = reader.getDefaultReadParam();
			reader.setInput(stream, true, true);

			try {
				bufferedImage = reader.read(0, param);
				metadata = reader.getImageMetadata(0);
			}
			finally {
				reader.dispose();
				stream.close();
			}


			input.close();
		}
		catch(Exception e){
			//e.printStackTrace();
		}
	}


	//**************************************************************************
	//** Rotate
	//**************************************************************************
	/**  Used to rotate the image (clockwise). Rotation angle is specified in
	 *   degrees relative to the top of the image.
	 */
	public void rotate(double Degrees){

		//Define Image Center (Axis of Rotation)
		int width = this.getWidth();
		int height = this.getHeight();
		int cx = width/2;
		int cy = height/2;

		//create an array containing the corners of the image (TL,TR,BR,BL)
		int[] corners = { 0, 0, width, 0, width, height, 0, height };

		//Define bounds of the image
		int minX, minY, maxX, maxY;
		minX = maxX = cx;
		minY = maxY = cy;
		double theta = Math.toRadians(Degrees);
		for (int i=0; i<corners.length; i+=2){

			//Rotates the given point theta radians around (cx,cy)
			int x = (int) Math.round(
					Math.cos(theta)*(corners[i]-cx) -
							Math.sin(theta)*(corners[i+1]-cy)+cx
			);

			int y = (int) Math.round(
					Math.sin(theta)*(corners[i]-cx) +
							Math.cos(theta)*(corners[i+1]-cy)+cy
			);

			//Update our bounds
			if(x>maxX) maxX = x;
			if(x<minX) minX = x;
			if(y>maxY) maxY = y;
			if(y<minY) minY = y;
		}


		//Update Image Center Coordinates
		cx = (int)(cx-minX);
		cy = (int)(cy-minY);

		//Create Buffered Image
		BufferedImage result = new BufferedImage(maxX-minX, maxY-minY,
				BufferedImage.TYPE_INT_ARGB);

		//Create Graphics
		Graphics2D g2d = result.createGraphics();

		//Enable anti-alias and Cubic Resampling
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		//Rotate the image
		AffineTransform xform = new AffineTransform();
		xform.rotate(theta,cx,cy);
		g2d.setTransform(xform);
		g2d.drawImage(bufferedImage,-minX,-minY,null);
		g2d.dispose();

		//Update Class Variables
		this.bufferedImage = result;

		//Delete Heavy Objects
		result = null;
		xform = null;
	}


	//**************************************************************************
	//** Rotate Clockwise
	//**************************************************************************
	/**  Rotates the image 90 degrees clockwise */

	public void rotateClockwise(){
		rotate(90);
	}


	//**************************************************************************
	//** Rotate Counter Clockwise
	//**************************************************************************
	/**  Rotates the image -90 degrees */

	public void rotateCounterClockwise(){
		rotate(-90);
	}


	//**************************************************************************
	//** Auto-Rotate
	//**************************************************************************
	/**  Used to automatically rotate the image based on the image metadata
	 *   (EXIF Orientation tag).
	 */
	public void rotate(){
		try {
			Integer orientation = (Integer) getExifTags().get(0x0112);
			switch (orientation) {
			case 1: return; //"Top, left side (Horizontal / normal)"
			case 2: flip(); break; //"Top, right side (Mirror horizontal)";
			case 3: rotate(180); break; //"Bottom, right side (Rotate 180)";
			case 4: {flip(); rotate(180);} break; //"Bottom, left side (Mirror vertical)";
			case 5: {flip(); rotate(270);} break; //"Left side, top (Mirror horizontal and rotate 270 CW)";
			case 6: rotate(90); break; //"Right side, top (Rotate 90 CW)";
			case 7: {flip(); rotate(90);} break; //"Right side, bottom (Mirror horizontal and rotate 90 CW)";
			case 8: rotate(270); break; //"Left side, bottom (Rotate 270 CW)";
			}
		}
		catch(Exception e){
			//Failed to parse exif orientation.
		}
	}


	//**************************************************************************
	//** setWidth
	//**************************************************************************
	/**  Resizes the image to a given width. The original aspect ratio is
	 *   maintained.
	 */
	public void setWidth(int Width){
		double ratio = (double)Width/(double)this.getWidth();

		double dw = this.getWidth() * ratio;
		double dh = this.getHeight() * ratio;

		int outputWidth =  (int)Math.round(dw);
		int outputHeight = (int)Math.round(dh);

		resize(outputWidth,outputHeight);
	}


	//**************************************************************************
	//** setHeight
	//**************************************************************************
	/**  Resizes the image to a given height. The original aspect ratio is
	 *   maintained.
	 */
	public void setHeight(int Height){
		double ratio = (double)Height/(double)this.getHeight();

		double dw = this.getWidth() * ratio;
		double dh = this.getHeight() * ratio;

		int outputWidth =  (int)Math.round(dw);
		int outputHeight = (int)Math.round(dh);

		resize(outputWidth,outputHeight);
	}


	//**************************************************************************
	//** Resize (Overloaded Member)
	//**************************************************************************
	/**  Used to resize an image. Does NOT automatically retain the original
	 *   aspect ratio.
	 */
	public void resize(int Width, int Height){
		resize(Width,Height,false);
	}


	//**************************************************************************
	//** Resize
	//**************************************************************************
	/**  Used to resize an image. Provides the option to maintain the original
	 *   aspect ratio (relative to the output width).
	 */
	public void resize(int Width, int Height, boolean maintainRatio){

		//long startTime = getStartTime();

		int outputWidth = Width;
		int outputHeight = Height;

		int width = this.getWidth();
		int height = this.getHeight();

		if (maintainRatio){

			double ratio = 0;

			if (width>height){
				ratio = (double)Width/(double)width;
			}
			else{
				ratio = (double)Height/(double)height;
			}

			double dw = width * ratio;
			double dh = height * ratio;

			outputWidth =  (int)Math.round(dw);
			outputHeight = (int)Math.round(dh);

			if (outputWidth>width || outputHeight>height){
				outputWidth=width;
				outputHeight=height;
			}
		}


		//Resize the image (create new buffered image)
		java.awt.Image outputImage = bufferedImage.getScaledInstance(outputWidth, outputHeight, BufferedImage.SCALE_AREA_AVERAGING);
		BufferedImage bi = new BufferedImage(outputWidth, outputHeight, getImageType());
		Graphics2D g2d = bi.createGraphics( );
		g2d.drawImage(outputImage, 0, 0, null);
		g2d.dispose();

		this.bufferedImage = bi;

		outputImage = null;
		bi = null;
		g2d = null;
	}



	//**************************************************************************
	//** Set/Update Corners (Skew)
	//**************************************************************************
	/**  Used to skew an image by updating the corner coordinates. Coordinates are
	 *   supplied in clockwise order starting from the upper left corner.
	 */
	public void setCorners(float x0, float y0,  //UL
			float x1, float y1,  //UR
			float x2, float y2,  //LR
			float x3, float y3){ //LL

		Skew skew = new Skew(this.bufferedImage);
		this.bufferedImage = skew.setCorners(x0,y0,x1,y1,x2,y2,x3,y3);

		if (corners==null) corners = new java.util.ArrayList();
		else corners.clear();
		corners.add((Float)x0); corners.add((Float)y0);
		corners.add((Float)x1); corners.add((Float)y1);
		corners.add((Float)x2); corners.add((Float)y2);
		corners.add((Float)x3); corners.add((Float)y3);
	}


	//**************************************************************************
	//** Get Corners
	//**************************************************************************
	/**  Used to retrieve the corner coordinates of the image. Coordinates are
	 *   supplied in clockwise order starting from the upper left corner. This
	 *   information is particularly useful for generating drop shadows, inner
	 *   and outer glow, and reflections.
	 *   NOTE: Coordinates are not updated after resize(), rotate(), or addImage()
	 */
	public float[] getCorners(){

		if (corners==null){
			float w = getWidth();
			float h = getHeight();
			corners = new java.util.ArrayList();
			corners.add((Float)0f); corners.add((Float)0f);
			corners.add((Float)w); corners.add((Float)0f);
			corners.add((Float)w); corners.add((Float)h);
			corners.add((Float)0f); corners.add((Float)h);
		}

		Object[] arr = corners.toArray();
		float[] ret = new float[arr.length];
		for (int i=0; i<arr.length; i++){
			Float f = (Float) arr[i];
			ret[i] = f.floatValue();
		}
		return ret;
	}


	//**************************************************************************
	//** Sharpen
	//**************************************************************************
	/**  Used to sharpen the image using a 3x3 kernal. */

	public void sharpen(){

		int width = this.getWidth();
		int height = this.getHeight();

		//define kernal
		Kernel kernel = new Kernel(3, 3,
				new float[] {
						0.0f, -0.2f,  0.0f,
						-0.2f,  1.8f, -0.2f,
						0.0f, -0.2f,  0.0f });

		//apply convolution
		BufferedImage out = new BufferedImage(width, height, getImageType());
		BufferedImageOp op = new ConvolveOp(kernel);
		out = op.filter(bufferedImage, out);

		//replace 2 pixel border created via convolution
		java.awt.Image overlay = out.getSubimage(2,2,width-4,height-4);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.drawImage(overlay,2,2,null);
		g2d.dispose();

	}


	//**************************************************************************
	//** Desaturate
	//**************************************************************************
	/**  Used to completely desaturate an image (creates a gray-scale image). */

	public void desaturate(){
		bufferedImage = desaturate(bufferedImage);
	}


	//**************************************************************************
	//** Desaturate
	//**************************************************************************
	/**  Used to desaturate an image by a specified percentage (expressed as
	 *   a double or float). The larger the percentage, the greater the
	 *   desaturation and the "grayer" the image. Valid ranges are from 0-1.
	 */

	public void desaturate(double percent){
		float alpha = (float) (percent);
		java.awt.Image overlay = desaturate(bufferedImage);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(overlay,0,0,null);
		g2d.dispose();
	}


	//**************************************************************************
	//** Desaturate (Private Function)
	//**************************************************************************
	/**  Convenience function called by the other 2 desaturation methods. */

	private BufferedImage desaturate(BufferedImage in){
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), getImageType(in) );
		BufferedImageOp op = new ColorConvertOp(
				ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		return op.filter(in, out);
	}



	//**************************************************************************
	//** setOpacity
	//**************************************************************************

	public void setOpacity(double percent){
		if (percent>1) percent=percent/100;
		float alpha = (float) (percent);
		int imageType = bufferedImage.getType();
		if (imageType == 0) {
			imageType = BufferedImage.TYPE_INT_ARGB;
		}
		BufferedImage out = new BufferedImage(getWidth(),getHeight(),imageType);
		Graphics2D g2d = out.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(bufferedImage,0,0,null);
		g2d.dispose();
		bufferedImage = out;
	}


	//**************************************************************************
	//** Flip (Horizonal)
	//**************************************************************************
	/**  Used to flip an image along it's y-axis (horizontal). Vertical flipping
	 *   is supported via the rotate method (i.e. rotate +/-180).
	 */
	public void flip(){
		BufferedImage out = new BufferedImage(getWidth(), getHeight(), getImageType());
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-bufferedImage.getWidth(), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
		bufferedImage = op.filter(bufferedImage, out);
	}


	//**************************************************************************
	//** Crop
	//**************************************************************************
	/**  Used to subset or crop an image. */

	public void crop(int x, int y, int width, int height){
		bufferedImage = getSubimage(x,y,width,height);
	}


	//**************************************************************************
	//** copy
	//**************************************************************************
	/** Returns a copy of the current image. */

	public LorittaImage copy(){
		return new LorittaImage(bufferedImage);
	}


	//**************************************************************************
	//** copyRect
	//**************************************************************************
	/** Returns a copy of the image at a given rectangle. */

	public LorittaImage copyRect(int x, int y, int width, int height){
		return new LorittaImage(getSubimage(x,y,width,height));
	}


	//**************************************************************************
	//** getSubimage
	//**************************************************************************
	/** Returns a copy of the image at a given rectangle. In Java 1.6, the
	 *  BufferedImage.getSubimage() throws an exception if the rectangle falls
	 *  outside the image bounds. This method was written to overcome this
	 *  limitation.
	 */
	private BufferedImage getSubimage(int x, int y, int width, int height){

		Rectangle rect1 = new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		Rectangle rect2 = new Rectangle(x, y, width, height);


		//If the requested rectangle falls inside the image bounds, simply return
		//the subimage
		if (rect1.contains(rect2)){
			return (bufferedImage.getSubimage(x,y,width,height));
		}
		else{ //requested rectangle falls outside the image bounds!


			//Create buffered image
			int imageType = bufferedImage.getType();
			if (imageType == 0 || imageType == 12) {
				imageType = BufferedImage.TYPE_INT_ARGB;
			}
			BufferedImage bi = new BufferedImage(width, height, imageType);


			//If the requested rectangle intersects the image bounds, crop the
			//the source image and insert it into the BufferedImage
			if (rect1.intersects(rect2)){

				Graphics2D g2d = bi.createGraphics();
				BufferedImage subImage = null;
				int X;
				int Y;

				if (x<0){
					int x1 = 0;
					int y1;
					int h;
					int w;

					if (y<0){
						y1 = 0;
						h = y+height;
						Y = height - h;
					}
					else{
						y1 = y;
						h = height;
						Y = 0;
					}

					if (h+y1>bufferedImage.getHeight()) h = bufferedImage.getHeight()-y1;

					w = x+width;
					if (w>bufferedImage.getWidth()) w = bufferedImage.getWidth();


					subImage = bufferedImage.getSubimage(x1,y1,w,h);

					X = width - w;
				}
				else{
					int x1 = x;
					int y1;
					int h;
					int w;

					if (y<0){
						y1 = 0;
						h = y+height;
						Y = height - h;
					}
					else{
						y1 = y;
						h = height;
						Y = 0;
					}

					if (h+y1>bufferedImage.getHeight()) h = bufferedImage.getHeight()-y1;

					w = width;
					if (w+x1>bufferedImage.getWidth()) w = bufferedImage.getWidth()-x1;

					X = 0;

					subImage = bufferedImage.getSubimage(x1,y1,w,h);
				}


				g2d.drawImage(subImage, X, Y, null);
				g2d.dispose();

			}

			return bi;
		}
	}


	//**************************************************************************
	//** trim
	//**************************************************************************
	/** Used to remove excess pixels around an image by cropping the image to its
	 *  "true" extents. Crop bounds are determined by finding the first non-null
	 *  or non-black pixel on each side of the image.
	 */
	public void trim(){
		trim(0,0,0);
	}


	//**************************************************************************
	//** trim
	//**************************************************************************
	/** Used to remove excess pixels around an image by cropping the image to its
	 *  "true" extents. Crop bounds are determined by finding pixels that *don't*
	 *  match the input color. For example, you can trim off excess black pixels
	 *  around an image by specifying an rgb value of 0,0,0. Similarly, you can
	 *  trim off pure white pixels around an image by specifying an rgb value of
	 *  255,255,255. Note that transparent pixels are considered as null values
	 *  and will be automatically trimmed from the edges.
	 */
	public void trim(int r, int g, int b){
		int top = 0;
		int bottom = 0;
		int left = 0;
		int right = 0;

		for (int y=0; y<bufferedImage.getHeight(); y++){
			for (int x=0; x<bufferedImage.getWidth(); x++){
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b)){
					bottom = y;
					break;
				}
			}
		}

		for (int y=bufferedImage.getHeight()-1; y>-1; y--){
			for (int x=0; x<bufferedImage.getWidth(); x++){
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b)){
					top = y;
					break;
				}
			}
		}

		for (int x=0; x<bufferedImage.getWidth(); x++){
			for (int y=0; y<bufferedImage.getHeight(); y++){
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b)){
					right = x;
					break;
				}
			}
		}

		for (int x=bufferedImage.getWidth()-1; x>-1; x--){
			for (int y=0; y<bufferedImage.getHeight(); y++){
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b)){
					left = x;
					break;
				}
			}
		}

		if (left==right || top==bottom){
			bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		else{
			bufferedImage = bufferedImage.getSubimage(left,top,right-left,bottom-top);
		}
	}


	//**************************************************************************
	//** getBufferedImage
	//**************************************************************************
	/**  Returns the java.awt.image.BufferedImage represented by the current
	 *   image.
	 */
	public BufferedImage getBufferedImage(){
		return bufferedImage;
	}


	//**************************************************************************
	//** getImage
	//**************************************************************************
	/**  Returns a java.awt.Image copy of the current image. */

	public java.awt.Image getImage(){
		return getBufferedImage();
	}

	//**************************************************************************
	//** getImage
	//**************************************************************************
	/**  Returns a java.awt.image.RenderedImage copy of the current image. */

	public java.awt.image.RenderedImage getRenderedImage(){
		return getBufferedImage();
	}


	//**************************************************************************
	//** getBufferedImage
	//**************************************************************************
	/**  Used to retrieve a scaled copy of the current image. */

	public BufferedImage getBufferedImage(int width, int height, boolean maintainRatio){
		LorittaImage image = new LorittaImage(getBufferedImage());
		image.resize(width,height,maintainRatio);
		return image.getBufferedImage();
	}


	//**************************************************************************
	//** getByteArray
	//**************************************************************************
	/** Returns the image as a jpeg byte array. Output quality is set using
	 *  the setOutputQuality method.
	 */
	public byte[] getByteArray(){
		return getByteArray("jpeg");
	}


	//**************************************************************************
	//** getByteArray
	//**************************************************************************
	/** Returns the image as a byte array. */

	public byte[] getByteArray(String format){
		byte[] rgb = null;

		format = format.toLowerCase();
		if (format.startsWith("image/")){
			format = format.substring(format.indexOf("/")+1);
		}

		try{
			if (isJPEG(format)){
				rgb = getJPEGByteArray(outputQuality);
			}
			else{
				ByteArrayOutputStream bas = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, format.toLowerCase(), bas);
				rgb = bas.toByteArray();
			}
		}
		catch(Exception e){}
		return rgb;
	}


	//**************************************************************************
	//** saveAs
	//**************************************************************************
	/**  Exports the image to a file. Output format is determined by the output
	 *   file extension.
	 */
	public void saveAs(String PathToImageFile){
		saveAs(new java.io.File(PathToImageFile));
	}


	//**************************************************************************
	//** saveAs
	//**************************************************************************
	/**  Exports the image to a file. Output format is determined by the output
	 *   file extension.
	 */
	public void saveAs(java.io.File OutputFile){
		try{
			//Create output directory
			OutputFile.getParentFile().mkdirs();

			//Write buffered image to disk
			String FileExtension = getExtension(OutputFile.getName()).toLowerCase();
			if (isJPEG(FileExtension)){
				try (FileOutputStream output = new FileOutputStream(OutputFile)) {
					output.write(getJPEGByteArray(outputQuality));
				}
			}
			else{
				RenderedImage rendImage = bufferedImage;
				if (isJPEG2000(FileExtension)){
					ImageIO.write(rendImage, "JPEG 2000", OutputFile);
				}
				else{
					ImageIO.write(rendImage, FileExtension, OutputFile);
				}
				rendImage = null;
			}
			//System.out.println("Output image is " + width + "x" + height + "...");
		}
		catch(Exception e){
			//printError(e);
		}
	}


    /*
    public void setCacheDirectory(java.io.File cacheDirectory){
        try{
            if (cacheDirectory.isFile()){
                cacheDirectory = cacheDirectory.getParentFile();
            }
            cacheDirectory.mkdirs();
            ImageIO.setUseCache(true);
            this.cacheDirectory = cacheDirectory;
        }
        catch(Exception e){
            this.cacheDirectory = null;
        }
    }

    public java.io.File getCacheDirectory(){
        return cacheDirectory;
    }
    */

	//**************************************************************************
	//** setOutputQuality
	//**************************************************************************
	/**  Used to set the output quality/compression ratio. Only applies when
	 *   creating JPEG images. Applied only when writing the image to a file or
	 *   byte array.
	 */
	public void setOutputQuality(double percentage){
		if (percentage>1&&percentage<=100) percentage=percentage/100;
		float q = (float) percentage;
		if (q==1f && useSunCodec) q = 1.2f;
		if (q>=0f && q<=1.2f) outputQuality = q;
	}


	//**************************************************************************
	//** isJPEG
	//**************************************************************************
	/**  Used to determine whether to create a custom jpeg compressed image   */

	private boolean isJPEG(String FileExtension){
		FileExtension = FileExtension.trim().toLowerCase();
		if (FileExtension.equals("jpg") ||
				FileExtension.equals("jpeg") ||
				FileExtension.equals("jpe") ||
				FileExtension.equals("jff") ){
			return true;
		}
		return false;
	}


	//**************************************************************************
	//** isJPEG2000
	//**************************************************************************
	/**  Used to determine whether to create a custom jpeg compressed image   */

	private boolean isJPEG2000(String FileExtension){
		FileExtension = FileExtension.trim().toLowerCase();
		if (FileExtension.equals("jp2") ||
				FileExtension.equals("jpc") ||
				FileExtension.equals("j2k") ||
				FileExtension.equals("jpx") ){
			return true;
		}
		return false;
	}


	//**************************************************************************
	//** getJPEGByteArray
	//**************************************************************************
	/** Returns a JPEG compressed byte array. */

	private byte[] getJPEGByteArray(float outputQuality) throws IOException {
		if (outputQuality>=0f && outputQuality<=1.2f) {
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			BufferedImage bi = bufferedImage;
			int t = bufferedImage.getTransparency();

			//if (t==BufferedImage.BITMASK) System.out.println("BITMASK");
			//if (t==BufferedImage.OPAQUE) System.out.println("OPAQUE");

			if (t==BufferedImage.TRANSLUCENT){
				bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D biContext = bi.createGraphics();
				biContext.drawImage ( bufferedImage, 0, 0, null );
			}



			//First we will try to compress the image using the com.sun.image.codec.jpeg
			//package. These classes are marked as deprecated in JDK 1.7 and several
			//users have reported problems with this method. Instead, we are
			//supposed to use the JPEGImageWriteParam class. However, I have not
			//been able to adequatly test the compression quality or find an
			//anology to the setHorizontalSubsampling and setVerticalSubsampling
			//methods. Therefore, we will attempt to compress the image using the
			//com.sun.image.codec.jpeg package. If the compression fails, we will
			//use the JPEGImageWriteParam.
			if (useSunCodec){


				try{

					//For Java 1.7 users, we will try to invoke the Sun JPEG Codec using reflection
					Object encoder = JPEGCodec.getMethod("createJPEGEncoder", java.io.OutputStream.class).invoke(JPEGCodec, bas);
					Object params = JPEGCodec.getMethod("getDefaultJPEGEncodeParam", BufferedImage.class).invoke(JPEGCodec, bi);
					params.getClass().getMethod("setQuality", float.class, boolean.class).invoke(params, outputQuality, true);
					params.getClass().getMethod("setHorizontalSubsampling", int.class, int.class).invoke(params, 0, 2);
					params.getClass().getMethod("setVerticalSubsampling", int.class, int.class).invoke(params, 0, 2);
					encoder.getClass().getMethod("encode", BufferedImage.class, JPEGEncodeParam).invoke(encoder, bi, params);


					//Here's the original compression code without reflection
                    /*
                    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bas);
                    JPEGEncodeParam params = JPEGCodec.getDefaultJPEGEncodeParam(bi);
                    params.setQuality(outputQuality, true); //true
                    params.setHorizontalSubsampling(0,2);
                    params.setVerticalSubsampling(0,2);
                    encoder.encode(bi, params);
                    */
				}
				catch(Exception e){
					bas.reset();
				}
			}


			//If the com.sun.image.codec.jpeg package is not found or if the
			//compression failed, we will use the JPEGImageWriteParam class.
			if (bas.size()==0){

				if (outputQuality>1f) outputQuality = 1f;

				ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				JPEGImageWriteParam params = (JPEGImageWriteParam) writer.getDefaultWriteParam();
				params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				params.setCompressionQuality(outputQuality);
				writer.setOutput(ImageIO.createImageOutputStream(bas));
				writer.write(null, new IIOImage(bi, null, null), params);
			}


			bas.flush();
			return bas.toByteArray();
		}
		else{
			return getByteArray();
		}
	}


	//**************************************************************************
	//** getImageType
	//**************************************************************************

	private int getImageType(){
		return getImageType(this.bufferedImage);
	}

	private int getImageType(BufferedImage bufferedImage){
		int i = bufferedImage.getType();
		if (i<=0) i = BufferedImage.TYPE_INT_ARGB; //<- is this ok?
		return i;
	}


	//**************************************************************************
	//** getExtension
	//**************************************************************************
	/**  Returns the file extension for a given file name, if one exists. */

	private String getExtension(String FileName){
		if (FileName.contains((CharSequence) ".")){
			return FileName.substring(FileName.lastIndexOf(".")+1,FileName.length());
		}
		else{
			return "";
		}
	}


	//**************************************************************************
	//** hasColor
	//**************************************************************************
	/** Used to determine whether a given pixel has a color value. Returns false
	 *  if the pixel matches the input color or is transparent.
	 */
	private boolean hasColor(int pixel, int red, int green, int blue){

		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;

		if ((r==red && g==green && b==blue) || a==0 ){
			return false;
		}
		return true;
	}


	//**************************************************************************
	//** equals
	//**************************************************************************
	/**  Used to compare this image to another. If the ARGB values match, this
	 *   method will return true.
	 */
	public boolean equals(Object obj){
		if (obj!=null){
			if (obj instanceof LorittaImage){
				LorittaImage image = (LorittaImage) obj;
				if (image.getWidth()==this.getWidth() &&
						image.getHeight()==this.getHeight())
				{

					//Iterate through all the pixels in the image and compare RGB values
					for (int i=0; i<image.getWidth(); i++){
						for (int j=0; j<image.getHeight(); j++){

							if (!image.getColor(i,j).equals(this.getColor(i,j))){
								return false;
							}
						}

					}

					return true;
				}

			}
		}
		return false;
	}





	//**************************************************************************
	//** getIIOMetadata
	//**************************************************************************
	/** Returns the raw, javax.imageio.metadata.IIOMetadata associated with this
	 * image. You can iterate through the metadata using an xml parser like this:
	 <pre>
	 IIOMetadata metadata = image.getMetadata().getIIOMetadata();
	 for (String name : metadata.getMetadataFormatNames()) {
	 System.out.println( "Format name: " + name );
	 org.w3c.dom.Node metadataNode = metadata.getAsTree(name);
	 System.out.println(javaxt.xml.DOM.getNodeValue(metadataNode));
	 }
	 </pre>
	 */
	public IIOMetadata getIIOMetadata(){
		return metadata;
	}


	//**************************************************************************
	//** setIIOMetadata
	//**************************************************************************
	/** Used to set/update the raw javax.imageio.metadata.IIOMetadata associated
	 *  with this image.
	 */
	public void setIIOMetadata(IIOMetadata metadata){
		this.metadata = metadata;
		iptc = null;
		exif = null;
		gps = null;
	}


	//**************************************************************************
	//** getIptcData
	//**************************************************************************
	/** Returns the raw IPTC byte array (marker 0xED).
	 */
	public byte[] getIptcData(){
		return (byte[]) getUnknownTags(0xED)[0].getUserObject();
	}


	//**************************************************************************
	//** getIptcTags
	//**************************************************************************
	/** Used to parse IPTC metadata and return a list of key/value pairs found
	 *  in the metadata. You can retrieve specific IPTC metadata values like
	 *  this:
	 <pre>
	 LorittaImage image = new LorittaImage("/temp/image.jpg");
	 java.util.HashMap&lt;Integer, String&gt; iptc = image.getIptcTags();
	 System.out.println("Date: " + iptc.get(0x0237));
	 System.out.println("Caption: " + iptc.get(0x0278));
	 System.out.println("Copyright: " + iptc.get(0x0274));
	 </pre>
	 */
	public HashMap<Integer, Object> getIptcTags(){

		if (iptc==null){
			iptc = new HashMap<Integer, Object>();
			for (IIOMetadataNode marker : getUnknownTags(0xED)){
				byte[] iptcData = (byte[]) marker.getUserObject();
				HashMap<Integer, Object> tags = new MetadataParser(iptcData, 0xED).getTags("IPTC");
				iptc.putAll(tags);
			}
		}
		return iptc;
	}


	//**************************************************************************
	//** getExifData
	//**************************************************************************
	/** Returns the raw EXIF byte array (marker 0xE1).
	 */
	public byte[] getExifData(){
		return (byte[]) getUnknownTags(0xE1)[0].getUserObject();
	}


	//**************************************************************************
	//** getExifTags
	//**************************************************************************
	/** Used to parse EXIF metadata and return a list of key/value pairs found
	 *  in the metadata. Values can be Strings, Integers, or raw Byte Arrays.
	 *  You can retrieve specific EXIF metadata values like this:
	 <pre>
	 LorittaImage image = new LorittaImage("/temp/image.jpg");
	 java.util.HashMap&lt;Integer, Object&gt; exif = image.getExifTags();
	 System.out.println("Date: " + exif.get(0x0132));
	 System.out.println("Camera: " + exif.get(0x0110));
	 System.out.println("Focal Length: " + exif.get(0x920A));
	 System.out.println("F-Stop: " + exif.get(0x829D));
	 System.out.println("Shutter Speed: " + exif.get(0x829A));
	 </pre>
	 * Note that the EXIF MakerNote is not parsed.
	 */
	public HashMap<Integer, Object> getExifTags(){
		if (exif==null) parseExif();
		return exif;
	}


	//**************************************************************************
	//** getGpsTags
	//**************************************************************************
	/** Used to parse EXIF metadata and return a list of key/value pairs
	 *  associated with GPS metadata. Values can be Strings, Integers, or raw
	 *  Byte Arrays.
	 */
	public HashMap<Integer, Object> getGpsTags(){
		if (gps==null) parseExif();
		return gps;
	}


	/** Private method used to initialize the exif and gps hashmaps */
	private void parseExif(){

		exif = new HashMap<Integer, Object>();
		gps = new HashMap<Integer, Object>();
		for (IIOMetadataNode marker : getUnknownTags(0xE1)){
			byte[] exifData = (byte[]) marker.getUserObject();

			MetadataParser metadataParser = new MetadataParser(exifData, 0xE1);
			HashMap<Integer, Object> exif = metadataParser.getTags("EXIF");
			HashMap<Integer, Object> gps = metadataParser.getTags("GPS");

			if (exif!=null) this.exif.putAll(exif);
			if (gps!=null) this.gps.putAll(gps);

			metadataParser = null;
		}
	}


	//**************************************************************************
	//** getGPSCoordinate
	//**************************************************************************
	/** Returns the x/y (lon/lat) coordinate tuple for the image. Value is
	 *  derived from EXIF GPS metadata (tags 0x0001, 0x0002, 0x0003, 0x0004).
	 */
	public double[] getGPSCoordinate(){
		getExifTags();
		try{
			Double lat = getCoordinate((String) gps.get(0x0002));
			Double lon = getCoordinate((String) gps.get(0x0004));
			String latRef = (String) gps.get(0x0001); //N
			String lonRef = (String) gps.get(0x0003); //W

			if (!latRef.equalsIgnoreCase("N")) lat = -lat;
			if (!lonRef.equalsIgnoreCase("E")) lon = -lon;

			return new double[]{lon, lat};
		}
		catch(Exception e){
			return null;
		}
	}


	private double getCoordinate(String RationalArray) {

		//num + "/" + den
		String[] arr = RationalArray.substring(1, RationalArray.length()-1).split(",");
		String[] deg = arr[0].trim().split("/");
		String[] min = arr[1].trim().split("/");
		String[] sec = arr[2].trim().split("/");

		double degNumerator = Double.parseDouble(deg[0]);
		double degDenominator = 1D; try{degDenominator = Double.parseDouble(deg[1]);} catch(Exception e){}
		double minNumerator = Double.parseDouble(min[0]);
		double minDenominator = 1D; try{minDenominator = Double.parseDouble(min[1]);} catch(Exception e){}
		double secNumerator = Double.parseDouble(sec[0]);
		double secDenominator = 1D; try{secDenominator = Double.parseDouble(sec[1]);} catch(Exception e){}

		double m = 0;
		if (degDenominator != 0 || degNumerator != 0){
			m = (degNumerator / degDenominator);
		}

		if (minDenominator != 0 || minNumerator != 0){
			m += (minNumerator / minDenominator) / 60D;
		}

		if (secDenominator != 0 || secNumerator != 0){
			m += (secNumerator / secDenominator / 3600D);
		}

		return m;
	}


	//**************************************************************************
	//** getGPSDatum
	//**************************************************************************
	/** Returns the datum associated with the GPS coordinate. Value is
	 *  derived from EXIF GPS metadata (tag 0x0012).
	 */
	public String getGPSDatum(){
		getExifTags();
		return (String) gps.get(0x0012);
	}


	//**************************************************************************
	//** getUnknownTags
	//**************************************************************************
	/** Returns a list of "unknown" IIOMetadataNodes for a given MarkerTag. You
	 *  can use this method to retrieve EXIF, IPTC, XPM, and other format
	 *  specific metadata. Example:
	 <pre>
	 byte[] IptcData = (byte[]) metadata.getUnknownTags(0xED)[0].getUserObject();
	 byte[] ExifData = (byte[]) metadata.getUnknownTags(0xE1)[0].getUserObject();
	 </pre>
	 */
	public IIOMetadataNode[] getUnknownTags(int MarkerTag){
		java.util.ArrayList<IIOMetadataNode> markers = new java.util.ArrayList<IIOMetadataNode>();
		if (metadata!=null)
			for (String name : metadata.getMetadataFormatNames()) {
				IIOMetadataNode node=(IIOMetadataNode) metadata.getAsTree(name);
				Node[] unknownNodes = getElementsByTagName("unknown", node);
				for (Node unknownNode : unknownNodes){
					try{
						int marker = Integer.parseInt(getAttributeValue(unknownNode.getAttributes(), "MarkerTag"));
						if (marker==MarkerTag) markers.add((IIOMetadataNode) unknownNode);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		return markers.toArray(new IIOMetadataNode[markers.size()]);
	}

	//**************************************************************************
	//** getMetadataByTagName
	//**************************************************************************
	/** Returns a list of IIOMetadataNodes for a given tag name (e.g. "Chroma",
	 *  "Compression", "Data", "Dimension", "Transparency", etc).
	 <pre>
	 //Print unknown tags
	 for (IIOMetadataNode unknownNode : metadata.getMetadataByTagName("unknown")){
	 int marker = Integer.parseInt(javaxt.xml.DOM.getAttributeValue(unknownNode, "MarkerTag"));
	 System.out.println(marker + "\t" + "0x" + Integer.toHexString(marker));
	 }
	 </pre>
	 */
	public IIOMetadataNode[] getMetadataByTagName(String tagName){
		java.util.ArrayList<IIOMetadataNode> tags = new java.util.ArrayList<IIOMetadataNode>();
		if (metadata!=null)
			for (String name : metadata.getMetadataFormatNames()) {
				IIOMetadataNode node=(IIOMetadataNode) metadata.getAsTree(name);
				Node[] unknownNodes = getElementsByTagName(tagName, node);
				for (Node unknownNode : unknownNodes){
					tags.add((IIOMetadataNode) unknownNode);
				}
			}
		return tags.toArray(new IIOMetadataNode[tags.size()]);
	}


	//**************************************************************************
	//** getElementsByTagName (Copied from javaxt.xml.DOM)
	//**************************************************************************
	/** Returns an array of nodes that match a given tagName (node name). The
	 *  results will include all nodes that match, regardless of namespace. To
	 *  narrow the results to a specific namespace, simply include the namespace
	 *  prefix in the tag name (e.g. "t:Contact"). Returns an empty array if
	 *  no nodes are found.
	 */
	private static Node[] getElementsByTagName(String tagName, Node node){
		java.util.ArrayList<Node> nodes = new java.util.ArrayList<Node>();
		getElementsByTagName(tagName, node, nodes);
		return nodes.toArray(new Node[nodes.size()]);
	}

	private static void getElementsByTagName(String tagName, Node node, java.util.ArrayList<Node> nodes){
		if (node!=null && node.getNodeType()==1){

			String nodeName = node.getNodeName().trim();
			if (nodeName.contains(":") && !tagName.contains(":")){
				nodeName = nodeName.substring(nodeName.indexOf(":")+1);
			}

			if (nodeName.equalsIgnoreCase(tagName)){
				nodes.add(node);
			}

			NodeList childNodes = node.getChildNodes();
			for (int i=0; i<childNodes.getLength(); i++){
				getElementsByTagName(tagName, childNodes.item(i), nodes);
			}
		}
	}


	//**************************************************************************
	//** getAttributeValue (Copied from javaxt.xml.DOM)
	//**************************************************************************
	/**  Used to return the value of a given node attribute. The search is case
	 *   insensitive. If no match is found, returns an empty string.
	 */
	public static String getAttributeValue(NamedNodeMap attrCollection, String attrName){

		if (attrCollection!=null){
			for (int i=0; i < attrCollection.getLength(); i++ ) {
				Node node = attrCollection.item(i);
				if (node.getNodeName().equalsIgnoreCase(attrName)) {
					return node.getNodeValue();
				}
			}
		}
		return "";
	}


	//******************************************************************************
	//**  MetadataParser Class
	//******************************************************************************
	/**
	 *   Used to decode EXIF and IPTC metadata. Adapted from 2 classes developed by
	 *   Norman Walsh and released under the W3C open source license. The original
	 *   exif classes can be found in the W3C Jigsaw project in the
	 *   org.w3c.tools.jpeg package.
	 *
	 *  @author  Norman Walsh
	 *  @copyright Copyright (c) 2003 Norman Walsh
	 ******************************************************************************/

	private static class MetadataParser {

		// Implementation notes:
		// (1) Merged Version 1.1 of the "Exif.java" and "ExifData.java" classes.
		// (2) Added new IPTC metadata parser.
		// (3) All unsigned integers are treated as signed ints (should be longs).
		// (4) Added logic to parse GPS Info using the GPS IFD offset value (tag 34853,
		//     hex 0x8825).
		// (5) Added logic to parse an array of rational numbers (e.g. GPS metadata).
		// (6) Improved performance in the parseExif() method by serializing only the
		//     first 8 characters into a string (vs the entire EXIF byte array).
		// (7) TODO: Need to come up with a clever scheme to parse MakerNotes.

		private final int bytesPerFormat[] = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};
		private final int NUM_FORMATS = 12;
		private final int FMT_BYTE = 1;
		private final int FMT_STRING = 2;
		private final int FMT_USHORT = 3;
		private final int FMT_ULONG = 4;
		private final int FMT_URATIONAL = 5;
		private final int FMT_SBYTE = 6;
		//private final int FMT_UNDEFINED = 7;
		private final int FMT_SSHORT = 8;
		private final int FMT_SLONG = 9;
		private final int FMT_SRATIONAL = 10;
		//private final int FMT_SINGLE = 11;
		//private final int FMT_DOUBLE = 12;

		private byte[] data = null;
		private boolean intelOrder = false;

		private final int TAG_EXIF_OFFSET = 0x8769;
		private final int TAG_INTEROP_OFFSET = 0xa005;
		private final int TAG_GPS_OFFSET = 0x8825;
		private final int TAG_USERCOMMENT = 0x9286;

		private HashMap<String, HashMap<Integer, Object>> tags =
				new HashMap<String, HashMap<Integer, Object>>();


		public MetadataParser(byte[] data, int marker) {
			switch (marker) {
			case 0xED: parseIptc(data); break;
			case 0xE1: parseExif(data); break;
			}

			data = null;
		}


		//**************************************************************************
		//** parseIptc
		//**************************************************************************
		/** Used to parse IPTC metadata
		 */
		private void parseIptc(byte[] iptcData) {

			HashMap<Integer, Object> tags = new HashMap<Integer, Object>();
			this.tags.put("IPTC", tags);

			data = iptcData;

			int offset = 0;
			while (offset < data.length) {
				if (data[offset] == 0x1c) {

					offset++;

					int directoryType;
					int tagType;
					int tagByteCount;
					try {
						directoryType = data[offset++];
						tagType = data[offset++];
						tagByteCount = get16u(offset);
						offset += 2;
					}
					catch (Exception e) {
						return;
					}


					int tagIdentifier = tagType | (directoryType << 8);

					String str = "";
					if (tagByteCount < 1 || tagByteCount>(data.length-offset)) {
					}
					else {
						try {
							str = new String(data, offset, tagByteCount, "UTF-8");
							offset += tagByteCount;
						}
						catch (Exception e) {
						}
					}
					tags.put(tagIdentifier, str);
				}
				else{
					offset++;
				}
			}
		}


		//**************************************************************************
		//** parseExif
		//**************************************************************************
		/** Used to parse EXIF metadata
		 */
		public void parseExif(byte[] exifData) {

			HashMap<Integer, Object> tags = new HashMap<Integer, Object>();
			this.tags.put("EXIF", tags);


			try{
				String dataStr = new String(exifData, 0, 8, "UTF-8"); //new String(exifData);
				if (exifData.length <= 4 || !"Exif".equals(dataStr.substring(0, 4))) {
					//System.err.println("Not really EXIF data");
					return;
				}

				String byteOrderMarker = dataStr.substring(6, 8);
				if ("II".equals(byteOrderMarker)) {
					intelOrder = true;
				} else if ("MM".equals(byteOrderMarker)) {
					intelOrder = false;
				} else {
					//System.err.println("Incorrect byte order in EXIF data.");
					return;
				}
			}
			catch(Exception e){
				return;
			}


			data = exifData;

			int checkValue = get16u(8);
			if (checkValue != 0x2a) {
				data = null;
				//System.err.println("Check value fails: 0x"+ Integer.toHexString(checkValue));
				return;
			}


			if (data==null) return;


			int firstOffset = get32u(10);
			processExifDir(6 + firstOffset, 6, tags);
		}



		//**************************************************************************
		//** getTags
		//**************************************************************************
		/** Returns key/value pairs representing the EXIF or IPTC data.
		 */
		public HashMap<Integer, Object> getTags(String dir) {
			return tags.get(dir);
		}


		private void processExifDir(int dirStart, int offsetBase, HashMap<Integer, Object> tags) {
			if (dirStart>=data.length) return;


			int numEntries = get16u(dirStart);
			for (int de = 0; de < numEntries; de++) {
				int dirOffset = dirStart + 2 + (12 * de);

				int tag = get16u(dirOffset);
				int format = get16u(dirOffset + 2);
				int components = get32u(dirOffset + 4);

				//System.err.println("EXIF: entry: 0x" + Integer.toHexString(tag)
				//		 + " " + format
				//		 + " " + components);

				if (format < 0 || format > NUM_FORMATS) {
					//System.err.println("Bad number of formats in EXIF dir: " + format);
					return;
				}

				int byteCount = components * bytesPerFormat[format];
				int valueOffset = dirOffset + 8;

				if (byteCount > 4) {
					int offsetVal = get32u(dirOffset + 8);
					valueOffset = offsetBase + offsetVal;
				}

				if (tag == TAG_EXIF_OFFSET || tag == TAG_INTEROP_OFFSET || tag == TAG_GPS_OFFSET) {

					String dirName = "";
					switch (tag) {
					case TAG_EXIF_OFFSET:
						dirName = "EXIF";
						break;
					case TAG_INTEROP_OFFSET:
						dirName = "EXIF";
						break;
					case TAG_GPS_OFFSET:
						dirName = "GPS";
						break;
					}

					tags = this.tags.get(dirName);
					if (tags==null){
						tags = new HashMap<Integer, Object>();
						this.tags.put(dirName, tags);
					}

					int subdirOffset = get32u(valueOffset);
					processExifDir(offsetBase + subdirOffset, offsetBase, tags);

				}

				//else if (tag==0x927c){ //Maker Note

				//TODO: Come up with a clever way to process the Maker Note
				//data = java.util.Arrays.copyOfRange(data, valueOffset, byteCount);
				//tags = new HashMap<Integer, String>();
				//processExifDir(0, 6);

				//}

				else {

					switch (format) {
					case FMT_STRING:
						String value = getString(valueOffset, byteCount);
						if (value!=null) tags.put(tag, value);
						break;
					case FMT_SBYTE:
					case FMT_BYTE:
					case FMT_USHORT:
					case FMT_SSHORT:
					case FMT_ULONG:
					case FMT_SLONG:
						tags.put(tag, (int) getDouble(format, valueOffset));
						break;
					case FMT_URATIONAL:
					case FMT_SRATIONAL:

						if (components>1) {

							//Create a string representing an array of rational numbers
							StringBuffer str = new StringBuffer();
							str.append("[");
							for (int i=0; i<components; i++){
								str.append( getRational(valueOffset + (8 * i)) );
								if (i<components-1) str.append(",");
							}
							str.append("]");
							tags.put(tag, str.toString());
						}
						else{
							tags.put(tag, getRational(valueOffset));
						}
						break;


					default: //including FMT_UNDEFINED
						byte[] result = getUndefined(valueOffset, byteCount);
						if (result!=null) tags.put(tag, result);
						break;
					}

				}
			}
		}

		//**************************************************************************
		//** getRational
		//**************************************************************************
		/** Returns a string representation of a rational number (numerator and
		 *  denominator separated with a "/" character).
		 */
		private String getRational(int offset) {
			int num = get32s(offset);
			int den = get32s(offset + 4);
			String result = "";

			// This is a bit silly, I really ought to find a real GCD algorithm
			if (num % 10 == 0 && den % 10 == 0) {
				num = num / 10;
				den = den / 10;
			}

			if (num % 5 == 0 && den % 5 == 0) {
				num = num / 5;
				den = den / 5;
			}

			if (num % 3 == 0 && den % 3 == 0) {
				num = num / 3;
				den = den / 3;
			}

			if (num % 2 == 0 && den % 2 == 0) {
				num = num / 2;
				den = den / 2;
			}

			if (den == 0) {
				result = "0";
			} else if (den == 1) {
				result = "" + num; // "" + int sure looks ugly...
			} else {
				result = "" + num + "/" + den;
			}
			return result;
		}

		private int get16s(int offset) {
			int hi, lo;

			if (intelOrder) {
				hi = data[offset + 1];
				lo = data[offset];
			} else {
				hi = data[offset];
				lo = data[offset + 1];
			}

			lo = lo & 0xFF;
			hi = hi & 0xFF;

			return (hi << 8) + lo;
		}

		private int get16u(int offset) {
			int value = get16s(offset);
			return value & 0xFFFF;
		}

		private int get32s(int offset) {
			int n1, n2, n3, n4;

			if (intelOrder) {
				n1 = data[offset + 3] & 0xFF;
				n2 = data[offset + 2] & 0xFF;
				n3 = data[offset + 1] & 0xFF;
				n4 = data[offset] & 0xFF;
			} else {
				n1 = data[offset] & 0xFF;
				n2 = data[offset + 1] & 0xFF;
				n3 = data[offset + 2] & 0xFF;
				n4 = data[offset + 3] & 0xFF;
			}

			return (n1 << 24) + (n2 << 16) + (n3 << 8) + n4;
		}

		private int get32u(int offset) {
			return get32s(offset); //Should probably return a long instead...
		}

		private byte[] getUndefined(int offset, int length) {
			return java.util.Arrays.copyOfRange(data, offset, offset+length);
		}

		private String getString(int offset, int length) {
			try{
				return new String(data, offset, length, "UTF-8").trim();
			}
			catch(Exception e){
				return null;
			}
		}

		//**************************************************************************
		//** getDouble
		//**************************************************************************
		/** Used convert a byte into a double. Note that this method used to be
		 *  called convertAnyValue().
		 */
		private double getDouble(int format, int offset) {
			switch (format) {
			case FMT_SBYTE:
				return data[offset];
			case FMT_BYTE:
				int iValue = data[offset];
				return iValue & 0xFF;
			case FMT_USHORT:
				return get16u(offset);
			case FMT_ULONG:
				return get32u(offset);
			case FMT_URATIONAL:
			case FMT_SRATIONAL:
				int num = get32s(offset);
				int den = get32s(offset + 4);
				if (den == 0) return 0;
				else return (double) num / (double) den;
			case FMT_SSHORT:
				return get16s(offset);
			case FMT_SLONG:
				return get32s(offset);
			default:
				return 0.0;
			}
		}
	}



	//***************************************************************************
	//**  Skew Class
	//***************************************************************************
	/**
	 *   Used to skew an image. Adapted from 2 image processing classes developed
	 *   by Jerry Huxtable (http://www.jhlabs.com) and released under
	 *   the Apache License, Version 2.0.
	 *
	 ***************************************************************************/

	private static class Skew {

		public final static int ZERO = 0;
		public final static int CLAMP = 1;
		public final static int WRAP = 2;

		public final static int NEAREST_NEIGHBOUR = 0;
		public final static int BILINEAR = 1;

		protected int edgeAction = ZERO;
		protected int interpolation = BILINEAR;

		protected Rectangle transformedSpace;
		protected Rectangle originalSpace;

		private float x0, y0, x1, y1, x2, y2, x3, y3;
		private float dx1, dy1, dx2, dy2, dx3, dy3;
		private float A, B, C, D, E, F, G, H, I;


		private BufferedImage src;
		private BufferedImage dst;


		public Skew(BufferedImage src) {
			this.src = src;
			this.dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		}

		public Skew(LorittaImage src) {
			this(src.getBufferedImage());
		}


		public BufferedImage setCorners(float x0, float y0,
				float x1, float y1,
				float x2, float y2,
				float x3, float y3)
		{
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.x3 = x3;
			this.y3 = y3;

			dx1 = x1-x2;
			dy1 = y1-y2;
			dx2 = x3-x2;
			dy2 = y3-y2;
			dx3 = x0-x1+x2-x3;
			dy3 = y0-y1+y2-y3;

			float a11, a12, a13, a21, a22, a23, a31, a32;

			if (dx3 == 0 && dy3 == 0) {
				a11 = x1-x0;
				a21 = x2-x1;
				a31 = x0;
				a12 = y1-y0;
				a22 = y2-y1;
				a32 = y0;
				a13 = a23 = 0;
			} else {
				a13 = (dx3*dy2-dx2*dy3)/(dx1*dy2-dy1*dx2);
				a23 = (dx1*dy3-dy1*dx3)/(dx1*dy2-dy1*dx2);
				a11 = x1-x0+a13*x1;
				a21 = x3-x0+a23*x3;
				a31 = x0;
				a12 = y1-y0+a13*y1;
				a22 = y3-y0+a23*y3;
				a32 = y0;
			}

			A = a22 - a32*a23;
			B = a31*a23 - a21;
			C = a21*a32 - a31*a22;
			D = a32*a13 - a12;
			E = a11 - a31*a13;
			F = a31*a12 - a11*a32;
			G = a12*a23 - a22*a13;
			H = a21*a13 - a11*a23;
			I = a11*a22 - a21*a12;


			return filter(src,dst);
		}



		protected void transformSpace(Rectangle rect) {
			rect.x = (int)Math.min( Math.min( x0, x1 ), Math.min( x2, x3 ) );
			rect.y = (int)Math.min( Math.min( y0, y1 ), Math.min( y2, y3 ) );
			rect.width = (int)Math.max( Math.max( x0, x1 ), Math.max( x2, x3 ) ) - rect.x;
			rect.height = (int)Math.max( Math.max( y0, y1 ), Math.max( y2, y3 ) ) - rect.y;
		}


		public float getOriginX() {
			return x0 - (int)Math.min( Math.min( x0, x1 ), Math.min( x2, x3 ) );
		}

		public float getOriginY() {
			return y0 - (int)Math.min( Math.min( y0, y1 ), Math.min( y2, y3 ) );
		}


		private BufferedImage filter( BufferedImage src, BufferedImage dst ) {
			int width = src.getWidth();
			int height = src.getHeight();
			//int type = src.getType();
			//WritableRaster srcRaster = src.getRaster();

			originalSpace = new Rectangle(0, 0, width, height);
			transformedSpace = new Rectangle(0, 0, width, height);
			transformSpace(transformedSpace);

			if ( dst == null ) {
				ColorModel dstCM = src.getColorModel();
				dst = new BufferedImage(
						dstCM,
						dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height),
						dstCM.isAlphaPremultiplied(),
						null
				);
			}
			//WritableRaster dstRaster = dst.getRaster();

			int[] inPixels = getRGB( src, 0, 0, width, height, null );

			if ( interpolation == NEAREST_NEIGHBOUR )
				return filterPixelsNN( dst, width, height, inPixels, transformedSpace );

			int srcWidth = width;
			int srcHeight = height;
			int srcWidth1 = width-1;
			int srcHeight1 = height-1;
			int outWidth = transformedSpace.width;
			int outHeight = transformedSpace.height;
			int outX, outY;
			//int index = 0;
			int[] outPixels = new int[outWidth];

			outX = transformedSpace.x;
			outY = transformedSpace.y;
			float[] out = new float[2];

			for (int y = 0; y < outHeight; y++) {
				for (int x = 0; x < outWidth; x++) {
					transformInverse(outX+x, outY+y, out);
					int srcX = (int)Math.floor( out[0] );
					int srcY = (int)Math.floor( out[1] );
					float xWeight = out[0]-srcX;
					float yWeight = out[1]-srcY;
					int nw, ne, sw, se;

					if ( srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
						// Easy case, all corners are in the image
						int i = srcWidth*srcY + srcX;
						nw = inPixels[i];
						ne = inPixels[i+1];
						sw = inPixels[i+srcWidth];
						se = inPixels[i+srcWidth+1];
					} else {
						// Some of the corners are off the image
						nw = getPixel( inPixels, srcX, srcY, srcWidth, srcHeight );
						ne = getPixel( inPixels, srcX+1, srcY, srcWidth, srcHeight );
						sw = getPixel( inPixels, srcX, srcY+1, srcWidth, srcHeight );
						se = getPixel( inPixels, srcX+1, srcY+1, srcWidth, srcHeight );
					}
					outPixels[x] = bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
				}
				setRGB( dst, outX, outY + y, transformedSpace.width, 1, outPixels );
			}
			return dst;
		}

		final private int getPixel( int[] pixels, int x, int y, int width, int height ) {
			if (x < 0 || x >= width || y < 0 || y >= height) {
				switch (edgeAction) {
				case ZERO:
				default:
					return 0;
				case WRAP:
					return pixels[(mod(y, height) * width) + mod(x, width)];
				case CLAMP:
					return pixels[(clamp(y, 0, height-1) * width) + clamp(x, 0, width-1)];
				}
			}
			return pixels[ y*width+x ];
		}


		protected BufferedImage filterPixelsNN( BufferedImage dst, int width,
				int height, int[] inPixels, Rectangle transformedSpace )
		{
			int srcWidth = width;
			int srcHeight = height;
			int outWidth = transformedSpace.width;
			int outHeight = transformedSpace.height;
			int outX, outY, srcX, srcY;
			int[] outPixels = new int[outWidth];

			outX = transformedSpace.x;
			outY = transformedSpace.y;
			int[] rgb = new int[4];
			float[] out = new float[2];

			for (int y = 0; y < outHeight; y++) {
				for (int x = 0; x < outWidth; x++) {
					transformInverse(outX+x, outY+y, out);
					srcX = (int)out[0];
					srcY = (int)out[1];
					// int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
					if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight) {
						int p;
						switch (edgeAction) {
						case ZERO:
						default:
							p = 0;
							break;
						case WRAP:
							p = inPixels[(mod(srcY, srcHeight) * srcWidth) + mod(srcX, srcWidth)];
							break;
						case CLAMP:
							p = inPixels[(clamp(srcY, 0, srcHeight-1) * srcWidth) + clamp(srcX, 0, srcWidth-1)];
							break;
						}
						outPixels[x] = p;
					} else {
						int i = srcWidth*srcY + srcX;
						rgb[0] = inPixels[i];
						outPixels[x] = inPixels[i];
					}
				}
				setRGB( dst, 0, y, transformedSpace.width, 1, outPixels );
			}
			return dst;
		}


		protected void transformInverse(int x, int y, float[] out) {
			out[0] = originalSpace.width * (A*x+B*y+C)/(G*x+H*y+I);
			out[1] = originalSpace.height * (D*x+E*y+F)/(G*x+H*y+I);
		}

/*
    public Rectangle2D getBounds2D( BufferedImage src ) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
        if ( dstPt == null )
            dstPt = new Point2D.Double();
        dstPt.setLocation( srcPt.getX(), srcPt.getY() );
        return dstPt;
    }
*/

		/**
		 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
		 * penalty of BufferedImage.getRGB unmanaging the image.
		 */
		public int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
			int type = image.getType();
			if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
				return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
			return image.getRGB( x, y, width, height, pixels, 0, width );
		}

		/**
		 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
		 * penalty of BufferedImage.setRGB unmanaging the image.
		 */
		public void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
			int type = image.getType();
			if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
				image.getRaster().setDataElements( x, y, width, height, pixels );
			else
				image.setRGB( x, y, width, height, pixels, 0, width );
		}



		/**
		 * Clamp a value to an interval.
		 * @param a the lower clamp threshold
		 * @param b the upper clamp threshold
		 * @param x the input parameter
		 * @return the clamped value
		 */
		private float clamp(float x, float a, float b) {
			return (x < a) ? a : (x > b) ? b : x;
		}

		/**
		 * Clamp a value to an interval.
		 * @param a the lower clamp threshold
		 * @param b the upper clamp threshold
		 * @param x the input parameter
		 * @return the clamped value
		 */
		private int clamp(int x, int a, int b) {
			return (x < a) ? a : (x > b) ? b : x;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to negative numbers.
		 * @param a the dividend
		 * @param b the divisor
		 * @return a mod b
		 */
		private double mod(double a, double b) {
			int n = (int)(a/b);

			a -= n*b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to negative numbers.
		 * @param a the dividend
		 * @param b the divisor
		 * @return a mod b
		 */
		private float mod(float a, float b) {
			int n = (int)(a/b);

			a -= n*b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to negative numbers.
		 * @param a the dividend
		 * @param b the divisor
		 * @return a mod b
		 */
		private int mod(int a, int b) {
			int n = a/b;

			a -= n*b;
			if (a < 0)
				return a + b;
			return a;
		}


		/**
		 * Bilinear interpolation of ARGB values.
		 * @param x the X interpolation parameter 0..1
		 * @param y the y interpolation parameter 0..1
		 * @return the interpolated value
		 */
		private int bilinearInterpolate(float x, float y, int nw, int ne, int sw, int se) {
			float m0, m1;
			int a0 = (nw >> 24) & 0xff;
			int r0 = (nw >> 16) & 0xff;
			int g0 = (nw >> 8) & 0xff;
			int b0 = nw & 0xff;
			int a1 = (ne >> 24) & 0xff;
			int r1 = (ne >> 16) & 0xff;
			int g1 = (ne >> 8) & 0xff;
			int b1 = ne & 0xff;
			int a2 = (sw >> 24) & 0xff;
			int r2 = (sw >> 16) & 0xff;
			int g2 = (sw >> 8) & 0xff;
			int b2 = sw & 0xff;
			int a3 = (se >> 24) & 0xff;
			int r3 = (se >> 16) & 0xff;
			int g3 = (se >> 8) & 0xff;
			int b3 = se & 0xff;

			float cx = 1.0f-x;
			float cy = 1.0f-y;

			m0 = cx * a0 + x * a1;
			m1 = cx * a2 + x * a3;
			int a = (int)(cy * m0 + y * m1);

			m0 = cx * r0 + x * r1;
			m1 = cx * r2 + x * r3;
			int r = (int)(cy * m0 + y * m1);

			m0 = cx * g0 + x * g1;
			m1 = cx * g2 + x * g3;
			int g = (int)(cy * m0 + y * m1);

			m0 = cx * b0 + x * b1;
			m1 = cx * b2 + x * b3;
			int b = (int)(cy * m0 + y * m1);

			return (a << 24) | (r << 16) | (g << 8) | b;
		}


	} //end skew class

} //end image class