package edu.kit.aifb.wikipedia.mlc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import edu.kit.aifb.JdbcFactory;
import gnu.trove.TIntArrayList;

public class MLCFactory implements ApplicationContextAware {
	private static Log logger = LogFactory.getLog( MLCFactory.class );
	
	ApplicationContext context;
	
	JdbcFactory jsb;
	String articleTable;
	String categoryTable;
	String categorylinksTable;
	
	int rootCatId;
	
	@Required
	public void setRootCatId( String id ) {
		this.rootCatId = Integer.parseInt( id );
	}
	
	@Required
	public void setArticleTable( String articleTable ) {
		this.articleTable = articleTable;
	}
	
	public String getArticleTable() {
		return articleTable;
	}
	
	@Required
	public void setCategoryTable( String categoryTable ) {
		this.categoryTable = categoryTable;
	}

	public MLCategory getRootCat() {
		return createMLCategory( rootCatId );
	}
	
	public String getCategoryTable() {
		return categoryTable;
	}

	@Autowired
	public void setJdbcFactory( JdbcFactory jsb ) {
		this.jsb = jsb;
	}

	public JdbcFactory getJdbcFactory() {
		return jsb;
	}

	@Required
	public void setCategorylinksTable( String categorylinksTable ) {
		this.categorylinksTable = categorylinksTable;
	}
	
	public String getCategorylinksTable() {
		return categorylinksTable;
	}
	
	public MLCategory createMLCategory( int id ) {
		return new MLCategory( this, id );
	}

	public MLArticle createMLArticle( int id ) {
		return new MLArticle( this, id );
	}

	public TIntArrayList readMLCategoryIds() throws SQLException {
		logger.info( "Reading category ids" );
		TIntArrayList categoryIds = new TIntArrayList();
		Statement st = jsb.createStatement();
		try {
			ResultSet rs = st.executeQuery(
					"select distinct mlc_id from "
					+ categoryTable
					+ " order by mlc_id;" );
			try {
				while( rs.next() ) {
					categoryIds.add( rs.getInt( 1 ) );
				}
				logger.info( "Found " + categoryIds.size() + " categories." );
				return categoryIds;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	/*public TIntArrayList readMLRootCategoryIds() throws SQLException {
		logger.info( "Reading root category ids" );
		TIntArrayList rootCategoryIds = new TIntArrayList();
		ResultSet rs = jsb.getStatement().executeQuery(
				"select mlc_id from "
				+ MLCConf.getCategoryStatisticsTable()
				+ " where cl_out=0"
				+ " order by mlc_id;" );
		while( rs.next() ) {
			rootCategoryIds.add( rs.getInt( 1 ) );
		}
		logger.info( "Found " + rootCategoryIds.size() + " root categories." );
		return rootCategoryIds;
	}*/

	/*public MLCategory getMLCategory( String title, Language language ) throws SQLException {
		IWikipediaDatabase wp = MLCConf.getWikipediaDatabase( language );
		IPage p = wp.getCategory( title );
		if( p == null ) {
			logger.error( "Page not found: " + title + "@" + language );
			return null;
		}

		PreparedStatement pst = jsb.getPreparedStatement(
				"select mlc_id from "
				+ MLCConf.getCategoryTable()
				+ " where mlc_lang=? and mlc_page=?;" );
		pst.setString( 1, language.toString() );
		pst.setInt( 2, p.getId() );
		
		ResultSet rs = pst.executeQuery();
		rs.next();
		return createMLCategory( jsb, rs.getInt(1) ); 
	}*/
	
	/*public static IMLArticle getMLArticle( JdbcStatementBuffer jsb, String title, Language language ) throws SQLException {
		IWikipediaDatabase wp = MLCConf.getWikipediaDatabase( language );
		IPage p = wp.getArticle( title );
		if( p == null ) {
			logger.error( "Page not found: " + title + "@" + language );
			return null;
		}
		
		PreparedStatement pst = jsb.getPreparedStatement(
				"select mlc_id from "
				+ MLCConf.getArticleTable()
				+ " where mlc_lang=? and mlc_page=?;" );
		pst.setString( 1, language.toString() );
		pst.setInt( 2, p.getId() );
		
		ResultSet rs = pst.executeQuery();
		rs.next();
		return createMLArticle( jsb, rs.getInt(1) ); 
	}*/

	public void setApplicationContext( ApplicationContext context )
			throws BeansException {
		this.context = context;
	}

}
