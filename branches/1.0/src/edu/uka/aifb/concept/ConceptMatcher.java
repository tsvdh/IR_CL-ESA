package edu.uka.aifb.concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.CollectionResultSet;
import uk.ac.gla.terrier.matching.ResultSet;
import uk.ac.gla.terrier.utility.HeapSort;
import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorData;
import edu.uka.aifb.api.concept.index.ICVIndexEntryIterator;
import edu.uka.aifb.api.concept.index.ICVIndexReader;
import edu.uka.aifb.api.concept.search.IConceptMatcher;
import edu.uka.aifb.api.concept.search.IScorer;
import edu.uka.aifb.api.ir.IMatch;
import edu.uka.aifb.concept.index.PersistantCVData;
import edu.uka.aifb.ir.Match;


public class ConceptMatcher implements IConceptMatcher {

	static Logger logger = Logger.getLogger( ConceptMatcher.class );
	
	ICVIndexReader m_indexReader;
	IScorer[] m_documentScorers;
	
	ResultSet resultSet;
	
	public ConceptMatcher( ICVIndexReader indexReader ) {
		m_indexReader = indexReader;
		
		m_documentScorers = new IScorer[indexReader.getNumberOfDocuments()];
		
		resultSet = new CollectionResultSet( indexReader.getNumberOfDocuments() );
	}
	
	@SuppressWarnings("unchecked")
	public void setScorerClass( String className ) {
		try {
			Class scorerClass = Class.forName( className );
			setScorerClass( scorerClass );
		}
		catch( Exception e ) {
			logger.error( e );
		} 
	}
	
	@Override
	public void setScorerClass(Class<? extends IScorer> scorerClass) {
		logger.info( "Initializing concept matcher with scorer class " + scorerClass.getName() );
		
		for( int i=0; i<m_documentScorers.length; i++ )
		{
			try {
				m_documentScorers[i] = scorerClass.newInstance();
			}
			catch( Exception e ) {
				logger.error( e );
			} 
		}
	}

	public void match( IConceptVector queryCV ) {
		logger.debug( "Matching " + queryCV.getData().getDocName() );
		
		logger.debug( "Resetting scorers" );
		IConceptVectorData queryCVData = new PersistantCVData( queryCV.getData() );
		for( int i=0; i<m_documentScorers.length; i++ )
		{
			m_documentScorers[i].reset(
					queryCVData,
					m_indexReader.getConceptVectorData( i ),
					m_indexReader.getNumberOfDocuments() );
		}
		
		IConceptIterator conceptIt = queryCV.iterator();
		while( conceptIt.next() )
		{
			int conceptId = conceptIt.getId();
			int documentFrequency = m_indexReader.getDocumentFrequency( conceptId );
			
			if( logger.isTraceEnabled() ) {
				logger.trace( "Getting index entries for concept " + conceptId + " ...");
			}
			
			ICVIndexEntryIterator documentIt = m_indexReader.getIndexEntryIterator( conceptId );
			while( documentIt.next() )
			{
				int documentId = documentIt.getDocId();

				m_documentScorers[documentId].addConcept(
						conceptId, conceptIt.getValue(),
						conceptId, documentIt.getValue(),
						documentFrequency );
			}
			if( logger.isTraceEnabled() ) {
				logger.trace( "Done." );
			}
			
		}
		
		logger.debug( "Finalizing scores and computing ranking" );
		for( int i=0; i<m_documentScorers.length; i++ ) {
			if( m_documentScorers[i].hasScore() )
			{
				IConceptVectorData docData = m_indexReader.getConceptVectorData( i );
				
				m_documentScorers[i].finalizeScore(
						queryCVData,
						docData );
			}
		}
	}

	public List<IMatch> getMatches() {
		List<IMatch> matchList = new ArrayList<IMatch>();
		
		for( int i=0; i<m_documentScorers.length; i++ ) {
			if( m_documentScorers[i].hasScore() )
			{
				IConceptVectorData docData = m_indexReader.getConceptVectorData( i );
				
				matchList.add( new Match(
						docData.getDocName(),
						m_documentScorers[i].getScore() ) );
			}
		}
		
		logger.debug( "Sorting result list" );
		Collections.sort( matchList );

		logger.info( "Found " + matchList.size() + " matches." );
		return matchList;
	}

	@Override
	public ResultSet getResultSet() {
		resultSet.initialise();
		
		int[] ids = resultSet.getDocids();
		double[] scores = resultSet.getScores();
		short[] occurences = resultSet.getOccurrences();

		int numberOfRetrievedDocs = 0;
		for( int i=0; i<m_documentScorers.length; i++ ) {
			if( m_documentScorers[i].hasScore() ) {
				scores[i] = m_documentScorers[i].getScore();
				numberOfRetrievedDocs++;
			}
		}
		
		//sets the effective size of the result set.
		resultSet.setExactResultSize( numberOfRetrievedDocs );

		//sets the actual size of the result set.
		resultSet.setResultSize( numberOfRetrievedDocs );

		HeapSort.descendingHeapSort( scores, ids, occurences, numberOfRetrievedDocs );

		return resultSet;
	}
	
}
