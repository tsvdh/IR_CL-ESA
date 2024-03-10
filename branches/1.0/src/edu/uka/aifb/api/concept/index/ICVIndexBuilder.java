package edu.uka.aifb.api.concept.index;

import edu.uka.aifb.api.concept.IConceptVector;


/**
 * An concept vector index is an inverted index of a set of concept vector.
 * 
 * This is an interface for classes that build such a CV index.
 * 
 * @author pso
 *
 */
public interface ICVIndexBuilder {

	/**
	 * Add an concept vector to the index. This function has to be called for
	 * each CV before buildIndex() is called.
	 * 
	 * @param cv The concept vector that should be added.
	 */
	public void add( IConceptVector cv );
	
	/**
	 * This actually builds the complete index. The index will contain all concept vectors
	 * that have been added using the add() function.
	 * 
	 */
	public void buildIndex();
	
	/**
	 * Checks if the index already exists. This can be used to avoid a
	 * recreation of an existing index.
	 * 
	 * @return True if the index exists already.
	 */
	public boolean indexExists();
	
}
