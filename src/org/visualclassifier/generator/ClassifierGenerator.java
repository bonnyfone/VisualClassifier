package org.visualclassifier.generator;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.visualclassifier.DataHandler;


public class ClassifierGenerator extends JFrame {

	private static final long serialVersionUID = -4087546224477720561L;



	private JButton btnMerge;
	private JButton btnGenerate;
	private JButton btnSfoglia;
	private JTextArea txtLog;
	private JTextArea txtGenerateFile;
	private String fileName;

	private static final String TAG_ATTR_INIT = "Attributes";
	private static final String TAG_ATTR_END = "Test mode";
	public static final String TAG_TREE_INIT = "------------------";
	public static final String TAG_TREE_END  = "Number of Leaves";


	public ClassifierGenerator(){
		init();
		bindListeners();
	}


	private void bindListeners() {
		btnMerge.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					chooseDir();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});


		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooseFileAndGenerate();
			}
		});

	}

	protected void chooseFileAndGenerate() {

		final JFileChooser fs = new JFileChooser();
		fs.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
		fs.setSelectedFile(new File("/media/Mistero/C++/Tesi/datasets/db.arff"));
		fs.setDialogTitle("Select dataset file to elaborate");
		int returnVal = fs.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			btnGenerate.setEnabled(false);
			addLog("Elaborating "+fs.getSelectedFile().getAbsolutePath());
			addLog("Weka is Working...");

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
	private void elaborate(File selectedFile) throws IOException, InterruptedException {
		String cmd = "java -cp /home/ziby/Desktop/weka-3-6-8/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -t "+selectedFile.getAbsolutePath() +"";
		Runtime run = Runtime.getRuntime();
		Process pr = run.exec(cmd);
		pr.waitFor();
		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		StringBuilder sb = new StringBuilder();
		boolean tree = false;
		while ((line=buf.readLine())!=null) {
			if(line.contains(TAG_TREE_END)) tree=false;
			if(tree){
				//addLog(line);
				sb.append(line+"\n");
			}
			
			if(line.startsWith(TAG_TREE_INIT))	tree=true;

		}
		Viewer v = new Viewer(sb.toString().trim(), Color.black);
		v.setTitle("J48 Tree (" + selectedFile.getAbsolutePath()+")");
		v.setVisible(true);

		addLog("Generating source code...");
		generateSourceCode(sb.toString().trim());
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
		txtLog.insert(s+"\n", 0);
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
	
	private void init(){
		setSize(600, 250);
		setTitle("Classifier Generator");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		btnMerge = new JButton("Merge datasets");
		btnGenerate = new JButton("Generate C++ Classifier");
		btnSfoglia = new JButton("File");
		btnSfoglia.setMinimumSize(new Dimension(300,40));

		txtLog = new JTextArea("Files to merge...");
		txtGenerateFile = new JTextArea("Source dataset ");
		setLayout(new GridLayout(2,1));

		JPanel a = new JPanel();
		a.setLayout(new GridLayout(2,1));
		a.add(btnMerge);
		a.add(btnGenerate);

		add(a);
		add(txtLog);

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


	private void chooseDir() throws IOException{
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

			//SAVE file
			if(datasets.size()>0){

				JFileChooser fs = new JFileChooser();
				fs.setCurrentDirectory(new File("/media/Mistero/C++/Tesi/datasets"));
				fs.setSelectedFile(new File("/media/Mistero/C++/Tesi/datasets/db.arff"));
				fs.setDialogTitle("Save marged datasets to file");
				returnVal = fs.showSaveDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					addLog("Saving merged file to "+fs.getSelectedFile());
					//MERGE FILES
					FileOutputStream fostream  = new FileOutputStream(fs.getSelectedFile());
					DataOutputStream  dout = new DataOutputStream(fostream);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dout));

					boolean first = true;
					for(String file : datasets){
						FileInputStream fstream = new FileInputStream(new File(file));
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String value;
						boolean tic = false;
						while ((value = br.readLine()) != null)   {
							if(first){
								bw.write(value+"\n");
								bw.flush();
							}
							else{
								if(tic){
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

	}








}
