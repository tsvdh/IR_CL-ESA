package edu.kit.aifb.wikipedia.wpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.wikipedia.miner.model.Page;
import org.wikipedia.miner.model.Wikipedia;

import edu.kit.aifb.concept.IConceptDescription;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.wikipedia.mlc.MLCDatabase;
import gnu.trove.TIntArrayList;

public class WpmMlcPageDescription implements IConceptDescription {
	private Log logger = LogFactory.getLog( WpmMlcPageDescription.class );
	
	MLCDatabase mlcDb;
	Map<Language,WikipediaMinerWrapper> wpmMap;
	
	List<WikipediaMinerWrapper> wpms;
	List<Language> languages;
	
	public WpmMlcPageDescription() {
	}
	
	@Required
	public void setMlcDatabase( MLCDatabase mlcDb ) {
		this.mlcDb = mlcDb;
	}
	
	@Required
	public void setWikipediaMiners( List<WikipediaMinerWrapper> wpms ) {
		this.wpms = wpms;
		initializeWpmMap();
	}
	
	@Required
	public void setLanguages( List<Language> languages ) {
		this.languages = languages;
		initializeWpmMap();
	}

	private void initializeWpmMap() {
		if( wpms != null && languages != null ) {
			wpmMap = new HashMap<Language, WikipediaMinerWrapper>();
			for( int i=0; i<wpms.size(); i++ ) {
				wpmMap.put( languages.get(i), wpms.get(i) );
			}
		}
	}
	
	public String getDescription( String conceptName, Language language ) throws Exception {
		if( !wpmMap.containsKey( language ) ) {
			throw new Exception( "Concept description in language " + language + " is not supported." );
		}
		Wikipedia wpm = wpmMap.get( language ).getWikipedia();

		int conceptId = MLCDatabase.getConceptId( conceptName );
		TIntArrayList articleIds = mlcDb.getPageIds( conceptId, language ); 
		
		if( articleIds.size() == 0 ) {
			logger.debug( "No pages found for concept " + conceptName );
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for( int i=0; i<articleIds.size(); i++ ) {
			Page p = wpm.getPageById( articleIds.get( i ) );
			if( p != null ) {
				sb.append( p.getTitle() );
				if( i<articleIds.size()-1 ) {
					sb.append( ";" );
				}
			}
		}
		
		if( logger.isDebugEnabled() )
			logger.debug( "Description of concept " + conceptName + ": " + sb.toString() );
		return sb.toString();
	}

}
