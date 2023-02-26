import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexQuantization {

    public static double getEuclideanDistanceBetweenTwoVectors(int[] coor1, int[] coor2) {

        int x1 = coor1[0];
        int y1 = coor1[1];
        int x2 = coor2[0];
        int y2 = coor2[1];

        double ac = Math.abs(y2 - y1);
        double cb = Math.abs(x2 - x1);

        return Math.hypot(ac, cb);
    }
    private static class VectorClusterStatus {
        Map<String, Integer> mapFromVectorToCodeword;

        List<List<String>> vectorClusters;

        public VectorClusterStatus(Map<String, Integer> mapFromVectorToCodeword, List<List<String>> vectorClusters) {
            this.mapFromVectorToCodeword = mapFromVectorToCodeword;
            this.vectorClusters = vectorClusters;
        }

        public Map<String, Integer> getMapFromVectorToCodeword() {
            return mapFromVectorToCodeword;
        }

        public void setMapFromVectorToCodeword(Map<String, Integer> mapFromVectorToCodeword) {
            this.mapFromVectorToCodeword = mapFromVectorToCodeword;
        }

        public List<List<String>> getVectorClusters() {
            return vectorClusters;
        }

        public void setVectorClusters(List<List<String>> vectorClusters) {
            this.vectorClusters = vectorClusters;
        }
    }
    public static int M = 2;

    public static List<int[]> getInitialCodewords(int N) {
        int width = ImageManipulator.WIDTH;
        int height = ImageManipulator.HEIGHT;

        int pins = (int) (Math.log(N) / Math.log(2));

        double xlength = 1.0 * width / pins;
        double ylength = 1.0 * height / pins;
        int[] xopts = new int[pins];
        int[] yopts = new int[pins];

        for (int i = 0; i<pins; i++) {
            xopts[i] = (int) (xlength * i + xlength / 2);
            yopts[i] = (int) (ylength * i + ylength / 2);
        }

        List<int[]> res = new ArrayList<>();
        for (int y = 0; y < pins; y++) {
            for (int x = 0; x < pins; x++) {
                res.add(new int[]{xopts[x], yopts[y]});
            }
        }

        return res;
    }

    public static VectorClusterStatus clusterVectorsByCodewords(Map<String, int[]> vectors, List<int[]> codewords) {
        Map<String, Integer> mapFromVectorToCodeword = new HashMap<>();
        List<List<String>> vectorClusters = new ArrayList<>();

        for (int i = 0; i<codewords.size(); i++) {
            vectorClusters.add(new ArrayList<>());
        }

        for (Map.Entry<String, int[]> kv : vectors.entrySet()) {
            double minDist = Double.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i<codewords.size(); i++) {
                int[] cw = codewords.get(i);
                double dist = getEuclideanDistanceBetweenTwoVectors(kv.getValue(), cw);
                if (dist < minDist) {
                    minDist = dist;
                    minIndex = i;
                }
            }

            mapFromVectorToCodeword.put(kv.getKey(), minIndex);
            List<String> vl = vectorClusters.get(minIndex);
            vl.add(kv.getKey());
            vectorClusters.set(minIndex, vl);
        }

        return new VectorClusterStatus(mapFromVectorToCodeword, vectorClusters);
    }

    public static int[] calculateCenterOfCluster(List<String> cluster, Map<String, int[]> vectors) {
        int x = 0;
        int y = 0;

        for (String k : cluster) {
            int[] vec = vectors.get(k);
            x += vec[0];
            y += vec[1];
        }

        return new int[]{x / cluster.size(), y / cluster.size()};
    }

    public static List<int[]> calculateNewCodewords(Map<String, int[]> vectors, List<List<String>> vectorClusters, List<int[]> codewords) {
        List<int[]> newCodewords = new ArrayList<>(codewords);

        for (int i = 0; i<vectorClusters.size(); i++) {
            List<String> cluster = vectorClusters.get(i);
            if (!cluster.isEmpty()) {
                int[] newCw = calculateCenterOfCluster(cluster, vectors);
                newCodewords.set(i, newCw);
            }
        }

        return newCodewords;
    }

    public static boolean isCodewordsSame(List<int[]> prevCodewords, List<int[]> newCodewords) {
        for (int i = 0; i<prevCodewords.size(); i++) {
            int[] coor1 = prevCodewords.get(i);
            int[] coor2 = newCodewords.get(i);

            if (coor1[0] != coor2[0] || coor1[1] != coor2[1]) {
                return false;
            }
        }

        return true;
    }

    // N is assumed to be power of 2
    public static BufferedImage vertexQuantization(String originalImgPath, int N) {
        BufferedImage originImage = null;

        try {
            originImage = ImageManipulator.getImageFromFile(originalImgPath, ImageManipulator.WIDTH, ImageManipulator.HEIGHT, false);
        } catch (Exception e) {
            System.out.println("Read original image ERROR: " + e);
            return null;
        }

        int[][] channel = ImageManipulator.extractOneChannelFromImage(originImage);
        Map<String, int[]> vectors = ImageManipulator.extractVectorFromOneChannel(channel);

        // initialize codewards
        List<int[]> codewords = getInitialCodewords(N);
        Map<String, Integer> vectorMembership = null;

        while (true) {
            VectorClusterStatus status = clusterVectorsByCodewords(vectors, codewords);
            List<int[]> newCodewords = calculateNewCodewords(vectors, status.vectorClusters, codewords);
            vectorMembership = status.mapFromVectorToCodeword;
            if (isCodewordsSame(newCodewords, codewords)) {
                break;
            } else {
                codewords = new ArrayList<>(newCodewords);
            }
        }

        BufferedImage resultImage = new BufferedImage(ImageManipulator.WIDTH, ImageManipulator.HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        for (Map.Entry<String, Integer> kv : vectorMembership.entrySet()) {
            String key = kv.getKey();
            int[] originImageCoordinate = ImageManipulator.coordinateFromKey(key);

            int[] newVectorValue = codewords.get(kv.getValue());
            resultImage.setRGB(originImageCoordinate[0], originImageCoordinate[1], new Color(newVectorValue[0], newVectorValue[0], newVectorValue[0]).getRGB());
        }

        return resultImage;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("ERROR: there should be exactly two arguments.");
            return;
        }

        String originalImgPath = args[0];
        int N = Integer.parseInt(args[1]);

        BufferedImage originImage = null;

        try {
            originImage = ImageManipulator.getImageFromFile(originalImgPath, ImageManipulator.WIDTH, ImageManipulator.HEIGHT, false);
        } catch (Exception e) {
            System.out.println("Read original image ERROR: " + e);
            return;
        }

        BufferedImage compressedImage = vertexQuantization(originalImgPath, N);

        ImageManipulator.showImage(originImage, "Original Image");
        ImageManipulator.showImage(compressedImage, "Compressed Image");

    }

}
