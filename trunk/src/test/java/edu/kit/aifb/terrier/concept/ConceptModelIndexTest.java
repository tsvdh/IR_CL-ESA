package edu.kit.aifb.terrier.concept;



import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.InvertedIndex;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;

import edu.kit.aifb.TestContextManager;
import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.document.DocumentListCollection;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.DummyAnalyzer;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.TerrierIndexFactory;
import edu.uka.aifb.concept.model.RtfIdfFixedSizeConceptModel;

public class ConceptModelIndexTest {

	static TerrierIndexFactory factory;
	
	@BeforeClass
	public static void buildIndex() throws IOException {
		DocumentListCollection col = new DocumentListCollection();
		TextDocument doc = new TextDocument( "test1" );
		doc.setText( "content", Language.DE, "fahrrad ampel" );
		col.addDocument( doc );
		
		doc = new TextDocument( "test2" );
		doc.setText( "content", Language.DE, "verkehr ampel" );
		col.addDocument( doc );

		doc = new TextDocument( "test3" );
		doc.setText( "content", Language.DE, "fahrrad fahrrad lenkrad" );
		col.addDocument( doc );

		doc = new TextDocument( "test4" );
		doc.setText( "content", Language.DE, "bla bla bla" );
		col.addDocument( doc );

		factory = (TerrierIndexFactory) TestContextManager.getContext().getBean(
			"terrier_index_factory" );

		factory.buildIndex( "test_ConceptModelIndex", Language.DE, col );
	}
	
	Index index;
	DocumentIndex docIndex;
	InvertedIndex invertedIndex;
	MetaIndex metaIndex;
	Lexicon<String> lexicon;
	
	@Before
	public void readIndex() throws IOException {
		Index index = factory.readIndex( "test_ConceptModelIndex" , Language.DE );
		docIndex = index.getDocumentIndex();
		invertedIndex = index.getInvertedIndex();
		metaIndex = index.getMetaIndex();
		lexicon = index.getLexicon();
	}
	
	@Test
	public void lexiconIntegrity() throws IOException {
		LexiconEntry entry = lexicon.getLexiconEntry( "verkehr" );
		Assert.assertEquals( 1, entry.getFrequency() );
		Assert.assertEquals( 1, entry.getDocumentFrequency() );
		int[][] pointers = invertedIndex.getDocuments( entry );
		Assert.assertEquals( 1, pointers[0].length );
		Assert.assertEquals( metaIndex.getDocument( "docno", "test2" ), pointers[0][0] );
		Assert.assertEquals( 1, pointers[1][0] );
		
		entry = lexicon.getLexiconEntry( "bla" );
		Assert.assertEquals( 3, entry.getFrequency() );
		Assert.assertEquals( 1, entry.getDocumentFrequency() );
		pointers = invertedIndex.getDocuments( entry );
		Assert.assertEquals( 1, pointers[0].length );
		Assert.assertEquals( metaIndex.getDocument( "docno", "test4" ), pointers[0][0] );
		Assert.assertEquals( 3, pointers[1][0] );
	}
	
	@Test
	public void query() throws IOException {
		TextDocument queryDoc = new TextDocument( "query" );
		queryDoc.setText( "content", Language.DE, "fahrrad verkehr bla" );
		
		TerrierConceptModelIndex conceptModelIndex = new TerrierConceptModelIndex();
		conceptModelIndex.setIndexId( "test_ConceptModelIndex" );
		conceptModelIndex.setLanguage( Language.DE );
		conceptModelIndex.setTokenAnalyzer( new DummyAnalyzer() );
		conceptModelIndex.setTerrierIndexFactory( factory );
		conceptModelIndex.readIndex();

		RtfIdfFixedSizeConceptModel conceptModel = new RtfIdfFixedSizeConceptModel();
		conceptModel.setSize( 3 );
		conceptModelIndex.setConceptModel( conceptModel );
		
		IConceptVector cv = conceptModelIndex.getConceptExtractor().extract( queryDoc );
		IConceptIterator it = cv.orderedIterator();

		Assert.assertTrue( it.next() );
		Assert.assertEquals( metaIndex.getDocument( "docno", "test4" ), it.getId() );
		Assert.assertEquals( 1.39, it.getValue(), .01 );

		Assert.assertTrue( it.next() );
		Assert.assertEquals( metaIndex.getDocument( "docno", "test2" ), it.getId() );
		Assert.assertEquals( .69, it.getValue(), .01 );
		
		Assert.assertTrue( it.next() );
		Assert.assertEquals( metaIndex.getDocument( "docno", "test3" ), it.getId() );
		Assert.assertEquals( .46, it.getValue(), .01 );

		/*Assert.assertTrue( it.next() );
		Assert.assertEquals( metaIndex.getDocument( "docno", "test1" ), it.getId() );
		Assert.assertEquals( .35, it.getValue(), .01 );*/

		Assert.assertFalse( it.next() );
	}

}
