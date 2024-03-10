package edu.uka.aifb.concept.scorer;

import edu.uka.aifb.api.concept.search.IScorer;


public abstract class WikipediaMinerScorer implements IScorer {

	static int[] m_queryLanguageInlinks;
	static int[] m_docLanguageInlinks;
	
	public static void setInlinks( int[] queryLanguageInlinks, int[] docLanguageInlinks ) {
		m_queryLanguageInlinks = queryLanguageInlinks;
		m_docLanguageInlinks = docLanguageInlinks;
	}
	
}
