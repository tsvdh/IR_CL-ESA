package edu.kit.aifb.wikipedia.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.JdbcFactory;
import edu.kit.aifb.nlp.Language;
import gnu.trove.TIntArrayList;

public class WikipediaDatabase {

	static Log logger = LogFactory.getLog( WikipediaDatabase.class); 
		
	String db;
	JdbcFactory jsb;
	Language language;
	
	@Autowired
	public void setJdbcFactory( JdbcFactory jsb ) {
		this.jsb = jsb;
	}
	
	@Required
	public void setDatabase( String database ) {
		db = database;
	}

	@Required
	public void setLanguage( Language language ) {
		this.language = language;
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public Collection<IPage> getCategories(IPage page) {
		try {
			Collection<IPage> articles = new ArrayList<IPage>();
			
			String sql =
				"select page_id, page_title, page_is_redirect "
				+"from "+db+".page, "+db+".categorylinks "
				+"where cl_from=? "
				+"and cl_to=page_title "
				+"and page_namespace="+Page.CATEGORY_NAMESPACE+";";

			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			try{
				st.setInt(1, page.getId());
				ResultSet rs = st.executeQuery();
				try{
					while (rs.next()) {
						IPage p = new Page(rs.getInt(1), Page.CATEGORY_NAMESPACE, rs.getString(2), rs.getInt(3) > 0);
						p = resolveRedirects(p);
						if (p != null) {
							articles.add(p);
						}
					}

					if (articles.size() > 0) {
						return articles;
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	public Collection<IPage> getLinkedPages(IPage page) {
		try {
			Collection<IPage> pages = new ArrayList<IPage>();
			
			String sql = 
				"select page_id, page_namespace, page_title, page_is_redirect "
				+"from "+db+".page, "+db+".pagelinks "
				+"where pl_from=? "
				+"and pl_namespace=page_namespace and pl_title=page_title;";
			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);

			try {
				st.setInt(1, page.getId());
				ResultSet rs = st.executeQuery();
				try {
					while (rs.next()) {
						IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
						p = resolveRedirects(p);
						if (p != null) {
							pages.add(p);
						}
					}
					return pages;
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
		} catch (SQLException e) {
			logger.error(e);
		} catch (PropertyNotInitializedException e) {
			logger.error(e);
		}
		return null;
	}
	
	public Collection<IPage> getSubCategories(IPage page) {
		try {
			Collection<IPage> pages = new ArrayList<IPage>();
			
			String sql = 
				"select page_id, page_namespace, page_title, page_is_redirect "
				+"from "+db+".page, "+db+".categorylinks "
				+"where page_id=cl_from and page_namespace=? "
				+"and cl_to=?;";
			PreparedStatement st = jsb.prepareStatement( sql );
			try {
				st.setInt(1, Page.CATEGORY_NAMESPACE );
				st.setString(2, page.getTitle() );
				if (logger.isDebugEnabled())
					logger.debug("SQL: "+sql + " {" + Page.CATEGORY_NAMESPACE + "," + page.getTitle() + "}" );

				ResultSet rs = st.executeQuery();
				try {
					while (rs.next()) {
						IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
						p = resolveRedirects(p);
						if (p != null) {
							pages.add(p);
						}
					}
					return pages;
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
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
				+"from "+db+".page, "+db+".categorylinks "
				+"where page_id=cl_from and page_namespace=? "
				+"and cl_to=?;";
			PreparedStatement st = jsb.prepareStatement( sql );
			try {
				st.setInt(1, Page.ARTICLE_NAMESPACE );
				st.setString(2, category.getTitle() );
				if (logger.isDebugEnabled())
					logger.debug("SQL: "+sql + " {" + Page.ARTICLE_NAMESPACE + "," + category.getTitle() + "}" );

				ResultSet rs = st.executeQuery();
				try {
					while (rs.next()) {
						IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
						p = resolveRedirects(p);
						if (p != null) {
							pages.add(p);
						}
					}
					return pages;
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
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
				+"from "+db+".page where page_title=? and page_namespace=?;";

			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				st.setString(1, title);
				st.setInt(2, Page.ARTICLE_NAMESPACE);
				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						IPage p = new Page(rs.getInt(1), Page.ARTICLE_NAMESPACE, title, rs.getInt(2) > 0);
						return resolveRedirects(p);
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	public IPage getCategory(String title) {
		try {
			String sql =
				"select page_id, page_is_redirect "
				+"from "+db+".page where page_title=? and page_namespace=?;";

			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: " + sql + " {" + title + "," + Page.CATEGORY_NAMESPACE + "}");
			try {
				st.setString(1, title);
				st.setInt(2, Page.CATEGORY_NAMESPACE);
				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						IPage p = new Page(rs.getInt(1), Page.CATEGORY_NAMESPACE, title, rs.getInt(2) > 0);
						return resolveRedirects(p);
					} 
				} 
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public void initializePage(IPage p) {
		if (!p.isInitialized()) try {
			String sql =
				"select page_namespace, page_title, page_is_redirect "
				+"from "+db+".page where page_id=?;";
 
			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				st.setInt(1, p.getId());
				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						p.setNamespace(rs.getInt(1));
						p.setTitle(rs.getString(2));
						p.setIsRedirect(rs.getInt(3) > 0);
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
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
			+"from "+db+".page, "+db+".redirect "
			+"where rd_namespace=page_namespace and rd_title=page_title "
			+"and rd_from=?";
		PreparedStatement stmt0 = jsb.prepareStatement( sql0 );
		try {
			stmt0.setInt(1, p.getId());
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql0);
			ResultSet rs = stmt0.executeQuery();
			try {
				if (rs.next()) {
					newPage = new Page(rs.getInt(1), p.getNamespace(), rs.getString(2), rs.getInt(3) > 0);
				} else {
					String sql1 =
						"select page_id, page_title, page_is_redirect "
						+"from "+db+".page, "+db+".pagelinks "
						+"where pl_namespace=page_namespace and pl_title=page_title "
						+"and pl_from=?";

					PreparedStatement stmt1 = jsb.prepareStatement( sql1 );
					try {
						stmt1.setInt(1, p.getId());
						if (logger.isDebugEnabled())
							logger.debug("SQL: "+sql1);
						ResultSet rs1 = stmt1.executeQuery();
						try {
							if (rs1.next()) {
								newPage = new Page(rs1.getInt(1), p.getNamespace(), rs1.getString(2), rs1.getInt(3) > 0);
							}
						}
						finally {
							rs1.close();
						}
					}
					finally {
						stmt1.close();
					}
				}
			}
			finally {
				rs.close();
			}
		}
		finally {
			stmt0.close();
		}
		/* recursion */
		return resolveRedirects(newPage);
	}

	public String getText(IPage p) {
		try {
			// String sql =
			// 	"select old_text "
			// 	+"from "+db+".revision, "
			// 	+"  "+db+".text "
			// 	+"where rev_page=? "
			// 	+"and rev_text_id=old_id "
			// 	+"order by rev_timestamp desc limit 1;";

			// --- NEW query to handle changed MediaWiki DB format ---
			String sql = String.format(
					"SELECT old_text "
					+ "FROM %s.revision "
					+ "JOIN %s.slots ON rev_id = slot_revision_id "
					+ "JOIN %s.content ON content_id = slot_content_id "
					+ "JOIN %s.text ON old_id = CONVERT(SUBSTRING(CONVERT(content_address, CHAR), 4), INT) "
					+ "WHERE rev_page = ? "
					+ "ORDER BY rev_timestamp DESC LIMIT 1",
			db, db, db, db);

			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			PreparedStatement stmt = jsb.prepareStatement( sql );
			try {
				stmt.setInt(1, p.getId());
				ResultSet rs = stmt.executeQuery();
				try {
					if (rs.next()) {
						Blob blob = rs.getBlob(1);

						StringBuilder sb = new StringBuilder((int)blob.length());
						BufferedReader r = new BufferedReader(new InputStreamReader(blob.getBinaryStream(), "UTF-8"));

						for (String line = r.readLine(); line != null; line = r.readLine()) {
							sb.append(line);
						}
						return sb.toString();
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				stmt.close();
			}
		} catch (SQLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	public JdbcFactory getJdbcFactory() {
		return jsb;
	}

	public String getDbName() {
		return db;
	}

	public int getInlinkCount(IPage p) {
		int count = -1;
		try {
			String sql =
				"select count(*) "+
				"from "+db+".page, "+db+".pagelinks "+
				"where page_id=? " +
				"and page_namespace=pl_namespace "+
				"and page_title=pl_title;";

			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				st.setInt(1, p.getId());
				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						count = rs.getInt(1);
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
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
				"from "+db+".pagelinks "+
				"where pl_from=?;";
 
			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				st.setInt(1, p.getId());
				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						count = rs.getInt(1);
					} 
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
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
				+"from "+db+".page, "+db+".pagelinks "
				+"where pl_from="+sourcePage.getId()+" "
				+"and pl_namespace=page_namespace and pl_title=page_title "
				+"and page_id in ("+idStringBuilder.toString()+");";
			Statement st = jsb.createStatement();
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				ResultSet rs = st.executeQuery(sql);
				try {
					while (rs.next()) {
						IPage p = new Page(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) > 0);
						p = resolveRedirects(p);
						if (p != null) {
							pages.add(p);
						}
					}
					return pages;
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
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
				+"from "+db+".page;";

			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						int maxPageId = rs.getInt( 1 );
						return maxPageId;
					}
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
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
				+"from "+db+".page "
				+"where page_namespace=0 and page_is_redirect=0";

			if( number > 0 ) {
				sql += " limit " + number;
			}
			sql += ";";

			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				TIntArrayList ids = new TIntArrayList();

				ResultSet rs = st.executeQuery();
				try {
					while (rs.next()) {
						ids.add( rs.getInt( 1 ) );
					}
					return ids;
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
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
			
			PreparedStatement st = jsb.prepareStatement( sql );
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			try {
				TIntArrayList ids = new TIntArrayList();

				ResultSet rs = st.executeQuery();
				try {
					while (rs.next()) {
						ids.add( rs.getInt( 1 ) );
					}
					return ids;
				}
				finally {
					rs.close();
				}
			}
			finally {
				st.close();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

}
