package org.clas.detectors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.commons.math3.linear.SparseRealMatrix;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;

public class MVTpulseViewer {
	
	public EmbeddedCanvasTabbed pulseCanvas = null;
    public JComboBox<String> hitComboBox = null;
    public int pulseThreshold = 0;
    public boolean pulseStatus = false;
    public ArrayList<int []> hitList = new ArrayList<int[]>();
    
    public int numberOfSamples = 16;
    public int sparseReading=0;
    public JTabbedPane tabbedpane;
    public String name;
    
    H1F pulseHistoBMT;

    public MVTpulseViewer(String title) {
    	// TODO Auto-generated constructor stub
    	this.name=title;
    	pulseHistoBMT = new H1F("Pulse","Pulse", numberOfSamples, 1., numberOfSamples+1);
    }
    
    public MVTpulseViewer(String title, int numberOfSamples, int sparseReading) {
    	// TODO Auto-generated constructor stub
    	this.name=title;
    	this.numberOfSamples=numberOfSamples;
    	this.sparseReading=sparseReading;
    	
    	pulseHistoBMT = new H1F("Pulse","Pulse", numberOfSamples*(1+sparseReading), 1., numberOfSamples*(1+sparseReading)+1);
    }
    
    public void createPulseView(){

    	JSplitPane pulseControlPanelMVT = new JSplitPane();
    	pulseControlPanelMVT.setDividerLocation(350);

    	JPanel    controlPanelMVT = new JPanel(new BorderLayout());
    	controlPanelMVT.setLayout(new BorderLayout());
    	pulseControlPanelMVT.setLeftComponent(controlPanelMVT);

    	pulseCanvas = new EmbeddedCanvasTabbed("pulseCanvas");
    	pulseControlPanelMVT.setRightComponent(pulseCanvas);
    	this.pulseCanvas.getCanvas("pulseCanvas").draw(pulseHistoBMT);

    	tabbedpane.add(pulseControlPanelMVT, "Pulse Viewer "+name);

    	JCheckBox pulseEnableButtonBMT = new JCheckBox("Enable Pulse "+name);
    	controlPanelMVT.add(pulseEnableButtonBMT,BorderLayout.NORTH);
    	pulseEnableButtonBMT.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			if (pulseStatus){
    				pulseStatus=false;
    				System.out.println("Pulse viewer "+name+" is now disabled");
    			}else{
    				pulseStatus=true;
    				System.out.println("Pulse viewer "+name+" is now enabled");
    			}
    		}
    	});

    	hitComboBox = new JComboBox<String>();
    	hitComboBox.setPreferredSize(new Dimension(200, 300));
    	controlPanelMVT.add(hitComboBox,BorderLayout.CENTER);
    	hitComboBox.addItemListener(new ItemListener() {

    		@Override
    		public void itemStateChanged(ItemEvent e) {
    			try{
//    				System.out.println("pulse to be updated");
    				int comboBoxHit = hitComboBox.getSelectedIndex();//getSelectedItem();
    				if (comboBoxHit>=0){
    					updatePulseMVT(comboBoxHit);
    				}
//    				System.out.println("Pulse updated");
    			}
    			catch(Exception e1){
    				e1.printStackTrace();
    			}

    		}
    		//    		public void actionPerformed(ItemEvent e) {
    		//    			System.out.println("pulse to be updated");
    		//    			int comboBoxHit = hitComboBox.getSelectedIndex();//getSelectedItem();
    		//    			if (comboBoxHit>=0){
    		//    				updatePulseMVT(comboBoxHit);
    		//    			}
    		//    			System.out.println("Pulse updated");
    		//    		}
    	});

    	//            pulseThresholdTextMVT = new JTextField("Adc Threshold");
    	//            pulseThresholdTextMVT.setPreferredSize(new Dimension(150, 30));
    	//            controlPanelMVT.add(pulseThresholdTextMVT,BorderLayout.SOUTH);
    	//            pulseThresholdTextMVT.addActionListener(this);

    }

    public void clearHits(){
    	hitList.clear();
    }

    public void updatePulseMVT(int hit){
    	for (int i=0;i<hitList.get(hit).length-3;i++){
    		if (sparseReading==0){
    			pulseHistoBMT.setBinContent(i, hitList.get(hit)[i+3]);
    		}else{
    			for (int j=0; j<=sparseReading;j++){
    				pulseHistoBMT.setBinContent(i*(1+sparseReading)+j, hitList.get(hit)[i+3]);
    			}
    		}
    		
    	}
    	this.pulseCanvas.repaint();
//    	System.out.println("updateed pulse: "+name);
    }

    public void updateComboBox() {
    	hitComboBox.removeAllItems();
    	if (pulseStatus){
    		//System.out.println("hitlist size: "+this.hitList.size());
    		for (int i=0; i<this.hitList.size(); i++){
    			hitComboBox.addItem("Hit: "+i+" Sector: "+this.hitList.get(i)[0]+" Layer: "+this.hitList.get(i)[1]+" Component: "+this.hitList.get(i)[2]);
    		}
    		if (this.hitList.size()>0){
    			hitComboBox.setSelectedIndex(0);
    		}
    		updatePulseMVT(0);
//    		System.out.println("combobox updated: "+name);
    	}
    	
    }

    public void add(int[] hit) {
    	this.hitList.add(hit);

    }

    public void draw(JTabbedPane tabbedpane){
    	this.tabbedpane=tabbedpane;
    	createPulseView();
    }


}
