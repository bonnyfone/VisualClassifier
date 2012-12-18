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
	private Color roadColor = Color.blue;

	private ArrayList<String> selectedClusterPoints;
	private ArrayList<String> areaClusterPoints;
	private DataHandler dh;
	private boolean showRoadPoints;

	private int startX;
	private int startY;
	private int endX;
	private int endY;
	
	private boolean shift;
	private boolean dragging;
	
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
	
	public void resetDragEvent(){
		areaClusterPoints = new ArrayList<String>();
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

		if(!dragging){
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
		else{ //DRAG MODE
			g.setColor(Color.red);
			int ox,oy,ex,ey;
			if(getStartX()<getEndX()){
				ox=getStartX();
				ex=getEndX();
			}else{
				ex=getStartX();
				ox=getEndX();
			}

			if(getStartY()<getEndY()){
				oy=getStartY();
				ey=getEndY();				
			}else{
				ey=getStartY();
				oy=getEndY();			
			}
			g.drawRect(ox,oy, ex-ox,ey-oy);
		}
		
	}
	
	public String calculateCluster(int x, int y){
		int currentEntry = x + y*this.getImgWidth();
		return dh.getPixel2cluster().get(""+currentEntry);
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

	public int getStartX() {
		return startX;
	}

	public void setStartX(int startX) {
		if(startX<0)startX=0;
		else if(startX>image.getWidth())startX=image.getWidth();
		this.startX = startX;
	}

	public int getStartY() {
		return startY;
	}

	public void setStartY(int startY) {
		if(startY<0)startY=0;
		else if(startY>image.getHeight())startY=image.getHeight();
		this.startY = startY;
	}

	public int getEndX() {
		return endX;
	}

	public void setEndX(int endX) {
		if(endX<0)endX=0;
		else if(endX>image.getWidth())endX=image.getWidth();
		this.endX = endX;
	}

	public int getEndY() {
		return endY;
	}

	public void setEndY(int endY) {
		if(endY<0)endY=0;
		else if(endY>image.getHeight())endY=image.getHeight();
		this.endY = endY;
	}

	public boolean isShift() {
		return shift;
	}

	public void setShift(boolean shift) {
		this.shift = shift;
	}

	public boolean isDragging() {
		return dragging;
	}

	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

}