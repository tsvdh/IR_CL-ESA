package edu.kit.aifb.wikipedia.wpm;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.TestContextManager;
import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.document.ICollectionIterator;
import edu.kit.aifb.document.IDocument;

public class WpmCollectionTest {

	static ICollection wpCol;
	static ICollection wpCatCol;
	
	@BeforeClass
	public static void loadDatabase() {
		wpCol = (ICollection)TestContextManager.getContext().getBean(
				"wpm_wp200909_mlc_articles_collection_en" );

		wpCatCol = (ICollection)TestContextManager.getContext().getBean(
				"wpm_wp200909_mlc_categories_collection_en" );
	}
	
	@Test
	public void collectionSize() {
		Assert.assertEquals( 358519, wpCol.size() );
		Assert.assertEquals( 35628, wpCatCol.size() );
	}
	
	@Test
	public void conceptIterator() {
		ICollectionIterator iterator = wpCol.iterator();
		iterator.next();
		IDocument doc = iterator.getDocument();
		
		Assert.assertEquals( 1, Integer.parseInt( doc.getName() ) );
		Assert.assertTrue( doc.getText( "title" ).startsWith( "Alan Smithee" ) );
		Assert.assertEquals( 9, doc.getText( "redirect" ).split( "\n" ).length );
		
		Assert.assertTrue( doc.getText( "content" ).contains( "Alan Smithee" ) );
		
		Assert.assertEquals( 89, doc.getText( "anchor" ).split( "\n" ).length );
	}
	
	@Test
	public void categoryIterator() {
		ICollectionIterator iterator = wpCatCol.iterator();
		iterator.next();
		IDocument doc = iterator.getDocument();
		
		Assert.assertEquals( 2, Integer.parseInt( doc.getName() ) );
		Assert.assertEquals( "History", doc.getText( "title").trim() );
		
		String content = doc.getText( "content" );
		Assert.assertTrue( content.contains( "study of the past" ) );
	}
	
}
