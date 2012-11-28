package org.visualclassifier.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Viewer extends JFrame implements ClipboardOwner{
	
	private static final long serialVersionUID = -8375144330131508984L;

	public Viewer(String content, Color txtColor){
		setSize(700, 600);
		setLayout(new GridLayout(1,1));
		final JTextArea t = new JTextArea(content);
		JScrollPane scroll = new JScrollPane(t);
		t.setTabSize(2);
		t.setForeground(txtColor);
		setLayout(new BorderLayout());
		add(scroll,BorderLayout.CENTER);
		
		JButton btn = new JButton("Copy");
		add(btn,BorderLayout.SOUTH);
		
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setClipboardContents(t.getText());
			}
		});
		
		
	}

	public void setClipboardContents( String aString ){
	    StringSelection stringSelection = new StringSelection( aString );
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents( stringSelection, this );
	  }

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		//?
	}
	
}
