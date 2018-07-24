package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.clas.detectors.*;
import org.jlab.detector.decode.CLASDecoder;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.elog.LogEntry; 

        
/**
 *
 * @author ziegler
 */

public class EventViewer implements IDataEventListener, DetectorListener, ActionListener, ChangeListener {
    
    List<DetectorPane2D> DetectorPanels     = new ArrayList<DetectorPane2D>();
    JTabbedPane tabbedpane           	    = null;
    JPanel mainPanel 			    = null;
    JMenuBar menuBar                        = null;
<<<<<<< HEAD
    JTextPane clas12Textinfo                = new JTextPane();
    DataSourceProcessorPane processorPane   = null;
    EmbeddedCanvasTabbed CLAS12Canvas       = null;
    //EmbeddedCanvasTabbed CLAS12CDCanvas         = null;
    
    CLASDecoder                clasDecoder = new CLASDecoder();
           
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 100;
    private int runNumber     = 2284;
    private int ccdbRunNumber = 0;
    
    public String outPath = "/home/clasrun/CLAS12MON";
=======
    DataSourceProcessorPane processorPane 	= null;
    JTextPane clas12Textinfo                = new JTextPane();
    EmbeddedCanvasTabbed CLAS12Canvas       = null;
    //EmbeddedCanvasTabbed CLAS12CDCanvas         = null;
    
    /* MVT PULSE DISPLAY */
    MVTpulseViewer pulseWindowBMT = new MVTpulseViewer("BMT", 6, 1);
    MVTpulseViewer pulseWindowFMT = new MVTpulseViewer("FMT", 6, 1);
    MVTpulseViewer pulseWindowFTT = new MVTpulseViewer("FTT", 10, 0);
    /* MVT PULSE DISPLAY */
    
    //CodaEventDecoder               decoder = new CodaEventDecoder();
    CLASDecoder                clasDecoder = new CLASDecoder();
    //DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
           
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 100;
    private int runNumber  = 2284;
    private int ccdbRunNumber = 0;
    
>>>>>>> Monitoring-Expert/devel
    
    // detector monitors
    DetectorMonitor[] monitors = {
                new BMTmonitor("BMT", pulseWindowBMT),        // 0 /* MVT PULSE DISPLAY */
//                new BSTmonitor("BST"),        // 1
//                new CNDmonitor("CND"),        // 2
//                new CTOFmonitor("CTOF"),      // 3
//                new DCmonitor("DC"),          // 4
//                new ECmonitor("ECAL"),        // 5
                new FMTmonitor("FMT", pulseWindowFMT),        // 6 /* MVT PULSE DISPLAY */
//                new FTCALmonitor("FTCAL"),    // 7
//                new FTHODOmonitor("FTHODO"),  // 8
//                new FTOFmonitor("FTOF"),      // 9
//                new FTTRKmonitor("FTTRK"),    // 10
//                new HTCCmonitor("HTCC"),      // 11
//                new LTCCmonitor("LTCC"),      // 12
//                new RICHmonitor("RICH"),      // 13
//                new RECmonitor("RECON"),      // 14
                new FTTRKmonitor("FTTRK", pulseWindowFTT),        // 15
//                new RFmonitor("RF"),          // 15
//                new HELmonitor("HEL"),        // 16
//                new FCUPmonitor("Faraday Cup"),  // 17
                new TRIGGERmonitor("Trigger"),   // 18
    };
        
    public EventViewer() {    	
        		
	// create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms to file", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms as png", KeyEvent.VK_B);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms as png");
        menuItem.addActionListener(this);
        file.add(menuItem);
        //menuItem = new JMenuItem("Create histogram PDF", KeyEvent.VK_P);
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        //menuItem.getAccessibleContext().setAccessibleDescription("Create historgram PDF");
        //menuItem.addActionListener(this);
        //file.add(menuItem);
        
        menuBar.add(file);
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis log scale", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis log scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis lin scale", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis lin scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
<<<<<<< HEAD
        menuItem = new JMenuItem("Set DC occupancy scale max");
        menuItem.getAccessibleContext().setAccessibleDescription("Set DC occupancy scale max");
        menuItem.addActionListener(this);
        settings.add(menuItem);
=======
>>>>>>> Monitoring-Expert/devel
        menuItem = new JMenuItem("Set run number");
        menuItem.getAccessibleContext().setAccessibleDescription("Set run number");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);
         
        JMenu upload = new JMenu("Upload");
        upload.setMnemonic(KeyEvent.VK_A);
        upload.getAccessibleContext().setAccessibleDescription("Upload histograms to the Logbook");
        menuItem = new JMenuItem("Upload all histos to the logbook");
        menuItem.getAccessibleContext().setAccessibleDescription("Upload all histos to the logbook");
        menuItem.addActionListener(this);
        upload.add(menuItem);
        menuItem = new JMenuItem("Upload occupancy histos to the logbook", KeyEvent.VK_U);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Upload occupancy histos to the logbook");
        menuItem.addActionListener(this);
        upload.add(menuItem);
        menuBar.add(upload);
        
        JMenu reset = new JMenu("Reset");
        reset.getAccessibleContext().setAccessibleDescription("Reset histograms");
        
        JMenuItem menuItemdefault = new JMenuItem("Default for all");
        menuItemdefault.getAccessibleContext().setAccessibleDescription("Default for all");
        menuItemdefault.addActionListener(this);
        reset.add(menuItemdefault);
        
        JMenuItem menuItemdisable = new JMenuItem("Disable histogram reset");
        menuItemdisable.getAccessibleContext().setAccessibleDescription("Disable histogram reset");
        menuItemdisable.addActionListener(this);
        reset.add(menuItemdisable);
        
        JMenuItem menuItemBMT = new JMenuItem("Reset BMT histograms");
        menuItemBMT.getAccessibleContext().setAccessibleDescription("Reset BMT histograms");
        menuItemBMT.addActionListener(this);
        reset.add(menuItemBMT);
        
        JMenuItem menuItemBST = new JMenuItem("Reset BST histograms");
        menuItemBST.getAccessibleContext().setAccessibleDescription("Reset BST histograms");
        menuItemBST.addActionListener(this);
        reset.add(menuItemBST);
        
        JMenuItem menuItemCND = new JMenuItem("Reset CND histograms");
        menuItemCND.getAccessibleContext().setAccessibleDescription("Reset CND histograms");
        menuItemCND.addActionListener(this);
        reset.add(menuItemCND);
        
        JMenuItem menuItemCTOF = new JMenuItem("Reset CTOF histograms");
        menuItemCTOF.getAccessibleContext().setAccessibleDescription("Reset CTOF histograms");
        menuItemCTOF.addActionListener(this);
        reset.add(menuItemCTOF);
        
        JMenuItem menuItemDC = new JMenuItem("Reset DC histograms");
        menuItemDC.getAccessibleContext().setAccessibleDescription("Reset DC histograms");
        menuItemDC.addActionListener(this);
        reset.add(menuItemDC);
        
