package edu.kit.aifb.concept;

import edu.kit.aifb.document.IDocument;
import edu.kit.aifb.nlp.ITokenStream;


public interface IConceptExtractor {

	public IConceptVector extract( IDocument doc );
	
	public IConceptVector extract( IDocument doc, String... fields );
	
	public IConceptVector extract( String docName, ITokenStream queryTokenStream );
	
}
