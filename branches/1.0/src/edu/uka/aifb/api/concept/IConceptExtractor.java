package edu.uka.aifb.api.concept;

import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.api.nlp.ITokenStream;


public interface IConceptExtractor {

	public void setTokenAnalyzer( ITokenAnalyzer tokenAnalyzer );
	
	public IConceptVector extract( IDocument doc );
	
	public IConceptVector extract( IDocument doc, String... fields );
	
	public IConceptVector extract( String docName, ITokenStream queryTokenStream );
	
}
