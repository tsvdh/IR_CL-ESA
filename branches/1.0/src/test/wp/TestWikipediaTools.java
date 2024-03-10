package test.wp;

import edu.uka.aifb.wikipedia.WikipediaTools;

public class TestWikipediaTools {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "Bla \n{| Zeile 1\n|Content\n|}";
		
		System.out.println( WikipediaTools.extractPlainText( text ) );
		
	}

}
