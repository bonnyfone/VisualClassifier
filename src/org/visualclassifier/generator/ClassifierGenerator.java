package org.visualclassifier.generator;

import java.awt.BorderLayout;
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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.View;

import org.visualclassifier.DataHandler;


public class ClassifierGenerator extends JFrame {

	private static final long serialVersionUID = -4087546224477720561L;

	private JButton btnMerge;
	private JButton btnGenerate;
	private JButton btnSfoglia;
	private JTextArea txtLog;
	private JTextArea txtGenerateFile;
	
	public static String TAG_TREE_INIT = "------------------";
	public static String TAG_TREE_END  = "Number of Leaves";
	

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
		Viewer v = new Viewer(sb.toString().trim());
		v.setTitle("J48 Tree (" + selectedFile.getAbsolutePath()+")");
		v.setVisible(true);
		
		addLog("Generating source code...");
		generateSourceCode(sb.toString().trim());
	}
	
	private void generateSourceCode(String tree){
		//TODO here! 
		addLog("Generation complete :)");
	}


	private void addLog(String s){
		txtLog.insert(s+"\n", 0);
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
