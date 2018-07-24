package org.clas.detectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.clas.viewer.DetectorMonitor;
import org.clas.viewer.EventViewer;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
//import org.jlab.rec.cvt.services.CVTReconstruction;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author guillaum
 */
public class FMTmonitor extends DetectorMonitor {
	
	/* ===== GEOMETRY CONSTANTS ===== */
	
	int numberOfLayers;
	int numberOfSectors;
	int numberOfStrips[];
	int maxNumberOfStrips;
	int numberOfChips[];
	int maxNumberOfChips;
	int numberOfStripsPerChip = 64;
	int isZ[];
	
	/* ===== DAQ CONSTANTS ===== */
	
	int  runNumber = 10;
	boolean mask[][][];
	short adcOffset;
	double sigmaThreshold;
	double noise = 10; /* To be added in CCDB */
	int samplingTime;
	int numberOfSamples = 16; /* To be added in CCDB */
	
	/* ===== DATA STORAGE & DISPLAY ===== */
	
	int numberOfHitsPerStrip[][][];
	int numberOfCentroidsPerStrip[][][];
	int numberOfCentroidsMatchedPerStrip[][][];
	int numberOfHitsPerDream[];
	
	int ratePlotScale=10;
	
	MVTpulseViewer pulseViewer;
	
	/* ===== RECONSTRUCTION ===== */
	
//	private CVTReconstruction recosmic;
	
	
	
	/**
	 * Main method
	 * @param name
	 */
	public FMTmonitor(String name, /* MVT PULSE DISPLAY */ MVTpulseViewer pulseViewer) {
		
		super(name);
		
		this.pulseViewer=pulseViewer;
		
		/* ===== LOAD GEOMETRY CONSTANTS ===== */
		
		DatabaseConstantProvider geometryConstants = new DatabaseConstantProvider(runNumber, "default");
		geometryConstants.loadTable("/geometry/fmt/fmt_layer");
		numberOfSectors = 1; /*geometryConstants.getInteger("/geometry/fmt/fmt_global/Nsector", 0)*/;
		numberOfLayers = geometryConstants.length("/geometry/fmt/fmt_layer/Layer");
		
		numberOfStrips = new int[numberOfLayers + 1];
		numberOfChips = new int[numberOfLayers + 1];
		isZ = new int[numberOfLayers +1];
		
		for (int layer = 1; layer <= numberOfLayers; layer++) {
			numberOfStrips[layer] = 1024/*geometryConstants.getInteger("/geometry/fmt/fmt_layer/Nstrip", 0 )*/;
			isZ[layer] = 1;/*geometryConstants.getInteger("/geometry/fmt/fmt_layer/Axis", (layer-1) );*/
			if (numberOfStrips[layer]>maxNumberOfStrips){maxNumberOfStrips=numberOfStrips[layer];}
			numberOfChips[layer] = numberOfStrips[layer]/numberOfStripsPerChip;
			maxNumberOfChips+=numberOfSectors*numberOfChips[layer];
		}
		
		/* ===== LOAD DAQ CONSTANTS ===== */
		
		List<String> keysFitter   = Arrays.asList(new String[]{"BMT","FMT"});
		List<String> tablesFitter = Arrays.asList(new String[]{"/daq/config/bmt","/daq/config/fmt"});
		ConstantsManager  fitterManager      = new ConstantsManager();
		fitterManager.init(keysFitter, tablesFitter);
		IndexedTable daqConstants = fitterManager.getConstants(runNumber, keysFitter.get(1));
		samplingTime = (byte) daqConstants.getDoubleValue("sampling_time", 0, 0, 0);
		adcOffset = (short) daqConstants.getDoubleValue("adc_offset", 0, 0, 0);
		sigmaThreshold = (short) daqConstants.getDoubleValue("adc_threshold", 0, 0, 0);
       
		mask = new boolean[numberOfSectors + 1][numberOfLayers + 1][maxNumberOfStrips + 1];
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				for (int component = 1 ; component <= numberOfStrips[layer]; component++){
					mask[sector][layer][component]=true;
				}
			}
		}
//		mask[1][1][80] = false;
//		mask[1][1][82] = false;
//		
//		mask[2][1][179] = false;
//		mask[2][1][192] = false;
//		mask[2][1][544] = false;
//		mask[2][1][545] = false;
//		
//		mask[3][4][757] = false;
//		
//		mask[3][5][102] = false; 
//		mask[3][5][735] = false;
		
		/* ===== INITIALIZE DATA STORAGE ===== */
		
		numberOfHitsPerStrip             = new int[numberOfSectors + 1][numberOfLayers + 1][maxNumberOfStrips+1];
		numberOfCentroidsPerStrip        = new int[numberOfSectors + 1][numberOfLayers + 1][maxNumberOfStrips+1];
		numberOfCentroidsMatchedPerStrip = new int[numberOfSectors + 1][numberOfLayers + 1][maxNumberOfStrips+1];
		numberOfHitsPerDream             = new int[maxNumberOfChips + 1];
		
		/* ===== RECONSTRUCTION ===== */
		
