package org.visualclassifier;

import java.io.File;

import org.visualclassifier.generator.ClassifierGenerator;

public class Main {
	
	public static void main(String[] args) {
		
		
		//DEBUG
		/*
		args = new String[3];
		String origin = "/media/Mistero/C++/Tesi/datasets/TRAINING SET/SSDB005451/";
		args[0] = origin+"frame.bmp";
		args[1] =  origin+"frameclus.bmp";
		args[2] =  origin+"set.arff";
		*/
		
		if(args.length==0){
			ClassifierGenerator cg = new ClassifierGenerator(true);
			cg.setVisible(true);
		}
		else if(args.length==1){ //DIR SPECIFIED
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
		else if(args.length<3){
			System.out.println("Specify: frame_image, cluster_image, raw_dataset");
		}else{
			DataHandler myData = new DataHandler(args[0], args[1], args[2]);
			VisualEditor v = new VisualEditor(myData);
			v.setVisible(true);
		}
	}

}
