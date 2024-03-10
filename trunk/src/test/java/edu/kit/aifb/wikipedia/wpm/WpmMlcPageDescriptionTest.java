package edu.kit.aifb.wikipedia.wpm;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.aifb.TestContextManager;
import edu.kit.aifb.nlp.Language;

public class WpmMlcPageDescriptionTest {

	static WpmMlcPageDescription desc;
	
	@BeforeClass
	static public void loadDatabase() {
		desc = (WpmMlcPageDescription) TestContextManager.getContext().getBean(
				"wpm_articles_descriptions" );
	}

	@Test
	public void specialCharacters() throws Exception {
		/*Page p = germanWp.getArticleByTitle( "Fähre" );
		Assert.assertNotNull( p );
		Assert.assertEquals( "Fähre", p.getTitle() );
		
		p = germanWp.getPageById( 16373 );
		Assert.assertNotNull( p );
		Assert.assertEquals( "Fähre", p.getTitle() );*/
		
		
		String d = desc.getDescription( "182015", Language.EN );
		Assert.assertEquals( "Hunter × Hunter", d );
	}
	
}
