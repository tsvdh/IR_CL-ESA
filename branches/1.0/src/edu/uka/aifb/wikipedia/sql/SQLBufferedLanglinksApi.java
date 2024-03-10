package edu.uka.aifb.wikipedia.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.wikipedia.LanglinksApi;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.wikipedia.Page;

public class SQLBufferedLanglinksApi extends SQLStatementBuffer implements LanglinksApi {

	static Logger logger = Logger.getLogger(SQLBufferedLanglinksApi.class);
	
	Configuration config;
	
	String llTable;
	String llSourceColumn;
	String llTargetColumn;
	
	public SQLBufferedLanglinksApi( 
			Configuration config, 
			Language sourceLanguage,
			Language targetLanguage) throws Exception {
		super( config, "wikipedia.langlinks.jdbc_id" );
		
		this.config = config;
		
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
	}

	public Page getTargetPage(Page sourcePage) {
		try {
			String sql = 
				"select " + llTargetColumn + " " +
				"from " + llTable + " " +
				"where " + llSourceColumn + "=?;";
			PreparedStatement st = getStatement(sql);
			st.setInt(1, sourcePage.getId());
			ResultSet rs = st.executeQuery();
			Page p = null;
			if (rs.next()) {
				p = new Page(rs.getInt(1));
			}
			rs.close();
			return p;
		} catch (SQLException e) {
			logger.error(e);
			return null;
		}
	}
	
	public List<Page[]> getCommonCategories(Page a, Page b) {
		return null;
	}

	public int[] getTargetPageIds( int[] sourcePageIds ) {
		if( logger.isTraceEnabled() )
			logger.trace( "Building sql command string, #ids=" + sourcePageIds.length );
		StringBuilder idStringBuilder = new StringBuilder();
		if( sourcePageIds.length > 0)
		{
			idStringBuilder.append( sourcePageIds[0] );
			for (int i=1; i<sourcePageIds.length; i++) {
				idStringBuilder.append(',');
				idStringBuilder.append(sourcePageIds[i]);
			}
		}
		else {
			return new int[0];
		}
		
		try {
			String sql =
				"select " + llSourceColumn + "," + llTargetColumn + " " +
				"from " + llTable + " " +
				"where " + llSourceColumn + " in " +
				"(" + idStringBuilder.toString() + ") " +
				"order by " + llSourceColumn + ";";
			
			if (logger.isDebugEnabled())
				logger.debug("SQL: selectManyBySource");
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			
			int index = 0;
			int[] targetPageIds = new int[sourcePageIds.length];

			while (rs.next()) {
				int sourceId = rs.getInt(1);
				int targetId = rs.getInt(2);
				while (sourcePageIds[index] < sourceId && index < sourcePageIds.length) {
					targetPageIds[index] = -1;
					index++;
				}
				
				if (index >= sourcePageIds.length) {
					break;
				}
				
				if (sourcePageIds[index] == sourceId) {
					targetPageIds[index] = targetId;
					index++;
				}
			}
			rs.close();
			return targetPageIds;

		} catch (SQLException e) {
			logger.error(e);
			return null;
		}
	}

}
