package org.clas.structures;

import java.util.ArrayList;

public class Hit {

	private int detector;	//0 for BMT
	private int hitID;		//Hit ID
    private int sector;		//1 to 3 for BMT
    private int layer;		//1 to 6 for BMT
    private int component;	//Strip
    private float adcMax; 	//ADC of sample max
    private int sampleMax;	//Sample of max
    private float timeMax;	//Time of max with fine timestamp correction and fit
    private int adcIntegral;//ADC summed over all samples
    
    private int timeMaxCorr;//Time of max with fine timestamp correction without fit
    private int timeMaxRaw;	//Time of max without fine timestamp and without fit
    
    private ArrayList<Integer> pulse;
    
    private Cluster cluster;
    
    

	public Hit() {
		
	}

    public Hit(int hitID, int sector, int layer, int component) {
		this.detector = 0;
		this.hitID = hitID;
		this.sector = sector;
		this.layer = layer;
		this.component = component;
	}
      
	public Hit(int hitID, int sector, int layer, int component, float adcMax, float timeMax, int adcIntegral) {
		this.detector = 0;
		this.hitID = hitID;
		this.sector = sector;
		this.layer = layer;
		this.component = component;
		this.adcMax = adcMax;
		this.timeMax = timeMax;
		this.adcIntegral = adcIntegral;
	}

	public Hit(int hitID, int sector, int layer, int component, float adcMax, float timeMax, int adcIntegral,
			ArrayList<Integer> pulse) {
		super();
		this.detector = 0;
		this.hitID = hitID;
		this.sector = sector;
		this.layer = layer;
		this.component = component;
		this.adcMax = adcMax;
		this.timeMax = timeMax;
		this.adcIntegral = adcIntegral;
		this.pulse = pulse;
	}

	
	
	public int getDetector() {
		return detector;
	}

	public void setDetector(int detector) {
		this.detector = detector;
	}

	public int getHitID() {
		return hitID;
	}

	public void setHitID(int hitID) {
		this.hitID = hitID;
	}

	public int getSector() {
		return sector;
	}

	public void setSector(int sector) {
		this.sector = sector;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public int getComponent() {
		return component;
	}

	public void setComponent(int component) {
		this.component = component;
	}

	public float getAdcMax() {
		return adcMax;
	}

	public void setAdcMax(float adcMax) {
		this.adcMax = adcMax;
	}

	public int getSampleMax() {
		return sampleMax;
	}

	public void setSampleMax(int sampleMax) {
		this.sampleMax = sampleMax;
	}

	public float getTimeMax() {
		return timeMax;
	}

	public void setTimeMax(float timeMax) {
		this.timeMax = timeMax;
	}

	public int getAdcIntegral() {
		return adcIntegral;
	}

	public void setAdcIntegral(int adcIntegral) {
		this.adcIntegral = adcIntegral;
	}

	public int getTimeMaxCorr() {
		return timeMaxCorr;
	}

	public void setTimeMaxCorr(int timeMaxCorr) {
		this.timeMaxCorr = timeMaxCorr;
	}

	public int getTimeMaxRaw() {
		return timeMaxRaw;
	}

	public void setTimeMaxRaw(int timeMaxRaw) {
		this.timeMaxRaw = timeMaxRaw;
	}

	public ArrayList<Integer> getPulse() {
		return pulse;
	}

	public void setPulse(ArrayList<Integer> pulse) {
		this.pulse = pulse;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	
	
	@Override
	public String toString() {
		return "Hit [detector=" + detector + ", hitID=" + hitID + ", sector=" + sector + ", layer=" + layer
				+ ", component=" + component + ", adcMax=" + adcMax + ", sampleMax=" + sampleMax + ", timeMax="
				+ timeMax + ", adcIntegral=" + adcIntegral + ", timeMaxCorr=" + timeMaxCorr + ", timeMaxRaw="
				+ timeMaxRaw + ", pulse=" + pulse + ", cluster=" + cluster + "]";
	}

}
