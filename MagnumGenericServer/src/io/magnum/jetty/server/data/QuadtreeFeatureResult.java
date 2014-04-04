package io.magnum.jetty.server.data;


public class QuadtreeFeatureResult {
    
    private Double horizontalBalance;
    private Double horizontalSymmetry;
    private Double verticleBalance;
    private Double verticleSymmetry;
    
    public Double getHorizontalBalance() {
        return horizontalBalance;
    }
    
    public void setHorizontalBalance(Double horizontalBalance) {
        this.horizontalBalance = horizontalBalance;
    }
    
    public Double getHorizontalSymmetry() {
        return horizontalSymmetry;
    }
    
    public void setHorizontalSymmetry(Double horizontalSymmetry) {
        this.horizontalSymmetry = horizontalSymmetry;
    }
    
    public Double getVerticleBalance() {
        return verticleBalance;
    }
    
    public void setVerticleBalance(Double verticleBalance) {
        this.verticleBalance = verticleBalance;
    }
    
    public Double getVerticleSymmetry() {
        return verticleSymmetry;
    }
    
    public void setVerticleSymmetry(Double verticleSymmetry) {
        this.verticleSymmetry = verticleSymmetry;
    }
}
