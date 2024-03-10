package edu.uka.aifb.terrier;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.structures.DocumentIndex;
import uk.ac.gla.terrier.structures.Index;
import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptIndex;
import edu.uka.aifb.ir.terrier.TerrierIndexFactory;
import edu.uka.aifb.nlp.Language;

public class TerrierESAIndex implements IConceptIndex {

	static Logger logger = Logger.getLogger( TerrierESAIndex.class );
	
	Index index;
	DocumentIndex documentIndex;

	Configuration m_config;
	Language m_language;
	
	public TerrierESAIndex( Configuration config, String indexId, Language language ) {
		index = TerrierIndexFactory.readIndex( indexId, language );
		documentIndex = index.getDocumentIndex();
		
		m_config = config;
		m_language = language;
	}
	
	public IConceptExtractor getConceptExtractor() {
		try {
			IConceptExtractor extractor = new TerrierESAConceptExtractor( m_config, index, m_language );
			return extractor;
		}
		catch( Exception e ) {
			logger.error( e );
			return null;
		}
	}

	public int getConceptId( String conceptName ) {
		return documentIndex.getDocumentId( conceptName );
	}

	public String getConceptName( int conceptId ) {
		return documentIndex.getDocumentNumber( conceptId );
	}

	public int size() {
		return documentIndex.getNumberOfDocuments();
	}

	public Index getTerrierIndex() {
		return index;
	}
}
