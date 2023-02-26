
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.util.Map;

public class ImageManipulator {

    public static int WIDTH = 352;

    public static int HEIGHT = 288;

    public static int ImageOffsetX = 0;
    public static void showImage(BufferedImage img1, String filename) {
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        JLabel lbText1 = new JLabel(filename);
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbIm1 = new JLabel(new ImageIcon(img1));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(lbText1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);
        frame.setLocation(ImageOffsetX, 0);
        ImageOffsetX += img1.getWidth() + 100;
        frame.pack();
        frame.setVisible(true);
    }

    public static int[][] extractOneChannelFromImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[][] res = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(img.getRGB(x, y));

                if (c.getRed() != c.getBlue() || c.getRed() != c.getGreen() || c.getBlue() != c.getGreen()) {
                    System.out.println("ERROR: RGB should have equal value");
                }
                res[y][x] = c.getRed();
            }
        }

        return res;
    }

    public static String coordinateSerializeKey(int x, int y) {
        return String.valueOf(x) + "," + String.valueOf(y);
    }

    public static int[] coordinateFromKey(String key) {
        String[] coors = key.split(",");
        int[] res = new int[]{Integer.valueOf(coors[0]), Integer.valueOf(coors[1])};
        return res;
    }

    public static Map<String, int[]> extractVectorFromOneChannel(int[][] channel) {
        int row = channel.length;
        int col = channel[0].length;
        Map<String, int[]> res = new HashMap<>();
        for (int i = 0; i < row; i++) {

            for (int j = 0; j<col; j++) {
                if (j < col - 1) {
                    int[] ele = new int[]{channel[i][j], channel[i][j+1]};
                    res.put(coordinateSerializeKey(j, i), ele);
                }
//                if (i < row - 1) {
//                    int[] ele = new int[]{channel[i][j], channel[i+1][j]};
//                    res.add(ele);
//                }
            }
        }
        return res;
    }

    public static BufferedImage getImageFromFile(String filename, int width, int height, boolean isThreeChannel) throws IOException {
        File f = new File(filename);
        byte[] fileContent = Files.readAllBytes(f.toPath());
        int[] pixArray = new int[fileContent.length];
        for (int i = 0; i<fileContent.length; i++) {
            pixArray[i] = (int) fileContent[i];
            pixArray[i] = pixArray[i] & 0xff;
        }

        int imgType = BufferedImage.TYPE_INT_RGB;
        if (!isThreeChannel) {
            imgType = BufferedImage.TYPE_BYTE_GRAY;
        }
        BufferedImage img = new BufferedImage(width, height, imgType);

        int ind = 0;
        int totalSize = WIDTH * HEIGHT;
        for(int y = 0; y < height; y++){

            for(int x = 0; x < width; x++){
                int pix, r, g, b;

                if (isThreeChannel) {
//                    r = pixArray[ind++];
//                    g = pixArray[ind++];
//                    b = pixArray[ind++];
                    r = pixArray[ind];
                    g = pixArray[ind + totalSize];
                    b = pixArray[ind + totalSize * 2];
                    ind++;
                } else {
                    pix = pixArray[ind++];
                    r = pix;
                    g = pix;
                    b = pix;
                }
                pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

                img.setRGB(x,y,pix);
            }
        }

        return img;
    }

    public static void main(String[] args) throws IOException {
//
//        String fileName = "image1-onechannel.rgb";
//        extractVectorFromFileOneChannel(fileName);
        String fileName = "image2-onechannel.rgb";
        BufferedImage img = getImageFromFile(fileName, WIDTH, HEIGHT, false);

        showImage(img, fileName);
    }

}
