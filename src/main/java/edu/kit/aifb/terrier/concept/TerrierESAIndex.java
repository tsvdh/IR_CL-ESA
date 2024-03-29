package edu.kit.aifb.terrier.concept;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.MetaIndex;

import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIndex;
import edu.kit.aifb.concept.IConceptIndexAware;
import edu.kit.aifb.concept.IConceptVectorBuilder;
import edu.kit.aifb.nlp.ITokenAnalyzer;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.TerrierIndexFactory;
import edu.kit.aifb.terrier.TerrierSearch;

public class TerrierESAIndex implements IConceptIndex {

	private static Log logger = LogFactory.getLog( TerrierESAIndex.class );
	
	Index index;
	DocumentIndex documentIndex;
	MetaIndex metaIndex;
	
	Language language;
	WeightingModel model;
	DocumentScoreModifier dsm;
	IConceptVectorBuilder builder;
	String indexId;
	
	ITokenAnalyzer analyzer;
	
	TerrierIndexFactory terrierIndexFactory;
	
	@Autowired  
	public void setTokenAnalyzer( ITokenAnalyzer analyzer ) {
		logger.info( "Setting token analyzer: " + analyzer.getClass().getName() );
		this.analyzer = analyzer;
	}
	
	@Autowired
	public void setTerrierIndexFactory( TerrierIndexFactory factory ) {
		terrierIndexFactory = factory;
	}
	
	@Required 
	public void setLanguage( Language language ) {
		logger.info( "Setting language: " + language );
		this.language = language;
	}

	@Required
	public void setIndexId( String indexId ) {
		logger.info( "Setting index: " + indexId );
		this.indexId = indexId;
	}
	
	@Required
	public void setWeightingModel( WeightingModel model ) {
		logger.info( "Setting weighting model: " + model.getClass().getName() );
		this.model = model;
	}
	
	public void setDocumentScoreModifier( DocumentScoreModifier dsm ) {
		if( dsm != null ) {
			logger.info( "Setting document score modifier: " + dsm.getClass().getName() );
			this.dsm = dsm;
		}
	}
	
	@Required
	public void setConceptVectorBuilder( IConceptVectorBuilder builder ) {
		logger.info( "Setting concept vector builder: " + builder.getClass().getName() );
		this.builder = builder;
		if( builder instanceof IConceptIndexAware ) {
			((IConceptIndexAware)builder).setConceptIndex( this );
		}
	}

	public void readIndex() throws IOException {
		index = terrierIndexFactory.readIndex( indexId, language );
		documentIndex = index.getDocumentIndex();
		metaIndex = index.getMetaIndex();
	}
	
	public IConceptExtractor getConceptExtractor() {
		TerrierSearch search = new TerrierSearch();
		if( dsm == null ) {
			search.setIndex( index, model );
		}
		else {
			search.setIndex( index, model, dsm );
		}
		search.setTokenAnalyzer( analyzer );
		
		TerrierESAConceptExtractor extractor = new TerrierESAConceptExtractor();
		extractor.setLanguage( language );
		extractor.setTerrierSearch( search );
		extractor.setConceptVectorBuilder( builder );
		return extractor;
	}

	public int getConceptId( String conceptName ) {
		try {
			return metaIndex.getDocument( "docno", conceptName );
		}
		catch( IOException e ) {
			logger.error( e );
			return -1;
		}
	}

	public String getConceptName( int conceptId ) {
		try {
			return metaIndex.getItem( "docno", conceptId );
		}
		catch( IOException e ) {
			logger.error( e );
			return null;
		}
	}

	public int size() {
		return documentIndex.getNumberOfDocuments();
	}

	public Index getIndex() {
		return index;
	}

	public Language getLanguage() {
		return language;
	}

	@Override
	public IConceptIndex clone() throws CloneNotSupportedException {
		try {
			TerrierESAIndex newIndex = new TerrierESAIndex();
			newIndex.setTokenAnalyzer( analyzer );
			newIndex.setTerrierIndexFactory( terrierIndexFactory );
			newIndex.setLanguage( language );
			newIndex.setIndexId( indexId );
			newIndex.setWeightingModel( model );
			newIndex.setDocumentScoreModifier( dsm );
			
			newIndex.setConceptVectorBuilder( builder.clone() );
			
			newIndex.readIndex();
			return newIndex;
		}
		catch( IOException e ) {
			logger.error( "Error while cloning: " + e );
		}
		return null;
	}
	
}