        JMenuItem menuItemECAL = new JMenuItem("Reset ECAL histograms");
        menuItemECAL.getAccessibleContext().setAccessibleDescription("Reset ECAL histograms");
        menuItemECAL.addActionListener(this);
        reset.add(menuItemECAL);
        
        JMenuItem menuItemFMT = new JMenuItem("Reset FMT histograms");
        menuItemFMT.getAccessibleContext().setAccessibleDescription("Reset FMT histograms");
        menuItemFMT.addActionListener(this);
        reset.add(menuItemFMT);
        
        JMenuItem menuItemFT = new JMenuItem("Reset FT histograms");
        menuItemFT.getAccessibleContext().setAccessibleDescription("Reset FT histograms");
        menuItemFT.addActionListener(this);
        reset.add(menuItemFT);
        
        JMenuItem menuItemFTOF = new JMenuItem("Reset FTOF histograms");
        menuItemFTOF.getAccessibleContext().setAccessibleDescription("Reset FTOF histograms");
        menuItemFTOF.addActionListener(this);
        reset.add(menuItemFTOF);
  
        JMenuItem menuItemHTTC = new JMenuItem("Reset HTTC histograms");
        menuItemHTTC.getAccessibleContext().setAccessibleDescription("Reset HTTC histograms");
        menuItemHTTC.addActionListener(this);
        reset.add(menuItemHTTC);
        
        JMenuItem menuItemLTTC = new JMenuItem("Reset LTTC histograms");
        menuItemLTTC.getAccessibleContext().setAccessibleDescription("Reset LTTC histograms");
        menuItemLTTC.addActionListener(this);
        reset.add(menuItemLTTC);
        
        JMenuItem menuItemRICH = new JMenuItem("Reset RICH histograms");
        menuItemRICH.getAccessibleContext().setAccessibleDescription("Reset RICH histograms");
        menuItemRICH.addActionListener(this);
        reset.add(menuItemRICH);
        
        menuBar.add(reset);
        
        JMenu trigBits = new JMenu("DetectorBits");
        trigBits.getAccessibleContext().setAccessibleDescription("Select Detectors for Testing Trigger Bits (not yet implmented)");
        
        JCheckBoxMenuItem cb1 = new JCheckBoxMenuItem("EC");    
        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors[5].setTestTrigger(true);
                } else {
                 	monitors[5].setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb1); 
        
        JCheckBoxMenuItem cb2 = new JCheckBoxMenuItem("HTCC");
        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors[11].setTestTrigger(true);
                } else {
                 	monitors[11].setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb2); 
        
        JCheckBoxMenuItem cb3 = new JCheckBoxMenuItem("BST");
        cb3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors[1].setTestTrigger(true);
                } else {
                 	monitors[1].setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb3); 
        
        JCheckBoxMenuItem cb4 = new JCheckBoxMenuItem("CTOF");
        cb4.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors[3].setTestTrigger(true);
                } else {
                 	monitors[3].setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb4); 
        
        JCheckBoxMenuItem cb5 = new JCheckBoxMenuItem("MVT");
        cb5.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors[0].setTestTrigger(true);
                } else {
                 	monitors[0].setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb5); 
        
        JCheckBoxMenuItem cb6 = new JCheckBoxMenuItem("FTOF");
        cb6.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                  	monitors[9].setTestTrigger(true);
                } else {
                 	monitors[9].setTestTrigger(false);
                };
            }
        });         
        trigBits.add(cb6); 
               
        menuBar.add(trigBits);
        
        String TriggerDef[] = { "Electron",
		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
		        "ElectronOR noDC>300","PCALxECAL>10","","","","","","","","","","",
		        "FTOFxPCALxECAL(1-4)","FTOFxPCALxECAL(2-5)","FTOFxPCALxECAL(3-6)","","",
		        "FTxHDxFTOFxPCALxCTOF",
		        "FTxHDx(FTOFxPCAL)^2","FTxHD>100","FT>100","","","",
		        "1K Pulser"};
/*        
        String TriggerDef[] = { "Electron",
		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
		        "HTCC(>1pe)","HTCCxPCAL(>300MeV)","FTOFxPCAL^3","FTOFxPCALxECAL^3",
		        "FTOFxPCALxCTOF","FTOFxPCALxCND","FTOFxPCALxCNDxCTOF","FTOFxPCAL^2",
		        "FTOFxPCALxECAL^2","FTOFxPCAL(1-4)","FTOFxPCAL(2-5)","FTOFxPCAL(3-6)",
		        "FTOFxPCALxECAL(1-4)","FTOFxPCALxECAL(2-5)","FTOFxPCALxECAL(3-6)",
		        "FTxFTOFxPCALxCTOF","FTxFTOFxPCALxCND","FTxFTOFxPCALxCTOFxCND",
		        "FTx(FTOFxPCAL)^2","FTx(FTOFxPCAL)^3","FT(>300)xHODO","FT(>500)xHODO","FT>300","FT>500",
		        "1K Pulser"};  
*/        		             
        JMenu trigBitsBeam = new JMenu("TriggerBits");
        trigBitsBeam.getAccessibleContext().setAccessibleDescription("Test Trigger Bits");
        
        for (int i=0; i<32; i++) {
        	
            JCheckBoxMenuItem bb = new JCheckBoxMenuItem(TriggerDef[i]);  
            final Integer bit = new Integer(i);
            bb.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                	
                    if(e.getStateChange() == ItemEvent.SELECTED) {
<<<<<<< HEAD
                        for(int k=0; k<monitors.length; k++) {
                      	monitors[k].setTriggerMask(bit);
                        }
                    } else {
                        for(int k=0; k<monitors.length; k++) {
=======
                        //for(int k=0; k<19; k++) {
                        for(int k=0; k<3; k++) {
                      	monitors[k].setTriggerMask(bit);
                        }
                    } else {
//                        for(int k=0; k<19; k++) {
                        for(int k=0; k<3; k++) {
>>>>>>> Monitoring-Expert/devel
                     	monitors[k].clearTriggerMask(bit);
                        }
                    };
                }
            });         
            trigBitsBeam.add(bb); 
        	        	
        }

        menuBar.add(trigBitsBeam);
        

        // create main panel
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
    
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        
        CLAS12Canvas    = new EmbeddedCanvasTabbed("FD");
        CLAS12Canvas.getCanvas("FD").divide(3,3);
        CLAS12Canvas.getCanvas("FD").setGridX(false);
        CLAS12Canvas.getCanvas("FD").setGridY(false); 
        CLAS12Canvas.addCanvas("CD");
        CLAS12Canvas.getCanvas("CD").divide(2,2);
        CLAS12Canvas.getCanvas("CD").setGridX(false);
        CLAS12Canvas.getCanvas("CD").setGridY(false);
        CLAS12Canvas.addCanvas("FT");
        CLAS12Canvas.getCanvas("FT").divide(1,3);
        CLAS12Canvas.getCanvas("FT").setGridX(false);
        CLAS12Canvas.getCanvas("FT").setGridY(false);

        
        JPanel    CLAS12View = new JPanel(new BorderLayout());
        JSplitPane splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(CLAS12View);
        splitPanel.setRightComponent(CLAS12Canvas);
        JTextPane clas12Text   = new JTextPane();
        clas12Text.setText("CLAS12\n monitoring plots\n V2.0\n");
