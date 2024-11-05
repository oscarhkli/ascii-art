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
        AVERAGE, LIGHTNESS, LUMINOSITY
    }

    public void run() {
        try {
            var resource = this.getClass().getClassLoader().getResourceAsStream(IMAGE_PATH);
            var image = ImageIO.read(Objects.requireNonNull(resource));
            var resizedImage = resizeImage(image);
            var width = resizedImage.getWidth();
            var height = resizedImage.getHeight();

            var pixelRGBs = getRGBs(resizedImage);
            var brightnessNumbers = getBrightnessNumber(pixelRGBs, BrightnessType.LUMINOSITY);
            var result = convertToAsciiMatrix(height, width, brightnessNumbers);

            var printedResult = new StringBuilder();
            for (var i = 0; i < height; i++) {
                for (var j = 0; j < width; j++) {
                    printedResult.repeat(result[i][j], REPEAT_CHAR);
                }
                printedResult.append('\n');
            }
            try (var stream = new FileOutputStream("output.txt")) {
                stream.write(printedResult.toString().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private char[][] convertToAsciiMatrix(int height, int width, double[][] brightnessNumbers) {
        var result = new char[height][width];
        var maxBrightness = Double.MIN_VALUE;
        var minBrightness = Double.MAX_VALUE;
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                maxBrightness = Math.max(maxBrightness, brightnessNumbers[y][x]);
                minBrightness = Math.min(minBrightness, brightnessNumbers[y][x]);
            }
        }
        var brightnessRange = maxBrightness - minBrightness;
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                var ratio = (brightnessNumbers[y][x] - minBrightness) / brightnessRange;
                var index = (int) Math.floor(ratio * (ASCII_MATRIX.length() - 1));
                result[y][x] = ASCII_MATRIX.charAt(index);
            }
        }
        return result;
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

    private int[][][] getRGBs(BufferedImage image) {
        var height = image.getHeight();
        var width = image.getWidth();
        var pixelRGBs = new int[height][width][3];
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                var color = new Color(image.getRGB(x, y));
                pixelRGBs[y][x][0] = color.getRed();
                pixelRGBs[y][x][1] = color.getGreen();
                pixelRGBs[y][x][2] = color.getBlue();
            }
        }
        return pixelRGBs;
    }

    private double[][] getBrightnessNumber(int[][][] pixelRGBs, BrightnessType strategy) {
        var height = pixelRGBs.length;
        var width = pixelRGBs[0].length;
        var brightnessNumbers = new double[height][width];
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                if (strategy == BrightnessType.AVERAGE) {
                    brightnessNumbers[y][x] = (pixelRGBs[y][x][0] + pixelRGBs[y][x][1] + pixelRGBs[y][x][2]) / 3.0;
                } else if (strategy == BrightnessType.LIGHTNESS) {
                    brightnessNumbers[y][x] = (Math.max(pixelRGBs[y][x][0], Math.max(pixelRGBs[y][x][1], pixelRGBs[y][x][2] * 0.07))
                            + Math.min(pixelRGBs[y][x][0], Math.min(pixelRGBs[y][x][1], pixelRGBs[y][x][2] * 0.07))
                            / 2);
                } else {
                    brightnessNumbers[y][x] = (pixelRGBs[y][x][0] * 0.21 + pixelRGBs[y][x][1] * 0.72 + pixelRGBs[y][x][2] * 0.07);
                }
            }
        }
        return brightnessNumbers;
    }
}
