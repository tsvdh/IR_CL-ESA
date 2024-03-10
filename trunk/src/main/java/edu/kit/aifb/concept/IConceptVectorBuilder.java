package edu.kit.aifb.concept;



public interface IConceptVectorBuilder extends Cloneable {

	public void reset( String docName, int maxConceptId );
	public void addScores( int[] conceptIds, double[] conceptScores, int count );
	public void addScores( IConceptVector cv );
	public IConceptVector getConceptVector();

	public IConceptVector getConceptVector( String docName, int maxConceptId,
			int[] conceptIds, double[] conceptScores, int count );
	
	public IConceptVectorBuilder clone() throws CloneNotSupportedException;

}
