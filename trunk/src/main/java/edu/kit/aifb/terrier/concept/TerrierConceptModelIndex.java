package edu.kit.aifb.terrier.concept;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.concept.IConceptExtractor;

public class TerrierConceptModelIndex extends TerrierESAIndex {

	private static Log logger = LogFactory.getLog( TerrierConceptModelIndex.class );
		
	IConceptModel conceptModel;
	
	@Required
	public void setConceptModel( IConceptModel conceptModel ) {
		this.conceptModel = conceptModel;
	}
	
	public IConceptExtractor getConceptExtractor() {
		try {
			IConceptExtractor extractor = new TerrierConceptModelExtractor(
					index, language, conceptModel );
			return extractor;
		}
		catch( Exception e ) {
			logger.error( e );
			return null;
		}
	}

}
