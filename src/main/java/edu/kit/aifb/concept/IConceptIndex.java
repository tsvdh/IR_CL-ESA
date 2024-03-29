package edu.kit.aifb.concept;

import edu.kit.aifb.nlp.ITokenAnalyzer;
import edu.kit.aifb.nlp.Language;


public interface IConceptIndex extends Cloneable {

	public IConceptExtractor getConceptExtractor();
	
	public String getConceptName( int conceptId );
	
	public int getConceptId( String conceptName );
	
	public int size();
	
	public void setTokenAnalyzer( ITokenAnalyzer analyzer );
	
	public void setLanguage( Language language );
	public Language getLanguage();
	
	public IConceptIndex clone() throws CloneNotSupportedException;
}
