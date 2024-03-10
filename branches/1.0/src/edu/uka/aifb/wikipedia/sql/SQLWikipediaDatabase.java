package edu.uka.aifb.wikipedia.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.wikipedia.IPage;
import edu.uka.aifb.api.wikipedia.IWikipediaDatabase;
import edu.uka.aifb.api.wikipedia.PropertyNotInitializedException;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;
import edu.uka.aifb.wikipedia.Page;
import gnu.trove.TIntArrayList;

public class SQLWikipediaDatabase extends SQLStatementBuffer implements IWikipediaDatabase {

	static final String[] REQUIRED_PROPERTIES = {
		"wikipedia.database.%1.database"
	};
	
	static Logger logger = Logger.getLogger(SQLWikipediaDatabase.class); 
		
	String m_db;
	Language m_language;
	
	public SQLWikipediaDatabase( Configuration config, Language language) throws Exception {
		super( config, "wikipedia.database.jdbc_id" );
		
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES, language );
		m_db = config.getString( "wikipedia.database." + language + ".database" );
		
		m_language = language;
		
		logger.info( "Initializing, language=" + m_language + ", db=" + m_db );
	}

	public Collection<IPage> getCategories(IPage page) {
		try {
			Collection<IPage> articles = new ArrayList<IPage>();
			
			String sql =
				"select page_id, page_title, page_is_redirect "
				+"from "+m_db+".page, "+m_db+".categorylinks "
				+"where cl_from=? "
				+"and cl_to=page_title "
				+"and page_namespace="+Page.CATEGORY_NAMESPACE+";";
			
			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			st.setInt(1, page.getId());
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				IPage p = new Page(rs.getInt(1), Page.CATEGORY_NAMESPACE, rs.getString(2), rs.getInt(3) > 0);
				p = resolveRedirects(p);
				if (p != null) {
					articles.add(p);
				}
			}
			rs.close();
			
			if (articles.size() > 0) {
				return articles;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public Collection<IPage> getLinkedPages(IPage page) {
		try {
			Collection<IPage> pages = new ArrayList<IPage>();
			
			String sql = 
				"select page_id, page_namespace, page_title, page_is_redirect "
				+"from "+m_db+".page, "+m_db+".pagelinks "
				+"where pl_from=? "
				+"and pl_namespace=page_namespace and pl_title=page_title;";
			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			st.setInt(1, page.getId());
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
				p = resolveRedirects(p);
				if (p != null) {
					pages.add(p);
				}
			}
			rs.close();
			
			return pages;
		} catch (SQLException e) {
			logger.error(e);
			return null;
		} catch (PropertyNotInitializedException e) {
			logger.error(e);
			return null;
		}
	}
	
	public Collection<IPage> getSubCategories(IPage page) {
		try {
			Collection<IPage> pages = new ArrayList<IPage>();
			
			String sql = 
				"select page_id, page_namespace, page_title, page_is_redirect "
				+"from "+m_db+".page, "+m_db+".categorylinks "
				+"where page_id=cl_from and page_namespace=? "
				+"and cl_to=?;";
			PreparedStatement st = getStatement(sql);
			st.setInt(1, Page.CATEGORY_NAMESPACE );
			st.setString(2, page.getTitle() );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql + " {" + Page.CATEGORY_NAMESPACE + "," + page.getTitle() + "}" );

			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
				p = resolveRedirects(p);
				if (p != null) {
					pages.add(p);
				}
			}
			rs.close();
			
			return pages;
		} catch (SQLException e) {
			logger.error(e);
			return null;
		} catch (PropertyNotInitializedException e) {
			logger.error(e);
			return null;
		}
	}

	public Collection<IPage> getArticlesInCategory( IPage category ) {
		try {
			Collection<IPage> pages = new ArrayList<IPage>();
			
			String sql = 
				"select page_id, page_namespace, page_title, page_is_redirect "
				+"from "+m_db+".page, "+m_db+".categorylinks "
				+"where page_id=cl_from and page_namespace=? "
				+"and cl_to=?;";
			PreparedStatement st = getStatement(sql);
			st.setInt(1, Page.ARTICLE_NAMESPACE );
			st.setString(2, category.getTitle() );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql + " {" + Page.ARTICLE_NAMESPACE + "," + category.getTitle() + "}" );

			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
				p = resolveRedirects(p);
				if (p != null) {
					pages.add(p);
				}
			}
			rs.close();
			
			return pages;
		} catch (SQLException e) {
			logger.error(e);
			return null;
		} catch (PropertyNotInitializedException e) {
			logger.error(e);
			return null;
		}
	}

	public IPage getArticle(String title) {
		try {
			String sql =
				"select page_id, page_is_redirect "
				+"from "+m_db+".page where page_title=? and page_namespace=?;";

			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			st.setString(1, title);
			st.setInt(2, Page.ARTICLE_NAMESPACE);
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				IPage p = new Page(rs.getInt(1), Page.ARTICLE_NAMESPACE, title, rs.getInt(2) > 0);
				rs.close();
				return resolveRedirects(p);

			} else {
				rs.close();
				return null;
			}
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public IPage getCategory(String title) {
		try {
			String sql =
				"select page_id, page_is_redirect "
				+"from "+m_db+".page where page_title=? and page_namespace=?;";

			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: " + sql + " {" + title + "," + Page.CATEGORY_NAMESPACE + "}");

			st.setString(1, title);
			st.setInt(2, Page.CATEGORY_NAMESPACE);
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				IPage p = new Page(rs.getInt(1), Page.CATEGORY_NAMESPACE, title, rs.getInt(2) > 0);
				rs.close();
				return resolveRedirects(p);

			} else {
				rs.close();
				return null;
			}
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public void initializePage(IPage p) {
		if (!p.isInitialized()) try {
			String sql =
				"select page_namespace, page_title, page_is_redirect "
				+"from "+m_db+".page where page_id=?;";
 
			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			st.setInt(1, p.getId());
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				p.setNamespace(rs.getInt(1));
				p.setTitle(rs.getString(2));
				p.setIsRedirect(rs.getInt(3) > 0);
				rs.close();

			} else {
				rs.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	private IPage resolveRedirects(IPage p) throws SQLException, PropertyNotInitializedException {
		/* page is null or no redirect */
		if ((p == null) || (!p.isRedirect())) {
			return p;
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Resolve redirect for '"+p.getId()+"'");

		/* find redirected page */
		Page newPage = null;
		
		String sql0 = 
			"select page_id, page_title, page_is_redirect "
			+"from "+m_db+".page, "+m_db+".redirect "
			+"where rd_namespace=page_namespace and rd_title=page_title "
			+"and rd_from=?";
		getStatement(sql0).setInt(1, p.getId());
		if (logger.isDebugEnabled())
			logger.debug("SQL: "+sql0);
		ResultSet rs = getStatement(sql0).executeQuery();
		if (rs.next()) {
			newPage = new Page(rs.getInt(1), p.getNamespace(), rs.getString(2), rs.getInt(3) > 0);
			rs.close();
		} else {
			rs.close();
			
			String sql1 =
				"select page_id, page_title, page_is_redirect "
				+"from "+m_db+".page, "+m_db+".pagelinks "
				+"where pl_namespace=page_namespace and pl_title=page_title "
				+"and pl_from=?";
				
			getStatement(sql1).setInt(1, p.getId());
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql1);
			rs = getStatement(sql1).executeQuery();
			if (rs.next()) {
				newPage = new Page(rs.getInt(1), p.getNamespace(), rs.getString(2), rs.getInt(3) > 0);
				rs.close();
			} else {
				rs.close();
			}
		}

		/* recursion */
		return resolveRedirects(newPage);
	}

	public String getText(IPage p) {
		try {
			String sql = 
				"select old_text "
				+"from "+m_db+".revision, "
				+"  "+m_db+".text "
				+"where rev_page=? "
				+"and rev_text_id=old_id "
				+"order by rev_timestamp desc limit 1;";
			
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			getStatement(sql).setInt(1, p.getId());
			ResultSet rs = getStatement(sql).executeQuery();
			if (rs.next()) {
				Blob blob = rs.getBlob(1);
				rs.close();
								
				StringBuilder sb = new StringBuilder((int)blob.length());
				
				BufferedReader r = new BufferedReader(new InputStreamReader(blob.getBinaryStream(), "UTF-8"));
				
				for (String line = r.readLine(); line != null; line = r.readLine()) {
					sb.append(line);
				}
				return sb.toString();
			} else {
				rs.close();
				return null;
			}
		} catch (SQLException e) {
			logger.error(e);
			return null;
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	public Language getLanguage() {
		return m_language;
	}

	public Connection getConnection() {
		return con;
	}

	public String getDbName() {
		return m_db;
	}

	public int getInlinkCount(IPage p) {
		int count = -1;
		try {
			String sql =
				"select count(*) "+
				"from "+m_db+".page, "+m_db+".pagelinks "+
				"where page_id=? " +
				"and page_namespace=pl_namespace "+
				"and page_title=pl_title;";
 
			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			st.setInt(1, p.getId());
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
				rs.close();

			} else {
				rs.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return count;
	}

	public int getOutlinkCount(IPage p) {
		int count = -1;
		try {
			String sql =
				"select count(*) "+
				"from "+m_db+".pagelinks "+
				"where pl_from=?;";
 
			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			st.setInt(1, p.getId());
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
				rs.close();

			} else {
				rs.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return count;
	}

	public Collection<IPage> getLinkedPagesFromCandidates(IPage sourcePage,
			List<IPage> candidates) {
		
		StringBuilder idStringBuilder = new StringBuilder();
		idStringBuilder.append(candidates.get(0).getId());
		for (int i=1; i<candidates.size(); i++) {
			idStringBuilder.append(',');
			idStringBuilder.append(candidates.get(i).getId());
		}

		try {
			Collection<IPage> pages = new ArrayList<IPage>();
			
			String sql = 
				"select page_id, page_namespace, page_title, page_is_redirect "
				+"from "+m_db+".page, "+m_db+".pagelinks "
				+"where pl_from="+sourcePage.getId()+" "
				+"and pl_namespace=page_namespace and pl_title=page_title "
				+"and page_id in ("+idStringBuilder.toString()+");";
			Statement st = con.createStatement();
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
				p = resolveRedirects(p);
				if (p != null) {
					pages.add(p);
				}
			}
			rs.close();
			
			return pages;
		} catch (SQLException e) {
			logger.error(e);
			return null;
		} catch (PropertyNotInitializedException e) {
			logger.error(e);
			return null;
		}
	}

	public int getMaxPageId() {
		try {
			String sql =
				"select max(page_id) "
				+"from "+m_db+".page;";

			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				int maxPageId = rs.getInt( 1 );
				rs.close();
				return maxPageId;

			} else {
				rs.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return 0;
	}

	public TIntArrayList getAllArticleIds() {
		return getArticleIds( -1 );
	}
		
	public TIntArrayList getArticleIds( int number ) {
		try {
			String sql =
				"select page_id "
				+"from "+m_db+".page "
				+"where page_namespace=0 and page_is_redirect=0";
			
			if( number > 0 ) {
				sql += " limit " + number;
			}
			sql += ";";

			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			TIntArrayList ids = new TIntArrayList();

			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				ids.add( rs.getInt( 1 ) );
			}
			rs.close();
			return ids;

		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	public TIntArrayList readArticleIds( String table, String column ) {
		try {
			String sql =
				"select " + column + " "
				+ "from " + table + " "
				+ "order by " + column + ";";
			
			PreparedStatement st = getStatement(sql);
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			TIntArrayList ids = new TIntArrayList();

			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				ids.add( rs.getInt( 1 ) );
			}
			rs.close();
			return ids;

		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

}
