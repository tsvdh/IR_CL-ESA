package edu.uka.aifb.concept.wikipedia;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.concept.IConceptIndex;
import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorMapper;
import edu.uka.aifb.api.wikipedia.ILanglinksMap;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.document.wikipedia.WikipediaCollection;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.wikipedia.sql.SQLLanglinksMap;

public class LanglinksConceptVectorMapper implements IConceptVectorMapper {

	static final String[] REQUIRED_PROPERTIES = {
	};
	
	static Logger logger = Logger.getLogger( LanglinksConceptVectorMapper.class );
	
	private int[] m_idMapping;
	
	public LanglinksConceptVectorMapper(
			Configuration config,
			IConceptIndex sourceIndex, Language sourceLanguage,
			IConceptIndex targetIndex, Language targetLanguage ) throws Exception
			{
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		logger.info( "Initializing langlinks concept vector mapper (" + sourceLanguage + " to " + targetLanguage + ")" );
		
		ILanglinksMap langlinksMap = new SQLLanglinksMap(
				config,
				sourceLanguage, targetLanguage );
		
		m_idMapping = new int[sourceIndex.size()];
		for( int sourceConceptId=0; sourceConceptId<sourceIndex.size(); sourceConceptId++ )
		{
			if( logger.isDebugEnabled() && sourceConceptId % 100 == 0 ) {
				logger.debug( "Loading mapping ... (" + sourceConceptId + " of " + sourceIndex.size() + ")" );
			}

			int sourceArticleId = WikipediaCollection.buildArticleId( sourceIndex.getConceptName( sourceConceptId ) );
			int targetArticleId = langlinksMap.map( sourceArticleId );
 			
			String targetArticleConceptName = WikipediaCollection.buildDocumentName( targetArticleId );
			int targetConceptId = targetIndex.getConceptId( targetArticleConceptName );
			
			m_idMapping[sourceConceptId] = targetConceptId;
		}
		
	}
	
	public IConceptVector map( IConceptVector cv ) {
		logger.debug( "Mapping concept vector for document " + cv.getData().getDocName() );
		
		IConceptVector mappedCv = new TroveConceptVector( cv.getData().getDocName(), cv.size() );
		
		IConceptIterator it = cv.iterator();
		while( it.next() )
		{
			mappedCv.set( m_idMapping[it.getId()], it.getValue() );
		}

		return mappedCv;
	}
	
}
