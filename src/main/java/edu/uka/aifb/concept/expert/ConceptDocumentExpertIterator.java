package edu.uka.aifb.concept.expert;

import edu.kit.aifb.concept.index.ICVIndexEntryIterator;
import edu.kit.aifb.document.expert.IDocumentExpertIterator;

public class ConceptDocumentExpertIterator implements IDocumentExpertIterator {

	ICVIndexEntryIterator indexEntryIt;
	
	public ConceptDocumentExpertIterator( ICVIndexEntryIterator indexEntryIt ) {
		this.indexEntryIt = indexEntryIt;
	}
	
	public double getDocumentGivenExpertProb() {
		return indexEntryIt.getValue() / indexEntryIt.getValueSum();
	}

	public int getExpertId() {
		return indexEntryIt.getDocId();
	}

	public boolean next() {
		return indexEntryIt.next();
	}

}
