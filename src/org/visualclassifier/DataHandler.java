package org.visualclassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class DataHandler {
	public static String TAG_RELATION =  "@relation ";
	public static String TAG_ATTRIBUTE = "@attribute '";
	public static String TAG_CLASS = "@attribute 'class' {";
	public static String TAG_DATA = "@data";
	public static String TAG_VAL = "#_";
	public static String TAG_W = "% w=";
	public static String TAG_H = "% h=";

	private String img_frame;
	private String img_cluster;
	private String dataset;
	private String relationName;
	private File arff;
	private File pix2clu;
	private ArrayList<String> roadCluster;

	private HashMap<String, ArrayList<String>> clusters;
	private HashMap<String,String>pixel2cluster;

	private ArrayList<String> attributes;
	private ArrayList<String> classValue;


	public DataHandler(String img_frame, String img_cluster, String dataset){
		this.img_frame = img_frame;
		this.img_cluster = img_cluster;
		this.dataset = dataset;

		clusters = new HashMap<String, ArrayList<String>>();
		pixel2cluster = new HashMap<String, String>();
		attributes = new ArrayList<String>();
		classValue = new ArrayList<String>();
		roadCluster = new ArrayList<String>();

		loadData();
		System.out.println("Finished.\nInitializing GUI...");
	}

	public void addRoadCluster(String clusterId){
		if(!roadCluster.contains(clusterId))
			roadCluster.add(clusterId);
	}

	public void removeRoadCluster(String clusterId){
		roadCluster.remove(clusterId);
	}

	public boolean isRoadCluster(String s){
		return roadCluster.contains(s);
	}

	public void exportData() throws IOException{
		/* ARFF */
		
		System.out.println("Loading data from"+ arff.getAbsolutePath() +"...");
		String out = arff.getAbsolutePath().replace(".arff", "") + "_classified.arff";
		
		
		FileInputStream fstream = new FileInputStream(arff);
		FileOutputStream fostream  = new FileOutputStream(new File(out));

		DataOutputStream  dout = new DataOutputStream(fostream);
		DataInputStream in = new DataInputStream(fstream);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dout));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String value;
		boolean process = false;
		int cluster = 0;
		while ((value = br.readLine()) != null)   {
			if(!process){
				if(value.startsWith(TAG_DATA)){
					process=true;
					bw.append(value+"\n");
					continue;
				}
				bw.append(value+"\n");
			}
			else{
				if(isRoadCluster(""+cluster)){
					value = value.replace(TAG_VAL, classValue.get(1));
				}else{
					value = value.replace(TAG_VAL, classValue.get(0));
				}
				
				bw.append(value+"\n");
				cluster++;
			}
		}
		bw.flush();
		dout.close();
		in.close();
	}

	private void loadData() {
		try{

			if(dataset.endsWith("arff")){
				arff = new File(dataset);
				pix2clu = new File(dataset.replace("arff", "pix2clu"));
			}
			else{
				pix2clu = new File(dataset);
				arff = new File(dataset.replace("pix2clu","arff"));
			}

			/* PIX2CLU */

			System.out.println("Loading data from"+ pix2clu.getAbsolutePath() +"...");
			FileInputStream fstream = new FileInputStream(pix2clu);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String value;
			int count=0;
			while ((value = br.readLine()) != null)   {
				if(clusters.get(value)==null)clusters.put(value, new ArrayList<String>());

				clusters.get(value).add(count+"");
				pixel2cluster.put(count+"", value);
				count++;
			}
			in.close();
			//System.out.println(pixel2cluster.get("214560"));
			//System.out.println(clusters.keySet().size());


			/* ARFF */
			System.out.println("Loading data from"+ arff.getAbsolutePath() +"...");
			fstream = new FileInputStream(arff);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			while ((value = br.readLine()) != null)   {
				//TODO
				if(value.startsWith(TAG_RELATION)){
					relationName = value.substring(value.indexOf(TAG_RELATION)+TAG_RELATION.length(),value.length()).trim();
				}
				else if(value.startsWith(TAG_CLASS)){
					int s = value.indexOf(TAG_CLASS)+TAG_CLASS.length();
					int i;
					while( (i=value.indexOf(",",s))!= -1){
						String v = value.substring(s,i).trim();
						//System.out.println(v);
						s = i+1;
						classValue.add(v);

					}
					i = value.indexOf("}");
					String v = value.substring(s,i).trim();
					//System.out.println(v);
					classValue.add(v);
				}
				else if(value.startsWith(TAG_ATTRIBUTE)){
					int s = value.indexOf(TAG_ATTRIBUTE)+TAG_ATTRIBUTE.length();
					String newAttr = value.substring(s,value.indexOf("'", s));
					attributes.add(newAttr);
					//System.out.println(newAttr);
				}
			}
			in.close();

			System.out.println("\n# Relation: " +relationName);
			System.out.println("\n# Attributes: ");
			for(String s : attributes) System.out.println(s);

					System.out.println("\n# Class: ");
					for(String s : classValue) System.out.println(s);

							System.out.println("------------------------------\n");

		}catch (Exception e){
			e.printStackTrace();
		}

	}


	public String getImg_frame() {
		return img_frame;
	}


	public void setImg_frame(String img_frame) {
		this.img_frame = img_frame;
	}


	public String getImg_cluster() {
		return img_cluster;
	}


	public void setImg_cluster(String img_cluster) {
		this.img_cluster = img_cluster;
	}


	public String getDataset() {
		return dataset;
	}


	public void setDataset(String dataset) {
		this.dataset = dataset;
	}


	public String getRelationName() {
		return relationName;
	}


	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}


	public HashMap<String, ArrayList<String>> getClusters() {
		return clusters;
	}


	public void setClusters(HashMap<String, ArrayList<String>> clusters) {
		this.clusters = clusters;
	}


	public HashMap<String, String> getPixel2cluster() {
		return pixel2cluster;
	}


	public void setPixel2cluster(HashMap<String, String> pixel2cluster) {
		this.pixel2cluster = pixel2cluster;
	}


	public ArrayList<String> getAttributes() {
		return attributes;
	}


	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}


	public ArrayList<String> getClassValue() {
		return classValue;
	}


	public void setClassValue(ArrayList<String> classValue) {
		this.classValue = classValue;
	}

	public ArrayList<String> getRoadCluster() {
		return roadCluster;
	}

	public void setRoadCluster(ArrayList<String> roadCluster) {
		this.roadCluster = roadCluster;
	}

}
