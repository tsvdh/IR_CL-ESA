package edu.uka.aifb.terrier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.Matching;
import uk.ac.gla.terrier.matching.MatchingQueryTerms;
import uk.ac.gla.terrier.matching.Model;
import uk.ac.gla.terrier.matching.ResultSet;
import uk.ac.gla.terrier.matching.dsms.DocumentScoreModifier;
import uk.ac.gla.terrier.querying.parser.MultiTermQuery;
import uk.ac.gla.terrier.querying.parser.SingleTermQuery;
import uk.ac.gla.terrier.structures.Index;
import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorBuilder;
import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.api.nlp.ITokenStream;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;

public class TerrierESAConceptExtractor implements IConceptExtractor {

	static final String[] REQUIRED_PROPERTIES = {
		"terrier.esa.model_class",
		"concepts.builder_class",
		"terrier.esa.document_scores_modifier_classes"
	};
	
	static Logger logger = Logger.getLogger( TerrierESAConceptExtractor.class );
	
	private Matching m_matching;
	
	private Language m_language;
	
	private int m_maxConceptId;
	
	private IConceptVectorBuilder conceptVectorBuilder;
	
	private ITokenAnalyzer m_tokenAnalyzer;
	
	private List<DocumentScoreModifier> documentScoreModifiers;
	
	protected TerrierESAConceptExtractor( Configuration config, Index index, Language language ) throws Exception {
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		
		m_language = language;
		
		m_maxConceptId = index.getDocumentIndex().getNumberOfDocuments();
		
		m_matching = new Matching( index );
		
		logger.info( "Setting model: " + config.getString( "terrier.esa.model_class" ) );
		Model model = (Model)Class.forName( config.getString( "terrier.esa.model_class" ) ).newInstance();
		m_matching.setModel( model );
		
		logger.info( "Setting concept vector builder: " + config.getString( "concepts.builder_class" ) );
		conceptVectorBuilder = (IConceptVectorBuilder)Class.forName( config.getString( "concepts.builder_class" ) ).newInstance();
		
		String[] documentScoreModifierClasses = config.getStringArray( "terrier.esa.document_scores_modifier_classes" );
		documentScoreModifiers = new ArrayList<DocumentScoreModifier>();
		for( int i=0; i<documentScoreModifierClasses.length; i++ ) {
			if( documentScoreModifierClasses[i].length() > 0 ) {
				logger.info( "Loading document score modifier: " + documentScoreModifierClasses[i] );
				documentScoreModifiers.add( 
					(DocumentScoreModifier)Class.forName( documentScoreModifierClasses[i] ).newInstance() );
			}
		}
	}
	
	public IConceptVector extract( IDocument doc ) {
		return extract( doc.getName(), doc.getTokens( m_language ) );
	}
	
	public IConceptVector extract( IDocument doc, String... fields ) {
		return extract( doc.getName(), doc.getTokens( fields ) );
	}

	public IConceptVector extract( String docName, ITokenStream queryTokenStream ) {
		logger.info( "Extracting concepts for document " + docName );
		
		/*
		 * Build query
		 */
		MultiTermQuery query = new MultiTermQuery();
		
		ITokenStream ts = queryTokenStream;
		if( m_tokenAnalyzer != null) {
			ts = m_tokenAnalyzer.getAnalyzedTokenStream( ts );
		}
		
		while( ts.next() ) {
			logger.trace( "Setting query term " + ts.getToken() );
			query.add( new SingleTermQuery( ts.getToken() ) );
		}
		logger.debug( query.toString() );

		/*
		 * Search
		 */
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		query.obtainQueryTerms( mqt );

		for( DocumentScoreModifier modifier : documentScoreModifiers ) {
			mqt.addDocumentScoreModifier( modifier );
		}
		
		if( mqt.getTerms() == null || mqt.getTerms().length == 0 ) {
			logger.debug( "Document is empty!" );
			return new TroveConceptVector( docName, m_maxConceptId );
		}
		
		m_matching.match( "ESA", mqt );
		
		/*
		 * Create concept vector
		 */
		ResultSet rs = m_matching.getResultSet();
		logger.info( "Found " + rs.getResultSize() + " matches in index." );
		
		conceptVectorBuilder.reset( docName, m_maxConceptId );
		conceptVectorBuilder.addScores( rs.getDocids(), rs.getScores() );
		return conceptVectorBuilder.getConceptVector();
	}

	public void setTokenAnalyzer( ITokenAnalyzer tokenAnalyzer ) {
		m_tokenAnalyzer = tokenAnalyzer;
	}

}
