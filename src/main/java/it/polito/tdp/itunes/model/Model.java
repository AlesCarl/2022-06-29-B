package it.polito.tdp.itunes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.itunes.db.ItunesDAO;

public class Model {
	
	//grafo semplica, pesato ,  orientato
	
		ItunesDAO dao; 
	    private SimpleDirectedWeightedGraph<Album, DefaultWeightedEdge> graph;  // SEMPLICE, PESATO, NON ORIENTATO
	    private List<Album> allAlbum ;

		
	public Model() {
	    	
	    	this.dao= new ItunesDAO();  
	    	this.graph= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	    	this.allAlbum= new ArrayList<>();
	    	
	    }


	public void creaGrafo(int durataSec) {
		
		this.allAlbum= dao.getAllAlbumDurata(durataSec);
		

		 /** VERTICI */
	   	Graphs.addAllVertices(this.graph, allAlbum);
			System.out.println("NUMERO vertici GRAFO: " +this.graph.vertexSet().size());
			
			
			/** ARCHI */
	/*
	 1. hanno una durata differente;
     2. la somma delle loro durate è maggiore di 4*n.
     
      L’eventuale arco è orientato dall’album di durata minore verso l’album di durata 
      maggiore, e
       il peso, sempre positivo , è definito come la somma delle durate di a1 e a2. 
     
	 */
			for(Album a1: this.allAlbum) {
				for(Album a2 : this.allAlbum) {
					
					int durataA1= dao.getDurata(a1,durataSec); 
					int durataA2= dao.getDurata(a2,durataSec); 
					
					int peso = durataA1+durataA2;
					
					if( peso>(4*durataSec*1000)) {
						
						if(durataA1>durataA2)
						Graphs.addEdgeWithVertices(this.graph, a2, a1, peso);
						
						else if(durataA2>durataA1)
							Graphs.addEdgeWithVertices(this.graph, a1, a2, peso);
					
//					System.out.println("\npeso= "+peso); 
						
					}
				}
			}
		
			System.out.println(this.graph.vertexSet().size());
		    System.out.println(this.graph.edgeSet().size());
	}


	public int getVertici() {
		return this.graph.vertexSet().size();
	}


	public int getArchi() {
		return this.graph.edgeSet().size();
	}


	
	public List<BilancioVertex> getBilancioSuccessori( Album a ) {
		
		List<BilancioVertex> listTemp= new ArrayList<>(); 
		
		for(Album aa: Graphs.successorListOf(this.graph, a)) {
			listTemp.add(getBilancio(aa)); 
		}
		
		Collections.sort(listTemp);
		return listTemp;
		
	}

		
	
	/*
	 BILANCIO :  SUM  pesi archi entranti -  SUM  pesi archi  uscenti. 
	 */
	public BilancioVertex getBilancio( Album a ) {
				
		double bilancio=0; 
		double SumArchiEntranti= 0;
		double SumArchiUscenti= 0;
		
		for(DefaultWeightedEdge ee: this.graph.incomingEdgesOf(a)) {
			SumArchiEntranti+= this.graph.getEdgeWeight(ee); 
		}
		
		for(DefaultWeightedEdge ee: this.graph.outgoingEdgesOf(a)) {
			SumArchiUscenti+= this.graph.getEdgeWeight(ee); 
		}
		
		
		bilancio= SumArchiEntranti-SumArchiUscenti; 

		BilancioVertex bv= new BilancioVertex(a, bilancio); 
		
		return bv;
		
	}


	public List<Album> getAllVertici() {
		return this.allAlbum;
	}



	
	
	                    /**        RICORSIONE      **/ 
	
	List <Album> bestPercorso; 
	double bestSize;  
	
	
	public List<Album> getPercorso(Album a1, Album a2, Double soglia) {
		
		List <Album> parziale = new ArrayList<>() ; 
		
		this.bestPercorso= new ArrayList<>() ; 
		this.bestSize = 0; 
		
	
		parziale.add(a1); 

		double bilancioA1 = this.getBilancio(a1).getBilancio(); 
		//System.out.println("\nbilancio a1: : "+bilancioA1); 

		
		ricorsione(parziale,a2, soglia, bilancioA1); 
		
		
		
		return bestPercorso ;
		
	}
	
	
	/*
	 1. parta da a1 (selezionato al punto 1d) e termini in a2;
	 
     2. attraversi solo archi con peso maggiore o uguale a x;
     
     3. tocchi il maggior numero di vertici che hanno un “bilancio” maggiore di quello del 
     vertice di partenza a1 (per il calcolo del “bilancio” di un vertice si veda il punto 1d).
 
	  
	 
	 */
	

	private void ricorsione(List<Album> parziale, Album a2, Double soglia, double bilancioA1) {

		Album current = parziale.get(parziale.size()-1); 
		
		if(parziale.size()>1) {
			
		 /** condizione uscita **/ 
		 if(current.equals(a2)) {

			/** soluzione migliore **/ 
			if(getSizeBilancio(parziale,bilancioA1) > bestSize) {
				
				bestSize= getSizeBilancio(parziale,bilancioA1); 
				bestPercorso= new ArrayList<>(parziale); 
			}
			
			return; 	
		  }

		}
		
//		/** soluzione migliore **/ 
//		if(getSizeBilancio(parziale,bilancioA1) > bestSize) {
//			
//			bestSize= getSizeBilancio(parziale,bilancioA1); 
//			bestPercorso= new ArrayList<>(parziale); 
//		}


		 List<Album> successori= Graphs.successorListOf(graph, current);
		 List<Album> newSuccessori= new ArrayList<>(); 
		 
		
		for(Album rr: successori) {
		    if(!parziale.contains(rr) ) {
		    	newSuccessori.add(rr);  //QUI METTO SOLO I VERTICI CHE NON SONO GIA' STATI USATI
		    	                        // e che NON sono compagni di squadra di "current"
		    }
	  }
		

		
	    /** continuo ad AGGIUNGERE elementi in parziale + backtracking **/ 
		
		for(Album aa : newSuccessori) {
			
			DefaultWeightedEdge ee = this.graph.getEdge(current, aa); 
			double peso= this.graph.getEdgeWeight(ee); 
			
			if(peso >= soglia) {
	
				parziale.add(aa);
				ricorsione(parziale,a2, soglia, bilancioA1); 
				parziale.remove(aa);	 
		}
	 }
	}

	
	
	private int getSizeBilancio(List<Album> parziale, double bilancioA1) {
	
		int cont=0; 
		
		for(Album aa: parziale) {
			System.out.println("\ngetBilancio(a): "+getBilancio(aa)); 
			if(getBilancio(aa).getBilancio()>bilancioA1) {

				cont++; 
			}
		 }
		
		//System.out.println("\ncont: "+cont); 
		return cont;
	}

	
	
	
	
	
	
	
	

	
	
	
}
