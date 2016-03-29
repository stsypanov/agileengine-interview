package tsypanov;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class Main {

	private static String path;

	public static void main(String[] args) {

		File image1 = getFile(args[0]);

		File image2 = getFile(args[1]);

		path = image2.getPath();

		readImages(image1, image2);
	}

	private static File getFile(String arg) {
		try {
			return Paths.get(Main.class.getClassLoader().getResource(arg).toURI()).toFile();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to get file for comparison", e);
		}
	}

	private static void readImages(File image1, File image2) {
		try {
			BufferedImage bufferedImage1 = ImageIO.read(image1);
			BufferedImage bufferedImage2 = ImageIO.read(image2);


			proceedImages(bufferedImage1, bufferedImage2);

		} catch (IOException e) {
			throw new RuntimeException("Something went wrong while reading the files", e);
		}
	}

	private static void proceedImages(BufferedImage bufferedImage1, BufferedImage bufferedImage2) {
		checkImages(bufferedImage1, bufferedImage2);

		int width = bufferedImage1.getWidth();
		int height = bufferedImage1.getHeight();

		List<Point> diffPoints = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			compareRows(diffPoints, bufferedImage1, bufferedImage2, y, width);
		}

		writeResult(bufferedImage2, diffPoints);

	}

	private static void compareRows(List<Point> diffPoints,
									BufferedImage bufferedImage1, BufferedImage bufferedImage2,
									int y, int width) {
		for (int x = 0; x < width; x++) {
			int rgb1 = bufferedImage1.getRGB(x, y);
			int rgb2 = bufferedImage2.getRGB(x, y);
			boolean result = comparePixels(rgb1, rgb2);
			if (result) {
				diffPoints.add(new Point(x, y));
			}
		}
	}

	private static boolean comparePixels(int rgb1, int rgb2) {
		Color color1 = new Color(rgb1);
		Color color2 = new Color(rgb2);

		return comparePixels(color1, color2);
	}

	private static boolean comparePixels(Color color1, Color color2) {
		final int red1 = color1.getRed();
		final int blue1 = color1.getBlue();
		final int green1 = color1.getGreen();

		final int red2 = color2.getRed();
		final int blue2 = color2.getBlue();
		final int green2 = color2.getGreen();

		double percentageDifference = sqrt(
				pow((double) (red2 - red1), 2) +
						pow((double) (green2 - green1), 2) +
						pow((double) (blue2 - blue1), 2)
		);

		return percentageDifference > 0.1;
	}

	private static void writeResult(BufferedImage bufferedImage2, List<Point> diffPoints) {

		BufferedImage im = copyImage(bufferedImage2);

		for (Point point : diffPoints) {
			im.setRGB(point.getX(), point.getY(), Color.RED.getRGB());
		}

		write(im);
	}

	private static BufferedImage copyImage(BufferedImage image) {
		return new BufferedImage(
				image.getColorModel(),
				image.copyData(null),
				image.getColorModel().isAlphaPremultiplied(),
				null
		);
	}

	private static void write(BufferedImage im) {
		try {
			String resultFileName = stripFileName(path) + "_comparison" + ".png";
			ImageIO.write(im, "png", new FileOutputStream(new File(resultFileName)));
		} catch (IOException e) {
			throw new RuntimeException("Something went wrong while writing result", e);
		}
	}

	private static void checkImages(BufferedImage bufferedImage1, BufferedImage bufferedImage2) {
		if (bufferedImage1.getWidth() != bufferedImage2.getWidth() ||
				bufferedImage1.getHeight() != bufferedImage2.getHeight()) {
			throw new RuntimeException("Images must have the same dimension");
		}
	}

	private static String stripFileName(String fullName){
		return fullName.replaceFirst("[.][^.]+$", "");
	}
}
