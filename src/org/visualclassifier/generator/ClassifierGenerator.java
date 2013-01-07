package org.visualclassifier.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.visualclassifier.DataHandler;
import org.visualclassifier.utils.Utils;


public class ClassifierGenerator extends JFrame {

	private static final long serialVersionUID = -4087546224477720561L;



	private JButton btnMerge;
	private JButton btnGenerate;
	private JButton btnSfoglia;
	private JButton btnBalance;
	private JTextArea txtLog;
	private JTextArea txtGenerateFile;
	private String fileName;
	private JTextField fakeWidth;
	private JTextField fakeHeight;
	private JCheckBox fakeCamera;
	private JProgressBar progress;
	private JCheckBox checkDiscard;
	private JTextField txtDiscard;
	private JTextField txtNegative;
	private JTextField txtPositive;

	private int targetWidth;
	private int targetHeight;

	private static final String TAG_ATTR_INIT = "Attributes";
	private static final String TAG_ATTR_END = "Test mode";
	public static final String TAG_TREE_INIT = "------------------";
	public static final String TAG_TREE_END  = "Number of Leaves";

	private boolean closeOnExit=true;

	public ClassifierGenerator(boolean closeOnExit){
		this.closeOnExit = closeOnExit;
		init();
		bindListeners();
	}


