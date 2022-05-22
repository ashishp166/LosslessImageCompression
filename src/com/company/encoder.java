package com.company;

import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class encoder {
    private static byte QOI_OP_RGB   = (byte)0b11111110;
    private static byte QOI_OP_RGBA  = (byte)0b11111111;
    private static byte QOI_OP_INDEX = (byte)0b00000000;
    private static byte QOI_OP_DIFF  = (byte)0b01000000;
    private static byte QOI_OP_LUMA  = (byte)0b10000000;
    private static byte QOI_OP_RUN   = (byte)0b11000000;
    public static void main(String[] args) {
        if(args.length != 2) {
            //exit
            System.exit(0);
        } else {
            try {
                FileWriter output = new FileWriter(args[1]);
                //parse png
                File f = new File(args[0]);
                if(!f.exists()) {
                    System.out.print("not there");
                    System.exit(0);
                }
                FileInputStream fis = new FileInputStream(f);
                BufferedImage image = null;
                image = new BufferedImage(32, 32,  BufferedImage.TYPE_INT_ARGB);
                image = ImageIO.read(fis);
                int width = image.getWidth();
                int height = image.getHeight();
                header(output, width, height);


                // arguments for data compression encoding method
                Pixel[] prevSeenPixel = new Pixel[64];
                for(int i = 0; i < 63; i++) {
                    prevSeenPixel[i] = new Pixel();
                }
                Pixel prev = new Pixel();
                int runlen = 0;

                encoding(image, prevSeenPixel, prev, runlen, width, height, output);

                output.write("Files in Java might be tricky, but it is fun enough!");
                output.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    public static void header(FileWriter output, int width, int height) throws IOException {
        //big endian
        byte[] s = "qoif".getBytes();
        for(int i = 0; i < 3; i++) {
            output.write(s[i]);
        }
        //this implementation probably got to be debugged
        output.write(String.valueOf(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(width).array()));
        output.write(String.valueOf(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(height).array()));
        output.write((byte)3);// since we are doing RGB instead of RGBA
        output.write((byte)1);// since linear channel = 1
    }

    public static void encoding(BufferedImage image, Pixel[] prevSeenPixel, Pixel prev, int runlen, int width, int height, FileWriter output) throws IOException {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                Color c = new Color(image.getRGB(i, j));
                Pixel curr = new Pixel(c.getRed(), c.getGreen(), c.getBlue());
                if(curr.equals(prev)) {
                    runlen++;
                    //qoi specifies that only show duplicates for max 62
                    if(runlen == 62) {
                        output.write((byte) QOI_OP_RUN | (runlen - 1));
                        runlen = 0;
                    }
                } else {
                    //new pixel or we maxed out on prev ones
                    if(runlen > 0) {
                        output.write((byte) QOI_OP_RUN | (runlen - 1));
                        runlen = 0;
                    }

                    // the index was in prevSeenPixel
                    int key = curr.key();
                    if(curr.equals(prevSeenPixel[key])) {
                        output.write((byte) QOI_OP_INDEX | key);
                    } else {
                        // not in prevSeenPixel or part of run
                        prevSeenPixel[key] = curr;
                        Pixel diff = new Pixel(curr.getR() - prev.getR(), curr.getG() - prev.getG(), curr.getB() - prev.getG());
                        int diffRG = diff.getR() - diff.getG();
                        int diffBG = diff.getB() - diff.getG();

                        // small difference between the 2 pixels
                        if(-2 <= diff.getR()  && diff.getR() <= 1 && -2 <= diff.getG() && diff.getG() <= 1 && -2 <= diff.getB() && diff.getB() <= 1) {
                            output.write((byte) QOI_OP_DIFF | diff.getR() + 2 << 4 | diff.getG() + 2 << 2 | diff.getB() + 2);
                        } else if (-32 <= diff.getG() && diff.getG() <= 31 && -8 <= diffRG && diffRG <= 7 && -8 <= diffBG && diffBG <= 7) {
                            // difference to previous pixel is large
                            output.write((byte) QOI_OP_LUMA | diff.getG() + 32);
                            output.write((byte) diffRG + 8 << 4 | diffBG + 8);
                        } else {
                            // write full rgb val
                            output.write((byte) QOI_OP_RGB);
                            output.write((byte) curr.getR());
                            output.write((byte) curr.getG());
                            output.write((byte) curr.getB());
                        }
                    }
                }
                prev = curr;
            }
        }
    }
}
