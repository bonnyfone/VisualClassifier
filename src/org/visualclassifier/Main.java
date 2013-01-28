package org.visualclassifier;

import java.io.File;
import java.util.ArrayList;

import org.visualclassifier.generator.ClassifierGenerator;

public class Main {

	public static ArrayList<File> sources = null;
	public static int current = 0;

	public static void nextVideo(){
		if(current>= sources.size()){
			System.out.println("No more file available (not classified)");
			return;
		}
		System.out.println("Opening next video..");
		File f = sources.get(current);
		if(f.isDirectory()){
			String[] list = f.list();
			boolean found=false;
			String arff="";
			for(int i=0;i<list.length &&!found;i++){
				if(list[i].endsWith(".arff") && !list[i].contains("classified")){
					arff=f.getAbsolutePath()+"/"+list[i];
					found=true;
				}
			}
			String frame = f.getAbsolutePath()+"/frame.bmp";
			String frameClus =  f.getAbsolutePath()+"/frameclus.bmp";
			System.out.println("Data found:");
			System.out.println(frame);
			System.out.println(frameClus);
			System.out.println(arff);
			DataHandler myData = new DataHandler(frame,frameClus,arff );
			VisualEditor v = new VisualEditor(myData);
			v.setVisible(true);
		}else{
			System.out.println("Not a valid dataset directory!");
		}

		current++;
	}

	public static void main(String[] args) {
		//DEBUG
		
		System.out.println(args.length);
		for(String s:args)
			System.out.println(s);
				
		/*
		args = new String[2];
		String origin = "/media/Mistero/C++/Tesi/datasets/NEW_TEST/";
		args[0] = origin;
		args[1] =  "batch";
		*/
		

		if(args.length==0){
			ClassifierGenerator cg = new ClassifierGenerator(true);
			cg.setVisible(true);
		}
		else if(args.length==1 ||args.length==2){ //DIR SPECIFIED
			if(args[1].equals("batch")){
				System.out.println("Batch mode");
				File f = new File(args[0]);
				
				sources = new ArrayList<File>();
				
				File[]list = f.listFiles();
				for(File fd : list){
					boolean skip = false;
					
					if(fd.getName().contains("discard"))continue;
					
					String[] elements = fd.list();
					for(String s:elements){
						if(s.contains("classified")){
							skip=true;
							break;
						}
					}
					if(!skip){
						sources.add(fd);
					}
				}
				
				nextVideo();
			}
			else{
				File f = new File(args[0]);
				if(f.isDirectory()){
					String[] list = f.list();
					boolean found=false;
					String arff="";
					for(int i=0;i<list.length &&!found;i++){
						if(list[i].endsWith(".arff") && !list[i].contains("classified")){
							arff=f.getAbsolutePath()+"/"+list[i];
							found=true;
						}
					}
					String frame = f.getAbsolutePath()+"/frame.bmp";
					String frameClus =  f.getAbsolutePath()+"/frameclus.bmp";
					System.out.println("Data found:");
					System.out.println(frame);
					System.out.println(frameClus);
					System.out.println(arff);
					DataHandler myData = new DataHandler(frame,frameClus,arff );
					VisualEditor v = new VisualEditor(myData);
					v.setVisible(true);
				}else{
					System.out.println("Not a valid dataset directory!");
				}
			}
		}
		else if(args.length<3){
			System.out.println("Specify: frame_image, cluster_image, raw_dataset");
		}else{
			DataHandler myData = new DataHandler(args[0], args[1], args[2]);
			VisualEditor v = new VisualEditor(myData);
			v.setVisible(true);
		}
	}

}
