package edu.kit.aifb.concept.scorer;



public abstract class WikipediaMinerScorer implements IScorer {

	static int[] m_queryLanguageInlinks;
	static int[] m_docLanguageInlinks;
	
	public static void setInlinks( int[] queryLanguageInlinks, int[] docLanguageInlinks ) {
		m_queryLanguageInlinks = queryLanguageInlinks;
		m_docLanguageInlinks = docLanguageInlinks;
	}
	
}
