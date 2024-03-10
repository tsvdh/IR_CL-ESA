package edu.kit.aifb.wikipedia.wpm;

import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Page;
import org.wikipedia.miner.model.Wikipedia;

import edu.kit.aifb.TestContextManager;

public class WPMTest {

	Wikipedia englishWp;
	Wikipedia germanWp;
	Wikipedia spanishWp;
	
	@Before
	public void loadDatabase() {
		englishWp = (Wikipedia) TestContextManager.getContext().getBean( "wpm_en" );
		germanWp = (Wikipedia) TestContextManager.getContext().getBean( "wpm_de" );
		spanishWp = (Wikipedia) TestContextManager.getContext().getBean( "wpm_es" );
	}
	
	@Test
	public void specialCharacters() throws SQLException {
		Page p = germanWp.getArticleByTitle( "Fähre" );
		Assert.assertNotNull( p );
		Assert.assertEquals( "Fähre", p.getTitle() );
		
		p = germanWp.getPageById( 16373 );
		Assert.assertNotNull( p );
		Assert.assertEquals( "Fähre", p.getTitle() );
		
		p = englishWp.getPageById( 771172 );
		Assert.assertNotNull( p );
		Assert.assertEquals( "Hunter × Hunter", p.getTitle() );
	}
	
	@Test
	public void spanishArticles() throws SQLException {
		Page p = spanishWp.getPageById( 959123 );
		Assert.assertNotNull( p );
		
		Article a = (Article)p;
		Assert.assertNotNull( a );
	}
	
}
