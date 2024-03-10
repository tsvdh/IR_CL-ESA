package edu.uka.aifb.api.concept.index;

import edu.uka.aifb.api.concept.IConceptVectorData;


/**
 *  An concept vector index is an inverted index of a set of concept vector.
 * 
 * This interface specifies how to read an existing CV index.
 * 
 * @author pso
 *
 */
public interface ICVIndexReader {

	/**
	 * Method to get an iterator over all concept vectors that have
	 * a value greater zero for a specified concept.
	 * 
	 * @param conceptId The id of the concept that should be searched in the inverted index.
	 * @return An iterator over concept vectors relevant to the specified concept.
	 */
	public ICVIndexEntryIterator getIndexEntryIterator( int conceptId ); 
	
	/**
	 * Method to retrieve the concept vector data for a specified concept vector.
	 * 
	 * @param cvId The id of the concept vector. This is a local id and only valid for this
	 * specific CV index.
	 * @return
	 */
	public IConceptVectorData getConceptVectorData( int cvId );
	
	/**
	 * The total number of documents / concept vectors stored in this index.
	 * 
	 * @return The number of CV in this index.
	 */
	public int getNumberOfDocuments();
	
	/**
	 * The number of documents for which the value for the specified concept of greater
	 * than zero. This corresponds to the document frequency in IR.
	 * 
	 * @param conceptId The concept id for which the number of relevant documents is returned. 
	 * @return The number of documents that activate the specified concept.
	 */
	public int getDocumentFrequency( int conceptId );
	
	public int getConceptVectorId( String docName );
}
