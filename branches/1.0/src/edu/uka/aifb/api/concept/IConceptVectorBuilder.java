package edu.uka.aifb.api.concept;



public interface IConceptVectorBuilder {

	public void reset( String docName, int maxConceptId );
	
	public void addScores( int[] conceptIds, double[] conceptScores );

	public void addScores( IConceptVector cv );
	
	public IConceptVector getConceptVector();

}
