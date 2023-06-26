package it.polito.tdp.itunes.model;

public class BilancioVertex  implements Comparable<BilancioVertex>{
	
	Album a; 
	Double bilancio;
	
	public BilancioVertex(Album a, Double bilancio) {
		super();
		this.a = a;
		this.bilancio = bilancio;
	}

	public Album getA() {
		return a;
	}

	public void setA(Album a) {
		this.a = a;
	}

	public Double getBilancio() {
		return bilancio;
	}

	public void setBilancio(Double bilancio) {
		this.bilancio = bilancio;
	}

	@Override
	public int compareTo(BilancioVertex o) {
		//decrescente
		return -(this.bilancio.compareTo(o.getBilancio()));
	}

	@Override
	public String toString() {
		return  a + " ------  bilancio =  " + bilancio ;
	} 
	
	
	
	

}
