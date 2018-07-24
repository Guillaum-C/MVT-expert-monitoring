package org.clas.structures;

import java.util.ArrayList;

public class Cluster {

	private int detector;		//0 for BMT
	private int clusterID;
    private int sector;			//1 to 3 for BMT
    private int layer;			//1 to 6 for BMT
    private double centroid;	//Centroid strip
    private double centroidPhi;	//Centroid Phi (for Z tiles)
    private double centroidZ;	//Centroid Z (for C tiles)
    private int size;		//Time of max with fine timestamp correction and fit
    private int adcCharge;	//ADC summed over all samples
    
    private ArrayList<Hit> hits;

    
    
	public Cluster(int clusterID, int sector, int layer) {
		this.detector = 0;
		this.clusterID = clusterID;
		this.sector = sector;
		this.layer = layer;
		this.hits = new ArrayList<>();
	}

	public Cluster(int detector, int clusterID, int sector, int layer) {
		this.detector = detector;
		this.clusterID = clusterID;
		this.sector = sector;
		this.layer = layer;
		this.hits = new ArrayList<>();
	}

	
	
	public int getDetector() {
		return detector;
	}

	public void setDetector(int detector) {
		this.detector = detector;
	}

	public int getClusterID() {
		return clusterID;
	}

	public void setClusterID(int clusterID) {
		this.clusterID = clusterID;
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

	public double getCentroid() {
		return centroid;
	}

	public void setCentroid(double centroid) {
		this.centroid = centroid;
	}

	public double getCentroidPhi() {
		return centroidPhi;
	}

	public void setCentroidPhi(double centroidPhi) {
		this.centroidPhi = centroidPhi;
	}

	public double getCentroidZ() {
		return centroidZ;
	}

	public void setCentroidZ(double centroidZ) {
		this.centroidZ = centroidZ;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getAdcCharge() {
		return adcCharge;
	}

	public void setAdcCharge(int adcCharge) {
		this.adcCharge = adcCharge;
	}

	public ArrayList<Hit> getHits() {
		return hits;
	}

	public void setHits(ArrayList<Hit> hits) {
		this.hits = hits;
	}
	
	public void addHit(Hit hit){
		this.hits.add(hit);
	}
    
	/**
	 * Get a hit of the cluster
	 * @param i hit ID (starts at 1)
	 */
	public void getHit(int i){
		this.hits.get(i-1);
	}

	
	@Override
	public String toString() {
		return "Cluster [detector=" + detector + ", clusterID=" + clusterID + ", sector=" + sector + ", layer=" + layer
				+ ", centroid=" + centroid + ", centroidPhi=" + centroidPhi + ", centroidZ=" + centroidZ + ", size="
				+ size + ", adcCharge=" + adcCharge + ", hits=" + hits + "]";
	}
	
}
