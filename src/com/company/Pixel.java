package com.company;

public class Pixel {
    private int r;
    private int g;
    private int b;
    public Pixel() {
        this.r = 0;
        this.b = 0;
        this.g = 0;
    }
    public Pixel(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    //uses qoi formual to get the index val
    public int key() {
        return (this.r*3 + this.g * 5 + this.b * 7) % 64;
    }

    public Pixel sub(Pixel val) {
        return new Pixel(this.r - val.r, this.g - val.g, this.b - val.b);
    }

    public boolean equal(Pixel val) {
        return this.r == val.r && this.b == val.b && this.g == val.g;
    }

    public int getB() {
        return b;
    }

    public int getG() {
        return g;
    }

    public int getR() {
        return r;
    }

    public static void main(String[] args) {
        System.out.print("this is pixel class");
    }
}