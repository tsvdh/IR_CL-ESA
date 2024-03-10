package edu.uka.aifb.wikipedia.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.tools.JdbcFactory;


public class SQLStatementBuffer {
	
	protected Connection con;
	protected Map<String, PreparedStatement> statements = new HashMap<String, PreparedStatement>(); 
	
	public SQLStatementBuffer( Configuration config, String jdbcIdKey ) throws Exception {
		ConfigurationManager.checkProperties( config, new String[] { jdbcIdKey } );
		
		con = JdbcFactory.getConnection( config, config.getString( jdbcIdKey ) );
	}
	
	public PreparedStatement getStatement( String sql ) throws SQLException {
		if (!statements.containsKey(sql)) {
			PreparedStatement stmt = con.prepareStatement(sql);
			statements.put(sql, stmt);
		}
		return statements.get(sql);
	}

}