//		recosmic = new CVTCosmicsReconstruction();
//		recosmic.init();
		
		/* ===== DECLARE TABS ===== */
		
		this.setDetectorTabNames("Occupancies", "Occupancy", "Occupancy C", "Occupancy Z", "NbHits vs Time", "Tile Multiplicity", "Tile Occupancy", "MaxADC", "MaxADC vs Strip", "IntegralPulse", "IntegralPulse vs Strip", "TimeMax", "TimeMax vs Strip", "TimeMax per Dream", "ToT", "ToT per strip","FToT","FToT per strip", "OccupancyClusters", "NbClusters vs Time", "ClusterCharge", "ClusterCharge per strip", "ClusterSize", "ClusterSize per strip", "ClusterSize vs angle", "Residuals", "MaxAdcOfCentroid", "MaxAdcOfCentroid per strip", "TimeOfCentroid", "TimeOfCentroid per strip","hitMultiplicity");
		this.init(false);
	}

	/**
	 * Create histograms, define legends, fill colors
	 */
	@Override
	public void createHistos() {
		
		this.setNumberOfEvents(0);
		
		/* ===== CREATE OCCUPANCY HISTOS ===== */
		
		DataGroup occupancyGroup = new DataGroup("");
		this.getDataGroup().add(occupancyGroup, 0, 0, 0);
		
		H2F occupancyHisto = new H2F("Occupancies","Occupancies",maxNumberOfStrips, 0, maxNumberOfStrips, numberOfLayers*numberOfSectors,0,numberOfLayers*numberOfSectors);
		occupancyHisto.setTitleX("Strips");
		occupancyHisto.setTitleY("Detector");
		occupancyGroup.addDataSet(occupancyHisto, 0);
		
		H1F hitMultiplicityHisto = new H1F("hitMultiplicity", "hitMultiplicity", 300, 1., 301);
		hitMultiplicityHisto.setTitleX("Multiplicity");
		hitMultiplicityHisto.setTitleY("Nb events");
		occupancyGroup.addDataSet(hitMultiplicityHisto, 3);
		
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				H1F hitmapHisto = new H1F("Hitmap : Layer " + layer + " Sector " + sector, "Hitmap : Layer " + layer + " Sector " + sector,
						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				hitmapHisto.setTitleX("Strips (Layer " + layer  + " Sector " + sector+")");
				hitmapHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					hitmapHisto.setFillColor(4);
				}else{
					hitmapHisto.setFillColor(8);
				}
				occupancyGroup.addDataSet(hitmapHisto, 1);
				
				H1F hitsVSTimeHisto = new H1F("NbHits vs Time : Layer " + layer + " Sector " + sector, "NbHits vs Time : Layer " + layer + " Sector " + sector,
						100, 0, 100);
				hitsVSTimeHisto.setTitleX("Events (One bin is "+ratePlotScale+" events)");
				hitsVSTimeHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					hitsVSTimeHisto.setFillColor(4);
				}else{
					hitsVSTimeHisto.setFillColor(8);
				}
				occupancyGroup.addDataSet(hitsVSTimeHisto, 2);
				
				H1F multiplicityHisto = new H1F("Multiplicity : Layer " + layer + " Sector " + sector, "ADCMax :Layer " + layer + " Sector " + sector,
						300, 0, 300);
				multiplicityHisto.setTitleX("Multiplicity (Layer " + layer + " Sector " + sector+")");
				//multiplicityHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					multiplicityHisto.setFillColor(4);
				}else{
					multiplicityHisto.setFillColor(8);
				}
				occupancyGroup.addDataSet(multiplicityHisto, 4);
				
				H1F tileOccupancyHisto = new H1F("TileOccupancy : Layer " + layer + " Sector " + sector, "TileOccupancy : Layer " + layer + " Sector " + sector,
						100, 0., 25);
				tileOccupancyHisto.setTitleX("Tile occupancy");
				tileOccupancyHisto.setTitleY("NumberOfEvents");
				if (isZ[layer]==1){
					tileOccupancyHisto.setFillColor(4);
				}else{
					tileOccupancyHisto.setFillColor(8);
				}
				occupancyGroup.addDataSet(tileOccupancyHisto, 5);
			}
		}
		
		/* ===== CREATE ADC HISTOS ===== */
		
		DataGroup adcGroup = new DataGroup("adc");
		this.getDataGroup().add(adcGroup, 0, 0, 1);
		
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {				
				H1F adcMaxHisto = new H1F("ADCMax : Layer " + layer + " Sector " + sector, "ADCMax :Layer " + layer + " Sector " + sector,
						600, -1000, 5000);
				adcMaxHisto.setTitleX("ADC max (Layer " + layer + " Sector " + sector+")");
				adcMaxHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					adcMaxHisto.setFillColor(4);
				}else{
					adcMaxHisto.setFillColor(8);
				}
				adcGroup.addDataSet(adcMaxHisto, 0);
				
				
				H1F adcMaxVSStripHisto = new H1F("ADCMax vs Strip : Layer " + layer + " Sector " + sector, "ADCMax per strip : Layer " + layer + " Sector " + sector,
						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				adcMaxVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				adcMaxVSStripHisto.setTitleY("Integral of pulse per strip");
				if (isZ[layer]==1){
					adcMaxVSStripHisto.setFillColor(4);
				}else{
					adcMaxVSStripHisto.setFillColor(8);
				}
				adcGroup.addDataSet(adcMaxVSStripHisto, 1);
				
//				H1F adcMaxVSStripHisto = new H1F("ADCMax vs Strip : Layer " + layer + " Sector " + sector, "IntegralPulse per strip : Layer " + layer + " Sector " + sector,
//						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
//				adcMaxVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
//				adcMaxVSStripHisto.setTitleY("Integral of pulse per strip");
//				if (isZ[layer]==1){
//					adcMaxVSStripHisto.setFillColor(4);
//				}else{
//					adcMaxVSStripHisto.setFillColor(8);
//				}
//				adcGroup.addDataSet(adcMaxVSStripHisto, 1);
				
				H1F adcIntegralHisto = new H1F("IntegralPulse : Layer " + layer + " Sector " + sector, "IntegralPulse : Layer " + layer + " Sector " + sector,
						2000, 1., 20000);
				adcIntegralHisto.setTitleX("Integral of pulse (Layer " + layer + " Sector " + sector+")");
				adcIntegralHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					adcIntegralHisto.setFillColor(4);
				}else{
					adcIntegralHisto.setFillColor(8);
				}
				adcGroup.addDataSet(adcIntegralHisto, 2);
				
				H1F adcIntegralVSStripHisto = new H1F("IntegralPulse vs Strip : Layer " + layer + " Sector " + sector, "IntegralPulse per strip : Layer " + layer + " Sector " + sector,
						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				adcIntegralVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				adcIntegralVSStripHisto.setTitleY("Integral of pulse per strip");
				if (isZ[layer]==1){
					adcIntegralVSStripHisto.setFillColor(4);
				}else{
					adcIntegralVSStripHisto.setFillColor(8);
				}
				adcGroup.addDataSet(adcIntegralVSStripHisto, 3);
			}
		}
		
		/* ===== CREATE TIME HISTOS ===== */
		
		DataGroup timeGroup = new DataGroup("time");
		this.getDataGroup().add(timeGroup, 0,0,2);
		
		H1F timeOfMaxHisto = new H1F("TimeOfMax","TimeOfMax",maxNumberOfChips,0,maxNumberOfChips);
		timeOfMaxHisto.setTitleX("Electronic chip");
		timeOfMaxHisto.setTitleY("Time of max adc");
		timeOfMaxHisto.setFillColor(4);
		timeGroup.addDataSet(timeOfMaxHisto, 0);
	
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				H1F timeMaxHisto = new H1F("TimeOfMax : Layer " + layer + " Sector " + sector, "TimeOfMax : Layer " + layer + " Sector " + sector,
						samplingTime*(numberOfSamples+1), 1.,samplingTime*(numberOfSamples+1) );
				timeMaxHisto.setTitleX("Time of max (Layer " + layer + " Sector " + sector+")");
				timeMaxHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					timeMaxHisto.setFillColor(4);
				}else{
					timeMaxHisto.setFillColor(8);
				}
				timeGroup.addDataSet(timeMaxHisto, 1);
				
//				H1F timeMaxVSStripHisto = new H1F("TimeOfMax vs Strip : Layer " + layer + " Sector " + sector, "TimeOfMax vs Strip : Layer " + layer + " Sector " + sector,
//						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
//				timeMaxVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
//				timeMaxVSStripHisto.setTitleY("Time of max per strip");
//				if (isZ[layer]==1){
//					timeMaxVSStripHisto.setFillColor(4);
//				}else{
//					timeMaxVSStripHisto.setFillColor(8);
//				}
//				timeGroup.addDataSet(timeMaxVSStripHisto, 2);
				
				H1F timeMaxVSStripHisto = new H1F("TimeOfMax vs Strip : Layer " + layer + " Sector " + sector, "TimeOfMax vs Strip : Layer " + layer + " Sector " + sector,
						1024, 1., 1024);
				timeMaxVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				timeMaxVSStripHisto.setTitleY("Time of max per strip");
				if (isZ[layer]==1){
					timeMaxVSStripHisto.setFillColor(4);
				}else{
					timeMaxVSStripHisto.setFillColor(8);
				}
				timeGroup.addDataSet(timeMaxVSStripHisto, 2);
				
				H1F totHisto = new H1F("ToT : Layer " + layer + " Sector " + sector, "ToT : Layer " + layer + " Sector " + sector,
						numberOfSamples-1, 0.,numberOfSamples-1 );
				totHisto.setTitleX("ToT (Layer " + layer + " Sector " + sector+")");
				totHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					totHisto.setFillColor(4);
				}else{
					totHisto.setFillColor(8);
				}
				timeGroup.addDataSet(totHisto, 3);
				
				H1F totPerStripHisto = new H1F("ToT per strip : Layer " + layer + " Sector " + sector, "ToT per strip : Layer " + layer + " Sector " + sector,
						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				totPerStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				totPerStripHisto.setTitleY("Time over threshold");
				if (isZ[layer]==1){
					totPerStripHisto.setFillColor(4);
				}else{
					totPerStripHisto.setFillColor(8);
				}
				timeGroup.addDataSet(totPerStripHisto, 4);				
				
				H1F ftotHisto = new H1F("FToT : Layer " + layer + " Sector " + sector, "FToT : Layer " + layer + " Sector " + sector,
						numberOfSamples-1, 0.,numberOfSamples-1 );
				ftotHisto.setTitleX("Ftot (Layer " + layer + " Sector " + sector+")");
				ftotHisto.setTitleY("Nb hits");
				if (isZ[layer]==1){
					ftotHisto.setFillColor(4);
				}else{
					ftotHisto.setFillColor(8);
				}
				timeGroup.addDataSet(ftotHisto, 5);
				
				H1F ftotPerStripHisto = new H1F("FToT per strip : Layer " + layer + " Sector " + sector, "FToT per strip : Layer " + layer + " Sector " + sector,
						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				ftotPerStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				ftotPerStripHisto.setTitleY("First time over threshold");
				if (isZ[layer]==1){
					ftotPerStripHisto.setFillColor(4);
				}else{
					ftotPerStripHisto.setFillColor(8);
				}
				timeGroup.addDataSet(ftotPerStripHisto, 6);
			}
		}

		/* ===== CREATE RECO HISTOS ===== */

		DataGroup effGroup = new DataGroup("efficiency");
		this.getDataGroup().add(effGroup, 0, 0, 3);

		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				H1F residualsHisto = new H1F("Residuals : Layer " + layer + " Sector " + sector,
						"Residuals : Layer " + layer + " Sector " + sector, 100, -100.,100.);
				residualsHisto.setTitleX("Residuals (Layer " + layer + " Sector " + sector + ")");
				residualsHisto.setTitleY("Nb hits");
				if (isZ[layer]==1) {
					residualsHisto.setFillColor(4);
				} else {
					residualsHisto.setFillColor(8);
				}
				effGroup.addDataSet(residualsHisto, 0);

