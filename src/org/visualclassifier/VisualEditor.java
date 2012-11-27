package org.visualclassifier;

import javax.swing.JFrame;

public class VisualEditor extends JFrame {
	
	private static final long serialVersionUID = -8723579947137926832L;
	private DataHandler dh;
	
	public VisualEditor(DataHandler dh){
		this.dh = dh;
		init();
	}
	
	private void init(){
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
