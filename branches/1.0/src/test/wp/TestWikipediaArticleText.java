package test.wp;

import org.apache.commons.configuration.Configuration;

import edu.uka.aifb.api.wikipedia.IPage;
import edu.uka.aifb.api.wikipedia.IWikipediaDatabase;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.wikipedia.WikipediaTools;
import edu.uka.aifb.wikipedia.sql.SQLWikipediaDatabase;

public class TestWikipediaArticleText {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Configuration config = ConfigurationManager.parseArgs( args );
		
		IWikipediaDatabase wp = new SQLWikipediaDatabase( config, Language.EN );
		
		IPage p = wp.getArticle( "Mulled_wine" );
		String text = wp.getText( p );
		
		System.out.println( "Wiki text:" );
		System.out.println( text );
		
		System.out.println( "\nCleaned text:" );
		System.out.println( WikipediaTools.extractPlainText( text ) );
	}
}
