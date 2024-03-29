package edu.kit.aifb.concept.scorer;

import edu.kit.aifb.concept.IConceptVectorData;


public class CSIDFScorer implements IScorer {

	double m_sum = 0;
	int m_numberOfDocuments;
	
	double m_docNorm1;
	
	public double getScore() {
		return m_sum;
	}

	public void reset( IConceptVectorData queryData, IConceptVectorData docData, int numberOfDocuments ) {
		m_sum = 0;
		m_numberOfDocuments = numberOfDocuments;
		
		m_docNorm1 = docData.getNorm1();
	}

	public void addConcept(
			int queryConceptId, double queryConceptScore,
			int docConceptId, double docConceptScore,
			int documentFrequency ) {
	
		double csQ = queryConceptScore;
		double csD = docConceptScore / m_docNorm1;
		double idf = Math.log( m_numberOfDocuments / documentFrequency );
		
		m_sum += csQ * csD * idf;
	}

	public void finalizeScore( IConceptVectorData queryData, IConceptVectorData docData ) {
	}

	public boolean hasScore() {
		return m_sum > 0;
	}

}
