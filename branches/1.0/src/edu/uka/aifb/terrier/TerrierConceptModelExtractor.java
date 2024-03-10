package edu.uka.aifb.terrier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.models.WeightingModel;
import uk.ac.gla.terrier.structures.CollectionStatistics;
import uk.ac.gla.terrier.structures.DocumentIndex;
import uk.ac.gla.terrier.structures.Index;
import uk.ac.gla.terrier.structures.InvertedIndex;
import uk.ac.gla.terrier.structures.Lexicon;
import uk.ac.gla.terrier.structures.LexiconEntry;
import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptModel;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.ir.ITermEstimateModel;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.api.nlp.ITokenStream;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;

public class TerrierConceptModelExtractor implements IConceptExtractor {

	static final int MAX_DOC_SCORE_CACHE = 500;
	
	static final String[] REQUIRED_PROPERTIES = {
		"concepts.model_class",
	};
	
	static Logger logger = Logger.getLogger( TerrierConceptModelExtractor.class );
	
	private Language language;
	
	private int maxConceptId;
	private IConceptModel conceptModel;
	private ITermEstimateModel termEstimateModel;
	
	private Index index;
	private CollectionStatistics collectionStatistics;
	private Lexicon lexicon;
	private InvertedIndex invertedIndex;
	private DocumentIndex docIndex;
	
	private ITokenAnalyzer tokenAnalyzer;
	
	private double[][] docScores;
	private double[][] docScoresCache;
	private double[] smoothingWeights;

	private short[] support;
	
	protected TerrierConceptModelExtractor( Configuration config, Index index, Language language ) throws Exception {
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		
		this.index = index;
		collectionStatistics = index.getCollectionStatistics();
		lexicon = index.getLexicon();
		invertedIndex = index.getInvertedIndex();
		docIndex = index.getDocumentIndex();
		
		this.language = language;
		maxConceptId = index.getDocumentIndex().getNumberOfDocuments();
		
		logger.info( "Setting concept model: " + config.getString( "concepts.model_class" ) );
		conceptModel = (IConceptModel)Class.forName( config.getString( "concepts.model_class" ) ).newInstance();
		conceptModel.setIndex( index );
		termEstimateModel = conceptModel.getTermEstimatModel();
		
		docScoresCache = new double[MAX_DOC_SCORE_CACHE][];
		support = new short[maxConceptId];
		
		smoothingWeights = new double[maxConceptId];
		for( int i=0; i<maxConceptId; i++ ) {
			smoothingWeights[i] = termEstimateModel.getSmoothingWeight( docIndex.getDocumentNumber( i ) );
		}
	}
	
	private void reset( int numberOfQueryTerms ) {
		Arrays.fill( support, (short)0 );
		
		docScores = new double[numberOfQueryTerms][];
		for( int i=0; i<numberOfQueryTerms; i++ ) {
			if( i>=MAX_DOC_SCORE_CACHE ) {
				docScores[i] = new double[maxConceptId];
			}
			else {
				if( docScoresCache[i] == null ) {
					docScoresCache[i] = new double[maxConceptId];
				}
				else {
					Arrays.fill( docScoresCache[i], 0d );
				}
				docScores[i] = docScoresCache[i];
			}
		}
	}
	
	public IConceptVector extract( IDocument doc ) {
		return extract( doc.getName(), doc.getTokens( language ) );
	}
	
	public IConceptVector extract( IDocument doc, String... fields ) {
		return extract( doc.getName(), doc.getTokens( fields ) );
	}

