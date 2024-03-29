package edu.kit.aifb.wikipedia.sql;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.document.ICollectionIterator;

public class WikipediaCollectionTest {

	static WikipediaCollection wpCol;
	
	@BeforeClass
	public static void loadDatabase() {
		ApplicationContext context = new FileSystemXmlApplicationContext( "config/*_beans.xml" );
		wpCol = (WikipediaCollection)context.getBean( "wp200909_full_collection_de" );
	}
	
	@Test
	public void collectionSize() {
		Assert.assertEquals( 1968996, wpCol.size() );
	}
	
	@Test
	public void iterator() {
		ICollectionIterator iterator = wpCol.iterator();
		iterator.next();
		Assert.assertEquals( 1, Integer.parseInt( iterator.getDocument().getName() ) );
		Assert.assertEquals( "Alan Smithee", iterator.getDocument().getText( "title") );
	}
}
