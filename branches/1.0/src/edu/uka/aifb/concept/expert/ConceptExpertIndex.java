package edu.uka.aifb.concept.expert;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.CollectionResultSet;
import uk.ac.gla.terrier.matching.ResultSet;
import uk.ac.gla.terrier.structures.CollectionStatistics;
import uk.ac.gla.terrier.structures.DocumentIndex;
import uk.ac.gla.terrier.structures.Index;
import uk.ac.gla.terrier.structures.Lexicon;
import uk.ac.gla.terrier.structures.LexiconEntry;
import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.index.ICVIndexReader;
import edu.uka.aifb.api.expert.IDocumentExpertIterator;
import edu.uka.aifb.api.expert.IExpertDocumentSet;
import edu.uka.aifb.api.expert.IExpertIndex;
import edu.uka.aifb.document.SingleTermTokenStream;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.terrier.TerrierConceptModelExtractor;
import edu.uka.aifb.tools.ConfigurationManager;

public class ConceptExpertIndex implements IExpertIndex {

	static final String[] REQUIRED_PROPERTIES = {
		"esa.concept_expert_index.use_apriori_model",
	}; 
	
	static Logger logger = Logger.getLogger( ConceptExpertIndex.class );
	
	ICVIndexReader indexReader;
	ResultSet rs;
	ConceptExpertDocumentSet eds;
	
	Map<Language,IConceptExtractor> conceptExtractors;
	
	boolean useAprioriModel;
	
	public ConceptExpertIndex( ICVIndexReader indexReader ) {
		Configuration config = ConfigurationManager.getCurrentConfiguration();
		ConfigurationManager.checkProperties( config,REQUIRED_PROPERTIES );
		
		useAprioriModel = config.getBoolean( "esa.concept_expert_index.use_apriori_model" ); 
		logger.info( "Using apriori model: " + useAprioriModel );
		
		this.indexReader = indexReader;
		conceptExtractors = new HashMap<Language, IConceptExtractor>();
		
		rs = new CollectionResultSet( indexReader.getNumberOfDocuments() );
		eds = new ConceptExpertDocumentSet( indexReader );
	}
	
	@Override
	public int getDocId( String docName ) {
		return indexReader.getConceptVectorId( docName );
	}

	@Override
	public String getDocName( int docId ) {
		return indexReader.getConceptVectorData( docId ).getDocName();
	}

	@Override
	public IExpertDocumentSet getExpertDocumentSet() {
		return eds;
	}

	@Override
	public String getId() {
		return "ConceptIndex";
	}

	@Override
	public boolean isSupportedLanguage( Language language ) {
		return conceptExtractors.containsKey( language );
	}

	@Override
	public ResultSet match( String token, Language language ) {
		IConceptVector cv = conceptExtractors.get( language ).extract(
				"query",
				new SingleTermTokenStream( token, language ) );
		
		int[] ids = rs.getDocids();
		double[] scores = rs.getScores();
		int count = 0;
		
		if( useAprioriModel ) {
			AprioriModel aprioriModel = new AprioriModel( conceptExtractors.get( language ) );
			double termApriori = aprioriModel.getTokenProbability( token );

			IConceptIterator it = cv.orderedIterator();
			while( it.next() ) {
				double conceptApriori = aprioriModel.getConceptProbability( it.getId() );
				if( logger.isDebugEnabled() ) {
					logger.debug( "Apriori probability: term=" + termApriori + ", concept=" + conceptApriori + ", p(t)/p(c)=" + termApriori / conceptApriori );
				}

				ids[count] = it.getId();
				scores[count] = it.getValue() * termApriori / conceptApriori;
				count++;
			}
		}
		else {
			IConceptIterator it = cv.orderedIterator();
			while( it.next() ) {
				ids[count] = it.getId();
				scores[count] = it.getValue();
				count++;
			}
		}
		
		rs.setExactResultSize( count );
		rs.setResultSize( count );
		return rs;
	}

	@Override
	public IDocumentExpertIterator getDocumentExpertIterator( int docId ) {
		return new ConceptDocumentExpertIterator( indexReader.getIndexEntryIterator( docId ) );
	}

	public void setConceptExtractor( IConceptExtractor extractor, Language language ) {
		conceptExtractors.put( language, extractor );
	}

	@Override
	public boolean useTranslations() {
		return false;
	}

	class AprioriModel {
		Index index;
		DocumentIndex docIndex;
		Lexicon lexicon;
		CollectionStatistics colStatistics;
		
		public AprioriModel( IConceptExtractor extractor ) {
			if( extractor instanceof TerrierConceptModelExtractor ) {
				index = ((TerrierConceptModelExtractor)extractor).getIndex();
				docIndex = index.getDocumentIndex();
				lexicon = index.getLexicon();
				colStatistics = index.getCollectionStatistics();
			}
		}
		
		public double getTokenProbability( String token ) {
			if( lexicon != null ) {
				LexiconEntry e = lexicon.getLexiconEntry( token );
				if( e != null ) {
					double pOfT = (double)e.TF / (double)colStatistics.getNumberOfTokens();
					double idf = Math.log( (double)colStatistics.getNumberOfDocuments() / (double)e.n_t )
						/ Math.log( (double)colStatistics.getNumberOfDocuments() );

					return pOfT * idf;
				}
			}
			return 1;
		}
		
		private double getConceptProbability( int conceptId ) {
			if( docIndex != null ) {
				return 1d / colStatistics.getNumberOfDocuments(); 
			}
			return 1;
		}
	}
	
}
