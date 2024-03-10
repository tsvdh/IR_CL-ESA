package edu.kit.aifb.concept.scorer;

import edu.kit.aifb.concept.IConceptVectorData;


public class KLDivergenceIDFScorer implements IScorer {

	double m_sum = 0;
	
	double m_queryNorm1;
	double m_docNorm1;
	
	int m_numberOfDocuments;
	
	public void reset( IConceptVectorData queryData, IConceptVectorData docData, int numberOfDocuments ) {
		m_sum = 0;
		
		m_queryNorm1 = queryData.getNorm1();
		m_docNorm1 = docData.getNorm1();
		
		m_numberOfDocuments = numberOfDocuments;
	}

	public void addConcept( int queryConceptId, double queryConceptScore,
			int docConceptId, double docConceptScore,
			int documentFrequency ) {
		
		double idfD = Math.log( m_numberOfDocuments / documentFrequency ) / Math.log( m_numberOfDocuments );
		
		m_sum -= idfD * queryConceptScore / m_queryNorm1 
		* Math.log( docConceptScore / m_docNorm1 );
	}

	public void finalizeScore( IConceptVectorData queryData, IConceptVectorData docData ) {
	}

	public double getScore() {
		return m_sum;
	}

	public boolean hasScore() {
		return m_sum > 0;
	}

}
