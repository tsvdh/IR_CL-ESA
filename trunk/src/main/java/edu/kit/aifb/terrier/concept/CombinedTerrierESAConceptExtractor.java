package edu.kit.aifb.terrier.concept;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terrier.matching.ResultSet;

import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.nlp.ITokenStream;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.TerrierSearch;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntIntHashMap;

public class CombinedTerrierESAConceptExtractor implements IConceptExtractor {

	private static Log logger = LogFactory.getLog( CombinedTerrierESAConceptExtractor.class );
	
	List<TerrierSearch> searches;
	TDoubleArrayList weights;
	List<TIntIntHashMap> idMaps;
	IConceptVectorBuilder conceptVectorBuilder;
	int maxConceptId;
	Language language;
	
	public CombinedTerrierESAConceptExtractor(
			List<TerrierSearch> searches, TDoubleArrayList weights, List<TIntIntHashMap> idMaps,
			IConceptVectorBuilder builder, Language language )
	{  
		this.language = language;
		this.idMaps = idMaps;
		this.searches = searches;
		this.weights = weights;
		this.conceptVectorBuilder = builder;

		if( searches.size() > 0 ) {
			maxConceptId = searches.get( 0 ).getIndex().getDocumentIndex().getNumberOfDocuments();
		}
		else  {
			maxConceptId = 0;
		}
	}

	public IConceptVector extract( IDocument doc ) {
		logger.info( "Extracting concepts for document " + doc.getName()+ ", language=" + language );
		reset( doc.getName() );
		for( int i=0; i<searches.size(); i++ ) {
			searches.get( i ).match( doc, language );
			addScores( i, searches.get( i ).getResultSet() );
		}
		return conceptVectorBuilder.getConceptVector();
	}
	
	public IConceptVector extract( IDocument doc, String... fields ) {
		logger.info( "Extracting concepts for document " + doc.getName() + ", fields=" + Arrays.toString( fields ) );
		reset( doc.getName() );
		for( int i=0; i<searches.size(); i++ ) {
			searches.get( i ).match( doc, fields );
			addScores( i, searches.get( i ).getResultSet() );
		}
		return conceptVectorBuilder.getConceptVector();
	}

	public IConceptVector extract( String docName, ITokenStream queryTokenStream ) {
		logger.info( "Extracting concepts for document " + docName );
		reset( docName );
		for( int i=0; i<searches.size(); i++ ) {
			searches.get( i ).match( queryTokenStream );
			addScores( i, searches.get( i ).getResultSet() );
		}
		return conceptVectorBuilder.getConceptVector();
	}
	
	private void reset( String docName ) {
		conceptVectorBuilder.reset( docName, maxConceptId );
	}
	
	private void addScores( int indexId, ResultSet rs ) {
		logger.info( "Found " + rs.getResultSize() + " matches in index " + indexId + "." );
		int[] docIds = rs.getDocids();
		double[] scores = rs.getScores();
		
		TIntIntHashMap currentIdMap = idMaps.get( indexId );
		double currentIndexWeight = weights.get( indexId ); 
		
		for( int i=0; i<rs.getResultSize(); i++ ) {
			if( indexId > 0 ) {
				docIds[i] = currentIdMap.get( docIds[i] );
			}
			scores[i] *= currentIndexWeight;
		}
		
		conceptVectorBuilder.addScores( docIds, rs.getScores(), rs.getResultSize() );
	}
	
}
