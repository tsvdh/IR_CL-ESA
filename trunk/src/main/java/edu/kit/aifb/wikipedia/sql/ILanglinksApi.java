package edu.kit.aifb.wikipedia.sql;

import java.util.List;


/**
 * Generel langlinks api for a wikipedia database.
 * 
 * The api covers langlinks from a source database
 * in one language to a target database in another
 * language.
 * 
 * @author pso
 *
 */
public interface ILanglinksApi {

	/**
	 * Get the corresponding page in the target database
	 * 
	 * @param sourcePage Page of the source database.
	 * @return Page Page of the target database.
	 */
	public Page getTargetPage(Page sourcePage);

	/**
	 * Speed optimized function to get the ids of
	 * target pages starting from a list of ids of
	 * source pages.
	 * 
	 * The target id is <code>-1</code> if no langlink
	 * for a source page is found.
	 * 
	 * @param sourcePageIds List of page ids in the source database.
	 * @return List if corresponding page ids in the target database.
	 */
	public int[] getTargetPageIds( int[] sourcePageIds );

	public int getTargetPageId( int sourcePageId );

	/**
	 * Get the common categories of 2 pages in
	 * the different databases.
	 * 
	 * Only categories that are linked through langlinks
	 * are covered.
	 * 
	 * @param sourcePage
	 * @param targetPage
	 * @return List if common categories in the source database.
	 */
	public List<Page[]> getCommonCategories(Page sourcePage, Page targetPage);
	
}
