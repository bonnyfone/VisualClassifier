package org.visualclassifier.generator;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Viewer extends JFrame{
	
	public Viewer(String content){
		setSize(700, 600);
		setLayout(new GridLayout(1,1));
		add(new JTextArea(content));
	}

}
