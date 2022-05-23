package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class test {
    public static void main(String[] args) throws IOException {
        System.out.print("this is pixel class");
        String fileName = "wiggins.png";
        String outputnorm = "wigginsnorm.png";
        String outputqoi = "wiggins.qoi";
        time("encode", fileName, outputnorm, outputqoi, "png");
    }

    public static void time(String type, String input, String base, String qoi, String format) throws IOException {
        long start = System.nanoTime();
        if(type.equals("encode")) {
            File f = new File(String.valueOf(input));
            if(!f.exists()) {
                System.out.print("file not there");
                System.exit(0);
            }
            FileInputStream fis = new FileInputStream(f);
            BufferedImage image = ImageIO.read(fis);
            File output = new File(base);
            if(!ImageIO.write(image, format, output)) {
                throw new IllegalStateException("Failed to write " + format);
            }
            double baseTime = System.nanoTime() - start;
            long encodeStart = System.nanoTime();
            String[] args = new String[2];
            args[0] = input;
            args[1] = qoi;
            encoder.benchmark(args);
            double encodeTime = System.nanoTime() - encodeStart;
            System.out.println("baseTime: " + baseTime + " encodeTime: " + encodeTime);
            double percent = Math.abs((encodeTime - baseTime) / encodeTime)*100;
            System.out.println("speedup: " + String.format("%.2f", percent) + "%");
        }
    }


}
