package edu.uka.aifb.api.concept;


public interface IConceptIndex {

	public IConceptExtractor getConceptExtractor();
	
	public String getConceptName( int conceptId );
	
	public int getConceptId( String conceptName );
	
	public int size();
}
