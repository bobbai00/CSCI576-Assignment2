import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

public class ImageManipulator {

    public static int WIDTH = 352;

    public static int HEIGHT = 288;
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

        frame.pack();
        frame.setVisible(true);
    }
    public static BufferedImage extractVectorFromFileOneChannel(String filename, int width, int height, boolean isThreeChannel) throws IOException {
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
        BufferedImage img = extractVectorFromFileOneChannel(fileName, WIDTH, HEIGHT, false);

        showImage(img, fileName);
    }

}
