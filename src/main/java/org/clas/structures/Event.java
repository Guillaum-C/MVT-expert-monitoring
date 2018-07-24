package org.clas.structures;

import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.services.CVTReconstruction;

public class Event {

	
	private static final int numberOfSectors = 3;
	private static final int numberOfLayers = 6;
	private static final int numberOfSamples = 16;
	
	private ArrayList<Hit> hits;
	private ArrayList<Cluster> clusters;
	
	int multiplicity[][];
	
	private CVTReconstruction recoCo;
	
	public Event() {
		hits = new ArrayList<>();
		clusters = new ArrayList<>();
		
		multiplicity = new int[numberOfSectors + 1][numberOfLayers + 1];
		
//		recoCo = new CVTReconstruction();
//		recoCo.init();
		
	}

	
	
	public ArrayList<Hit> getHits() {
		return hits;
	}

	public void setHits(ArrayList<Hit> hits) {
		this.hits = hits;
	}

	public void addHits(Hit hit) {
		this.hits.add(hit);
	}
	
	public ArrayList<Cluster> getClusters() {
		return clusters;
	}

	public void setClusters(ArrayList<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	public void addCluster(Cluster cluster){
		this.getClusters().add(cluster);
	}

	@Override
	public String toString() {
		return "Event [hits=" + hits + ", clusters=" + clusters + "]";
	}
	
	

	public void fillRawData(DataEvent event){
		
		if (event.hasBank("BMT::adc") == true) {
			DataBank bank = event.getBank("BMT::adc");
			//bank.show();
			
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

				ArrayList<Integer> pulse = new ArrayList<Integer>();
				
				for (int j = 0; j < numberOfSamples; j++) {
					adcOfPulse[j]=0;//*/bank.getInt("bin"+j,i);
					//pulseHistoBMT.setBinContent(j,adcOfPulse[j]);
					//System.out.println("  bin : "+j+"  adc : "+adcOfPulse[j]);
					pulse.add(adcOfPulse[j]);
				}
				//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component);
				//System.out.println("Sector: "+sector+" Layer: "+layer+" Component: "+component+ " TimeOfMax: "+timeOfMax+" AdcOfMax: "+adcOfMax);
				 if (component == -1){ /* Happens in simulation and have to be avoided*/
					 continue;
				 }

				/* ===== FILL EVENT =====*/
				 
				Hit currentHit = new Hit(i, sector, layer, component, adcOfMax, timeOfMax, integralOverPulse, pulse);
				this.addHits(currentHit);
			}
		}
	}
	public void clustering(){
		this.clustering(false);
	}

	
	public void clustering(boolean debug){
		int[][][] hitArray = new int[3+1][6+1][1200+1]; //CHANGE THAT
		for (int sector = 1; sector <= 3; sector++) {
			for (int layer = 1; layer <= 6; layer++) {
				for (int component = 1; component <= 1200 ; component++){ //CHANGE THAT
					hitArray[sector][layer][component]=-888;
				}
			}
		}
		for (int i=0; i<hits.size(); i++){
			Hit currentHit= hits.get(i);
			hitArray[currentHit.getSector()][currentHit.getLayer()][currentHit.getComponent()]=i;
			if (debug){
				System.out.println("Clustering: Added to hit array: hit "+i+" sector "+currentHit.getSector()+" layer "+currentHit.getLayer()+" component "+currentHit.getComponent());
			}
		}
		clusters.clear();
		int clusterID = 1;
		int numberStripsToSkip=1;
		int offStrips=-1; /*-1 not in a cluster, 0 in a cluster after a hit strip, 1 in a cluster after 1 missed strip, 2 in a cluster after 2 missed strip, 3 in a cluster after 3 missed strip ... */
		int allowedOffStrips=1;
		Cluster currentCluster = null;
		for (int sector = 1; sector <= 3; sector++) { //CHANGE THAT
			for (int layer = 1; layer <= 6; layer++) { //CHANGE THAT
				int component=1;
				while (component<1200){ //CHANGE THAT
					if (debug){
						//System.out.println("Clustering: Now looking at sector "+sector+" layer "+layer+" component "+component+" stripOff "+offStrips);
					}
					if (hitArray[sector][layer][component]>=0){ /* If there is a hit*/
						if (debug){
							System.out.println("Clustering: Hit found at sector "+sector+" layer "+layer+" component "+component);
						}
						if (offStrips==-1){ /*If we are not in a cluster, create a new cluster with the current hit*/
							currentCluster = new Cluster(clusterID, sector, layer);
							currentCluster.addHit(hits.get(hitArray[sector][layer][component])); //CHANGE THAT
							if (debug){
								System.out.println("Clustering: Created new cluster");
							}
						}else{ /*If we are in a cluster, add the current hit to the current cluster*/
							currentCluster.addHit(hits.get(hitArray[sector][layer][component])); //CHANGE THAT
							if (debug){
								System.out.println("Clustering: Added to previous cluster");
							}
						}
						offStrips=0;
					}else { /*If there is no hit*/
						if (offStrips==allowedOffStrips){ /*If we are too far (depends on allowedOffStrips) from previous hit in the current cluster, end this cluster*/
							this.addCluster(currentCluster);
							currentCluster=null;
							offStrips=-1;
							if (debug){
								System.out.println("Clustering: Cluster finished");
							}
						} else if (offStrips>=0){ /*If we are still close enough (depends on allowedOffStrips) from previous hit in the current cluster, continue looking for a hit*/
							offStrips++;
						}
					}
					component ++;
				}
				if (currentCluster!=null){
					this.addCluster(currentCluster);
				}
			}
		}
		if (debug){
			System.out.println("Clustering: Done");
		}
	}

	public int getClusterNumber(int sector, int layer){
		int numberOfCluster=0;
		for (Cluster currentCluster : clusters){
			if (currentCluster.getSector()==sector && currentCluster.getLayer()==layer){
				numberOfCluster++;
			}
		}
		return numberOfCluster;
	}
	
	

	public void reconstruct(DataEvent event) {
		
		recoCo.processDataEvent(event);
		
		
	}

	
}
