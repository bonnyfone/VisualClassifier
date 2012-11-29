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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.visualclassifier.generator.ClassifierGenerator;



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
	
	private JLabel l1;
	private JLabel l2;
	private JLabel l3;
	private JCheckBox checkShowRoad;
	
	private String totCluster = "Total clusters: ";
	private String markedCluster = "Marked clusters: ";
	private String unmarkedCluster = "Unmarked clusters: ";
	//private JComboBox combo;
	//private JComboBox comboval;
	private JButton export;
	private JButton openGenerator;
	
	public VisualEditor(DataHandler dh){
		this.dh = dh;
		init();
		bindListeners();
	}
	
	private void bindListeners() {
		
		openGenerator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ClassifierGenerator cg = new ClassifierGenerator(false);
				cg.setVisible(true);
				setExtendedState(JFrame.ICONIFIED);
			}
		});
		
		export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addLog("Exporting updated dataset...");
				try {
					dh.exportData();
					addLog("Export complete.");
					JOptionPane.showMessageDialog(VisualEditor.this, "Export complete.");
				} catch (IOException e) {
					addLog("ERROR exporting dataset, see console");
					e.printStackTrace();
				}
			}
		});
		
		MouseListener ml = new MouseListener() {
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
					refreshStats();
				}else{ //Mark as road
					dh.removeRoadCluster(cluster);
					refreshStats();
				}
				img1.setRoadColor(dh.isRoadCluster(cluster));
				
				if(checkShowRoad.isSelected())img1.repaint();
			}
		};
		
		MouseMotionListener  mm = new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent arg0) {
				String cluster = calculateCluster(arg0.getX(), arg0.getY());
				ArrayList<String> points = dh.getClusters().get(cluster);
				img1.setSelectedClusterPoints(points);
				img1.setRoadColor(dh.isRoadCluster(cluster));
				if(checkShowRoad.isSelected())img1.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {}
		};
		
		img2.addMouseListener(ml);
		img1.addMouseListener(ml);
		img2.addMouseMotionListener(mm);
		img1.addMouseMotionListener(mm);
	}
	
	public void addLog(String s){
		log.insert(s+"\n", 0);
		log.setCaretPosition(0);
	}
	
	private void refreshStats(){
		l1.setText(totCluster + dh.getClusters().keySet().size());
		l2.setText(markedCluster + dh.getRoadCluster().size());
		l3.setText(markedCluster + (dh.getClusters().keySet().size()-dh.getRoadCluster().size()));
		
	}
	
	private String calculateCluster(int x, int y){
		int currentEntry = x + y*img2.getImgWidth();
		return dh.getPixel2cluster().get(""+currentEntry);
	}

	private void init(){
		//setMinimumSize(new Dimension(500, minH));
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
		setSize(1280,700);
		setLocation(0, 0);
		//setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("VisualClassifier: " +dh.getRelationName());
		
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
		JPanel pimg1 = new JPanel();
		pimg1.setLayout(new GridLayout(1,1));
		JPanel pimg2 = new JPanel();
		pimg2.setLayout(new GridLayout(1,1));
		pimg1.add(img1);
		pimg2.add(img2);
		pimg1.setBorder(BorderFactory.createTitledBorder(null, "Frame",TitledBorder.LEFT, TitledBorder.TOP, null, Color.darkGray));
		pimg2.setBorder(BorderFactory.createTitledBorder(null, "Clusters",TitledBorder.LEFT, TitledBorder.TOP, null, Color.darkGray));
		
		center.add(pimg1);
		center.add(pimg2);
				
		//Bottom
		bottom = new JPanel();
		bottom.setLayout(new GridLayout(1,2));
		bottom.setPreferredSize(new Dimension(400,120));
		log = new JTextArea("Initialized...");
		
		JScrollPane js = new JScrollPane(log);
		bottom.add(js);
		js.setBorder(BorderFactory.createTitledBorder(null, "Logs",TitledBorder.LEFT, TitledBorder.TOP, null, Color.darkGray));
		log.setBackground(getBackground());
		controls = new JPanel();
		controls.setBorder(BorderFactory.createTitledBorder(null, "Controls",TitledBorder.LEFT, TitledBorder.TOP, null, Color.darkGray));
		bottom.add(controls);
		controls.setLayout(new GridLayout(1,2));
		JPanel c1 = new JPanel();
		JPanel c2 = new JPanel();
		c1.setLayout(new GridLayout(4,1));
		l1 = new JLabel(totCluster);
		l2 = new JLabel(markedCluster);
		l3 = new JLabel(unmarkedCluster);
		checkShowRoad = new JCheckBox("Show marked clusters");
		checkShowRoad.setSelected(true);
		c1.add(l1);
		c1.add(l2);
		c1.add(l3);
		c1.add(checkShowRoad);
		/*
		Vector<String> v = new Vector<String>();
		for(int i=0;i<dh.getClusters().keySet().size();i++)
			v.add(i+"");
		
		combo = new JComboBox(v);
		
		Vector<String>vv = new Vector<String>();
		vv.addAll(dh.getClassValue());
		comboval = new JComboBox(vv);
		c1.add(combo);
		c1.add(comboval);
		*/

		export = new JButton("Export dataset");
		c2.add(export);
		
		openGenerator = new JButton("Merge dataset / Generate classifier");
		c2.add(openGenerator);
		controls.add(c1);
		controls.add(c2);
		
		//Add all
		//add(scroll,BorderLayout.EAST);
		add(center,BorderLayout.CENTER);
		add(bottom,BorderLayout.SOUTH);
		
		refreshStats();
	}
	
	private void askSave(){
		JFileChooser fs = new JFileChooser();
		fs.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
		fs.setSelectedFile(new File("/media/Mistero/C++/Tesi/datasets/db.arff"));
		fs.setDialogTitle("Save classified datasets to file");
		int returnVal = fs.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
		}
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
