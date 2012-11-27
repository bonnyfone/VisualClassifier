package org.visualclassifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



public class VisualEditor extends JFrame {
	
	private static final long serialVersionUID = -8723579947137926832L;
	private DataHandler dh;
	private int minH = 500;
	
	private JPanel right;
	private JPanel center;
	private JPanel bottom;
	private JPanel controls;
	private ImagePanel img1;
	private ImagePanel img2;
	private JTextArea log;
	private JScrollPane  scroll;
	private JComboBox combo;
	private JComboBox comboval;
	private JButton export;
	
	public VisualEditor(DataHandler dh){
		this.dh = dh;
		init();
		bindListeners();
	}
	
	private void bindListeners() {
		export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addLog("Exporting updated dataset...");
				try {
					dh.exportData();
					addLog("Export complete.");
				} catch (IOException e) {
					addLog("ERROR exporting dataset, see console");
					e.printStackTrace();
				}
			}
		});
		
		img2.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String cluster = calculateCluster(arg0.getX(), arg0.getY());
				addLog("("+arg0.getX() + ","+arg0.getY() + ") --> CLUSTER: "+cluster);
				
				ArrayList<String> points = dh.getClusters().get(cluster);
				img1.setSelectedClusterPoints(points);
				

				if(arg0.getButton() == MouseEvent.BUTTON1){ //LEFT CLICK->SET ROAD
					dh.addRoadCluster(cluster); //RIGTH CLICK->SET NOT ROAD
				}else{ //Mark as road
					dh.removeRoadCluster(cluster);
				}
				img1.setRoadColor(dh.isRoadCluster(cluster));
				img1.repaint();
			}
		});
		
		img2.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent arg0) {
				String cluster = calculateCluster(arg0.getX(), arg0.getY());
				ArrayList<String> points = dh.getClusters().get(cluster);
				img1.setSelectedClusterPoints(points);
				img1.setRoadColor(dh.isRoadCluster(cluster));
				img1.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {}
		});
	}
	
	public void addLog(String s){
		log.insert(s+"\n", 0);
	}
	
	private String calculateCluster(int x, int y){
		int currentEntry = x + y*img2.getImgWidth();
		return dh.getPixel2cluster().get(""+currentEntry);
	}

	private void init(){
		setBackground(Color.gray);
		setMinimumSize(new Dimension(500, minH));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(dh.getRelationName());
		
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		
		//Right
		/*
		right = new JPanel();
		scroll = new JScrollPane(right);
		right.setPreferredSize(new Dimension(110,minH));
		populateRight();
		*/
		
		//Center
		center = new JPanel();
		center.setPreferredSize(new Dimension(500,minH));
		center.setLayout(new GridLayout(1, 2));
		img1 = new ImagePanel(dh.getImg_frame(),dh);
		img2 = new ImagePanel(dh.getImg_cluster(),dh);
		img1.setShowRoadPoints(true);
		center.add(img1);
		center.add(img2);
				
		//Bottom
		bottom = new JPanel();
		bottom.setLayout(new GridLayout(1,2));
		bottom.setPreferredSize(new Dimension(400,120));
		log = new JTextArea("Initialized...");
		bottom.add(log);
		controls = new JPanel();
		controls.setBackground(Color.DARK_GRAY);
		bottom.add(controls);
		controls.setLayout(new GridLayout(1,2));
		JPanel c1 = new JPanel();
		JPanel c2 = new JPanel();
		c1.setLayout(new GridLayout(3,1));
		Vector<String> v = new Vector<String>();
		for(int i=0;i<dh.getClusters().keySet().size();i++)
			v.add(i+"");
		
		combo = new JComboBox(v);
		
		Vector<String>vv = new Vector<String>();
		vv.addAll(dh.getClassValue());
		comboval = new JComboBox(vv);
		c1.add(combo);
		c1.add(comboval);

		export = new JButton("Export dataset");
		c2.add(export);
		
		controls.add(c1);
		controls.add(c2);
		
		//Add all
		//add(scroll,BorderLayout.EAST);
		add(center,BorderLayout.CENTER);
		add(bottom,BorderLayout.SOUTH);
		
	}

	/*
	private void populateRight() {
		for(int i=0;i<dh.getClusters().keySet().size();i++){
			JPanel pan = new JPanel();
			pan.setLayout(new BorderLayout());
			pan.add(new JLabel(""),BorderLayout.WEST);
			Vector<String> v = new Vector<String>();
			v.addAll(dh.getClassValue());
			JComboBox jc = new JComboBox(v);
			classified.add(jc);
			pan.add(jc,BorderLayout.CENTER);
			
			right.add(pan);
		}
	}*/

}