//				H1F residualCenterVSStripHisto = new H1F("Residuals center vs Strip : Layer " + layer + " Sector " + sector,
//						"Residuals center vs Strip : Layer " + layer + " Sector " + sector, (numberStrips[layer]), 1.,
//						(double) (numberStrips[layer]) + 1);
//				residualCenterVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector + ")");
//				residualCenterVSStripHisto.setTitleY("Residuals center");
//				if (isZ[layer]==1) {
//					residualCenterVSStripHisto.setFillColor(4);
//				} else {
//					residualCenterVSStripHisto.setFillColor(8);
//				}
//				effGroup.addDataSet(residualCenterVSStripHisto, 1);
				
//				H1F residualWidthVSStripHisto = new H1F("Residuals width vs Strip : Layer " + layer + " Sector " + sector,
//						"Residuals width vs Strip : Layer " + layer + " Sector " + sector, (numberStrips[layer]), 1.,
//						(double) (numberStrips[layer]) + 1);
//				residualWidthVSStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector + ")");
//				residualWidthVSStripHisto.setTitleY("Residuals width");
//				if (isZ[layer]==1) {
//					residualWidthVSStripHisto.setFillColor(4);
//				} else {
//					residualWidthVSStripHisto.setFillColor(8);
//				}
//				effGroup.addDataSet(residualWidthVSStripHisto, 2);
			}
		}
		
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				
				H1F hitmapHisto = new H1F("HitmapClusters : Layer " + layer + " Sector " + sector, "HitmapClusters : Layer " + layer + " Sector " + sector,
						(numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				hitmapHisto.setTitleX("Strips (Layer " + layer  + " Sector " + sector+")");
				hitmapHisto.setTitleY("Nb clusters");
				if (isZ[layer]==1){
					hitmapHisto.setFillColor(4);
				}else{
					hitmapHisto.setFillColor(8);
				}
				occupancyGroup.addDataSet(hitmapHisto, 3);
				
				
				H1F hitsVSTimeHisto = new H1F("NbClusters vs Time : Layer " + layer + " Sector " + sector, "NbClusters vs Time : Layer " + layer + " Sector " + sector,
						100, 0, 100);
				hitsVSTimeHisto.setTitleX("Time (Layer " + layer  + " Sector " + sector+")");
				hitsVSTimeHisto.setTitleY("Nb clusters");
				if (isZ[layer]==1){
					hitsVSTimeHisto.setFillColor(4);
				}else{
					hitsVSTimeHisto.setFillColor(8);
				}
				occupancyGroup.addDataSet(hitsVSTimeHisto, 4);
				
				
				H1F clusterChargeHisto = new H1F("ClusterCharge : Layer " + layer + " Sector " + sector,
						"ClusterCharge : Layer " + layer + " Sector " + sector, 6000, -1000,5000.);
				clusterChargeHisto.setTitleX("ClusterCharge (Layer " + layer + " Sector " + sector + ")");
				clusterChargeHisto.setTitleY("Nb hits");
				if (isZ[layer]==1) {
					clusterChargeHisto.setFillColor(4);
				} else {
					clusterChargeHisto.setFillColor(8);
				}
				adcGroup.addDataSet(clusterChargeHisto, 4);
				
				H1F clusterChargePerStripHisto = new H1F("ClusterCharge per strip : Layer " + layer + " Sector " + sector,
						"ClusterCharge per strip : Layer " + layer + " Sector " + sector, (numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				clusterChargePerStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				clusterChargePerStripHisto.setTitleY("Cluster Charge");
				if (isZ[layer]==1) {
					clusterChargePerStripHisto.setFillColor(4);
				} else {
					clusterChargePerStripHisto.setFillColor(8);
				}
				adcGroup.addDataSet(clusterChargePerStripHisto, 5);
				
				H1F clusterSizeHisto = new H1F("ClusterSize : Layer " + layer + " Sector " + sector,
						"ClusterSize : Layer " + layer + " Sector " + sector, 100, 0,100.);
				clusterSizeHisto.setTitleX("ClusterSize (Layer " + layer + " Sector " + sector + ")");
				clusterSizeHisto.setTitleY("Nb hits");
				if (isZ[layer]==1) {
					clusterSizeHisto.setFillColor(4);
				} else {
					clusterSizeHisto.setFillColor(8);
				}
				adcGroup.addDataSet(clusterSizeHisto, 6);
				
				H1F clusterSizePerStripHisto = new H1F("ClusterSize per strip : Layer " + layer + " Sector " + sector,
						"ClusterSize per strip : Layer " + layer + " Sector " + sector, (numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				clusterSizePerStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				clusterSizePerStripHisto.setTitleY("Cluster Size");
				if (isZ[layer]==1) {
					clusterSizePerStripHisto.setFillColor(4);
				} else {
					clusterSizePerStripHisto.setFillColor(8);
				}
				adcGroup.addDataSet(clusterSizePerStripHisto, 7);
				
				H1F clusterSizeVsAngleHisto = new H1F("ClusterSize vs angle : Layer " + layer + " Sector " + sector,
						"ClusterSize vs angle : Layer " + layer + " Sector " + sector, 360, 0, 360);
				clusterSizeVsAngleHisto.setTitleX("Angle (Layer " + layer + " Sector " + sector+")");
				clusterSizeVsAngleHisto.setTitleY("Cluster Size");
				if (isZ[layer]==1) {
					clusterSizeVsAngleHisto.setFillColor(4);
				} else {
					clusterSizeVsAngleHisto.setFillColor(8);
				}
				adcGroup.addDataSet(clusterSizeVsAngleHisto, 8);
				
				H1F maxAdcCentroidHisto = new H1F("MaxAdcOfCentroid : Layer " + layer + " Sector " + sector,
						"MaxAdcOfCentroid : Layer " + layer + " Sector " + sector, 600, -1000,5000.);
				maxAdcCentroidHisto.setTitleX("MaxAdcOfCentroid (Layer " + layer + " Sector " + sector + ")");
				maxAdcCentroidHisto.setTitleY("Nb hits");
				if (isZ[layer]==1) {
					maxAdcCentroidHisto.setFillColor(4);
				} else {
					maxAdcCentroidHisto.setFillColor(8);
				}
				adcGroup.addDataSet(maxAdcCentroidHisto, 9);
				
				H1F maxAdcCentroidHistoPerStripHisto = new H1F("MaxAdcOfCentroid per strip : Layer " + layer + " Sector " + sector,
						"MaxAdcOfCentroid per strip : Layer " + layer + " Sector " + sector, (numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				maxAdcCentroidHistoPerStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				maxAdcCentroidHistoPerStripHisto.setTitleY("Adc");
				if (isZ[layer]==1) {
					maxAdcCentroidHistoPerStripHisto.setFillColor(4);
				} else {
					maxAdcCentroidHistoPerStripHisto.setFillColor(8);
				}
				adcGroup.addDataSet(maxAdcCentroidHistoPerStripHisto, 10);
				
				H1F timeCentroidHisto = new H1F("TimeOfCentroid : Layer " + layer + " Sector " + sector,
						"TimeOfCentroid : Layer " + layer + " Sector " + sector, samplingTime*(numberOfSamples+1), 1.,samplingTime*(numberOfSamples+1));
				timeCentroidHisto.setTitleX("TimeOfCentroid (Layer " + layer + " Sector " + sector + ")");
				timeCentroidHisto.setTitleY("Nb hits");
				if (isZ[layer]==1) {
					timeCentroidHisto.setFillColor(4);
				} else {
					timeCentroidHisto.setFillColor(8);
				}
				timeGroup.addDataSet(timeCentroidHisto, 7);
				
				H1F timeCentroidHistoPerStripHisto = new H1F("TimeOfCentroid per strip : Layer " + layer + " Sector " + sector,
						"TimeOfCentroid per strip : Layer " + layer + " Sector " + sector, (numberOfStrips[layer]), 1., (double) (numberOfStrips[layer])+1);
				timeCentroidHistoPerStripHisto.setTitleX("Strips (Layer " + layer + " Sector " + sector+")");
				timeCentroidHistoPerStripHisto.setTitleY("TimeOfMax");
				if (isZ[layer]==1) {
					timeCentroidHistoPerStripHisto.setFillColor(4);
				} else {
					timeCentroidHistoPerStripHisto.setFillColor(8);
				}
				timeGroup.addDataSet(timeCentroidHistoPerStripHisto, 8);
				
			}
		}
		//pulseHistoFMT = new H1F("Pulse","Pulse", numberOfSamples, 1., numberOfSamples+1);
	}
	
	/**
	 * Divide canvas and draw histograms
	 */
	@Override
	public void plotHistos() {
		
		// initialize canvas and plot histograms
		this.getDetectorCanvas().getCanvas("Occupancies").setGridX(false);
		this.getDetectorCanvas().getCanvas("Occupancies").setGridY(false);
		this.getDetectorCanvas().getCanvas("Occupancies").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Occupancies").setAxisLabelSize(12);
		this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0, 0, 0).getH2F("Occupancies"));
		this.getDetectorCanvas().getCanvas("Occupancies").update();
		
		this.getDetectorCanvas().getCanvas("Occupancy").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("Occupancy").setGridX(false);
		this.getDetectorCanvas().getCanvas("Occupancy").setGridY(false);
		this.getDetectorCanvas().getCanvas("Occupancy").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Occupancy").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("Occupancy C").divide(numberOfSectors, numberOfLayers/2);
		this.getDetectorCanvas().getCanvas("Occupancy C").setGridX(false);
		this.getDetectorCanvas().getCanvas("Occupancy C").setGridY(false);
		this.getDetectorCanvas().getCanvas("Occupancy C").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Occupancy C").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("Occupancy Z").divide(numberOfSectors, numberOfLayers/2);
		this.getDetectorCanvas().getCanvas("Occupancy Z").setGridX(false);
		this.getDetectorCanvas().getCanvas("Occupancy Z").setGridY(false);
		this.getDetectorCanvas().getCanvas("Occupancy Z").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Occupancy Z").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("NbHits vs Time").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("NbHits vs Time").setGridX(false);
		this.getDetectorCanvas().getCanvas("NbHits vs Time").setGridY(false);
		this.getDetectorCanvas().getCanvas("NbHits vs Time").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("NbHits vs Time").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("hitMultiplicity").setGridX(false);
		this.getDetectorCanvas().getCanvas("hitMultiplicity").setGridY(false);
		this.getDetectorCanvas().getCanvas("hitMultiplicity").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("hitMultiplicity").setAxisLabelSize(12);
		this.getDetectorCanvas().getCanvas("hitMultiplicity").draw(this.getDataGroup().getItem(0, 0, 0).getH1F("hitMultiplicity"));
		this.getDetectorCanvas().getCanvas("hitMultiplicity").update();
		
		this.getDetectorCanvas().getCanvas("Tile Multiplicity").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("Tile Multiplicity").setGridX(false);
		this.getDetectorCanvas().getCanvas("Tile Multiplicity").setGridY(false);
		this.getDetectorCanvas().getCanvas("Tile Multiplicity").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Tile Multiplicity").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("Tile Occupancy").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("Tile Occupancy").setGridX(false);
		this.getDetectorCanvas().getCanvas("Tile Occupancy").setGridY(false);
		this.getDetectorCanvas().getCanvas("Tile Occupancy").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Tile Occupancy").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("MaxADC").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("MaxADC").setGridX(false);
		this.getDetectorCanvas().getCanvas("MaxADC").setGridY(false);
		this.getDetectorCanvas().getCanvas("MaxADC").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("MaxADC").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("MaxADC vs Strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("MaxADC vs Strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("MaxADC vs Strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("MaxADC vs Strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("MaxADC vs Strip").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("IntegralPulse").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("IntegralPulse").setGridX(false);
		this.getDetectorCanvas().getCanvas("IntegralPulse").setGridY(false);
		this.getDetectorCanvas().getCanvas("IntegralPulse").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("IntegralPulse").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("TimeMax").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("TimeMax").setGridX(false);
		this.getDetectorCanvas().getCanvas("TimeMax").setGridY(false);
		this.getDetectorCanvas().getCanvas("TimeMax").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("TimeMax").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("TimeMax vs Strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("TimeMax vs Strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("TimeMax vs Strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("TimeMax vs Strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("TimeMax vs Strip").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("TimeMax per Dream").setGridX(false);
		this.getDetectorCanvas().getCanvas("TimeMax per Dream").setGridY(false);
		this.getDetectorCanvas().getCanvas("TimeMax per Dream").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("TimeMax per Dream").setAxisLabelSize(12);
		this.getDetectorCanvas().getCanvas("TimeMax per Dream").draw(this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax"));
		this.getDetectorCanvas().getCanvas("TimeMax per Dream").update();
		
		this.getDetectorCanvas().getCanvas("ToT").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("ToT").setGridX(false);
		this.getDetectorCanvas().getCanvas("ToT").setGridY(false);
		this.getDetectorCanvas().getCanvas("ToT").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("ToT").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("ToT per strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("ToT per strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("ToT per strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("ToT per strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("ToT per strip").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("FToT").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("FToT").setGridX(false);
		this.getDetectorCanvas().getCanvas("FToT").setGridY(false);
		this.getDetectorCanvas().getCanvas("FToT").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("FToT").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("FToT per strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("FToT per strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("FToT per strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("FToT per strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("FToT per strip").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("OccupancyClusters").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("OccupancyClusters").setGridX(false);
		this.getDetectorCanvas().getCanvas("OccupancyClusters").setGridY(false);
		this.getDetectorCanvas().getCanvas("OccupancyClusters").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("OccupancyClusters").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("NbClusters vs Time").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("NbClusters vs Time").setGridX(false);
		this.getDetectorCanvas().getCanvas("NbClusters vs Time").setGridY(false);
		this.getDetectorCanvas().getCanvas("NbClusters vs Time").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("NbClusters vs Time").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("ClusterCharge").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("ClusterCharge").setGridX(false);
		this.getDetectorCanvas().getCanvas("ClusterCharge").setGridY(false);
		this.getDetectorCanvas().getCanvas("ClusterCharge").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("ClusterCharge").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("ClusterCharge per strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("ClusterCharge per strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("ClusterCharge per strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("ClusterCharge per strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("ClusterCharge per strip").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("ClusterSize").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("ClusterSize").setGridX(false);
		this.getDetectorCanvas().getCanvas("ClusterSize").setGridY(false);
		this.getDetectorCanvas().getCanvas("ClusterSize").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("ClusterSize").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("ClusterSize per strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("ClusterSize per strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("ClusterSize per strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("ClusterSize per strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("ClusterSize per strip").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("Residuals").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("Residuals").setGridX(false);
		this.getDetectorCanvas().getCanvas("Residuals").setGridY(false);
		this.getDetectorCanvas().getCanvas("Residuals").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("Residuals").setAxisLabelSize(12);
		
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").setGridX(false);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").setGridY(false);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("TimeOfCentroid").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid").setGridX(false);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid").setGridY(false);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid").setAxisLabelSize(12);

		this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").divide(numberOfSectors, numberOfLayers);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").setGridX(false);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").setGridY(false);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").setAxisTitleSize(12);
		this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").setAxisLabelSize(12);
		
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				int column=numberOfSectors - sector;
				int row;
				int numberOfColumns=numberOfSectors;
				
				switch (layer) {
				case 1: row=2; break;
				case 2: row=2; break;
				case 3: row=1; break;
				case 4: row=1; break;
				case 5: row=0; break;
				case 6: row=0; break;
				default:row=-1;break;
				}
				if (isZ[layer]==1){
					this.getDetectorCanvas().getCanvas("Occupancy Z").cd(column + numberOfColumns * row);
					this.getDetectorCanvas().getCanvas("Occupancy Z").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
				}else{
					this.getDetectorCanvas().getCanvas("Occupancy C").cd(column + numberOfColumns * row);
					this.getDetectorCanvas().getCanvas("Occupancy C").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
				}
				
				switch (layer) {
				
				case 1: row=5; break;
				case 2: row=4; break;
				case 3: row=3; break;
				case 4: row=2; break;
				case 5: row=1; break;
				case 6: row=0; break;
				default:row=-1;break;
				}
//				this.getDetectorCanvas().getCanvas("Occupancies").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("Occupancies").draw(
//						this.getDataGroup().getItem(sector, layer, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
//				
				
				this.getDetectorCanvas().getCanvas("Occupancy").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("Occupancy").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("NbHits vs Time").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("NbHits vs Time").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("Tile Multiplicity").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("Tile Multiplicity").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("Multiplicity : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("Tile Occupancy").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("Tile Occupancy").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("TileOccupancy : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("MaxADC").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("MaxADC").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("MaxADC vs Strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("MaxADC vs Strip").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax vs Strip : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("IntegralPulse").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("IntegralPulse").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse vs Strip : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("TimeMax").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("TimeMax").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("TimeMax vs Strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("TimeMax vs Strip").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax vs Strip : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("ToT").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("ToT").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("ToT : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("ToT per strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("ToT per strip").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("ToT per strip : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("FToT").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("FToT").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("FToT : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("FToT per strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("FToT per strip").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("FToT per strip : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("OccupancyClusters").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("OccupancyClusters").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("HitmapClusters : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("NbClusters vs Time").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("NbClusters vs Time").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("NbClusters vs Time : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("ClusterCharge").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("ClusterCharge").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterCharge : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("ClusterCharge per strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("ClusterCharge per strip").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterCharge per strip : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("ClusterSize").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("ClusterSize").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterSize : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("ClusterSize per strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("ClusterSize per strip").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterSize per strip : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("Residuals").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("Residuals").draw(
						this.getDataGroup().getItem(0, 0, 3).getH1F("Residuals : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("MaxAdcOfCentroid : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").draw(
						this.getDataGroup().getItem(0, 0, 1).getH1F("MaxAdcOfCentroid per strip : Layer " + layer + " Sector " + sector));

				this.getDetectorCanvas().getCanvas("TimeOfCentroid").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("TimeOfCentroid").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfCentroid : Layer " + layer + " Sector " + sector));
				
				this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").draw(
						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfCentroid per strip : Layer " + layer + " Sector " + sector));
				
			}
			this.getDetectorCanvas().getCanvas("Occupancies").update();
			this.getDetectorCanvas().getCanvas("Occupancy").update();
			this.getDetectorCanvas().getCanvas("Occupancy C").update();
			this.getDetectorCanvas().getCanvas("Occupancy Z").update();
			this.getDetectorCanvas().getCanvas("NbHits vs Time").update();
			this.getDetectorCanvas().getCanvas("Tile Multiplicity").update();
			this.getDetectorCanvas().getCanvas("Tile Occupancy").update();
			
			this.getDetectorCanvas().getCanvas("MaxADC").update();
			this.getDetectorCanvas().getCanvas("MaxADC vs Strip").update();
			this.getDetectorCanvas().getCanvas("IntegralPulse").update();
			this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").update();
			
			this.getDetectorCanvas().getCanvas("TimeMax").update();
			this.getDetectorCanvas().getCanvas("TimeMax vs Strip").update();
			this.getDetectorCanvas().getCanvas("ToT").update();
			this.getDetectorCanvas().getCanvas("ToT per strip").update();
			this.getDetectorCanvas().getCanvas("FToT").update();
			this.getDetectorCanvas().getCanvas("FToT per strip").update();
			
			this.getDetectorCanvas().getCanvas("OccupancyClusters").update();
			this.getDetectorCanvas().getCanvas("NbClusters vs Time").update();
			this.getDetectorCanvas().getCanvas("ClusterCharge").update();
			this.getDetectorCanvas().getCanvas("ClusterCharge per strip").update();
			this.getDetectorCanvas().getCanvas("ClusterSize").update();
			this.getDetectorCanvas().getCanvas("ClusterSize per strip").update();
			this.getDetectorCanvas().getCanvas("Residuals").update();
			this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").update();
			this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").update();
			this.getDetectorCanvas().getCanvas("TimeOfCentroid").update();
			this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").update();
		}
	}
	
	public void updateHistoChanges() {
		for (int sector = 1; sector <= numberOfSectors; sector++) {
			for (int layer = 1; layer <= numberOfLayers; layer++) {
				int column=numberOfSectors - sector;
				int row;
				int numberOfColumns=numberOfSectors;
				
				switch (layer) {
				case 1: row=1; break;
				case 2: row=2; break;
				case 3: row=3; break;
				case 4: row=4; break;
				case 5: row=5; break;
				case 6: row=6; break;
				default:row=-1;break;
				}
//				if (isZ[layer]==1){
//					this.getDetectorCanvas().getCanvas("Occupancy Z").cd(column + numberOfColumns * row);
//					this.getDetectorCanvas().getCanvas("Occupancy Z").draw(
//						this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
//				}else{
//					this.getDetectorCanvas().getCanvas("Occupancy C").cd(column + numberOfColumns * row);
//					this.getDetectorCanvas().getCanvas("Occupancy C").draw(
//						this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
//				}
				
				switch (layer) {
				case 1: row=5; break;
				case 2: row=2; break;
				case 3: row=1; break;
				case 4: row=4; break;
				case 5: row=0; break;
				case 6: row=3; break;
				default:row=-1;break;
				}
////				this.getDetectorCanvas().getCanvas("Occupancies").cd(column + numberOfColumns * row);
////				this.getDetectorCanvas().getCanvas("Occupancies").draw(
////						this.getDataGroup().getItem(sector, layer, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
////				
//				this.getDetectorCanvas().getCanvas("Occupancy").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("Occupancy").draw(
//						this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector));
//
				this.getDetectorCanvas().getCanvas("NbHits vs Time").cd(column + numberOfColumns * row);
				this.getDetectorCanvas().getCanvas("NbHits vs Time").draw(
						this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("MaxADC").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("MaxADC").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("MaxADC vs Strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("MaxADC vs Strip").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax vs Strip : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("IntegralPulse").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("IntegralPulse").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse vs Strip : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("TimeMax").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("TimeMax").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("TimeMax vs Strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("TimeMax vs Strip").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax vs Strip : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("ToT").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("ToT").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("ToT : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("ToT per strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("ToT per strip").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("ToT per strip : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("FToT").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("FToT").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("FToT : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("FToT per strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("FToT per strip").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("FToT per strip : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("OccupancyClusters").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("OccupancyClusters").draw(
//						this.getDataGroup().getItem(0, 0, 0).getH1F("HitmapClusters : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("NbClusters vs Time").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("NbClusters vs Time").draw(
//						this.getDataGroup().getItem(0, 0, 0).getH1F("NbClusters vs Time : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("ClusterCharge").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("ClusterCharge").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterCharge : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("ClusterCharge per strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("ClusterCharge per strip").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterCharge per strip : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("ClusterSize").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("ClusterSize").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterSize : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("ClusterSize per strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("ClusterSize per strip").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterSize per strip : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("Residuals").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("Residuals").draw(
//						this.getDataGroup().getItem(0, 0, 3).getH1F("Residuals : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("MaxAdcOfCentroid : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").draw(
//						this.getDataGroup().getItem(0, 0, 1).getH1F("MaxAdcOfCentroid per strip : Layer " + layer + " Sector " + sector));
//
//				this.getDetectorCanvas().getCanvas("TimeOfCentroid").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("TimeOfCentroid").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfCentroid : Layer " + layer + " Sector " + sector));
//				
//				this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").cd(column + numberOfColumns * row);
//				this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").draw(
//						this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfCentroid per strip : Layer " + layer + " Sector " + sector));
//				
			}
	}
//		this.getDetectorCanvas().getCanvas("Occupancies").update();
//		this.getDetectorCanvas().getCanvas("Occupancy").update();
//		this.getDetectorCanvas().getCanvas("Occupancy C").update();
//		this.getDetectorCanvas().getCanvas("Occupancy Z").update();
//		this.getDetectorCanvas().getCanvas("NbHits vs Time").update();
//
//		this.getDetectorCanvas().getCanvas("MaxADC").update();
//		this.getDetectorCanvas().getCanvas("MaxADC vs Strip").update();
//		this.getDetectorCanvas().getCanvas("IntegralPulse").update();
//		this.getDetectorCanvas().getCanvas("IntegralPulse vs Strip").update();
//
//		this.getDetectorCanvas().getCanvas("TimeMax").update();
//		this.getDetectorCanvas().getCanvas("TimeMax vs Strip").update();
//		this.getDetectorCanvas().getCanvas("ToT").update();
//		this.getDetectorCanvas().getCanvas("ToT per strip").update();
//		this.getDetectorCanvas().getCanvas("FToT").update();
//		this.getDetectorCanvas().getCanvas("FToT per strip").update();
//
//		this.getDetectorCanvas().getCanvas("OccupancyClusters").update();
//		this.getDetectorCanvas().getCanvas("NbClusters vs Time").update();
//		this.getDetectorCanvas().getCanvas("ClusterCharge").update();
//		this.getDetectorCanvas().getCanvas("ClusterCharge per strip").update();
//		this.getDetectorCanvas().getCanvas("ClusterSize").update();
//		this.getDetectorCanvas().getCanvas("ClusterSize per strip").update();
//		this.getDetectorCanvas().getCanvas("Residuals").update();
//		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid").update();
//		this.getDetectorCanvas().getCanvas("MaxAdcOfCentroid per strip").update();
//		this.getDetectorCanvas().getCanvas("TimeOfCentroid").update();
//		this.getDetectorCanvas().getCanvas("TimeOfCentroid per strip").update();
	}
	
	/**
	 * Read 1 event and fill histograms 
	 */
	public void processEvent(DataEvent event) {
		try {


			if (this.getNumberOfEvents() >= super.eventResetTime_current[6] && super.eventResetTime_current[6] > 0){
				resetEventListener();
			}

			if (!testTriggerMask()) return;

			//System.out.println("Event FMT: " + this.getNumberOfEvents());

			//		if (this.getNumberOfEvents()>=30000){
			//			resetEventListener();
			//		}

			this.pulseViewer.clearHits(); /* Remove all hits from previous event*/

			/* ===== RUN RECONSTRUCTION ===== */

			//		recosmic.processDataEvent(event);
			//		System.out.println("Reconstruction done ");

			//event.show();

			/* ===== READ DECODED BANK ===== */
			
			int multiplicity[][] = new int[numberOfSectors + 1][numberOfLayers + 1];
			
			//System.out.println("notKnownFMT");
			if (event.hasBank("FMT::adc") == true) {
				//System.out.println("hasBankFMT");
				DataBank bank = event.getBank("FMT::adc");
				//bank.show();
				this.getDataGroup().getItem(0, 0, 0).getH1F("hitMultiplicity").fill(bank.rows(),1);
				//if (bank.rows()>4){ //CUT

				for (int i = 0; i < bank.rows(); i++) { /* For all hits */
					//System.out.println("Hit: "+(i+1)+"/"+bank.rows());
					int sector = bank.getByte("sector", i);
					int layer = bank.getByte("layer", i);
					int component = bank.getShort("component", i);
					float timeOfMax = bank.getFloat("time", i);
					int integralOverPulse = bank.getInt("integral", i);
					float adcOfMax = bank.getInt("ADC", i);
					int adcOfPulse[] = new int[numberOfSamples];
					long timestamp = bank.getLong("timestamp",i);

					for (int j = 0; j < numberOfSamples; j++) {
						adcOfPulse[j]=0;//*/bank.getInt("bin"+j,i);
						//pulseHistoFMT.setBinContent(j,adcOfPulse[j]);
						//System.out.println("  bin : "+j+"  adc : "+adcOfPulse[j]);
					}
					//			    System.out.println("FMT Sector: "+sector+" Layer: "+layer+" Component: "+component+"  time: "+timeOfMax+"   adc: "+adcOfMax);

					/* ===== APPLY CUTS ===== */

					if ((!mask[sector][layer][component])/*||(adcOfMax < 700)*/){
						continue;
					}

					/* ===== COMPUTE GENERAL QUANTITIES ===== */

					multiplicity[sector][layer]++;
					numberOfHitsPerStrip[sector][layer][component]++;
					//				offset[layer][sector][component]=256;
					//				threshold[layer][sector][component]=5*8.5;
					int dream=0;
					int dreamLayer = 0;
					for (int layerNb=1; layerNb<layer; layerNb++){
						dreamLayer = dreamLayer + numberOfSectors * numberOfChips[layerNb];
					}
					int dreamSector = (sector-1) * numberOfChips[layer] ;
					int dreamTile = (component - 1) / numberOfStripsPerChip + 1;
					dream = dreamLayer + dreamSector + dreamTile;
					numberOfHitsPerDream[dream]++;

					/* ===== FILL OCCUPANCY PLOTS ===== */

					this.getDataGroup().getItem(0, 0, 0).getH1F("Hitmap : Layer " + layer + " Sector " + sector).fill(component, 1);
					this.getDataGroup().getItem(0, 0, 0).getH2F("Occupancies").fill(component,3*(layer-1)+(sector-1),1);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component);

					int eventBin = this.getNumberOfEvents()/ratePlotScale;
					this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layer + " Sector " + sector).fill(eventBin);
					int numberOfBins=100;
					int rescaleFactor=2;
					if (eventBin > (numberOfBins-numberOfBins*10/100)){
						ratePlotScale*=rescaleFactor;
						double hitNbVSEvents[][][] = new double[numberOfSectors + 1][numberOfLayers + 1][numberOfBins];
						for (int sectorCounter = 1; sectorCounter <= numberOfSectors; sectorCounter++) {
							for (int layerCounter = 1; layerCounter <= numberOfLayers; layerCounter++) {
								for (int eventBinCounter=0; eventBinCounter<numberOfBins; eventBinCounter++){
									hitNbVSEvents[sectorCounter][layerCounter][eventBinCounter]=this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layerCounter + " Sector " + sectorCounter).getBinContent(eventBinCounter);					
									this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layerCounter + " Sector " + sectorCounter).setBinContent(eventBinCounter,0);
								}
								this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layerCounter + " Sector " + sectorCounter).setTitleX("Events (Each bin is "+ratePlotScale+" events)");
								updateHistoChanges();

								for (int eventBinCounter=0; eventBinCounter<numberOfBins/rescaleFactor; eventBinCounter++){
									for (int binAdding=0; binAdding<rescaleFactor; binAdding++){
										this.getDataGroup().getItem(0, 0, 0).getH1F("NbHits vs Time : Layer " + layerCounter + " Sector " + sectorCounter).fill(eventBinCounter,hitNbVSEvents[sectorCounter][layerCounter][rescaleFactor*eventBinCounter+binAdding]);
									}
								}
							}
						}
					}

					/* ===== FILL ADC PLOTS ===== */

					this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax : Layer " + layer + " Sector " + sector).fill(adcOfMax);
					double adcMaxOldAvg = this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax vs Strip : Layer " + layer + " Sector " + sector).getBinContent(component);
					this.getDataGroup().getItem(0, 0, 1).getH1F("ADCMax vs Strip : Layer " + layer + " Sector " + sector).setBinContent(component, adcMaxOldAvg + (adcOfMax-adcMaxOldAvg)/numberOfHitsPerStrip[sector][layer][component]);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+" Max: "+ adcOfMax);

					this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse : Layer " + layer + " Sector " + sector).fill(integralOverPulse);
					double integralOldAvg = this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse vs Strip : Layer " + layer + " Sector " + sector).getBinContent(component);
					this.getDataGroup().getItem(0, 0, 1).getH1F("IntegralPulse vs Strip : Layer " + layer + " Sector " + sector).setBinContent(component, integralOldAvg + (integralOverPulse-integralOldAvg)/numberOfHitsPerStrip[sector][layer][component]);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+" Integral: "+integralOverPulse);

					/* ===== FILL TIME PLOTS ===== */

					this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax : Layer " + layer + " Sector " + sector).fill(timeOfMax);
					double timeOldAvg = this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax vs Strip : Layer " + layer + " Sector " + sector).getBinContent(component);
					this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax vs Strip : Layer " + layer + " Sector " + sector).setBinContent(component, timeOldAvg + (timeOfMax-timeOldAvg)/numberOfHitsPerStrip[sector][layer][component]);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+ "TimeOfMax: "+timeOfMax);

					double timeMaxDreamOldAvg = this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax").getBinContent(dream);
					this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfMax").setBinContent(dream,timeMaxDreamOldAvg + (timeOfMax-timeMaxDreamOldAvg)/numberOfHitsPerDream[dream]);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+ "Dream: "+dream+"TimeOfMax:"+timeOfMax);

					int ftot=-1; /*First time over threshold */
					int ltot=-1; /*Last time over threshold */
					int tot=-1;  /*Time over threshold*/
					for (int j = 0; j < numberOfSamples; j++) {
						if (adcOfPulse[j]>=(adcOffset+noise*sigmaThreshold)&&(ftot==-1)){
							ftot=j;
						}else if ((adcOfPulse[j]<=(adcOffset+noise*sigmaThreshold))&&(ftot!=-1)&&(tot==-1)){
							ltot=j;
							tot = ltot-ftot;
						}
					}
					if (ftot>=0){
						this.getDataGroup().getItem(0, 0, 2).getH1F("FToT : Layer " + layer + " Sector " + sector).fill(ftot);
						double ftotOldAvg = this.getDataGroup().getItem(0,0,2).getH1F("FToT per strip : Layer " + layer + " Sector " + sector).getBinContent(component);
						this.getDataGroup().getItem(0, 0, 2).getH1F("FToT per strip : Layer " + layer + " Sector " + sector).setBinContent(component, ftotOldAvg + (ftot-ftotOldAvg)/numberOfHitsPerStrip[sector][layer][component]);
					}
					if (tot>=0){
						this.getDataGroup().getItem(0, 0, 2).getH1F("ToT : Layer " + layer + " Sector " + sector).fill(tot);
						double totOldAvg = this.getDataGroup().getItem(0,0,2).getH1F("ToT per strip : Layer " + layer + " Sector " + sector).getBinContent(component);
						this.getDataGroup().getItem(0, 0, 2).getH1F("ToT per strip : Layer " + layer + " Sector " + sector).setBinContent(component, totOldAvg + (tot-totOldAvg)/numberOfHitsPerStrip[sector][layer][component]);
					}
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+"Time Max: "+timeOfMax+" ftot: "+ftot+" tot: "+tot);

					/* ===== PULSE VIEWER ===== */

					int [] hit = new int [numberOfSamples+3];
					hit[0]=sector;
					hit[1]=layer;
					hit[2]=component;
					for (int j = 0; j < numberOfSamples; j++) {
						hit[j+3]=adcOfPulse[j];
					}
					this.pulseViewer.add(hit);
					//System.out.println();

				} /* End of hits loop */

				if (this.pulseViewer.pulseStatus==true){
					this.pulseViewer.updateComboBox();
				}
				//System.out.println("Pulse Done");
				
				for (int sector = 1; sector <= numberOfSectors; sector++) {
					for (int layer = 1; layer <= numberOfLayers; layer++) {
						if (multiplicity[sector][layer]>0){
							this.getDataGroup().getItem(0, 0, 0).getH1F("Multiplicity : Layer " + layer + " Sector " + sector).fill(multiplicity[sector][layer]);
							double occupancy = (100*multiplicity[sector][layer]/(numberOfStrips[layer]+0.0));
							this.getDataGroup().getItem(0, 0, 0).getH1F("TileOccupancy : Layer " + layer + " Sector " + sector).fill(occupancy);
						}
					}
				}
				
			} /* End of if event has BMT::adc bank loop */

			//		event.show();
			//		System.out.println("BMT::adc");
			//		DataBank bank3 = event.getBank("BMT::adc");
			//		bank3.show();
			//		System.out.println("BMTRec::Hits");
			//		DataBank bank4 = event.getBank("BMTRec::Hits");
			//		bank4.show();
			//		System.out.println("BMTRec::Crosses");
			//		DataBank bank5 = event.getBank("BMTRec::Crosses");
			//		bank5.show();
			//		System.out.println("BMTRec::Clusters");
			//		DataBank bank6 = event.getBank("BMTRec::Clusters");
			//		bank6.show();
			//		System.out.println("CVTRec::Trajectory");
			//		DataBank bank7 = event.getBank("CVTRec::Trajectory");
			//		bank7.show();

			/* ===== READ RECONSTRUCTED BANK ===== */

			if (event.hasBank("FMTRec::Clusters") == true) {
				DataBank bankClusters = event.getBank("FMTRec::Clusters");
				//bankClusters.show();
				for (int i = 0; i < bankClusters.rows(); i++) {

					int sectorCentroid = bankClusters.getByte("sector", i);
					int layerCentroid = bankClusters.getByte("layer", i);
					float centroid = bankClusters.getFloat("centroid", i);
					float etot = bankClusters.getFloat("ETot", i);
					short size = bankClusters.getShort("size" ,i);
					//System.out.println("Sector: "+sectorCentroid+" Layer: "+layerCentroid+" Component: "+centroid);

					/* ===== COMPUTE GENERAL QUANTITIES ===== */

					int centroidInt = Math.round(centroid);
					if ((centroidInt > numberOfStrips[layerCentroid])||(centroidInt < 1)){
						System.out.println("avoid");
						continue;
					}
					numberOfCentroidsPerStrip[sectorCentroid][layerCentroid][centroidInt]++;

					/* ===== FILL HISTOS ===== */

					this.getDataGroup().getItem(0, 0, 0).getH1F("HitmapClusters : Layer " + layerCentroid + " Sector " + sectorCentroid).fill(centroidInt);
					int timeBin=20000; /* For Cosmics with SVT trigger 20000 is ~ an hour */
					int hour = this.getNumberOfEvents()/timeBin;
					int rescale=1;
					//				hitNbVST[layer][sector][hour]++;
					//				if (hour>100&&hour%100==0){ /* Try automatic rescaling */
					//					rescale++;
					//					System.out.println("scale : "+rescale);
					//					for (int sector2 = 1; sector2 <= maxNumberSector; sector2++) {
					//						for (int layer2 = 1; layer2 <= maxNumberLayer; layer2++) {
					//							for (int hour2=0; hour2<rescale*100; hour2++){
					//								this.getDataGroup().getItem(sector2, layer2, 4).getH1F("NbHits vs Time : Layer " + layer + " Sector " + sector).fill(hour2/rescale, hitNbVST[layer][sector][hour]);
					//							}
					//						}
					//					}
					//				}
					this.getDataGroup().getItem(0, 0, 0).getH1F("NbClusters vs Time : Layer " + layerCentroid + " Sector " + sectorCentroid).fill(hour/rescale);

					this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterCharge : Layer " + layerCentroid + " Sector " + sectorCentroid).fill(etot);
					double clusterChargeOldAvg = this.getDataGroup().getItem(0,0,1).getH1F("ClusterCharge per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).getBinContent(centroidInt);
					this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterCharge per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).setBinContent(centroidInt, clusterChargeOldAvg + (etot-clusterChargeOldAvg)/numberOfCentroidsPerStrip[sectorCentroid][layerCentroid][centroidInt]);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+" Cluster charge: "+etot);

					this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterSize : Layer " + layerCentroid + " Sector " + sectorCentroid).fill(size);
					double clusterSizeOldAvg = this.getDataGroup().getItem(0,0,1).getH1F("ClusterSize per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).getBinContent(centroidInt);
					this.getDataGroup().getItem(0, 0, 1).getH1F("ClusterSize per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).setBinContent(centroidInt, clusterSizeOldAvg + (size-clusterSizeOldAvg)/numberOfCentroidsPerStrip[sectorCentroid][layerCentroid][centroidInt]);
					//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+" Cluster size: "+size);

					/* ===== FILL HIT/CLUSTERS HISTOS ===== */

					if (event.hasBank("FMT::adc") == true) {
						DataBank bankAdc = event.getBank("FMT::adc");
						//bankAdc.show();
						int hit = 0;
						int sector = 0;//bankAdc.getByte("sector", hit);
						int layer = 0;//bankAdc.getByte("layer", hit);
						int component = 0;//bankAdc.getShort("component", hit);
						float adcOfMax = 0;//bankAdc.getInt("ADC", hit);
						float timeOfMax =0;

						int nearestStrip=0;
						float adc=0;
						float time=0;

						while ( (component != centroidInt || sector != sectorCentroid || layer != layerCentroid) && hit<bankAdc.rows() ){
							sector = bankAdc.getByte("sector", hit);
							layer = bankAdc.getByte("layer", hit);
							component = bankAdc.getShort("component", hit);
							adcOfMax = bankAdc.getInt("ADC", hit);
							timeOfMax = bankAdc.getFloat("time", i);
							if ( sector == sectorCentroid && layer == layerCentroid && Math.abs(component-centroid)<2 && Math.abs(component-centroid)<Math.abs(nearestStrip-centroidInt) ){
								nearestStrip=component;
								adc = adcOfMax;
								time = timeOfMax;
							}
							hit++;
						}
						if (component == centroidInt && sector == sectorCentroid && layer == layerCentroid){
							adc=adcOfMax;
							time=timeOfMax;
							numberOfCentroidsMatchedPerStrip[sectorCentroid][layerCentroid][centroidInt]++;
							this.getDataGroup().getItem(0, 0, 1).getH1F("MaxAdcOfCentroid : Layer " + layerCentroid + " Sector " + sectorCentroid).fill(adc);
							double maxAdcOfCentroidOldAvg = this.getDataGroup().getItem(0,0,1).getH1F("MaxAdcOfCentroid per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).getBinContent(centroidInt);
							this.getDataGroup().getItem(0, 0, 1).getH1F("MaxAdcOfCentroid per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).setBinContent(centroidInt, maxAdcOfCentroidOldAvg + (adc-maxAdcOfCentroidOldAvg)/numberOfCentroidsPerStrip[sectorCentroid][layerCentroid][centroidInt]);
							//System.out.println("Matching Sector: "+sectorCentroid+" Layer: "+layerCentroid+" Component: "+centroid+ " Max adc of centroid: "+adc);	

							this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfCentroid : Layer " + layerCentroid + " Sector " + sectorCentroid).fill(time);
							double timeOfCentroidOldAvg = this.getDataGroup().getItem(0,0,2).getH1F("TimeOfCentroid per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).getBinContent(centroidInt);
							this.getDataGroup().getItem(0, 0, 2).getH1F("TimeOfCentroid per strip : Layer " + layerCentroid + " Sector " + sectorCentroid).setBinContent(centroidInt, timeOfCentroidOldAvg + (time-timeOfCentroidOldAvg)/numberOfCentroidsPerStrip[sectorCentroid][layerCentroid][centroidInt]);
						}
						//					else{
						//						System.out.println("ERROR : Sector: "+sectorCentroid+" Layer: "+layerCentroid+" Component: "+centroid);
						//						System.out.println("nearestStrip: "+nearestStrip);
						//					}
					}	
				}
			}
			//		if (event.hasBank("CVTRec::Cosmics") == true) {			
			//			DataBank bankCosmics = event.getBank("CVTRec::Cosmics");
			//			//bankCosmics.show();
			//		}
			//		if (event.hasBank("CVTRec::Trajectory") == true) {			
			//			DataBank bankTrajectory = event.getBank("CVTRec::Trajectory");
			//			//bankTrajectory.show();	
			//		}
		} catch (Exception e){
			e.printStackTrace();
			// Deal with e as you please.
			//e may be any type of exception at all.

		}

		//		System.out.println("FMT Analysis Done Event: " + this.getNumberOfEvents());
	}
}
