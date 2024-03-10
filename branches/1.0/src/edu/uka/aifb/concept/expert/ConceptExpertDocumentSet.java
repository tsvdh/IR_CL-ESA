package edu.uka.aifb.concept.expert;

import edu.uka.aifb.api.concept.index.ICVIndexReader;
import edu.uka.aifb.api.expert.IExpertDocumentSet;

public class ConceptExpertDocumentSet implements IExpertDocumentSet {

	ICVIndexReader indexReader;
	
	public ConceptExpertDocumentSet( ICVIndexReader indexReader ) {
		this.indexReader = indexReader;
	}
	
	@Override
	public int getExpertId( String expertName ) {
		return indexReader.getConceptVectorId( expertName );
	}

	@Override
	public String getExpertName( int expertId ) {
		String name = indexReader.getConceptVectorData( expertId ).getDocName();
		int pos = name.indexOf( '/' );
		if( pos >= 0 ) {
			name = name.substring( 0, pos );
		}
		return name;
	}

	@Override
	public int getNumberOfExperts() {
		return indexReader.getNumberOfDocuments();
	}

}
