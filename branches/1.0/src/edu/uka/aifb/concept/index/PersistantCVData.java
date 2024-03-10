package edu.uka.aifb.concept.index;

import java.io.Serializable;

import edu.uka.aifb.api.concept.IConceptVectorData;


public class PersistantCVData implements IConceptVectorData, Serializable {

	private static final long serialVersionUID = -5652433970704377758L;

	private int m_conceptCount;
	private String m_docName;
	private double m_norm1;
	private double m_norm2;

	public PersistantCVData( IConceptVectorData data ) {
		m_conceptCount = data.getConceptCount();
		m_docName = data.getDocName();
		m_norm1 = data.getNorm1();
		m_norm2 = data.getNorm2();
	}
	
	public int getConceptCount() {
		return m_conceptCount;
	}

	public String getDocName() {
		return m_docName;
	}

	public double getNorm1() {
		return m_norm1;
	}

	public double getNorm2() {
		return m_norm2;
	}

}