<<<<<<< HEAD
        clas12Text.setEditable(false);       
        this.clas12Textinfo.setEditable(false);
        this.clas12Textinfo.setFont(new Font("Avenir",Font.PLAIN,16));
        this.clas12Textinfo.setBackground(CLAS12View.getBackground());
=======
//        clas12Text.setEditable(false);
//        JTextPane clas12Textinfo  = new JTextPane();
//        clas12Textinfo.setText("\nrun number: "+this.runNumber + "\nmode:" + "\nfile:" + "\n");
//        clas12Textinfo.setEditable(false);
        clas12Text.setEditable(false);       
        this.clas12Textinfo.setEditable(false);
>>>>>>> Monitoring-Expert/devel
        StyledDocument styledDoc = clas12Text.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        styledDoc.setParagraphAttributes(0, styledDoc.getLength(), center, false);
        clas12Text.setBackground(CLAS12View.getBackground());
        clas12Text.setFont(new Font("Avenir",Font.PLAIN,20));
        JLabel clas12Design = this.getImage("https://www.jlab.org/Hall-B/clas12-web/sidebar/clas12-design.jpg",0.08);
        JLabel clas12Logo   = this.getImage("https://www.jlab.org/Hall-B/pubs-web/logo/CLAS-frame-low.jpg", 0.3);
//        CLAS12View.add(clas12Name,BorderLayout.PAGE_START);
        CLAS12View.add(clas12Textinfo,BorderLayout.BEFORE_FIRST_LINE );
        CLAS12View.add(clas12Design);
        CLAS12View.add(clas12Text,BorderLayout.PAGE_END);

        tabbedpane.add(splitPanel,"Summary");
        tabbedpane.addChangeListener(this);
        
       
        for(int k =0; k<this.monitors.length; k++) {
                this.tabbedpane.add(this.monitors[k].getDetectorPanel(), this.monitors[k].getDetectorName());
        	        this.monitors[k].getDetectorView().getView().addDetectorListener(this);
                        
        }
        
        this.processorPane.addEventListener(this);
        
        this.tabbedpane.add(new Contact(),"Contacts");        
        this.tabbedpane.add(new Acronyms(),"Acronyms");
        
        this.setCanvasUpdate(canvasUpdateTime);
        this.plotSummaries();
        
        /* MVT PULSE DISPLAY */
        pulseWindowBMT.draw(tabbedpane);
        pulseWindowFMT.draw(tabbedpane);
        pulseWindowFTT.draw(tabbedpane);
        /* MVT PULSE DISPLAY */
        
    }
      
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand()=="Set GUI update interval") {
            this.chooseUpdateInterval();
        }
        if(e.getActionCommand()=="Set global z-axis log scale") {
        	   for(int k=0; k<this.monitors.length; k++) {this.monitors[k].setLogZ(true);this.monitors[k].plotHistos();}
        }
        if(e.getActionCommand()=="Set global z-axis lin scale") {
           for(int k=0; k<this.monitors.length; k++) {this.monitors[k].setLogZ(false);this.monitors[k].plotHistos();}
        }
<<<<<<< HEAD
        if(e.getActionCommand()=="Set DC occupancy scale max") {
           setDCRange(e.getActionCommand());
        }
        if(e.getActionCommand()=="Set run number") {
           setRunNumber(e.getActionCommand());
        }

=======
        
        if(e.getActionCommand()=="Set run number") {
        	setRunNumber(e.getActionCommand());
        }
        
