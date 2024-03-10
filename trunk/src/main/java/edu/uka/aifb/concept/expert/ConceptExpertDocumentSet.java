package edu.uka.aifb.concept.expert;

import edu.kit.aifb.concept.index.ICVIndexReader;
import edu.kit.aifb.document.expert.IExpertDocumentSet;

public class ConceptExpertDocumentSet implements IExpertDocumentSet {

	ICVIndexReader indexReader;
	
	public ConceptExpertDocumentSet( ICVIndexReader indexReader ) {
		this.indexReader = indexReader;
	}
	
	public int getExpertId( String expertName ) {
		return indexReader.getConceptVectorId( expertName );
	}

	public String getExpertName( int expertId ) {
		return indexReader.getConceptVectorData( expertId ).getDocName();
	}

	public int getNumberOfExperts() {
		return indexReader.getNumberOfDocuments();
	}

}
