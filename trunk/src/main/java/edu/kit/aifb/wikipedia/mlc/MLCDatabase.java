package edu.kit.aifb.wikipedia.mlc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.JdbcFactory;
import edu.kit.aifb.nlp.Language;
import gnu.trove.TIntArrayList;

public class MLCDatabase {

	private static Log logger = LogFactory.getLog( MLCDatabase.class );
	
	String mlcTable;
	String mlcCategorylinksTable;
	JdbcFactory jsb;
	TIntArrayList conceptIds;
	
	@Required
	public void setMlcTable( String table ) {
		mlcTable = table;
	}
	
	@Autowired
	public void setJdbcFactory( JdbcFactory jsb ) {
		this.jsb = jsb;
	}
	
	public String getMlcTable() {
		return mlcTable;
	}
	
	public void setMlcCategorylinksTable( String table ) {
		mlcCategorylinksTable = table;
	}
	
	public String getMlcCategorylinksTable() {
		return mlcCategorylinksTable;
	}
	
	public void readConcepts() throws SQLException {
		logger.info( "Reading concept ids from " + mlcTable );
		conceptIds = new TIntArrayList();
		PreparedStatement pst = jsb.prepareStatement(
				"select distinct mlc_id from " + mlcTable + " order by mlc_id;" );
		try {
			ResultSet rs = pst.executeQuery();
			try {
				while( rs.next() ) {
					conceptIds.add( rs.getInt( 1 ) );
				}
				logger.info( "Found " + conceptIds.size() + " concepts." );
			}
			finally {
				rs.close();
			}
		}
		finally {
			pst.close();
		}
	}
	
	public TIntArrayList getPageIds( int conceptId, Language language ) throws SQLException {
		PreparedStatement pst = jsb.prepareStatement(
				"select mlc_page from " + mlcTable + " where mlc_id=? and mlc_lang=?;" );
		try {
			pst.setInt( 1, conceptId );
			pst.setString( 2, language.toString() );

			TIntArrayList pageIds = new TIntArrayList();
			ResultSet pageResultSet = pst.executeQuery();
			try {
				while( pageResultSet.next() ) {
					pageIds.add( pageResultSet.getInt( 1 ) );
				}
				return pageIds;
			}
			finally {
				pageResultSet.close();
			}
		}
		finally {
			pst.close();
		}
	}
	
	public TIntArrayList getMlcArticleIdsInCategory( int categoryId ) throws SQLException {
		TIntArrayList mlcArticleIds = new TIntArrayList();
		if( mlcCategorylinksTable == null ) {
			logger.error( "Table for MLC category links was not set!" );
		}
		else {
			PreparedStatement pstLinks = jsb.prepareStatement(
					"select mlcl_from from " + mlcCategorylinksTable
					+ " where mlcl_namespace=0 and mlcl_to=?;" );
			if( logger.isDebugEnabled() )
				logger.debug( "Retrieving mlc concepts linking to category " + categoryId );
			try {
				pstLinks.setInt( 1, categoryId );
				ResultSet linkResultSet = pstLinks.executeQuery();
				try {
					while( linkResultSet.next() ) {
						mlcArticleIds.add( linkResultSet.getInt( 1 ) );
					}
				}
				finally {
					linkResultSet.close();
				}
			}
			finally {
				pstLinks.close();
			}
		}
		return mlcArticleIds;
	}
	
	public int size() {
		return conceptIds.size();
	}
	
	public TIntArrayList getConceptIds() {
		return conceptIds;
	}

	static public String getConceptName( int conceptId ) { 
		StringBuilder docNameBuilder = new StringBuilder();
		Formatter docIdFormatter = new Formatter( docNameBuilder );

		docIdFormatter.format( "%1$010d", conceptId );
		return docNameBuilder.toString();
	}
	
	static public int getConceptId( String docName ) {
		return Integer.parseInt( docName );
	}

}