	private void bindListeners() {

		btnBalance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				Thread t = new Thread(){
					public void run(){
						doBalance();
					}
				};
				t.start();
			}
		});

		txtNegative.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					double negative = Double.parseDouble(txtNegative.getText());
					double pos = 1.0-negative;
					txtPositive.setText(pos+"");
				}
				catch(Exception ex){ex.printStackTrace();}
			}
		});

		txtPositive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					double positive = Double.parseDouble(txtPositive.getText());
					double neg = 1.0-positive;
					txtNegative.setText(neg+"");
				}
				catch(Exception ex){ex.printStackTrace();}
			}
		});

		checkDiscard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				txtDiscard.setEnabled(checkDiscard.isSelected());
			}
		});

		fakeCamera.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				fakeHeight.setEnabled(fakeCamera.isSelected());
				fakeWidth.setEnabled(fakeCamera.isSelected());

			}
		});

		btnMerge.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				Thread t = new Thread(){
					public void run(){
						try {
							doMerge();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						hideProgress();

					}
				};
				t.start();
			}
		});


		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooseFileAndGenerate();
			}
		});

	}

	private void doBalance() {
		final JFileChooser fs = new JFileChooser();
		fs.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
		fs.setSelectedFile(new File("/media/Mistero/C++/Tesi/datasets/db.arff"));
		fs.setDialogTitle("Select dataset file to balance");
		int returnVal = fs.showOpenDialog(this);


		File openFile=null;
		File saveFile=null;

		//Select file to read
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			btnGenerate.setEnabled(false);
			addLog("Elaborating "+fs.getSelectedFile().getAbsolutePath());
			fileName = fs.getSelectedFile().getName();
			openFile = fs.getSelectedFile();
			
			//Select dest file
			fs.setSelectedFile(new File(fs.getSelectedFile().getParent()+"/"+calculateBalancedFileName(fileName)));
			fs.setDialogTitle("Select destination file name for balanced dataset");
	        returnVal = fs.showSaveDialog(this);
	        
	        if(returnVal == JFileChooser.APPROVE_OPTION){
	        	saveFile = fs.getSelectedFile();
	        	//do stuff
	        	showProgress();
	        	final File open =openFile;
	        	final File save =saveFile;
	        	Thread t = new Thread(){
	        		public void run(){
	        			try {
							balanceDataset(open, save);
						} catch (IOException e) {
							addLog("Error: "+e.getMessage());
							e.printStackTrace();
						}
	        			hideProgress();
	        		}
	        	};
	        	t.start();
	        	
	        }	        
		}
	}

	protected void balanceDataset(File openFile, File saveFile) throws IOException {
		//Passo 1: count active cluste
		FileInputStream fstream = new FileInputStream(openFile);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String value;
		int count=0;
		int positive=0;
		int negative=0;
		boolean discardEnabled  = checkDiscard.isSelected();
		int discardCount=0;
		int discardPositive=0;
		int discardNegative=0;
		int areaLimit = Integer.parseInt(txtDiscard.getText());
		
		boolean discardValid = false;
		while ((value = br.readLine()) != null)   {
			if(value.startsWith("@") || value.startsWith("%") || value.startsWith("0"))continue;
			if(discardEnabled){
				discardValid = checkDiscardValidity(value,areaLimit);
			}
			
			//Cluster counts
			count++;
			if(discardEnabled && discardValid)discardCount++;
			
			if(value.contains("positive")){
				positive++;
				if(discardEnabled && discardValid)discardPositive++;
			}
			else{
				negative++;
				if(discardEnabled && discardValid)discardNegative++;
			}
		}
		in.close();
		int decimalDigits = 3;
		double percPos = Utils.roundDecimals((((double)positive/count)*100.0),decimalDigits);
		double percNeg = Utils.roundDecimals(100.0-percPos,decimalDigits);
		addLog("Active clusters: "+count + "  ->  pos="+positive+" ("+percPos+"%), neg="+negative+" ("+percNeg+"%)");
		if(discardEnabled){
			double discardPercPos = Utils.roundDecimals((((double)discardPositive/count)*100),decimalDigits);
			double discardPercNeg = Utils.roundDecimals(100.0-discardPercPos,decimalDigits);
			addLog("Filtered clusters (area="+areaLimit+"): "+discardCount + "  ->  pos="+discardPositive+" ("+discardPercPos+"%), neg="+discardNegative+" ("+discardPercNeg+"%)");	
		}
		
	}

	
	private boolean checkDiscardValidity(String value, int param) {
		int area = Integer.parseInt(value.substring(0,value.indexOf(",")));
		return param<=area;
	}


	private String calculateBalancedFileName(String rawName){
		String ris ="";
		ris = fileName.replace(".arff", "")+"_BALANCED_pos"+ (int)(Double.parseDouble(txtPositive.getText())*100);
		if(checkDiscard.isSelected()){
			ris += "_disc" + txtDiscard.getText();
		}
		ris+=".arff";
		System.out.println("Balanced fileName is: "+ris);
		return ris;
	}

	protected void chooseFileAndGenerate() {

		final JFileChooser fs = new JFileChooser();
		fs.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
		fs.setSelectedFile(new File("/media/Mistero/C++/Tesi/datasets/db.arff"));
		fs.setDialogTitle("Select dataset file to elaborate");
		int returnVal = fs.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			btnGenerate.setEnabled(false);
			addLog("Elaborating "+fs.getSelectedFile().getAbsolutePath());

			Thread t = new Thread(){
				public void run(){
					try {
						fileName = fs.getSelectedFile().getName();
						elaborate(fs.getSelectedFile());
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					btnGenerate.setEnabled(true);
					hideProgress();
				}
			};
			t.start();
		}
	}

	/**
	 * Elaborate using weka
	 * @param selectedFile
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void elaborate(final File selectedFile) throws IOException, InterruptedException {

		String s = (String)JOptionPane.showInputDialog( this, "Confidence", "Confidence factor", JOptionPane.PLAIN_MESSAGE,null, null, "0.1");
		addLog("Weka is Working...");
		//If a string was returned, say so.
		if (!( (s != null) && (s.length() > 0)))return;

		new Thread(){public void run(){showProgress();}}.start();

		String cmd = "java -Xmx2g -cp /home/ziby/Desktop/weka-3-6-8/weka.jar weka.classifiers.trees.J48 -C "+s+" -M 2 -no-cv -t "+selectedFile.getAbsolutePath() +"";
		Runtime run = Runtime.getRuntime();
		final Process pr = run.exec(cmd);


		Thread t=new Thread(){
			public void run(){

				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = "";
				StringBuilder sb = new StringBuilder();
				boolean tree = false;
				try {
					while ((line=buf.readLine())!=null) {
						if(line.contains(TAG_TREE_END)) tree=false;
						if(tree){
							//addLog(line);
							sb.append(line+"\n");
						}

						if(line.startsWith(TAG_TREE_INIT))	tree=true;

					}
					addLog("Generating source code...");
					Viewer v = new Viewer(sb.toString().trim(), Color.black);
					v.setTitle("J48 Tree (" + selectedFile.getAbsolutePath()+")");
					v.setVisible(true);

					generateSourceCode(sb.toString().trim());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();


		Thread e = new Thread(){
			public void run(){
				//Print error stream
				BufferedReader bufer = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
				String line = "";
				try {
					while ((line=bufer.readLine())!=null) {
						System.out.println(line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		e.start();


		pr.waitFor();
	}

	private String header(){

		return "inline bool classify_" + fileName.replace(".arff", "") +" (Cluster* cluster){\n" ;
	}

	private void generateSourceCode(String tree){
		ArrayList<String> vars = new ArrayList<String>();

		tree=tree.replace(":", " :");
		StringBuilder sb = new StringBuilder();
		sb.append(header());

		String object = "cluster->";
		Scanner scanner = new Scanner(tree);

		int level = 0;
		int prevLevel = 0;
		boolean previusReturned = false;
		boolean firstLine = true;
		while (scanner.hasNextLine()) {
			String rawLine = scanner.nextLine();
			String statement = "";
			level = countOccurrences(rawLine, '|');
			boolean returnLine = rawLine.contains(":");
			Scanner line = new Scanner(rawLine);

			if(returnLine || prevLevel<level || firstLine){

				if(( previusReturned && (prevLevel==level) ) ||  (returnLine && (prevLevel>level)) ){
					statement = "else return ";
					if(rawLine.contains("negative"))statement += "false;\n";
					else if(rawLine.contains("positive")){
						statement += "true;\n";
					}
					sb.append(tabbing(level)+statement);
					prevLevel = level;
					previusReturned = returnLine;
					continue;
				}

				statement = "if(";

				int counter = 0;
				while(line.hasNext()){
					String token = line.next();

					if(isVar(token) && !vars.contains(token))vars.add(token);

					if(counter < level+3){ 
						if(!token.equals("|")){
							statement += (" " +token);
							if(!line.hasNext()){
								statement +=") \n";	
							}
						}
					}else{
						if(returnLine){
							if(token.contains("negative"))
								statement += ") return false;\n";
							else if(token.contains("positive"))
								statement += ") return true;\n";
						}else{
							statement +=") {\n"; //??
						}
					}
					counter++;

				}
				sb.append(tabbing(level)+statement);
			}
			else{

				/*
				int diff = prevLevel - level;
				if(diff>0){
					int tmp = prevLevel;
					for(int i=0;i<diff;i++){
						//sb.append("\n"+tabbing(tmp-1)+"}\n");
						sb.append("\n");
						tmp--;
					}
				}
				 */
				//sb.append(tabbing(level)+"else{\n");
				sb.append(tabbing(level)+"else\n");
			}
			firstLine = false;
			prevLevel = level;
			previusReturned =returnLine;

		}
		/*
		int tmp = prevLevel;
		for(int i=0;i<prevLevel;i++){
			//sb.append("\n"+tabbing(tmp-1)+"}\n");
			sb.append("\n");
			tmp--;
		}
		 */

		sb.append("\n}");
		String out = sb.toString();
		for(String v : vars){
			out = out.replace(v, object+v);
		}

		Viewer v = new Viewer(out, Color.blue);
		v.setVisible(true);
		v.setTitle("Classifier C++ snippet");
		addLog("Generation complete :)");
	}


	private String tabbing(int howMany){
		String ris="";
		for(int i=0;i<howMany+1;i++)ris += "\t";
		return ris;
	}

	private void addLog(String s){
		txtLog.insert(Utils.getCurrentTimeStamp() +"  : "+ s+"\n", 0);
	}

	public static boolean isVar(String str){
		if(!isNumeric(str) && !str.contains("=") && !str.contains(">") && !str.contains("<"))return true;
		return false;
	}

	public static boolean isNumeric(String str)  
	{  
		try  
		{  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}

	private void showProgress(){
		progress.setEnabled(true);
		progress.setIndeterminate(true);
	}

	private void hideProgress(){
		progress.setEnabled(false);
		progress.setIndeterminate(false);		
	}

	private void init(){
		setSize(700, 500);
		setTitle("Classifier Generator");
		setLocationRelativeTo(null);
		if(closeOnExit)setDefaultCloseOperation(EXIT_ON_CLOSE);

		btnMerge = new JButton("Merge datasets");
		btnGenerate = new JButton("Generate C++ Classifier");
		btnSfoglia = new JButton("File");
		btnSfoglia.setMinimumSize(new Dimension(300,40));

		progress = new JProgressBar();
		progress.setEnabled(false);

		txtLog = new JTextArea("");
		addLog("ClassifierGenerator started.");
		txtGenerateFile = new JTextArea("Source dataset ");
		setLayout(new GridLayout(2,1));

		JPanel a = new JPanel();
		a.setLayout(new GridLayout(3,1));

		JPanel b = new JPanel();
		b.setLayout(new GridLayout(1, 2));
		b.add(btnMerge);

		JPanel c=new JPanel();
		c.setLayout(new GridLayout(3,1));

		fakeCamera = new JCheckBox("Scale to fake camera (ROAD CLUSTERS ONLY)");
		fakeWidth = new JTextField("width");
		fakeHeight = new JTextField("height");
		fakeWidth.setEnabled(false);
		fakeHeight.setEnabled(false);

		c.add(fakeCamera);
		c.add(fakeWidth);
		c.add(fakeHeight);

		JPanel panelBalance = new JPanel();
		panelBalance.setLayout(new GridLayout(1,2));
		btnBalance = new JButton("Balance dataset");
		JPanel balanceParams = new JPanel();		
		panelBalance.add(btnBalance);
		panelBalance.add(balanceParams);
		balanceParams.setLayout(new FlowLayout());

		JLabel label_negative = new JLabel("% negative");
		JLabel label_positive = new JLabel("% positive");
		checkDiscard = new JCheckBox("Discard cluster with area < of ");
		txtDiscard = new JTextField("10");
		txtDiscard.setPreferredSize(new Dimension(50, 20));
		txtDiscard.setHorizontalAlignment(JTextField.CENTER);
		txtDiscard.setEnabled(false);
		txtNegative = new JTextField("0.5");
		txtNegative.setPreferredSize(new Dimension(50, 20));
		txtNegative.setHorizontalAlignment(JTextField.CENTER);
		txtPositive = new JTextField("0.5");
		txtPositive.setPreferredSize(new Dimension(50, 20));
		txtPositive.setHorizontalAlignment(JTextField.CENTER);

		balanceParams.add(checkDiscard);
		balanceParams.add(txtDiscard);
		balanceParams.add(label_negative);
		balanceParams.add(txtNegative);
		balanceParams.add(label_positive);
		balanceParams.add(txtPositive);

		b.add(c);
		a.add(b);

		a.add(panelBalance);
		a.add(btnGenerate);

		add(a);
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.add(txtLog,BorderLayout.CENTER);
		bottom.add(progress,BorderLayout.SOUTH);
		add(bottom);

	}

	public static int countOccurrences(String haystack, char needle){
		int count = 0;
		for (int i=0; i < haystack.length(); i++)
		{
			if (haystack.charAt(i) == needle)
			{
				count++;
			}
		}
		return count;
	}


	private void doMerge() throws IOException{
		if(fakeCamera.isSelected()){
			try{
				targetWidth = Integer.parseInt(fakeWidth.getText());
				targetHeight = Integer.parseInt(fakeHeight.getText());
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, "Error, width or height not valid!","ERROR",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}




		//Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
		fc.setDialogTitle("Select datasets folders to merge");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File[] selectedFiles = fc.getSelectedFiles();
			ArrayList<String> datasets = new ArrayList<String>();
			for(File f:selectedFiles){
				//System.out.println(f.getAbsolutePath());

				String[] list = f.list();
				for(String l : list){
					if(l.endsWith(".arff") && l.contains("classified")){
						addLog(f.getAbsolutePath()+"/"+l);
						datasets.add(f.getAbsolutePath()+"/"+l);
					}
				}

			}
			addLog("File selezionati ("+datasets.size()+")");


			//Aggiornati da ogni dataset
			int currentWidth;
			int currentHeight;
			double currentArea;

			double scaleArea=1.0;
			double scaleWidth=1.0;
			double scaleHeight=1.0;

			//SAVE file
			if(datasets.size()>0){

				JFileChooser fs = new JFileChooser();
				fs.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
				fs.setSelectedFile(new File("/media/Mistero/C++/Tesi/datasets/db.arff"));
				fs.setDialogTitle("Save marged datasets to file");
				returnVal = fs.showSaveDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					Thread t =new Thread(){
						public void run(){
							showProgress();
						}
					};
					t.start();
					addLog("Saving merged file to "+fs.getSelectedFile());
					//MERGE FILES
					FileOutputStream fostream  = new FileOutputStream(fs.getSelectedFile());
					DataOutputStream  dout = new DataOutputStream(fostream);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dout));
					double targetArea = targetWidth*targetHeight;
					boolean first = true;
					for(String file : datasets){
						FileInputStream fstream = new FileInputStream(new File(file));
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String value;
						boolean tic = false;

						currentWidth = 720;//default val
						currentHeight = 480;//default val
						currentArea = currentHeight*currentWidth;

						while ((value = br.readLine()) != null)   {

							//Estrai current w ed h
							if(fakeCamera.isSelected()){
								if(value.startsWith(DataHandler.TAG_W)){
									currentWidth = Integer.parseInt(value.replace(DataHandler.TAG_W, ""));
								}
								else if(value.startsWith(DataHandler.TAG_H)){
									currentHeight = Integer.parseInt(value.replace(DataHandler.TAG_H, ""));

									//Calcola scale
									currentArea = currentHeight*currentWidth;
									scaleArea = targetArea/currentArea;
									scaleWidth = ((double)targetWidth)/(double)currentWidth;
									scaleHeight = ((double)targetHeight)/(double)currentHeight;

								}
							}

							if(first){
								//Scala per camera Fake
								if(tic && fakeCamera.isSelected())
									value = scaleToFake(value,scaleArea,scaleWidth,scaleHeight);

								//Update di w e h per il primo file da cui copiamo l'header
								if(fakeCamera.isSelected()){
									if(!tic && value.startsWith(DataHandler.TAG_W))
										value = "% w="+targetWidth;
									else if(!tic && value.startsWith(DataHandler.TAG_H))
										value = "% h="+targetHeight;
								}

								bw.write(value+"\n");
								bw.flush();
								if(value.startsWith(DataHandler.TAG_DATA))tic=true;
							}
							else{
								if(tic){
									if(fakeCamera.isSelected())
										value = scaleToFake(value,scaleArea,scaleWidth,scaleHeight);

									bw.write(value+"\n");
									bw.flush();
								}
								if(value.startsWith(DataHandler.TAG_DATA))tic=true;
							}
						}
						in.close();
						first=false;

					}
					dout.close();
					addLog("Complete.");

				}
			}

		} else {
			//Nothing
		}
		hideProgress();

	}


	private String scaleToFake(String raw, double areaFactor, double widthFactor, double heightFactor){
		/*
		@attribute 0'area' real
		@attribute 1'max_x' real
		@attribute 2'max_y' real
		@attribute 3'min_x' real
		@attribute 4'min_y' real
		@attribute 5'meanC1' real
		@attribute 6'meanC2' real
		@attribute 7'meanC3' real
		@attribute 8'stddevC1' real
		@attribute 9'stddevC2' real
		@attribute 10'stddevC3' real
		@attribute 11'cx' real
		@attribute 12'cy' real
		@attribute 'class' { road_negative, road_positive}
		 */
		String[] temp;
		String delimiter = ",";
		temp = raw.split(delimiter);
		String v;
		String ris = "";
		for(int i=0;i<temp.length;i++){
			v=temp[i];

			if(i==0){//AREA
				//TODO FARE valore*scala!!
				v = ""+(int)(Double.parseDouble(v)*areaFactor);
			}
			else if(i==1){//MAX_X
				v = ""+(int)(Double.parseDouble(v)*widthFactor);
			}
			else if(i==2){//MAX_Y
				v = ""+(int)(Double.parseDouble(v)*heightFactor);
			}
			else if(i==3){//MIN_X
				v = ""+(int)(Double.parseDouble(v)*widthFactor);
			}
			else if(i==4){//MIN_Y
				v = ""+(int)(Double.parseDouble(v)*heightFactor);
			}
			else if(i==11){//cx
				v = ""+(int)(Double.parseDouble(v)*widthFactor);
			}
			else if(i==12){//cy
				v = ""+(int)(Double.parseDouble(v)*heightFactor);
			}


			if(i< temp.length-1 )
				ris+= (v +","); 
			else
				ris+= v;

		}

		return ris;
	}







}
