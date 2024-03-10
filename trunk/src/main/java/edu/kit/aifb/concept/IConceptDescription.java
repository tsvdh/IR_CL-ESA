package edu.kit.aifb.concept;

import edu.kit.aifb.nlp.Language;

public interface IConceptDescription {

	public String getDescription( String conceptName, Language language ) throws Exception;
	
}
