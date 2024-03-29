package edu.kit.aifb.concept;

import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.nlp.ITokenStream;


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

	public IConceptVector extract( IDocument doc, String... fields ) {
		return m_mapper.map( m_extractor.extract( doc, fields ) );
	}

	public IConceptVector extract(String docName, ITokenStream queryTokenStream) {
		return m_mapper.map( m_extractor.extract( docName, queryTokenStream ) );
	}

}
