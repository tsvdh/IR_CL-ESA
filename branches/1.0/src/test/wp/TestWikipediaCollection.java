package test.wp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.document.ICollection;
import edu.uka.aifb.api.document.ICollectionIterator;
import edu.uka.aifb.api.document.IDocument;
import edu.uka.aifb.api.nlp.ITokenAnalyzer;
import edu.uka.aifb.api.nlp.ITokenStream;
import edu.uka.aifb.document.wikipedia.WikipediaMLConceptCollection;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.nlp.MultiLingualAnalyzer;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.tools.JdbcFactory;
import edu.uka.aifb.tools.JdbcStatementBuffer;

public class TestWikipediaCollection {
	
	static final String[] REQUIRED_PROPERTIES = {
		"connection_id",
	};
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Configuration config = ConfigurationManager.parseArgs( args );
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );

		Logger.getRootLogger().setLevel( Level.INFO );

		ITokenAnalyzer analyzer = new MultiLingualAnalyzer( config );
		
		JdbcStatementBuffer jsb = new JdbcStatementBuffer(
				JdbcFactory.getConnection( config, config.getString( "connection_id" ) ) );
		
		ICollection wpCol = new WikipediaMLConceptCollection( config, jsb, Language.DE );
		System.out.println( "Collection size=" + wpCol.size() );
		
		ICollectionIterator it = wpCol.iterator();
		BufferedWriter out = new BufferedWriter( new FileWriter( "test_wikipedia.txt" ) );
		while( it.next() ) {
			IDocument doc = it.getDocument();
			out.write( "Name: " + doc.getName() + "\n" );
			//out.write( "Content: " + doc.getText() + "\n" );

			out.write( "Tokens:" );
			ITokenStream ts = analyzer.getAnalyzedTokenStream( doc.getTokens() );
			while( ts.next() ) {
				if( !Pattern.matches( "[a-z0-9]+", ts.getToken() ) ) {
					out.write( " " + ts.getLanguage() + ":" + ts.getToken() );
				}
			};
			
			out.write( "\n" );
			out.write( "###############################\n" );
			out.write( "#                             #\n" );
			out.write( "###############################\n" );
			out.write( "\n" );
		}
		out.close();
	}

}
