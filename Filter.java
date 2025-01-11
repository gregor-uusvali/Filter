import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Filter {
    public static void main(String[] args) {
        for(String s : args) {
            System.out.println(s);
        }

        if (args.length == 0 || args[0].equals("help")) {
            System.out.println("Enter the filter type (\"grayscale\", \"edges\", \"ascii\") and file name.");
            return;
        } 

        if (!(args[0].equals("grayscale") || args[0].equals("edges") || args[0].equals("help") || args[0].equals("ascii"))) {
            System.out.println("Wrong arguments! Enter \"help\" for instructions");
            return;
        } 

        if (args.length == 1) {
            System.out.println("Please enter file name.");
            return;
        }
        BufferedImage img = null;
        try {

            String filter = args[0];
            String file = args[1];

            int lastDotIndex = file.lastIndexOf('.');

            String fileName = file.substring(0, lastDotIndex);

            String extension = file.substring(lastDotIndex + 1);

            if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
                img = ImageIO.read(new File(file));
                int[][] matrix = getImgMatrix(img);
                // int[][] grayScaleMatrix = converToGrayScale(matrix);
                int[][] filterMatrix = {};
                switch (filter) {
                    case "grayscale":
                        filterMatrix = converToGrayScale(matrix);
                        break;
                
                    case "edges":
                        filterMatrix = convertToEdges(matrix);
                        break;
                    case "ascii":
                        String asciiArt = convertToAscii(img);
                        System.out.println(asciiArt);
                        BufferedImage asciiImage = asciiToImage(asciiArt, 12); // 12 is the font size
                        ImageIO.write(asciiImage, "png", new File("ascii_art.png"));
                        return;
                }
                BufferedImage image = createImageFromMatrix(filterMatrix);
                BufferedImage rgbImage = convertToRGB(image);
                File outputfile = new File(filter +  "_" + fileName + "." + extension);

                if (saveImage(extension.equals("jpg") || extension.equals("jpeg") ? rgbImage : image, extension, outputfile)) {
                    System.out.println("Image saved successfully: " + outputfile.getAbsolutePath());
                } else {
                    System.out.println("Image saving failed.");
                }
            } else {
                System.out.println("Unsupported file type!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[][] getImgMatrix(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] matrix = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                matrix[x][y] = img.getRGB(x, y);
            }
        }
        return matrix;
    }

    public static int[][] converToGrayScale(int[][] matrix) {
        int width = matrix.length;
        int height = matrix[0].length;
        int[][] return_matrix = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = matrix[x][y];

                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                int average = (r + g + b) / 3;

                int gray = (a << 24) | (average << 16) | (average << 8) | average;

                return_matrix[x][y] = gray;
            }
        }
        return return_matrix;
    }

    public static int[][] convertToEdges(int[][] matrix) {
        int width = matrix.length;
        int height = matrix[0].length;
        int[] gx_array = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        int[] gy_array = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
        int[][] return_matrix = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int gx_red = 0, gx_green = 0, gx_blue = 0, gy_red = 0, gy_green = 0, gy_blue = 0; 
                int counter = 0;
                for(int updated_x = x - 1; updated_x < x+2; updated_x++) {
                    for(int updated_y = y - 1; updated_y < y+2; updated_y++) {
                        if (!(updated_x < 0 || updated_y < 0 || updated_x >= width || updated_y >= height)) {
                            gx_red += gx_array[counter] * ((matrix[updated_x][updated_y] >> 16) & 0xff);
                            gx_green += gx_array[counter] * ((matrix[updated_x][updated_y] >> 8) & 0xff);
                            gx_blue += gx_array[counter] * (matrix[updated_x][updated_y] & 0xff);
                            gy_red += gy_array[counter] * ((matrix[updated_x][updated_y] >> 16) & 0xff);
                            gy_green += gy_array[counter] * ((matrix[updated_x][updated_y] >> 8) & 0xff);
                            gy_blue += gy_array[counter] * (matrix[updated_x][updated_y] & 0xff);
                        }
                        counter++;
                    }
                }
                int final_alpha = (matrix[x][y] >> 24) & 0xff;
                int final_red = (int) Math.min(Math.round(Math.sqrt((Math.pow(gx_red, 2) + Math.pow(gy_red, 2)))), 255);
                int final_green = (int) Math.min(Math.round(Math.sqrt((Math.pow(gx_green, 2) + Math.pow(gy_green, 2)))), 255);
                int final_blue = (int) Math.min(Math.round(Math.sqrt((Math.pow(gx_blue, 2) + Math.pow(gy_blue, 2)))), 255);

                // int average = (r + g + b) / 3;

                int val = (final_alpha << 24) | (final_red << 16) | (final_green << 8) | final_blue;

                return_matrix[x][y] = val;
            }
        }

        return return_matrix;
    }

    public static String convertToAscii(BufferedImage img) {
        int targetWidth = 40; // Adjust this value to change the width of the ASCII art
        int width = img.getWidth();
        int height = img.getHeight();
        float aspectRatio = (float) width / (float) height;
        int targetHeight = Math.round(targetWidth / aspectRatio);

        BufferedImage resizedImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImg.createGraphics();
        g2d.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        String asciiBrightnessStr = " .:-=+*#%@";
        int brightnessLevels = asciiBrightnessStr.length();

        StringBuilder asciiArt = new StringBuilder();

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int rgb = resizedImg.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int average = (r + g + b) / 3;
                int brightnessIndex = Math.round((float) average / 255 * (brightnessLevels - 1));
                char asciiChar = asciiBrightnessStr.charAt(brightnessIndex);

                asciiArt.append(asciiChar);
                asciiArt.append(asciiChar);
            }
            asciiArt.append("\n");
        }

        return asciiArt.toString();
    }

    public static BufferedImage asciiToImage(String asciiArt, int fontSize) {
        String[] lines = asciiArt.split("\n");
        int width = (lines[0].length() * (int) Math.round((float) fontSize / 1.8));
        int height = lines.length * fontSize;
    
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
    
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
    
        for (int i = 0; i < lines.length; i++) {
            g2d.drawString(lines[i], 0, fontSize * (i + 1));
        }
    
        g2d.dispose();
        return image;
    }

    public static BufferedImage createImageFromMatrix(int[][] matrix) {
        int width = matrix.length;
        int height = matrix[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, matrix[x][y]);
            }
        }
        return img;
    }

    public static BufferedImage convertToRGB(BufferedImage img) {
        BufferedImage rgbImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int argb = img.getRGB(x, y);
                int rgb = argb & 0x00FFFFFF;  // Strip out the alpha channel
                rgbImage.setRGB(x, y, rgb);
            }
        }
        return rgbImage;
    }

    public static boolean saveImage(BufferedImage img, String format, File file) {
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(file);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                System.out.println("No writers found for format: " + format);
                return false;
            }
            ImageWriter writer = writers.next();
            writer.setOutput(ios);
            writer.write(img);
            ios.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}