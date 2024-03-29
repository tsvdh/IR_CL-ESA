package edu.kit.aifb.wikipedia.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.JdbcFactory;


public class FastLanglinksApi implements ILanglinksApi {

	private static Log logger = LogFactory.getLog( FastLanglinksApi.class);
	
	WikipediaDatabase sourceWikiApi;
	WikipediaDatabase targetWikiApi;
	
	JdbcFactory jsb;
	
	@Required
	public void setSourceWikipediaDatabase( WikipediaDatabase source ) {
		sourceWikiApi = source;
	}
	
	@Required
	public void setTargetWikipediaDatabase( WikipediaDatabase target ) {
		targetWikiApi = target;
	}

	@Autowired
	public void setJdbcFactory( JdbcFactory jsb ) {
		this.jsb = jsb;
	}
	
	public Page getTargetPage(Page sourcePage) {
		int targetPageId = getTargetPageId( sourcePage.getId() );
		if( targetPageId >= 0 ) {
			return new Page( targetPageId );
		}
		return null;
	}
	
	public int getTargetPageId( int sourcePageId ) {
		int targetPageId = -1;
		try {
			// String sql =
			// 	"select ll_to "+
			// 	"from "+sourceWikiApi.getDbName()+".fast_langlinks "+
			// 	"where ll_from=? "+
			// 	"and ll_lang=\""+targetWikiApi.getLanguage()+"\";";
			String sql =
					"select page_id "
					+ "from " + sourceWikiApi.getDbName() + ".langlinks "
					+ "join " + targetWikiApi.getDbName() + ".page on page_title = replace(ll_title, ' ', '_') "
					+ "where ll_from = ? "
					+ "and ll_lang = ? ";
			PreparedStatement st = jsb.prepareStatement( sql );
			try {
				st.setInt(1, sourcePageId);
				st.setString(2, targetWikiApi.getLanguage().toString());

				ResultSet rs = st.executeQuery();
				try {
					if (rs.next()) {
						targetPageId = rs.getInt(1);
					}
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
		}
		return targetPageId;
	}
	
	public List<Page[]> getCommonCategories(Page a, Page b) {
		List<Page[]> l = new ArrayList<Page[]>();
		
		try {
			String sql = 
				"select "+
				"  FROM_PAGE.page_id, "+
				"  FROM_LANGLINKS.ll_to "+
				"from "+
				sourceWikiApi.getDbName()+".categorylinks as FROM_CATEGORYLINKS, "+
				sourceWikiApi.getDbName()+".page as FROM_PAGE, "+
				sourceWikiApi.getDbName()+".fast_langlinks as FROM_LANGLINKS, "+
				targetWikiApi.getDbName()+".page as TO_PAGE, "+
				targetWikiApi.getDbName()+".categorylinks as TO_CATEGORYLINKS "+
				"where "+
				"  FROM_CATEGORYLINKS.cl_from=? "+
				"  and FROM_PAGE.page_namespace=14 "+
				"  and FROM_CATEGORYLINKS.cl_to=FROM_PAGE.page_title "+
				"  and FROM_PAGE.page_id=FROM_LANGLINKS.ll_from "+
				"  and FROM_LANGLINKS.ll_lang=\""+targetWikiApi.getLanguage()+"\" "+
				"  and FROM_LANGLINKS.ll_to=TO_PAGE.page_id "+
				"  and TO_PAGE.page_namespace=14 "+
				"  and TO_PAGE.page_title=TO_CATEGORYLINKS.cl_to "+
				"  and TO_CATEGORYLINKS.cl_from=?;";
			
			if (logger.isDebugEnabled())
				logger.debug("SQL: "+sql);
			PreparedStatement st = jsb.prepareStatement( sql );
			try {
				st.setInt(1, a.getId());
				st.setInt(2, b.getId());
				ResultSet rs = st.executeQuery();
				try {
					while (rs.next()) {
						Page[] pageArray = new Page[2]; 
						pageArray[0] = new Page(rs.getInt(1));
						pageArray[1] = new Page(rs.getInt(2));
						l.add(pageArray);
					}
					return l;
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
		}
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
				"select ll_from,ll_to "+
				"from "+sourceWikiApi.getDbName()+".fast_langlinks "+
				"where ll_from in "+
				"("+idStringBuilder.toString()+") "+
				"and ll_lang=\""+targetWikiApi.getLanguage()+"\" order by ll_from;";
			
			if (logger.isDebugEnabled())
				logger.debug("SQL: selectManyBySource");
			Statement st = jsb.createStatement();
			try {
				ResultSet rs = st.executeQuery(sql);
				try {
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
					return targetPageIds;
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
		}
	}

}
