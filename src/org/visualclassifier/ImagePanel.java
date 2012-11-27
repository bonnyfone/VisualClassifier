package org.visualclassifier;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{
	private static final long serialVersionUID = 4582135752753705514L;

	private BufferedImage image;
	private int imgWidth;
	private int imgHeight;
	private Color currentColor = Color.red;
	private Color roadColor = Color.cyan;

	private ArrayList<String> selectedClusterPoints;
	private DataHandler dh;
	private boolean showRoadPoints;

	public ImagePanel(String path, DataHandler dh) {
		try {                
			this.dh = dh;
			image = ImageIO.read(new File(path));
			imgWidth = image.getWidth();
			imgHeight = image.getHeight();
			setShowRoadPoints(false);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters

		int x,y,val;
		g.setColor(roadColor);
		if(showRoadPoints && dh.getRoadCluster()!=null){
			for(String c : dh.getRoadCluster()){
				for(String p : dh.getClusters().get(c)){
					val = Integer.parseInt(p);
					x = val % imgWidth;
					y = val / imgWidth;
					g.drawLine(x, y, x, y);
				}
			}
		}

		g.setColor(currentColor);
		if(selectedClusterPoints!=null){
			for(String s : selectedClusterPoints){
				val = Integer.parseInt(s);
				x = val % imgWidth;
				y = val / imgWidth;
				g.drawLine(x, y, x, y);
			}
		}
	}

	public void setRoadColor(boolean isRoad){
		if(isRoad){
			currentColor = Color.green;
		}
		else{
			currentColor = Color.red;
		}
	}

	public int getImgWidth() {
		return imgWidth;
	}

	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}

	public int getImgHeight() {
		return imgHeight;
	}

	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public ArrayList<String> getSelectedClusterPoints() {
		return selectedClusterPoints;
	}

	public void setSelectedClusterPoints(ArrayList<String> selectedClusterPoints) {
		this.selectedClusterPoints = selectedClusterPoints;
	}

	public boolean isShowRoadPoints() {
		return showRoadPoints;
	}

	public void setShowRoadPoints(boolean showRoadPoints) {
		this.showRoadPoints = showRoadPoints;
	}

}