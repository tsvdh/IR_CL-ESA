package edu.uka.aifb.concept.expert;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terrier.matching.CollectionResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;

import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.index.ICVIndexReader;
import edu.kit.aifb.document.SingleTermTokenStream;
import edu.kit.aifb.document.expert.IDocumentExpertIterator;
import edu.kit.aifb.document.expert.IExpertDocumentSet;
import edu.kit.aifb.document.expert.IExpertIndex;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.concept.TerrierConceptModelExtractor;

public class ConceptExpertIndex implements IExpertIndex {

	static Log logger = LogFactory.getLog( ConceptExpertIndex.class );
	
	ICVIndexReader indexReader;
	ResultSet rs;
	ConceptExpertDocumentSet eds;
	
	Map<Language,IConceptExtractor> conceptExtractors;
	
	public ConceptExpertIndex( ICVIndexReader indexReader ) {
		this.indexReader = indexReader;
		conceptExtractors = new HashMap<Language, IConceptExtractor>();
		
		rs = new CollectionResultSet( indexReader.getNumberOfDocuments() );
		eds = new ConceptExpertDocumentSet( indexReader );
	}
	
	public int getDocId( String docName ) {
		return indexReader.getConceptVectorId( docName );
	}

	public String getDocName( int docId ) {
		return indexReader.getConceptVectorData( docId ).getDocName();
	}

	public IExpertDocumentSet getExpertDocumentSet() {
		return eds;
	}

	public String getId() {
		return "ConceptIndex";
	}

	public boolean isSupportedLanguage( Language language ) {
		return conceptExtractors.containsKey( language );
	}

	public ResultSet match( String token, Language language ) {
		IConceptVector cv = conceptExtractors.get( language ).extract(
				"query",
				new SingleTermTokenStream( token, language ) );
		
		int[] ids = rs.getDocids();
		double[] scores = rs.getScores();
		
		AprioriModel aprioriModel = new AprioriModel( conceptExtractors.get( language ) );
		double termApriori = aprioriModel.getTokenProbability( token );
		
		int count = 0;
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
		rs.setExactResultSize( count );
		rs.setResultSize( count );
		return rs;
	}

	public IDocumentExpertIterator getDocumentExpertIterator( int docId ) {
		return new ConceptDocumentExpertIterator( indexReader.getIndexEntryIterator( docId ) );
	}

	public void setConceptExtractor( IConceptExtractor extractor, Language language ) {
		conceptExtractors.put( language, extractor );
	}

	public boolean useTranslations() {
		return false;
	}

	class AprioriModel {
		Index index;
		DocumentIndex docIndex;
		Lexicon<String> lexicon;
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
					double pOfT = (double)e.getFrequency() / (double)colStatistics.getNumberOfTokens();
					double idf = Math.log( (double)colStatistics.getNumberOfDocuments() / (double)e.getDocumentFrequency() )
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
