package edu.kit.aifb.terrier.concept;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.kit.aifb.TestContextManager;
import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.concept.TerrierESAIndex;

public class TerrierESAIndexTest {

	static TerrierESAIndex terrierESAIndexDE;
	static TerrierESAIndex terrierESAIndexEN;
	
	@BeforeClass
	public static void loadDatabase() {
		terrierESAIndexDE = (TerrierESAIndex) TestContextManager.getContext().getBean(
				"wp200909_mlc_articles_concept_index_de" );
		terrierESAIndexEN = (TerrierESAIndex) TestContextManager.getContext().getBean(
			"wp200909_mlc_articles_concept_index_en" );
	}
	
	@Test
	public void indexStatistics() {
		Assert.assertEquals( 358519, terrierESAIndexDE.size() );
	}

	@Test
	public void conceptExtractor() {
		IConceptExtractor extractor = terrierESAIndexDE.getConceptExtractor();
		
		TextDocument query = new TextDocument( "query" );
		query.setText( "query", Language.DE, "Albert Einstein" );
		
		IConceptVector cv = extractor.extract( query );
		Assert.assertTrue( cv.size() > 0 );
	}
	
	@Test
	public void specialCharacters() {
		IConceptExtractor extractor = terrierESAIndexEN.getConceptExtractor();

		TextDocument query = new TextDocument( "query" );
		query.setText( "query", Language.EN, "Königsplatz" );
		
		IConceptVector cv = extractor.extract( query );
		IConceptIterator it = cv.iterator();
		Assert.assertTrue( it.next() );
		Assert.assertEquals( "0000048222", terrierESAIndexEN.getConceptName( it.getId() ) );
		Assert.assertFalse( it.next() );
	}
	
}
