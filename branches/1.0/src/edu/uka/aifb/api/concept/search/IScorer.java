package edu.uka.aifb.api.concept.search;

import edu.uka.aifb.api.concept.IConceptVectorData;


public interface IScorer {

	public void reset(
			IConceptVectorData queryData,
			IConceptVectorData docData,
			int numberOfDocuments );
	
	public void addConcept(
			int queryConceptId, double queryConceptScore,
			int docConceptId, double docConceptScore,
			int documentFrequency );
	
	public void finalizeScore(
			IConceptVectorData queryData,
			IConceptVectorData docData );
	
	public double getScore();
	
	public boolean hasScore();
	
}
