package io.magnum.jetty.server.analyzer;

import io.magnum.jetty.server.data.ColorFeatureResult;
import io.magnum.jetty.server.data.QuadtreeFeatureResult;
import io.magnum.jetty.server.data.XYFeatureResult;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vizweb.ColorFeatureComputer;
import org.vizweb.QuadtreeFeatureComputer;
import org.vizweb.XYFeatureComputer;
import org.vizweb.quadtree.Quadtree;
import org.vizweb.structure.Block;

import com.hd4ar.awscommon.retry.AbortException;

public class VizwebAnalyzer extends BBDSAnalyzer {
    
    private static Logger logger = LoggerFactory.getLogger(VizwebAnalyzer.class);   
    
    public ColorFeatureResult computeColorFeature(String id, File inputFile) {
        BufferedImage input = null;
        try {
            File f = inputFile == null ? syncInputFile(id) : inputFile;
            input = ImageIO.read(f);
        } catch (AbortException e) {
            logger.error("Failed to get the input file.", e);
        } catch (IOException e) {
            logger.error("Failed to get the input file.", e);
        }
        
        ColorFeatureResult result = new ColorFeatureResult();        
        try {
            double col = ColorFeatureComputer.computeColorfulness(input);
            result.setColorfulness1(col);
        } catch (Exception e) {
            logger.error("Failed to get the colorfullness", e);
        }
        
        // the following feature calculate cause exception every time
//        try {
//            double col = ColorFeatureComputer.computeColorfulness2(input);
//            result.setColorfulness2(col);
//        } catch (Exception e) {
//            logger.error("Failed to get the colorfullness2", e);
//        }        
        return result;
    }
    
    public XYFeatureResult computerXYFeature(String id, File inputFile) {
        BufferedImage input = null;
        try {
            File f = inputFile == null ? syncInputFile(id) : inputFile;
            input = ImageIO.read(f);
        } catch (AbortException e) {
            logger.error("Failed to get the input file.", e);
        } catch (IOException e) {
            logger.error("Failed to get the input file.", e);
        }
        
        XYFeatureResult result = new XYFeatureResult();
        
        try {
            Block root = XYFeatureComputer.getXYBlockStructure(input);
            int numOfLeaves = XYFeatureComputer.countNumberOfLeaves(root);
            int numOfTextGroup = XYFeatureComputer.countNumberOfTextGroup(root);
            int numOfImageArea = XYFeatureComputer.countNumberOfImageArea(root);
            int maxDecompositionLevel = XYFeatureComputer.computeMaximumDecompositionLevel(root);
            
            result.setNumOfLeaves(numOfLeaves);
            result.setNumOfTextGroup(numOfTextGroup);
            result.setNumOfImageArea(numOfImageArea);
            result.setMaxDecompositionLevel(maxDecompositionLevel);
        } catch (Exception e) {
            logger.error("Failed to computer XY feature.", e);
        }
        
        return result;
    }
    
    public QuadtreeFeatureResult computerQuadtreeFeature(String id, File inputFile) {
        BufferedImage input = null;
        try {
            File f = inputFile == null ? syncInputFile(id) : inputFile;
            input = ImageIO.read(f);
        } catch (AbortException e) {
            logger.error("Failed to get the input file.", e);
        } catch (IOException e) {
            logger.error("Failed to get the input file.", e);
        }
        
        QuadtreeFeatureResult result = new QuadtreeFeatureResult();
        
        try {
            Quadtree qtColor = QuadtreeFeatureComputer.getQuadtreeColorEntropy(input);
            double horizontalBalance = QuadtreeFeatureComputer.computeHorizontalBalance(qtColor);
            double horizontalSymmetry = QuadtreeFeatureComputer.computeHorizontalSymmetry(qtColor);
            double verticleBalance = QuadtreeFeatureComputer.computeVerticalBalance(qtColor);
            double verticleSymmetry = QuadtreeFeatureComputer.computeVerticalSymmetry(qtColor);
            result.setHorizontalBalance(horizontalBalance);
            result.setHorizontalSymmetry(horizontalSymmetry);
            result.setVerticleBalance(verticleBalance);
            result.setVerticleSymmetry(verticleSymmetry);
        } catch (Exception e) {
            logger.error("Failed to computer quadtree feature.", e);
        }
        
        return result;
    }
}
