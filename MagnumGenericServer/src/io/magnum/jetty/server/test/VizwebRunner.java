package io.magnum.jetty.server.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.vizweb.ColorFeatureComputer;
import org.vizweb.XYFeatureComputer;
import org.vizweb.QuadtreeFeatureComputer;
import org.vizweb.quadtree.Quadtree;
import org.vizweb.structure.Block;

public class VizwebRunner {

	/**
	 * This java class shows a couple of example using the vizweb
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		/*******************************
		 * Compute color features
		 *******************************/
		BufferedImage input = ImageIO.read(new File("/Users/yusun/Desktop/testweb2.png"));
		
		double col = ColorFeatureComputer.computeColorfulness(input);
		System.out.println("Colorfulness (method 1): " + col);
		
		//double col2 = ColorFeatureComputer.computeColorfulness2(input);
		//System.out.println("Colorfulness (method 2): " + col2);
		
		
		/*******************************
		 * Compute xy decomposition features (it may take a while)
		 *******************************/
		Block root = XYFeatureComputer.getXYBlockStructure(input);
		System.out.println("Number of leaves: " + XYFeatureComputer.countNumberOfLeaves(root));
		System.out.println("Number of group of text: " + XYFeatureComputer.countNumberOfTextGroup(root));		
		
		/*******************************
		 * Compute quadtree features
		 *******************************/
		Quadtree qtColor = QuadtreeFeatureComputer.getQuadtreeColorEntropy(input);
		System.out.println("Horizontal Balance: " + QuadtreeFeatureComputer.computeHorizontalBalance(qtColor));
		System.out.println("Horizontal Symmetry: " + QuadtreeFeatureComputer.computeHorizontalSymmetry(qtColor));
		System.out.println("Verticle Balance: " + QuadtreeFeatureComputer.computeVerticalBalance(qtColor));
        System.out.println("Verticle Symmetry: " + QuadtreeFeatureComputer.computeVerticalSymmetry(qtColor));        
	}
}