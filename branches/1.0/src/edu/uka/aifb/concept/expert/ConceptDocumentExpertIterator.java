package edu.uka.aifb.concept.expert;

import edu.uka.aifb.api.concept.index.ICVIndexEntryIterator;
import edu.uka.aifb.api.expert.IDocumentExpertIterator;

public class ConceptDocumentExpertIterator implements IDocumentExpertIterator {

	ICVIndexEntryIterator indexEntryIt;
	
	public ConceptDocumentExpertIterator( ICVIndexEntryIterator indexEntryIt ) {
		this.indexEntryIt = indexEntryIt;
	}
	
	@Override
	public double getDocumentGivenExpertProb() {
		return indexEntryIt.getValue() / indexEntryIt.getValueSum();
	}

	@Override
	public int getExpertId() {
		return indexEntryIt.getDocId();
	}

	@Override
	public boolean next() {
		return indexEntryIt.next();
	}

}
