package edu.uka.aifb.concept.scorer;

import edu.uka.aifb.api.concept.IConceptVectorData;
import edu.uka.aifb.api.concept.search.IScorer;


public class KLDivergenceScorer implements IScorer {

	double m_sum = 0;
	
	double m_queryNorm1;
	double m_docNorm1;
	
	public void reset( IConceptVectorData queryData, IConceptVectorData docData, int numberOfDocuments ) {
		m_sum = 0;
		
		m_queryNorm1 = queryData.getNorm1();
		m_docNorm1 = docData.getNorm1();
	}

	public void addConcept( int queryConceptId, double queryConceptScore,
			int docConceptId, double docConceptScore,
			int conceptFrequency ) {
		
		m_sum -= queryConceptScore / m_queryNorm1 
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
