package test.concept;



import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.gla.terrier.structures.DirectIndex;
import uk.ac.gla.terrier.structures.DocumentIndex;
import uk.ac.gla.terrier.structures.Index;
import uk.ac.gla.terrier.structures.InvertedIndex;
import uk.ac.gla.terrier.structures.Lexicon;
import uk.ac.gla.terrier.structures.LexiconEntry;
import edu.uka.aifb.api.concept.IConceptIndex;
import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.document.DocumentListCollection;
import edu.uka.aifb.document.TextDocument;
import edu.uka.aifb.ir.terrier.TerrierIndexFactory;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.terrier.TerrierConceptModelIndex;
import edu.uka.aifb.terrier.TerrierESAIndex;
import edu.uka.aifb.tools.ConfigurationManager;

public class TestConceptModelIndex {

	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ConfigurationException {
		Configuration config = ConfigurationManager.parseArgs(args);
		config.addProperty( "terrier.esa.document_scores_modifier_classes", "" );
		
		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		DocumentListCollection col = new DocumentListCollection();
		TextDocument doc = new TextDocument( "test1" );
		doc.setText( "content", Language.DE, "fahrrad ampel" );
		col.addDocument( doc );
		
		doc = new TextDocument( "test2" );
		doc.setText( "content", Language.DE, "verkehr ampel" );
		col.addDocument( doc );

		doc = new TextDocument( "test3" );
		doc.setText( "content", Language.DE, "fahrrad lenkrad" );
		col.addDocument( doc );

		doc = new TextDocument( "test4" );
		doc.setText( "content", Language.DE, "bla bla bla" );
		col.addDocument( doc );

		TerrierIndexFactory.buildIndex( "test", Language.DE, null, col, true, null );
		
		/*
		 * Test index
		 */
		Index index = TerrierIndexFactory.readIndex( "test" , Language.DE );
		DocumentIndex docIndex = index.getDocumentIndex();
		DirectIndex directIndex = index.getDirectIndex();
		InvertedIndex invertedIndex = index.getInvertedIndex();
		Lexicon lexicon = index.getLexicon();
		for( int docId=0; docId<docIndex.getNumberOfDocuments(); docId++ ) {
			System.out.println( "Doc " + docId + ": " + docIndex.getDocumentNumber( docId ) );
			int[][] pointers = directIndex.getTerms( docId );
			for( int i=0; i<pointers[0].length; i++ ) {
				System.out.print( " " + pointers[1][i] + ":" + lexicon.getLexiconEntry( pointers[0][i] ).term );
			}
			System.out.println();
		}
		
		for( LexiconEntry entry : new LexiconEntry[] {
				lexicon.getLexiconEntry( "verkehr" ),
				lexicon.getLexiconEntry( "fahrrad" ),
				lexicon.getLexiconEntry( "bla" ) } ) {
			System.out.println( "Term: " + entry.term );
			int[][] pointers = invertedIndex.getDocuments( entry );
			for( int i=0; i<pointers[0].length; i++ ) {
				System.out.print( " " + pointers[1][i] + ":" + docIndex.getDocumentNumber( pointers[0][i] ) );
			}
			System.out.println();
		}
		
		/*
		 * Test concept model index
		 */
		TextDocument queryDoc = new TextDocument( "query" );
		queryDoc.setText( "content", Language.DE, "fahrrad verkehr bla" );
		
		TerrierConceptModelIndex conceptModelIndex = new TerrierConceptModelIndex( config, "test", Language.DE );
		IConceptVector cv = conceptModelIndex.getConceptExtractor().extract( queryDoc );
		IConceptIterator it = cv.iterator();
		while( it.next() ) {
			System.out.println( conceptModelIndex.getConceptName( it.getId() ) + ": " + it.getValue() );
		}

		
		/*
		 * Test concept index
		 */
		IConceptIndex conceptIndex = new TerrierESAIndex( config, "test", Language.DE );
		cv = conceptIndex.getConceptExtractor().extract( queryDoc );
		it = cv.iterator();
		while( it.next() ) {
			System.out.println( conceptIndex.getConceptName( it.getId() ) + ": " + it.getValue() );
		}

	}

}