	public IConceptVector extract( String docName, ITokenStream queryTokenStream ) {
		logger.info( "Extracting concepts for document " + docName + ", language=" + language );

		/*
		 * Build query
		 */
		ITokenStream ts = queryTokenStream;
		if( tokenAnalyzer != null) {
			ts = tokenAnalyzer.getAnalyzedTokenStream( ts );
		}

		Map<String,Integer> queryTermFrequencyMap = new HashMap<String, Integer>();
		Map<String,LexiconEntry> queryTermLexiconEntryMap = new HashMap<String, LexiconEntry>();
		while( ts.next() ) {
			String token = ts.getToken();
			if( token == null || token.length() == 0 ) {
				logger.debug( "Skipping empty token!" );
				continue;
			}
			if( !ts.getLanguage().equals( language ) ) {
				logger.error( "Skipping token, language=" + ts.getLanguage() );
				continue;
			}
			
			if( queryTermFrequencyMap.containsKey( token ) ) {
				queryTermFrequencyMap.put( token, queryTermFrequencyMap.get( token ) + 1 );
			}
			else {
				LexiconEntry lEntry = lexicon.getLexiconEntry( token );
				if( lEntry == null ) {
					logger.info( "Term not found: " + token );
				}
				else {
					queryTermFrequencyMap.put( token, 1 );
					queryTermLexiconEntryMap.put( token, lEntry );
				}
			}
		}
		
		int numberOfQueryTerms = queryTermFrequencyMap.size();
		if( numberOfQueryTerms == 0 ) {
			logger.info( "Empty document: " + docName );
			return new TroveConceptVector( docName, maxConceptId ); 
		}
		
		String[] queryTerms = new String[numberOfQueryTerms];
		int[] queryTermFrequencies = new int[numberOfQueryTerms];
		LexiconEntry[] lexiconEntries = new LexiconEntry[numberOfQueryTerms];
		double[] queryTermEstimates = new double[numberOfQueryTerms];
		int i=0;
		for( String queryTerm : queryTermFrequencyMap.keySet() ) {
			queryTerms[i] = queryTerm;
			queryTermFrequencies[i] = queryTermFrequencyMap.get( queryTerm );
			lexiconEntries[i] = queryTermLexiconEntryMap.get( queryTerm );
			queryTermEstimates[i] = termEstimateModel.getEstimate( queryTerm );
			i++;
		}
		
		reset( numberOfQueryTerms );
		
		/*
		 * Initialize weighting model
		 */
			
		//inform the weighting model of the collection statistics		
		WeightingModel wmodel = conceptModel.getWeightingModel();
		wmodel.setNumberOfTokens((double)collectionStatistics.getNumberOfTokens());
		wmodel.setNumberOfDocuments((double)collectionStatistics.getNumberOfDocuments());
		wmodel.setAverageDocumentLength((double)collectionStatistics.getAverageDocumentLength());
		wmodel.setNumberOfUniqueTerms((double)collectionStatistics.getNumberOfUniqueTerms());
		wmodel.setNumberOfPointers((double)collectionStatistics.getNumberOfPointers());
		
		wmodel.setKeyFrequency( 1 );
		
		/*
		 * Read inverted index and compute doc scores
		 */
		
		//the pointers read from the inverted file
		int[][] pointers;
		int activatedConceptCount = 0;
		
		for( i=0; i<numberOfQueryTerms; i++ ) {
			//the weighting model is prepared for assigning scores to documents
			wmodel.setDocumentFrequency( lexiconEntries[i].n_t);
			wmodel.setTermFrequency( lexiconEntries[i].TF );

			//the postings are being read from the inverted file.
			pointers = invertedIndex.getDocuments( lexiconEntries[i] );
			for( int j=0; j<pointers[0].length; j++ ) {
				int conceptId = pointers[0][j];
				double score = wmodel.score(
						pointers[1][j], 
						docIndex.getDocumentLength(conceptId));
				if( score > 0 ) {
					docScores[i][conceptId] = score;
					if( support[conceptId] == 0 ) {
						activatedConceptCount++;
					}
					support[conceptId]++;
				}
			}
		}
		logger.info( "Matched " + activatedConceptCount + " concepts." );

		IConceptVector cv = conceptModel.getConceptVector( 
				docName, 
				queryTerms, queryTermFrequencies,
				queryTermEstimates, smoothingWeights,
				docScores, support );
		logger.info( "Extracted concept vector with " + cv.count() + " activated concepts." );
		return cv;
	}

	public void setTokenAnalyzer( ITokenAnalyzer tokenAnalyzer ) {
		this.tokenAnalyzer = tokenAnalyzer;
	}

	public Index getIndex() {
		return index;
	}
}
