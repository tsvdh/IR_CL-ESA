package edu.uka.aifb.api.concept.index;

/**
 * Specifies the interface for an iterative access to entries of
 * a concept vector index.
 * 
 * For a certain concept all documents that activate the concept are iterated
 * together with the association strength between the document and the concept.
 * 
 * @author pso
 *
 */
public interface ICVIndexEntryIterator {

	/**
	 * Proceed to the next entry. The first call of next() moves the
	 * iterator to the first entry.
	 * 
	 * @return True if the next entry exists. False if there are no more entries.
	 */
	public boolean next();
	
	/**
	 * Returns the document id of the current entry. This is an internal id only valid for
	 * this CV index. The document name can be retrieved through the concept vector data accessed
	 * by this internal id.
	 * 
	 * @return The internal document / concept vector id.
	 */
	public int getDocId();
	
	/**
	 * Returns the association strength between the concept for which the iterator
	 * was initialized and the current document that is selected by calling the next()
	 * function.
	 * 
	 * @return The association strength between the concept and current document.
	 */
	public double getValue();
	
	public double getValueSum();
	
}
