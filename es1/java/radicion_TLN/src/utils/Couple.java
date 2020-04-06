package utils;

public class Couple {


    private String s1;
    private String s2;
    private double targetSimilarity;


    public Couple(String x, String y, double target) {
        this.s1 = x;
        this.s2 = y;
        this.targetSimilarity = target;
    }
    public String getS2() {
        return s2;
    }

    public String getS1() {
        return s1;
    }


    @Override
    public String toString() {
        return "[S1: "+s1+", S2: "+s2+" sim: " + targetSimilarity+"]";
    }
}