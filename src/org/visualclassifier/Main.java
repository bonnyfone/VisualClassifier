package org.visualclassifier;

import org.visualclassifier.generator.ClassifierGenerator;

public class Main {
	
	public static void main(String[] args) {
		
		
		//DEBUG
		/*
		args = new String[3];
		args[0] = "/media/Mistero/C++/Tesi/datasets/frame.bmp";
		args[1] = "/media/Mistero/C++/Tesi/datasets/frameclus.bmp";
		args[2] = "/media/Mistero/C++/Tesi/datasets/road1.arff";
		*/
		
		if(args.length==0){
			ClassifierGenerator cg = new ClassifierGenerator();
			cg.setVisible(true);
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
