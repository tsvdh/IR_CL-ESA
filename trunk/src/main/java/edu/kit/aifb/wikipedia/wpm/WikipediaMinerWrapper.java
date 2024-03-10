package edu.kit.aifb.wikipedia.wpm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.wikipedia.miner.model.Wikipedia;

import edu.kit.aifb.JdbcFactory;

public class WikipediaMinerWrapper {
	private Log logger = LogFactory.getLog( WikipediaMinerWrapper.class );
	
	JdbcFactory jdbcFactory;
	String database;
	
	Wikipedia wikipedia;
	Connection connection;
	
	@Autowired
	public void setJdbcFactory( JdbcFactory jdbcFactory ) {
		this.jdbcFactory = jdbcFactory;
	}

	@Required
	public void setDatabase( String database ) {
		this.database = database;
	}
	
	public Wikipedia getWikipedia() throws Exception {
		if( wikipedia == null || connection == null || connection.isClosed() ) {
			logger.info( "Connecting to WikipediaMiner: " + database );
			connection = jdbcFactory.createConnection();
			
			Statement stmt = connection.createStatement();
			try {
				String sql = "USE " + database;
				boolean result = stmt.execute( sql );
				if( !result ) {
					logger.warn( "SQL command failed: " + sql );
				}
			}
			finally {
				stmt.close();
			}
			wikipedia = new Wikipedia( database, connection );
		}
		return wikipedia;
	}
	
	@Override
	protected void finalize() {
		try {
			if( connection != null && !connection.isClosed() ) {
				connection.close();
			}
		}
		catch( SQLException e ) {
			logger.error( e );
		}
	}
}
