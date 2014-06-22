package io.magnum.jetty.server.data;

public class CostData {

    public double getOptimal() {
        return optimal;
    }
    public void setOptimal(double optimal) {
        this.optimal = optimal;
    }
    public double getWorst() {
        return worst;
    }
    public void setWorst(double worst) {
        this.worst = worst;
    }
    public double getPractical() {
        return practical;
    }
    public void setPractical(double practical) {
        this.practical = practical;
    }
    public double getS1() {
        return s1;
    }
    public void setS1(double s1) {
        this.s1 = s1;
    }
    public double getS2() {
        return s2;
    }
    public void setS2(double s2) {
        this.s2 = s2;
    }
    public double getS3() {
        return s3;
    }
    public void setS3(double s3) {
        this.s3 = s3;
    }
    
    public double getS1Rate() {
        return (s1 - optimal) / optimal;
    }
    
    public double getS2Rate() {
        return (s2 - optimal) / optimal;
    }
    
    public double getS3Rate() {
        return (s3 - optimal) / optimal;
    }
    
    public double getPracticalRate() {
        return (practical - optimal) / optimal;
    }
    
    private double optimal;
    private double worst;
    private double practical;
    private double s1;
    private double s2;
    private double s3;
    
    private double s4;
    
    public CostData(double optimal, double worst, double practical, double s1,
            double s2, double s3, double s4) {
        this.optimal = optimal;
        this.worst = worst;
        this.practical = practical;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
    }
    
    public double getS4() {
        return s4;
    }
    public void setS4(double s4) {
        this.s4 = s4;
    }

}
