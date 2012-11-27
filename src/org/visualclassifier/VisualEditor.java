package org.visualclassifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class VisualEditor extends JFrame {
	
	private static final long serialVersionUID = -8723579947137926832L;
	private DataHandler dh;
	private int minH = 500;
	
	private JPanel right;
	private JPanel center;
	private JPanel bottom;
	private ImagePanel img1;
	private ImagePanel img2;
	private JTextArea log;
	
	public VisualEditor(DataHandler dh){
		this.dh = dh;
		init();
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
		right = new JPanel();
		right.setPreferredSize(new Dimension(110,minH));
		populateRight();
		
		//Center
		center = new JPanel();
		center.setPreferredSize(new Dimension(500,minH));
		center.setLayout(new GridLayout(1, 2));
		img1 = new ImagePanel(dh.getImg_frame());
		img2 = new ImagePanel(dh.getImg_cluster());
		center.add(img1);
		center.add(img2);
				
		//Bottom
		bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.setPreferredSize(new Dimension(500,120));
		log = new JTextArea("Initialized...");
		bottom.add(log,BorderLayout.CENTER);
		
		
		//Add all
		add(right,BorderLayout.EAST);
		add(center,BorderLayout.CENTER);
		add(bottom,BorderLayout.SOUTH);
		
	}

	private void populateRight() {
		
	}

}
