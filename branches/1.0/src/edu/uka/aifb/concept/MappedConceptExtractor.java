package edu.uka.aifb.concept;

import edu.uka.aifb.api.concept.IConceptExtractor;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.IConceptVectorMapper;
import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.api.nlp.ITokenStream;


public class MappedConceptExtractor implements IConceptExtractor {

	private IConceptVectorMapper m_mapper;
	
	private IConceptExtractor m_extractor;
	
	public MappedConceptExtractor( IConceptExtractor extractor, IConceptVectorMapper mapper ) {
		m_extractor = extractor;
		m_mapper = mapper;
	}
	
	public IConceptVector extract( IDocument doc ) {
		return m_mapper.map( m_extractor.extract( doc ) );
	}

	public void setTokenAnalyzer( ITokenAnalyzer tokenAnalyzer ) {
		m_extractor.setTokenAnalyzer( tokenAnalyzer );
	}

	public IConceptVector extract( IDocument doc, String... fields ) {
		return m_mapper.map( m_extractor.extract( doc, fields ) );
	}

	@Override
	public IConceptVector extract(String docName, ITokenStream queryTokenStream) {
		return m_mapper.map( m_extractor.extract( docName, queryTokenStream ) );
	}

}
