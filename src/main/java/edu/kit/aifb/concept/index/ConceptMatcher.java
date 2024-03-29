package edu.kit.aifb.concept.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terrier.matching.CollectionResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.utility.HeapSort;

import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorData;
import edu.kit.aifb.concept.scorer.IScorer;
import edu.kit.aifb.ir.IMatch;
import edu.kit.aifb.ir.Match;


public class ConceptMatcher {

	static Log logger = LogFactory.getLog( ConceptMatcher.class );
	
	ICVIndexReader m_indexReader;
	IScorer[] m_documentScorers;
		
	ResultSet resultSet;

	int resultLimit = Integer.MAX_VALUE;
	
	public ConceptMatcher( ICVIndexReader indexReader ) {
		m_indexReader = indexReader;
		m_documentScorers = new IScorer[indexReader.getNumberOfDocuments()];
		resultSet = new CollectionResultSet( indexReader.getNumberOfDocuments() );
	}
	
	public void setResultLimit( int resultLimit ) {
		this.resultLimit = resultLimit;
	}
	
	/**
	 * This sets the scoring model for the concept based retrieval.
	 * 
	 * @param className The name of the IScorer class that implements the
	 * concept based retrieval model.
	 */
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
	
	/**
	 * This sets the scoring model for the concept based retrieval.
	 * 
	 * @param scorerClass The scorer class that implements IScorer interface.
	 */
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

	/**
	 * This function performs a complete retrieval step for a specified query which
	 * is represented by a concept vector.  
	 * 
	 * @param queryCV The concept vector of the query that should be searched for.
	 */
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

	/**
	 * This function returns a sorted list of matches found for a previous call of match().
	 * 
	 * @return A sorted list of matches containing relevant documents and their scores.
	 */
	public List<IMatch> getMatches() {
		List<IMatch> matchList = new ArrayList<IMatch>();
		
		ResultSet rs = getResultSet();
		int[] ids = rs.getDocids();
		double[] scores = rs.getScores();
		
		for( int i=0; i<rs.getResultSize(); i++ ) {
			IConceptVectorData docData = m_indexReader.getConceptVectorData( ids[i] );

			matchList.add( new Match(
					docData.getDocName(),
					scores[i] ) );
		}
		
		logger.info( "Found " + matchList.size() + " matches." );
		return matchList;
	}

	/**
	 * This function returns a sorted list of matches found for a previous call of match().
	 * 
	 * @return A result set containing relevant documents and their scores.
	 */
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
		if( numberOfRetrievedDocs > resultLimit ) {
			resultSet.setResultSize( resultLimit );
		}
		
		HeapSort.descendingHeapSort( scores, ids, occurences, resultSet.getResultSize() );
		return resultSet;
	}
	
}