>>>>>>> Monitoring-Expert/devel
        if(e.getActionCommand()=="Open histograms file") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(System.getProperty("user.dir"));  
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if(e.getActionCommand()=="Print histograms as png") {
            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Create histogram PDF") {
            this.createHistoPDF();
        }
        if(e.getActionCommand()=="Save histograms to file") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "CLAS12Mon_run_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(System.getProperty("user.dir"));   
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        
        if(e.getActionCommand()=="Upload all histos to the logbook") {   
            
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String tstamp = df.format(new Date());
            String data = outPath + "/output" + "/clas12mon_" + this.runNumber + "_" + tstamp;        
            File theDir = new File(data);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                boolean result = false;
                try{theDir.mkdir();result = true;} 
                catch(SecurityException se){}        
                if(result){ System.out.println("Created directory: " + data);}
            }
            
            String fileName1 = data + "/summary_FD_"+tstamp+".png";
            System.out.println(fileName1);
            CLAS12Canvas.getCanvas("FD").save(fileName1);
            String fileName2 = data + "/summary_CD_"+tstamp+".png";
            System.out.println(fileName2);
            CLAS12Canvas.getCanvas("CD").save(fileName2);
            String fileName3 = data + "/summary_FT_"+tstamp+".png";
            System.out.println(fileName3);
            CLAS12Canvas.getCanvas("FT").save(fileName3);
            
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].printCanvas(data,tstamp);
            }
            
            LogEntry entry = new LogEntry("All online monitoring histograms for run number " + this.runNumber, "HBLOG");
            
            System.out.println("Starting to upload all monitoring plots");
            
            try{
              entry.addAttachment(data+"/summary_FD_"+tstamp+".png", "Summary plots FD");
              entry.addAttachment(data+"/summary_CD_"+tstamp+".png", "Summary plots CD");
              entry.addAttachment(data+"/summary_FT_"+tstamp+".png", "Summary plots FT");
              System.out.println("Summary plots uploaded");
              entry.addAttachment(data+"/BMT_canvas0_"+tstamp+".png", "BMT occupancies");
              entry.addAttachment(data+"/BMT_canvas1_"+tstamp+".png", "BMT occupancies c");
              entry.addAttachment(data+"/BMT_canvas2_"+tstamp+".png", "BMT occupancies z");
              entry.addAttachment(data+"/BMT_canvas3_"+tstamp+".png", "BMT time of max");
              entry.addAttachment(data+"/BMT_canvas4_"+tstamp+".png", "BMT multiplicity");
              System.out.println("BMT plots uploaded");
              entry.addAttachment(data+"/BST_canvas0_"+tstamp+".png", "BST occupancies 2D");
              entry.addAttachment(data+"/BST_canvas1_"+tstamp+".png", "BST occupancies 1D");
              entry.addAttachment(data+"/BST_canvas2_"+tstamp+".png", "BST multiplicity"); 
              System.out.println("BST plots uploaded");
              entry.addAttachment(data+"/CND_canvas0_"+tstamp+".png", "CND ADC occupancies and spectra");
              entry.addAttachment(data+"/CND_canvas1_"+tstamp+".png", "CND TDC occupancies and spectra");
              System.out.println("CND plots uploaded");
              entry.addAttachment(data+"/CTOF_canvas0_"+tstamp+".png", "CTOF ADC occupancies and spectra");
              entry.addAttachment(data+"/CTOF_canvas1_"+tstamp+".png", "CTOF TDC occupancies and spectra");
              System.out.println("CTOF plots uploaded");
              entry.addAttachment(data+"/DC_canvas0_"+tstamp+".png", "DC occupancies raw");
              entry.addAttachment(data+"/DC_canvas1_"+tstamp+".png", "DC occupancies normalized logarithmic scale");
              entry.addAttachment(data+"/DC_canvas2_"+tstamp+".png", "DC occupancies normalized linear scale");
              entry.addAttachment(data+"/DC_canvas3_"+tstamp+".png", "DC region occupancies");
              entry.addAttachment(data+"/DC_canvas4_"+tstamp+".png", "DC TDC raw value versus layer");
              entry.addAttachment(data+"/DC_canvas5_"+tstamp+".png", "DC TDC spectra per Sector / Superlayer");
              entry.addAttachment(data+"/DC_canvas6_"+tstamp+".png", "DC hit multiplicity");
              System.out.println("DC plots uploaded");
              entry.addAttachment(data+"/ECAL_canvas0_"+tstamp+".png", "ECAL ADC occupancies");
              entry.addAttachment(data+"/ECAL_canvas1_"+tstamp+".png", "ECAL TDC occupancies");
              entry.addAttachment(data+"/ECAL_canvas2_"+tstamp+".png", "ECAL ADC histograms");
              entry.addAttachment(data+"/ECAL_canvas3_"+tstamp+".png", "ECAL FADC timing");
              entry.addAttachment(data+"/ECAL_canvas4_"+tstamp+".png", "ECAL TDC histograms");
              entry.addAttachment(data+"/ECAL_canvas5_"+tstamp+".png", "ECAL ADC sum");
              System.out.println("ECAL plots uploaded");
              //entry.addAttachment(data+"/Faraday Cup_canvas0_"+tstamp+".png", "Faraday Cup");
              //System.out.println("Farady Cup plots uploaded");
              entry.addAttachment(data+"/FMT_canvas0_"+tstamp+".png", "FMT occupancies 2D");
              entry.addAttachment(data+"/FMT_canvas1_"+tstamp+".png", "FMT Time of Max");
              entry.addAttachment(data+"/FMT_canvas2_"+tstamp+".png", "FMT occupancies 1D");
              entry.addAttachment(data+"/FMT_canvas3_"+tstamp+".png", "FMT Multiplicity");
              System.out.println("FMT plots uploaded");
              entry.addAttachment(data+"/FTCAL_canvas0_"+tstamp+".png", "FTCAL");
              System.out.println("FTCAL plot uploaded");
              entry.addAttachment(data+"/FTHODO_canvas0_"+tstamp+".png", "FTHODO FADC occupancies");
              entry.addAttachment(data+"/FTHODO_canvas1_"+tstamp+".png", "FTHODO FADC spectra");
              System.out.println("FTHODO plots uploaded");
              entry.addAttachment(data+"/FTOF_canvas0_"+tstamp+".png", "FTOF ADC occupancies");
              entry.addAttachment(data+"/FTOF_canvas1_"+tstamp+".png", "FTOF TDC occupancies");
              entry.addAttachment(data+"/FTOF_canvas2_"+tstamp+".png", "FTOF ADC histograms");
              entry.addAttachment(data+"/FTOF_canvas3_"+tstamp+".png", "FTOF FADC timing");
              entry.addAttachment(data+"/FTOF_canvas4_"+tstamp+".png", "FTOF TDC histograms");
              entry.addAttachment(data+"/FTOF_canvas5_"+tstamp+".png", "FTOF GMEAN");
              System.out.println("FTOF plots uploaded");
              entry.addAttachment(data+"/FTTRK_canvas0_"+tstamp+".png", "FTTRK occupancies 2D");
              entry.addAttachment(data+"/FTTRK_canvas1_"+tstamp+".png", "FTTRK occupancies 1D");
              entry.addAttachment(data+"/FTTRK_canvas2_"+tstamp+".png", "FTTRK average time maximum");
              entry.addAttachment(data+"/FTTRK_canvas3_"+tstamp+".png", "FTTRK ADC and time spectra");
              System.out.println("FTTRK plots uploaded");
              //entry.addAttachment(data+"/HEL_canvas0_"+tstamp+".png", "Helicity");
              //System.out.println("Helicity plot uploaded");
              entry.addAttachment(data+"/HTCC_canvas0_"+tstamp+".png", "HTCC occupancies");
              entry.addAttachment(data+"/HTCC_canvas1_"+tstamp+".png", "HTCC ADC spectra");
              entry.addAttachment(data+"/HTCC_canvas2_"+tstamp+".png", "HTCC FADC timing spectra");
              //entry.addAttachment(data+"/HTCC_canvas3_"+tstamp+".png", "HTCC TDC spectra");
              System.out.println("HTCC plots uploaded");
              entry.addAttachment(data+"/LTCC_canvas0_"+tstamp+".png", "LTCC ADC occupancies and spectra");
              entry.addAttachment(data+"/LTCC_canvas1_"+tstamp+".png", "LTCC FADC timing");
              //entry.addAttachment(data+"/LTCC_canvas2_"+tstamp+".png", "LTCC TDC occupancies and spectra");
              System.out.println("LTTC plots uploaded");
              System.out.println("RICH plot uploaded");
              entry.addAttachment(data+"/RICH_canvas0_"+tstamp+".png", "RICH occupancy");
              //entry.addAttachment(data+"/RECON_canvas0_"+tstamp+".png", "RECON CVT cosmic");
              //entry.addAttachment(data+"/RECON_canvas1_"+tstamp+".png", "RECON CVT positive tracks");
              //entry.addAttachment(data+"/RECON_canvas2_"+tstamp+".png", "RECON CVT negative tracks");
              //entry.addAttachment(data+"/RECON_canvas3_"+tstamp+".png", "RECON DC tracks per event");
              //entry.addAttachment(data+"/RECON_canvas4_"+tstamp+".png", "RECON DC hits per track");
              //entry.addAttachment(data+"/RECON_canvas5_"+tstamp+".png", "RECON DC momentum");
              //entry.addAttachment(data+"/RECON_canvas6_"+tstamp+".png", "RECON DC theta angle");
              //System.out.println("RECON plots uploaded");
              entry.addAttachment(data+"/RF_canvas0_"+tstamp+".png", "RF canvas 1");
              entry.addAttachment(data+"/RF_canvas1_"+tstamp+".png", "RF canvas 2");
              entry.addAttachment(data+"/RF_canvas2_"+tstamp+".png", "RF canvas 3");
              entry.addAttachment(data+"/RF_canvas3_"+tstamp+".png", "RF canvas 4");
              System.out.println("RF plots uploaded");
              entry.addAttachment(data+"/HEL_canvas0_"+tstamp+".png", "HEL canvas 1");
              entry.addAttachment(data+"/HEL_canvas1_"+tstamp+".png", "HEL canvas 2");
              System.out.println("HEL plots uploaded");
              entry.addAttachment(data+"/Trigger_canvas0_"+tstamp+".png", "Trigger bits");
              //entry.addAttachment(data+"/Trigger_canvas1_"+tstamp+".png", "Trigger EC peak");
              //entry.addAttachment(data+"/Trigger_canvas2_"+tstamp+".png", "Trigger EC cluster");
              //entry.addAttachment(data+"/Trigger_canvas3_"+tstamp+".png", "Trigger HTCC cluster");
              //entry.addAttachment(data+"/Trigger_canvas4_"+tstamp+".png", "Trigger FTOF cluster");
              System.out.println("Trigger plots uploaded");

              long lognumber = entry.submitNow();
              System.out.println("Successfully submitted log entry number: " + lognumber); 
            } catch(Exception exc){
                exc.printStackTrace(); 
                System.out.println( exc.getMessage());
            }
              
        }
        
        
        if(e.getActionCommand()=="Upload occupancy histos to the logbook") {   
                
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String tstamp = df.format(new Date());
            String data = outPath + "/output" + "/clas12mon_" + this.runNumber + "_" + tstamp;        
            File theDir = new File(data);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                boolean result = false;
                try{theDir.mkdir(); result = true;} 
                catch(SecurityException se){}        
                if(result){ System.out.println("Created directory: " + data);}
            }
            
            String fileName1 = data + "/summary_FD_"+tstamp+".png";
            System.out.println(fileName1);
            CLAS12Canvas.getCanvas("FD").save(fileName1);
            String fileName2 = data + "/summary_CD_"+tstamp+".png";
            System.out.println(fileName2);
            CLAS12Canvas.getCanvas("CD").save(fileName2);
            String fileName3 = data + "/summary_FT_"+tstamp+".png";
            System.out.println(fileName3);
            CLAS12Canvas.getCanvas("FT").save(fileName3);
            
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].printCanvas(data,tstamp);
            }
            
            LogEntry entry = new LogEntry("Occupancy online monitoring histograms for run number " + this.runNumber, "HBLOG");
            
            System.out.println("Starting to upload the occupancy plots");
            
            try{
              entry.addAttachment(data+"/summary_FD_"+tstamp+".png", "Summary plots for the forward detector");
              entry.addAttachment(data+"/summary_CD_"+tstamp+".png", "Summary plots for the central detector");
              entry.addAttachment(data+"/summary_FT_"+tstamp+".png", "Summary plots for the forward tagger");
              System.out.println("Summary plots uploaded");
              entry.addAttachment(data+"/BMT_canvas0_"+tstamp+".png", "BMT occupancies");
              entry.addAttachment(data+"/BMT_canvas1_"+tstamp+".png", "BMT occupancies c");
              entry.addAttachment(data+"/BMT_canvas2_"+tstamp+".png", "BMT occupancies z");
              System.out.println("BMT plots uploaded");
              entry.addAttachment(data+"/BST_canvas0_"+tstamp+".png", "BST occupancies 2D");
              entry.addAttachment(data+"/BST_canvas1_"+tstamp+".png", "BST occupancies 1D"); 
              System.out.println("BST plots uploaded");
              entry.addAttachment(data+"/CND_canvas0_"+tstamp+".png", "CND ADC occupancies and spectra");
              entry.addAttachment(data+"/CND_canvas1_"+tstamp+".png", "CND TDC occupancies and spectra");
              System.out.println("CND plots uploaded");
              entry.addAttachment(data+"/CTOF_canvas0_"+tstamp+".png", "CTOF ADC occupancies and spectra");
              entry.addAttachment(data+"/CTOF_canvas1_"+tstamp+".png", "CTOF TDC occupancies and spectra");
              System.out.println("CTOF plots uploaded");
              entry.addAttachment(data+"/DC_canvas0_"+tstamp+".png", "DC occupancies raw");
              entry.addAttachment(data+"/DC_canvas1_"+tstamp+".png", "DC occupancies normalized logarithmic scale");
              entry.addAttachment(data+"/DC_canvas2_"+tstamp+".png", "DC occupancies normalized linear scale");
              entry.addAttachment(data+"/DC_canvas3_"+tstamp+".png", "DC region occupancies");
              System.out.println("DC plots uploaded");
              entry.addAttachment(data+"/ECAL_canvas0_"+tstamp+".png", "ECAL ADC occupancies");
              entry.addAttachment(data+"/ECAL_canvas1_"+tstamp+".png", "ECAL TDC occupancies");
              System.out.println("ECAL plots uploaded");
              entry.addAttachment(data+"/FMT_canvas0_"+tstamp+".png", "FMT occupancies 2D");
              entry.addAttachment(data+"/FMT_canvas2_"+tstamp+".png", "FMT occupancies 1D");
              System.out.println("FMT plots uploaded");
              entry.addAttachment(data+"/FTCAL_canvas0_"+tstamp+".png", "FTCAL");
              System.out.println("FTCAL plot uploaded");
              entry.addAttachment(data+"/FTHODO_canvas0_"+tstamp+".png", "FTHODO FADC occupancies");
              System.out.println("FTHODO plot uploaded");
              entry.addAttachment(data+"/FTOF_canvas0_"+tstamp+".png", "FTOF ADC occupancies");
              entry.addAttachment(data+"/FTOF_canvas1_"+tstamp+".png", "FTOF TDC occupancies");
              System.out.println("FTOF plots uploaded");
              entry.addAttachment(data+"/FTTRK_canvas0_"+tstamp+".png", "FTTRK occupancies 2D");
              entry.addAttachment(data+"/FTTRK_canvas1_"+tstamp+".png", "FTTRK occupancies 1D");
              System.out.println("FTTRK plots uploaded");
              entry.addAttachment(data+"/HTCC_canvas0_"+tstamp+".png", "HTCC occupancies");
              System.out.println("HTCC plot uploaded");
              entry.addAttachment(data+"/LTCC_canvas0_"+tstamp+".png", "LTCC occupancies and spectra");
              System.out.println("LTCC plots uploaded");
              entry.addAttachment(data+"/RICH_canvas0_"+tstamp+".png", "RICH occupancy");
              System.out.println("RICH plot uploaded");
              entry.addAttachment(data+"/Trigger_canvas0_"+tstamp+".png", "Trigger bits");
              System.out.println("Trigger plots uploaded");
            
              long lognumber = entry.submitNow();
              System.out.println("Successfully submitted log entry number: " + lognumber); 
            } catch(Exception exc) {
                exc.printStackTrace(); 
                System.out.println( exc.getMessage());
            }
        }
         
         
         if (e.getActionCommand()=="Default for all"){
<<<<<<< HEAD
            for (int k=0;k<monitors.length;k++){
=======
            for (int k=0;k<3;k++){
>>>>>>> Monitoring-Expert/devel
                this.monitors[k].eventResetTime_current[k] = this.monitors[k].eventResetTime_default[k];
            }
        }
         
         if (e.getActionCommand()=="Disable histogram reset"){
<<<<<<< HEAD
            for (int k=0;k<monitors.length;k++){
=======
            for (int k=0;k<3;k++){
>>>>>>> Monitoring-Expert/devel
                this.monitors[k].eventResetTime_current[k] = 0;
            }
        }
        
        if ( e.getActionCommand().substring(0, 5).equals("Reset")){
            resetHistograms(e.getActionCommand());
        }
        
        
    }

    public void chooseUpdateInterval() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "GUI update interval (ms)",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "1000");
        if(s!=null){
            int time = 1000;
            try { 
                time= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if(time>0) {
                this.setCanvasUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }
        
    private JLabel getImage(String path,double scale) {
        JLabel label = null;
        Image image = null;
        try {
            URL url = new URL(path);
            image = ImageIO.read(url);
        } catch (IOException e) {
        	e.printStackTrace();
                System.out.println("Picture upload from " + path + " failed");
        }
        ImageIcon imageIcon = new ImageIcon(image);
        double width  = imageIcon.getIconWidth()*scale;
        double height = imageIcon.getIconHeight()*scale;
        imageIcon = new ImageIcon(image.getScaledInstance((int) width,(int) height, Image.SCALE_SMOOTH));
        label = new JLabel(imageIcon);
        return label;
    }
    
    public JPanel  getPanel(){
        return mainPanel;
    }
    
    public long getTriggerWord(DataEvent event) {    	
 	    DataBank bank = event.getBank("RUN::config");	        
        return bank.getLong("trigger", 0);
    }
    
    public long getTriggerPhase(DataEvent event) {    	
 	    DataBank bank = event.getBank("RUN::config");	        
        long timestamp = bank.getLong("timestamp",0);    
        int phase_offset = 1;
        return ((timestamp%6)+phase_offset)%6; // TI derived phase correction due to TDC and FADC clock differences 
    }
    
    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if(bank!=null) {
            rNum      = bank.getInt("run", 0);
        }
        return rNum;
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
       // EvioDataEvent decodedEvent = deco.DecodeEvent(event, decoder, table);
        //decodedEvent.show();
        		
        HipoDataEvent hipo = null;
        
	    if(event!=null ){
            //event.show();
<<<<<<< HEAD
            
            if(event instanceof EvioDataEvent){
             	hipo = (HipoDataEvent) clasDecoder.getDataEvent(event);
                DataBank   header = clasDecoder.createHeaderBank(hipo, this.ccdbRunNumber, 0, (float) 0, (float) 0);
                DataBank  trigger = clasDecoder.createTriggerBank(hipo);
                hipo.appendBanks(header);
                hipo.appendBank(trigger);
                if(this.runNumber != this.getRunNumber(hipo)) {
//                this.saveToFile("mon12_histo_run_" + runNumber + ".hipo");
                    this.runNumber = this.getRunNumber(hipo);
                    System.out.println("Setting run number to: " +this.runNumber);
                    resetEventListener();
                    this.clas12Textinfo.setText("\nrun number: "+this.runNumber + "\nmode:" + "\nfile:" + "\n");
=======
//
//            if (event.getType() == DataEventType.EVENT_START) {
//                this.runNumber = this.getRunNumber(event);
//                System.out.println("RunNumber: "+this.runNumber);
//            }
//            
//            if(this.runNumber != this.getRunNumber(event)) {
////                this.saveToFile("mon12_histo_run_" + runNumber + ".hipo");
//                this.runNumber = this.getRunNumber(event);
//                System.out.println("RunNumber: "+this.runNumber);
//                resetEventListener();
//            }
            
            if(event instanceof EvioDataEvent){
             	hipo = (HipoDataEvent) clasDecoder.getDataEvent(event);
//                DataBank   header = clasDecoder.createHeaderBank(hipo, 0, 0, (float) 0, (float) 0);
             	DataBank   header = clasDecoder.createHeaderBank(hipo, this.ccdbRunNumber, 0, (float) 0, (float) 0);
             	DataBank  trigger = clasDecoder.createTriggerBank(hipo);
             	hipo.appendBanks(header);
             	hipo.appendBank(trigger);
             	if(this.runNumber != this.getRunNumber(hipo)) {
//                this.saveToFile("mon12_histo_run_" + runNumber + ".hipo");
             	this.runNumber = this.getRunNumber(hipo);
             	System.out.println("Setting run number to: " +this.runNumber);
             	resetEventListener();
             	this.clas12Textinfo.setText("\nrun number: "+this.runNumber + "\nmode:" + "\nfile:" + "\n");
>>>>>>> Monitoring-Expert/devel
//                    this.clas12Textinfo.updateUI();
                }
            } 
            else {
                hipo = (HipoDataEvent) event;    
            }
            
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].setTriggerPhase(getTriggerPhase(hipo));
                this.monitors[k].setTriggerWord(getTriggerWord(hipo));   
                this.monitors[k].dataEventAction(hipo);
            }      
	}
    }

    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].readDataGroup(dir);
        }
        this.plotSummaries();
    }

    public void plotSummaries() {
        
        /////////////////////////////////////////////////
        /// FD:
        
        // DC
        this.CLAS12Canvas.getCanvas("FD").cd(0);
        //if(this.monitors[4].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[4].getDetectorSummary().getH1F("summary")); 
        // HTTC
        this.CLAS12Canvas.getCanvas("FD").cd(1);
        //if(this.monitors[11].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[11].getDetectorSummary().getH1F("summary"));
        // LTTC
        this.CLAS12Canvas.getCanvas("FD").cd(2);
        //if(this.monitors[12].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[12].getDetectorSummary().getH1F("summary"));
        // RICH
        this.CLAS12Canvas.getCanvas("FD").cd(3);
        this.CLAS12Canvas.getCanvas("FD").getPad(3).getAxisZ().setLog(true);
        //if(this.monitors[13].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[13].getDetectorSummary().getH2F("summary"));
        
        // ECAL 
        this.CLAS12Canvas.getCanvas("FD").cd(4);
        this.CLAS12Canvas.getCanvas("FD").getPad(4).getAxisZ().setLog(true);
        //if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[5].getDetectorSummary().getH2F("sumPCAL"));
        this.CLAS12Canvas.getCanvas("FD").cd(5);
        this.CLAS12Canvas.getCanvas("FD").getPad(5).getAxisZ().setLog(true);
        //if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[5].getDetectorSummary().getH2F("sumECin"));
   
        // FMT:
        this.CLAS12Canvas.getCanvas("FD").cd(6);
        this.CLAS12Canvas.getCanvas("FD").getPad(6).getAxisZ().setLog(true);
        //if(this.monitors[6].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[6].getDetectorSummary().getH2F("summary"));
        
        // FTOF:
        this.CLAS12Canvas.getCanvas("FD").cd(7);
        this.CLAS12Canvas.getCanvas("FD").getPad(7).getAxisZ().setLog(true);
<<<<<<< HEAD
        if(this.monitors[9].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[9].getDetectorSummary().getH1F("sum_p1"));
        this.CLAS12Canvas.getCanvas("FD").cd(8);
        this.CLAS12Canvas.getCanvas("FD").getPad(8).getAxisZ().setLog(true);
        if(this.monitors[9].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[9].getDetectorSummary().getH1F("sum_p2"));
=======
        //if(this.monitors[9].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[9].getDetectorSummary().getH2F("sum_p1"));
        this.CLAS12Canvas.getCanvas("FD").cd(8);
        this.CLAS12Canvas.getCanvas("FD").getPad(8).getAxisZ().setLog(true);
        //if(this.monitors[9].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FD").draw(this.monitors[9].getDetectorSummary().getH2F("sum_p2"));
>>>>>>> Monitoring-Expert/devel
        
        //////////////////////////////////////////////////
        ///  CD:
        
        // CND
        this.CLAS12Canvas.getCanvas("CD").cd(0);
        //if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors[2].getDetectorSummary().getH1F("summary"));
        // CTOF
        this.CLAS12Canvas.getCanvas("CD").cd(1);
        //if(this.monitors[3].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors[3].getDetectorSummary().getH1F("summary"));
        // BVT
        this.CLAS12Canvas.getCanvas("CD").cd(2);
        this.CLAS12Canvas.getCanvas("CD").getPad(2).getAxisZ().setLog(true);
        //if(this.monitors[0].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors[0].getDetectorSummary().getH2F("summary"));
        // BST
        this.CLAS12Canvas.getCanvas("CD").cd(3);
        this.CLAS12Canvas.getCanvas("CD").getPad(3).getAxisZ().setLog(true);
        //if(this.monitors[1].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("CD").draw(this.monitors[1].getDetectorSummary().getH2F("summary"));
        
        
        
        ///////////////////////////////////////////////////
        // FT:
        
        // FTCAL
        this.CLAS12Canvas.getCanvas("FT").cd(0);
        //if(this.monitors[7].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FT").draw(this.monitors[7].getDetectorSummary().getH1F("summary"));
        // FTHODO
        this.CLAS12Canvas.getCanvas("FT").cd(1);
        //if(this.monitors[8].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FT").draw(this.monitors[8].getDetectorSummary().getH1F("summary"));
        // FTTRK
        this.CLAS12Canvas.getCanvas("FT").cd(2);
        //if(this.monitors[0].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("FT").draw(this.monitors[10].getDetectorSummary().getH1F("summary"));
        
        ////////////////////////////////////////////////////
      
        
    }
    
    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String tstamp = df.format(new Date());
        String data = outPath + "/output" + "/clas12mon_" + this.runNumber + "_" + tstamp;        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        
        String fileName1 = data + "/summary_FD_"+tstamp+".png";
        System.out.println(fileName1);
        CLAS12Canvas.getCanvas("FD").save(fileName1);
        String fileName2 = data + "/summary_CD_"+tstamp+".png";
        System.out.println(fileName2);
        CLAS12Canvas.getCanvas("CD").save(fileName2);
        String fileName3 = data + "/summary_FT_"+tstamp+".png";
        System.out.println(fileName3);
        CLAS12Canvas.getCanvas("FT").save(fileName3);
        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].printCanvas(data,tstamp);
        }
        
        System.out.println("Histogram pngs succesfully saved in: " + data);
    }
       
    public void createHistoPDF() {
        /*
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String data = System.getProperty("user.dir") + "/output" + "/clas12mon_" + this.runNumber + "_" + df.format(new Date());        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        
        String fileName = data + "/clas12_canvas.pdf";
        System.out.println(fileName);
        
       // this.CLAS12Canvas.getCanvas("CLAS12-summary").save(fileName);
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].printCanvas(data);
        }
        */
    }
    
    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
    }
    
    @Override
    public void resetEventListener() {
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].resetEventListener();
            this.monitors[k].timerUpdate();
        }      
        this.plotSummaries();
    }
    
    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
        
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        this.CLAS12Canvas.getCanvas("FD").initTimer(time);
        this.CLAS12Canvas.getCanvas("FD").update();
        this.CLAS12Canvas.getCanvas("CD").initTimer(time);
        this.CLAS12Canvas.getCanvas("CD").update();
        this.CLAS12Canvas.getCanvas("FT").initTimer(time);
        this.CLAS12Canvas.getCanvas("FT").update();
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].setCanvasUpdate(time);
        }
    }

    public void stateChanged(ChangeEvent e) {
        this.timerUpdate();
    }
    
    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].timerUpdate();
        }
   }

    public static void main(String[] args){
        int xSize = 1600;
        int ySize = 1000;
        
        if(args.length>0){
            xSize = Integer.parseInt(args[0]);
            if(args.length>1){
                ySize = Integer.parseInt(args[1]);
            } else {
                ySize = (int) (xSize/1.6);
            }
        }
        JFrame frame = new JFrame("CLAS12Mon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EventViewer viewer = new EventViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(xSize, ySize);
        frame.setVisible(true);
        if (args.length>0){
        	if (args[0]!=null){
        		int numberOfSamples = Integer.parseInt(args[0]);
        		if (0<numberOfSamples && numberOfSamples<100){
        			viewer.monitors[0].setNumberOfSamples(numberOfSamples);
        			viewer.monitors[1].setNumberOfSamples(numberOfSamples);
        			
        			if (args[2]!=null){
                        int sparseReading = Integer.parseInt(args[3]);
                        if (0<sparseReading && sparseReading<100){
                        	viewer.monitors[0].setSparseReading(sparseReading);
                        	viewer.monitors[1].setSparseReading(sparseReading);
                		}
                        
                        viewer.pulseWindowBMT = new MVTpulseViewer("BMT", numberOfSamples, sparseReading);
                        viewer.pulseWindowFMT = new MVTpulseViewer("FMT", numberOfSamples, sparseReading);
                    }else{
                    	viewer.pulseWindowBMT = new MVTpulseViewer("BMT", numberOfSamples, 0);
                    	viewer.pulseWindowFMT = new MVTpulseViewer("FMT", numberOfSamples, 0);
                    }
        			
        			
        		}
        	}
        	if (args[1]!=null){
        		int numberOfSamples = Integer.parseInt(args[2]);
        		if (0<numberOfSamples && numberOfSamples<100){
        			viewer.monitors[2].setNumberOfSamples(numberOfSamples);
        		}
        		if (args[3]!=null){
                    int sparseReading = Integer.parseInt(args[4]);
                    if (0<sparseReading && sparseReading<100){
                    	viewer.monitors[2].setSparseReading(sparseReading);
            		}
                    
                    viewer.pulseWindowFTT = new MVTpulseViewer("FTT", numberOfSamples, sparseReading);
                }else{
                	viewer.pulseWindowFTT = new MVTpulseViewer("FTT", numberOfSamples, 0);
                }
        	}
        	
        }
        
    }
    
<<<<<<< HEAD
    
    private void setDCRange(String actionCommand) {
    
        System.out.println("Set normalized DC occuopancy range maximum");
        String  DC_scale = (String) JOptionPane.showInputDialog(null, "Set normalized DC occuopancy range maximum to ", " ", JOptionPane.PLAIN_MESSAGE, null, null, "15");
        
        if (DC_scale != null) { 
            double DC_scale_max= 0;
            try {DC_scale_max = Double.parseDouble(DC_scale);} 
            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
            if (DC_scale_max > 0){ this.monitors[4].max_occ = DC_scale_max;} 
            else {JOptionPane.showMessageDialog(null, "Value must be a positive number!");}   
        }
        
    }
    
    private void setRunNumber(String actionCommand) {
    
        System.out.println("Set run number for CCDB access");
        String  RUN_number = (String) JOptionPane.showInputDialog(null, "Set run number to ", " ", JOptionPane.PLAIN_MESSAGE, null, null, "2284");
        
        if (RUN_number != null) { 
            int cur_runNumber= this.runNumber;
=======
    private void setRunNumber(String actionCommand) {
        
    	System.out.println("Set run number for CCDB access");
    	String  RUN_number = (String) JOptionPane.showInputDialog(null, "Set run number to ", " ", JOptionPane.PLAIN_MESSAGE, null, null, "2284");
    	
    	if (RUN_number != null) { 
    		int cur_runNumber= this.runNumber;
>>>>>>> Monitoring-Expert/devel
            try {
                cur_runNumber = Integer.parseInt(RUN_number);
            } 
            catch (
                NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if (cur_runNumber > 0){ 
                this.ccdbRunNumber = cur_runNumber;
                clasDecoder.setRunNumber(cur_runNumber,true);
<<<<<<< HEAD
            } 
            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
        }
        
=======
            }
    		else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
    	}    
>>>>>>> Monitoring-Expert/devel
    }

    private void resetHistograms(String actionCommand) {
        
        
        if (actionCommand=="Reset BMT histograms"){
            System.out.println("Reset BMT histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset BMT plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[0].eventResetTime_default[0];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[0].eventResetTime_current[0] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[0].eventResetTime_current[0] = 0;
                    }	
         }
        
        if (actionCommand=="Reset BST histograms"){
            System.out.println("Reset BST histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset BST plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[1].eventResetTime_default[1];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[1].eventResetTime_current[1] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[1].eventResetTime_current[1] = 0;
                    }	
         }
        
        if (actionCommand=="Reset CND histograms"){
            System.out.println("Reset CND histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset CND plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[2].eventResetTime_default[2];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[2].eventResetTime_current[2] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[2].eventResetTime_current[2] = 0;
                    }	
         }
        
        if (actionCommand=="Reset CTOF histograms"){
            System.out.println("Reset CTOF histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset CTOF plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[3].eventResetTime_default[3];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[3].eventResetTime_current[3] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[3].eventResetTime_current[3] = 0;
                    }	
         }
        
        if (actionCommand=="Reset DC histograms"){
            System.out.println("Reset DC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset DC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[4].eventResetTime_default[4];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[4].eventResetTime_current[4] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[4].eventResetTime_current[4] = 0;
                    }	
         }
        
        if (actionCommand=="Reset ECAL histograms"){
            System.out.println("Reset ECAL histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset ECAL plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[5].eventResetTime_default[5];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[5].eventResetTime_current[5] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[5].eventResetTime_current[5] = 0;
                    }	
         }
        
        if (actionCommand=="Reset FMT histograms"){
            System.out.println("Reset FMT histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset FMT plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[1].eventResetTime_default[1];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[1].eventResetTime_current[1] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
                    	this.monitors[1].eventResetTime_current[1] = 0;
                    }	
         }
        
        if (actionCommand=="Reset FT histograms"){
            System.out.println("Reset FT histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset FT plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[2].eventResetTime_default[2];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {
                                this.monitors[2].eventResetTime_current[2] = time;
                            } 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
                    	this.monitors[2].eventResetTime_current[2] = 0;
                    }	
         }
        
        if (actionCommand=="Reset FTOF histograms"){
            System.out.println("Reset FTOF histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset FTOF plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[9].eventResetTime_default[9];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[9].eventResetTime_current[9] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[9].eventResetTime_current[9] = 0;
                    }	
         }
        
        if (actionCommand=="Reset HTTC histograms"){
            System.out.println("Reset HTTC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset HTTC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[11].eventResetTime_default[11];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[11].eventResetTime_current[11] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[11].eventResetTime_current[1] = 0;
                    }	
         }
        
        if (actionCommand=="Reset LTTC histograms"){
            System.out.println("Reset LTTC histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset LTTC plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[12].eventResetTime_default[12];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[12].eventResetTime_current[12] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[12].eventResetTime_current[12] = 0;
                    }	
         }
        
        if (actionCommand=="Reset RICH histograms"){
            System.out.println("Reset RICH histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RICH plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[13].eventResetTime_default[13];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[13].eventResetTime_current[13] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[13].eventResetTime_current[13] = 0;
                    }	
         }
        
        if (actionCommand=="Reset RECON histograms"){
            System.out.println("Reset RECON histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RECON plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[14].eventResetTime_default[14];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[14].eventResetTime_current[14] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[14].eventResetTime_current[14] = 0;
                    }	
         }
        
        if (actionCommand=="Reset RF histograms"){
            System.out.println("Reset RF histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset RF plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[15].eventResetTime_default[15];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[15].eventResetTime_current[15] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[15].eventResetTime_current[15] = 0;
                    }	
         }
        
        if (actionCommand=="Reset HEL histograms"){
            System.out.println("Reset HEL histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset HEL plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[16].eventResetTime_default[16];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[16].eventResetTime_current[16] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[16].eventResetTime_current[16] = 0;
                    }	
         }
        
        if (actionCommand=="Reset Faraday Cup histograms"){
            System.out.println("Reset Faraday Cup histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset Faraday Cup plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                            int time = this.monitors[17].eventResetTime_default[17];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[17].eventResetTime_current[17] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
 			this.monitors[17].eventResetTime_current[17] = 0;
                    }	
         }
        
        if (actionCommand=="Reset Trigger histograms"){
            System.out.println("Reset Trigger histograms");
        	int resetOption = JOptionPane.showConfirmDialog(null, "Do you want to automaticaly reset Trigger plots ?", " ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resetOption == JOptionPane.YES_OPTION) {
                        String  resetTiming = (String) JOptionPane.showInputDialog(null, "Update every (number of events)", " ", JOptionPane.PLAIN_MESSAGE, null, null, "10000");
                        if (resetTiming != null) {    
                        	int time = this.monitors[3].eventResetTime_default[3];
                            //int time = this.monitors[18].eventResetTime_default[18];
                            try {time = Integer.parseInt(resetTiming);} 
                            catch (NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}
                            if (time > 0) {this.monitors[3].eventResetTime_current[3] = time;} 
                            //if (time > 0) {this.monitors[18].eventResetTime_current[18] = time;} 
                            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
                        }
                    }else if (resetOption == JOptionPane.NO_OPTION){
            this.monitors[3].eventResetTime_current[3] = 0;
 			//this.monitors[18].eventResetTime_current[18] = 0;
                    }	
         }
         
    }

    
    
}