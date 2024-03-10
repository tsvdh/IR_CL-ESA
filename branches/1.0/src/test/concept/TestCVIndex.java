package test.concept;



import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.ResultSet;
import edu.uka.aifb.api.concept.index.ICVIndexBuilder;
import edu.uka.aifb.api.concept.index.ICVIndexEntryIterator;
import edu.uka.aifb.api.concept.index.ICVIndexReader;
import edu.uka.aifb.api.ir.IMatch;
import edu.uka.aifb.concept.ConceptMatcher;
import edu.uka.aifb.concept.TroveConceptVector;
import edu.uka.aifb.concept.index.IndexedFileCVIndexBuilder;
import edu.uka.aifb.concept.index.IndexedFileCVIndexReader;
import edu.uka.aifb.concept.scorer.CosineScorer;
import edu.uka.aifb.tools.ConfigurationManager;

public class TestCVIndex {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Configuration config = ConfigurationManager.parseArgs(args);
		config.addProperty( "terrier.esa.document_scores_modifier_classes", "" );
		
		Logger.getRootLogger().setLevel( Level.DEBUG );

		ICVIndexBuilder builder = new IndexedFileCVIndexBuilder( config, 10, "test", null );
		TroveConceptVector cv0 = new TroveConceptVector( "doc0", 10 );
		cv0.add( 4, .1 );
		cv0.add( 7, .3 );
		builder.add( cv0 );
		
		TroveConceptVector cv1 = new TroveConceptVector( "doc1", 10 );
		cv1.add( 4, .4 );
		cv1.add( 9, .1 );
		builder.add( cv1 );
		builder.buildIndex();
		
		ICVIndexReader reader = new IndexedFileCVIndexReader( config, "test", null );
		ICVIndexEntryIterator it = reader.getIndexEntryIterator( 9 );
		while( it.next() ) {
			System.out.println( reader.getConceptVectorData( it.getDocId() ).getDocName() + ": " + it.getValue() );
		}
		
		ConceptMatcher matcher = new ConceptMatcher( reader );
		matcher.setScorerClass( CosineScorer.class );

		TroveConceptVector cv2 = new TroveConceptVector( "query", 10 );
		cv2.add( 4, .1 );

		matcher.match( cv2 );
		List<IMatch> matches = matcher.getMatches();
		for( IMatch match : matches ) {
			System.out.println( match.getScore() + ": " + match.getDocName() );
		}
		
		ResultSet rs = matcher.getResultSet();
		int[] ids = rs.getDocids();
		double[] values = rs.getScores();
		for( int i=0; i<rs.getResultSize(); i++ ) {
			System.out.println( values[i] + ": " + reader.getConceptVectorData( ids[i] ).getDocName() );
		}
		
	}

}
