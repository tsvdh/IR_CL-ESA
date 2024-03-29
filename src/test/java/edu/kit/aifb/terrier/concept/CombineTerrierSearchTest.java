package edu.kit.aifb.terrier.concept;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terrier.structures.Index;

import edu.kit.aifb.TestContextManager;
import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.builder.FixedSizeConceptVectorBuilder;
import edu.kit.aifb.document.DocumentListCollection;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.DummyAnalyzer;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.TerrierIndexFactory;
import edu.kit.aifb.terrier.TerrierSearch;
import edu.kit.aifb.terrier.concept.CombinedTerrierESAIndex;
import edu.kit.aifb.terrier.model.RtfModel;

public class CombineTerrierSearchTest {

	static TerrierIndexFactory factory;
	
	@BeforeClass
	public static void buildIndex() throws IOException {
		TextDocument doc0 = new TextDocument( "doc0" );
		doc0.setText( "text", Language.EN, "cats dogs horses" );
		
		TextDocument doc1 = new TextDocument( "doc1" );
		doc1.setText( "text", Language.EN, "chicken cats chicken chicken" );
		
		TextDocument doc0b = new TextDocument( "doc0" );
		doc0b.setText( "text", Language.EN, "turtles" );
		
		DocumentListCollection col0 = new DocumentListCollection();
		col0.addDocument( doc0 );
		col0.addDocument( doc1 );
		
		DocumentListCollection col1 = new DocumentListCollection();
		col1.addDocument( doc1 );
		col1.addDocument( doc0b );
		
		factory = (TerrierIndexFactory) TestContextManager.getContext().getBean(
				"terrier_index_factory" );
		factory.buildIndex( "test_CombinedTerrierESASearch_col0", Language.EN, col0 );
		factory.buildIndex( "test_CombinedTerrierESASearch_col1", Language.EN, col1 );
	}
	
	CombinedTerrierESAIndex combinedEsaIndex;
	TerrierSearch terrierSearch0;
	TerrierSearch terrierSearch1;
	
	@Before
	public void initializeCombinedIndex() throws IOException {
		combinedEsaIndex = new CombinedTerrierESAIndex();
		
		FixedSizeConceptVectorBuilder cvb = new FixedSizeConceptVectorBuilder();
		cvb.setSize( 10 );
		combinedEsaIndex.setConceptVectorBuilder( cvb );
		
		combinedEsaIndex.setLanguage( Language.EN );
		
		terrierSearch0 = new TerrierSearch();
		terrierSearch0.setTokenAnalyzer( new DummyAnalyzer() );
		Index index = factory.readIndex( "test_CombinedTerrierESASearch_col0", Language.EN );
		terrierSearch0.setIndex( index, new RtfModel() );
		
		terrierSearch1 = new TerrierSearch();
		index = factory.readIndex( "test_CombinedTerrierESASearch_col1", Language.EN );
		terrierSearch1.setTokenAnalyzer( new DummyAnalyzer() );
		terrierSearch1.setIndex( index, new RtfModel() );
	}
	
	@Test
	public void simpleSearch() throws IOException {
		combinedEsaIndex.addSearch( terrierSearch0, 1 );
		combinedEsaIndex.addSearch( terrierSearch1, 1 );
		
		IConceptExtractor extractor = combinedEsaIndex.getConceptExtractor();
		Assert.assertNotNull( extractor );
		
		TextDocument query = new TextDocument( "query" );
		query.setText( "query", Language.EN, "chicken" );
		IConceptVector cv = extractor.extract( query );
		Assert.assertNotNull( cv );
		
		Assert.assertEquals( 1, cv.count() );
		Assert.assertEquals( 2d*3d/4d, cv.get( combinedEsaIndex.getConceptId( "doc1" ) ), .000001 );
		
		query.setText( "query", Language.EN, "cats turtles" );
		cv = extractor.extract( query );
		Assert.assertNotNull( cv );
		
		Assert.assertEquals( 2, cv.count() );
		Assert.assertEquals( 1d/3d + 1d, cv.get( combinedEsaIndex.getConceptId( "doc0" ) ), .000001 );
		Assert.assertEquals( 2d*1d/4d, cv.get( combinedEsaIndex.getConceptId( "doc1" ) ), .000001 );
	}

	@Test
	public void weightedSearch() throws IOException {
		combinedEsaIndex.addSearch( terrierSearch0, 1.5 );
		combinedEsaIndex.addSearch( terrierSearch1, .5 );
		
		IConceptExtractor extractor = combinedEsaIndex.getConceptExtractor();
		Assert.assertNotNull( extractor );
		
		TextDocument query = new TextDocument( "query" );
		query.setText( "query", Language.EN, "chicken" );
		IConceptVector cv = extractor.extract( query );
		Assert.assertNotNull( cv );
		
		Assert.assertEquals( 1, cv.count() );
		Assert.assertEquals( 1.5*3d/4d + .5*3d/4d, cv.get( combinedEsaIndex.getConceptId( "doc1" ) ), .000001 );
		
		query.setText( "query", Language.EN, "cats turtles" );
		cv = extractor.extract( query );
		Assert.assertNotNull( cv );
		
		Assert.assertEquals( 2, cv.count() );
		Assert.assertEquals( 1.5*1d/3d + .5*1d, cv.get( combinedEsaIndex.getConceptId( "doc0" ) ), .000001 );
		Assert.assertEquals( 1.5*1d/4d + .5*1d/4d, cv.get( combinedEsaIndex.getConceptId( "doc1" ) ), .000001 );
	}
}
