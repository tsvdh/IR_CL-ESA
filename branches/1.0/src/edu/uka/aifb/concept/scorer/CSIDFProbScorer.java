package edu.uka.aifb.concept.scorer;

import edu.uka.aifb.api.concept.IConceptVectorData;


public class CSIDFProbScorer extends WikipediaMinerScorer {

	double m_sum = 0;
	int m_numberOfDocuments;
	
	double m_queryNorm1;
	double m_docNorm1;
	
	static int m_totalInlinks = 0;
	
	public double getScore() {
		return m_sum;
	}

	public void reset( IConceptVectorData queryData, IConceptVectorData docData, int numberOfDocuments ) {
		m_sum = 0;
		m_numberOfDocuments = numberOfDocuments;
		
		m_queryNorm1 = queryData.getNorm1();
		m_docNorm1 = docData.getNorm1();
		
		if( m_totalInlinks == 0 ) {
			for( int inlinks : m_queryLanguageInlinks ) {
				m_totalInlinks += inlinks;
			}
			for( int inlinks : m_docLanguageInlinks ) {
				m_totalInlinks += inlinks;
			}
		}
	}

	public void addConcept(
			int queryConceptId, double queryConceptScore,
			int docConceptId, double docConceptScore,
			int documentFrequency ) {
	
		double probConcept = (double)( m_queryLanguageInlinks[queryConceptId] + m_docLanguageInlinks[docConceptId] )
			/ (double)m_totalInlinks;
		
		double csQ = queryConceptScore / m_queryNorm1;
		double csD = docConceptScore / m_docNorm1;
		//double icfD = Math.log( m_numberOfDocuments / documentFrequency ) / Math.log( m_numberOfDocuments );
		
		m_sum += csQ * csD * probConcept;
	}

	public void finalizeScore( IConceptVectorData queryData, IConceptVectorData docData ) {
	}

	public boolean hasScore() {
		return m_sum > 0;
	}

}
