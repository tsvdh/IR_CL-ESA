package edu.kit.aifb.concept.scorer;

import edu.kit.aifb.concept.IConceptVectorData;


public class LMScorer implements IScorer {

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
	
		double pAgivenQ = queryConceptScore;
		double pAgivenD = docConceptScore / m_docNorm1;
		double pA = (double)documentFrequency / (double)m_numberOfDocuments;
		
		m_sum += pAgivenQ * pAgivenD / pA;
	}

	public void finalizeScore( IConceptVectorData queryData, IConceptVectorData docData ) {
	}

	public boolean hasScore() {
		return m_sum > 0;
	}

}
