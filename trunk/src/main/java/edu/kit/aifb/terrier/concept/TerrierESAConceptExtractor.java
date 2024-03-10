package edu.kit.aifb.terrier.concept;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.terrier.matching.ResultSet;

import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.nlp.ITokenStream;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.TerrierSearch;

public class TerrierESAConceptExtractor implements IConceptExtractor {

	private static Log logger = LogFactory.getLog( TerrierESAConceptExtractor.class );
	
	TerrierSearch search;
	IConceptVectorBuilder conceptVectorBuilder;
	int maxConceptId;
	Language language;
	
	@Required
	public void setLanguage( Language language ) {
		this.language = language;
	}
	
	@Required
	public void setTerrierSearch( TerrierSearch search ) {
		this.search = search;
		maxConceptId = search.getIndex().getDocumentIndex().getNumberOfDocuments();
	}
	
	@Required
	public void setConceptVectorBuilder( IConceptVectorBuilder builder ) {
		conceptVectorBuilder = builder;
	}

	public IConceptVector extract( IDocument doc ) {
		logger.info( "Extracting concepts for document " + doc.getName() );
		search.match( doc, language );
		return buildVector( doc.getName(), search.getResultSet() );
	}
	
	public IConceptVector extract( IDocument doc, String... fields ) {
		logger.info( "Extracting concepts for document " + doc.getName() );
		search.match( doc, fields );
		return buildVector( doc.getName(), search.getResultSet() );
	}

	public IConceptVector extract( String docName, ITokenStream queryTokenStream ) {
		logger.info( "Extracting concepts for document " + docName );
		search.match( queryTokenStream );
		return buildVector( docName, search.getResultSet() );
	}
	
	public IConceptVector buildVector( String docName, ResultSet rs ) {
		logger.info( "Found " + rs.getResultSize() + " matches in index." );
		
		return conceptVectorBuilder.getConceptVector(
				docName, maxConceptId,
				rs.getDocids(), rs.getScores(), rs.getResultSize() );
	}

}
