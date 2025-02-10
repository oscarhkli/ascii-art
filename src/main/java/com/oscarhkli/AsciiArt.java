package com.oscarhkli;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class AsciiArt {

  private static final String ASCII_MATRIX = "`^\",:;Il!i~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$";
  private static final int MAX_SIZE = 400;
  private static final String IMAGE_PATH = "ealing-common-station.jpg";
  private static final int REPEAT_CHAR = 2;

  private enum BrightnessType {
    AVERAGE {
      @Override
      double getBrightness(int red, int green, int blue) {
        return (red + green + blue) / 3.0;
      }
    }, LIGHTNESS {
      @Override
      double getBrightness(int red, int green, int blue) {
        return (Math.max(red, Math.max(green, blue)) + Math.min(red, Math.min(green, blue))) / 2.0;
      }
    }, LUMINOSITY {
      @Override
      double getBrightness(int red, int green, int blue) {
        return Math.sqrt(0.299 * red * red + 0.587 * green * green + 0.114 * blue * blue);
      }
    },
    ;

    abstract double getBrightness(int red, int green, int blue);
  }

  public void run(String strategyArg) throws IOException {
    BufferedImage image;
    try (var resource = this.getClass().getClassLoader().getResourceAsStream(IMAGE_PATH)) {
      image = ImageIO.read(Objects.requireNonNull(resource));
    }
    var resizedImage = resizeImage(image);

    BrightnessType brightnessType = switch (strategyArg) {
      case "avg" -> BrightnessType.AVERAGE;
      case "hsl" -> BrightnessType.LIGHTNESS;
      default -> BrightnessType.LUMINOSITY;
    };

    var brightnessNumbers = getBrightnessNumber(resizedImage, brightnessType);
    var result = convertToAsciiMatrix(brightnessNumbers);

    try (var stream = new FileOutputStream("output.txt")) {
      stream.write(result.getBytes());
    }
  }

  private String convertToAsciiMatrix(double[][] brightnessNumbers) {
    var sb = new StringBuilder();
    for (var row : brightnessNumbers) {
      for (var col : row) {
        sb.repeat(ASCII_MATRIX.charAt((int) Math.floor(ASCII_MATRIX.length() * col / 256)),
            REPEAT_CHAR);
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  private BufferedImage resizeImage(BufferedImage image) {
    var width = image.getWidth();
    var height = image.getHeight();
    if (width <= MAX_SIZE && height <= MAX_SIZE) {
      return image;
    }

    int newWidth;
    int newHeight;
    if (width > height) {
      newWidth = MAX_SIZE;
      newHeight = height * MAX_SIZE / width;
    } else {
      newHeight = MAX_SIZE;
      newWidth = width * MAX_SIZE / height;
    }
    final var resizedImage = new BufferedImage(newWidth, newHeight, image.getType());
    final var graphics = resizedImage.createGraphics();
    graphics.drawImage(image, 0, 0, newWidth, newHeight, null);
    graphics.dispose();
    return resizedImage;
  }

  private double[][] getBrightnessNumber(BufferedImage image, BrightnessType strategy) {
    var height = image.getHeight();
    var width = image.getWidth();
    var brightnessNumbers = new double[height][width];
    for (var y = 0; y < height; y++) {
      for (var x = 0; x < width; x++) {
        var color = new Color(image.getRGB(x, y));
        brightnessNumbers[y][x] = strategy.getBrightness(color.getRed(), color.getGreen(),
            color.getBlue());
      }
    }
    return brightnessNumbers;
  }
}
