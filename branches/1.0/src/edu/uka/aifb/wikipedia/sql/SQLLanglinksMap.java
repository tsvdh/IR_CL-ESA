package edu.uka.aifb.wikipedia.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.wikipedia.ILanglinksMap;
import edu.uka.aifb.nlp.Language;

public class SQLLanglinksMap extends SQLStatementBuffer implements ILanglinksMap {

	static final String[] REQUIRED_PROPERTIES = {
		"wikipedia.langlinks.jdbc_id"
	};
	
	static Logger logger = Logger.getLogger( SQLLanglinksMap.class );
	
	String llTable;
	String llSourceColumn;
	String llTargetColumn;
	
	public SQLLanglinksMap( 
			Configuration config, 
			Language sourceLanguage,
			Language targetLanguage) throws Exception
			{
		super( config, "wikipedia.langlinks.jdbc_id" );
		
		String languagePair = sourceLanguage + "_" + targetLanguage;
		
		if( config.containsKey( "wikipedia.langlinks." + languagePair + ".table" ) ) {
			llTable = config.getString( "wikipedia.langlinks." + languagePair + ".table" );
		}
		else {
			llTable = config.getString( "wikipedia.langlinks.table" );
		}
		
		if( config.containsKey( "wikipedia.langlinks." + languagePair + ".source_column" ) ) {
			llSourceColumn = config.getString( "wikipedia.langlinks." + languagePair + ".source_column" );
		}
		else {
			llSourceColumn = config.getString( "wikipedia.langlinks.source_column" );
		}
		
		if( config.containsKey( "wikipedia.langlinks." + languagePair + ".target_column" ) ) {
			llTargetColumn = config.getString( "wikipedia.langlinks." + languagePair + ".target_column" );
		}
		else {
			llTargetColumn = config.getString( "wikipedia.langlinks.target_column" );
		}
		
		logger.info( "Initializing map: " + llTable + "." + llSourceColumn + " -> " + llTable + "." + llTargetColumn );
	}

	public int map( int sourceId ) {
		try {
			String sql = 
				"select " + llTargetColumn + " " +
				"from " + llTable + " " +
				"where " + llSourceColumn + "=?;";
			PreparedStatement st = getStatement(sql);
			st.setInt( 1, sourceId );
			ResultSet rs = st.executeQuery();
			
			int targetId = -1;
			if( rs.next() ) {
				targetId = rs.getInt( 1 );
			}
			rs.close();
			
			return targetId;
		} catch (SQLException e) {
			logger.error(e);
			return -1;
		}
	}

}
