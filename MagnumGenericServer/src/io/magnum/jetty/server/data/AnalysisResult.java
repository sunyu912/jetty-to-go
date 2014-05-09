package io.magnum.jetty.server.data;

public class AnalysisResult {
    private ColorFeatureResult colorResult;
    private XYFeatureResult xyResult;
    private QuadtreeFeatureResult quadResult;
    
    public ColorFeatureResult getColorResult() {
        return colorResult;
    }
    
    public void setColorResult(ColorFeatureResult colorResult) {
        this.colorResult = colorResult;
    }
    
    public XYFeatureResult getXyResult() {
        return xyResult;
    }
    
    public void setXyResult(XYFeatureResult xyResult) {
        this.xyResult = xyResult;
    }
    
    public QuadtreeFeatureResult getQuadResult() {
        return quadResult;
    }
    
    public void setQuadResult(QuadtreeFeatureResult quadResult) {
        this.quadResult = quadResult;
    }    
}
