package edu.kit.aifb.wikipedia.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.concept.IConceptDescription;
import edu.kit.aifb.nlp.Language;

public class WikipediaPageDescription implements IConceptDescription {
	private Log logger = LogFactory.getLog( WikipediaPageDescription.class );
	
	WikipediaDatabase wpDb;
	
	@Required
	public void setWikipediaDatabase( WikipediaDatabase wpDb ) {
		this.wpDb = wpDb;
	}
	
	public String getDescription( String conceptName, Language language ) throws Exception {
		Page p = new Page( WikipediaCollection.getArticleId( conceptName ) );
		wpDb.initializePage( p );
		if( p.isInitialized() ) {
			if( logger.isDebugEnabled() )
				logger.debug( "Description of concept " + conceptName + ": " + p.getTitle() );
			return p.getTitle();
		}
		else {
			logger.warn( "Description for concept " + conceptName + " could not be initialized." );
			return conceptName;
		}
	}

}
