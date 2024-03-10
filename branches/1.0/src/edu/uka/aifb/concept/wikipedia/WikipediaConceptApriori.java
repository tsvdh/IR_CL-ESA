package edu.uka.aifb.concept.wikipedia;

import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.concept.IConceptApriori;
import edu.uka.aifb.api.concept.IConceptIndex;
import edu.uka.aifb.document.wikipedia.WikipediaCollection;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.tools.JdbcFactory;

public class WikipediaConceptApriori implements IConceptApriori {

	static final String[] REQUIRED_PROPERTIES = {
		"wikipedia.concepts.jdbc_id",
		"wikipedia.concepts.%1.apriori.table",
		"wikipedia.concepts.%1.apriori.article_id_column",
		"wikipedia.concepts.%1.apriori.value_column"
	};
	
	static Logger logger = Logger.getLogger( LanglinksConceptVectorMapper.class );
	
	double[] m_apriori;
	
	public WikipediaConceptApriori( Configuration config, IConceptIndex index, Language language ) throws Exception
	{
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES, language );
		m_apriori = new double[index.size()];
		
		Connection con = JdbcFactory.getConnection( config, config.getString( "wikipedia.concepts.jdbc_id" ) );
		
		String sql = "SELECT " + config.getString( "wikipedia.concepts." + language + ".apriori.article_id_column" ) + ", "
			+ config.getString( "wikipedia.concepts." + language + ".apriori.value_column" ) + " "
			+ "FROM " + config.getString( "wikipedia.concepts." + language + ".apriori.table" );
		
		ResultSet rs = con.createStatement().executeQuery( sql );
		while( rs.next() )
		{
			String conceptName = WikipediaCollection.buildDocumentName( rs.getInt( 1 ) );
			int conceptId = index.getConceptId( conceptName );
			if( conceptId >= 0 ) {
				m_apriori[conceptId] = rs.getDouble( 2 );
			}
		}
		rs.close();
	}
	
	public double getPropability( int conceptId ) {
		return m_apriori[conceptId];
	}

}
