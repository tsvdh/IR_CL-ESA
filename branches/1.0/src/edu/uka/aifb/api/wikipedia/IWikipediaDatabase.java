package edu.uka.aifb.api.wikipedia;

import java.util.Collection;
import java.util.List;

import edu.uka.aifb.nlp.Language;
import gnu.trove.TIntArrayList;

/**
 * Interface for a general wikipedia api.
 * 
 * @author pso
 *
 */
public interface IWikipediaDatabase {

	/**
	 * Get a wiki page by title in the default
	 * article namespace. 
	 * 
	 * @param title Title of the wiki page.
	 * @return Wiki page with initialized properties.
	 */
	public IPage getArticle(String title);

	/**
	 * Get a wiki category page by title in the category
	 * namespace. 
	 * 
	 * @param title Title of the wiki page.
	 * @return Wiki page with initialized properties.
	 */
	public IPage getCategory(String title);
	
	
	/**
	 * Get all subcategories of this category.
	 * 
	 * @param p The category page.
	 * @return All subcategories of the specified category.
	 */
	public Collection<IPage> getSubCategories( IPage p );
	
	/**
	 * Get all a wiki page in a specified category in the default
	 * article namespace. 
	 * 
	 * @param category The category if which the articles will be retrieved.
	 * @return A collection of pages in the specified category.
	 */
	public Collection<IPage> getArticlesInCategory( IPage category );
	
	/**
	 * Initialize the properties of a page stub.
	 * 
	 * @param p Page stub that will be initialized.
	 */
	public void initializePage(IPage p);
	
	/**
	 * Starting from a source page, get all linked pages.
	 * 
	 * @param p Source page.
	 * @return Collection of page that are linked from the source page.
	 */
	public Collection<IPage> getLinkedPages(IPage p);
	
	
	/**
	 * Starting from a source page and a list of ids, get all pages
	 * with an id from the list that are linked from the source page. 
	 * 
	 * @param p Source page.
	 * @param ids List of ids from pages that are searched.
	 * @return Collection of pages from the candidates that are linked from the source page.
	 */
	public Collection<IPage> getLinkedPagesFromCandidates(IPage p, List<IPage> pages);

	/**
	 * Get all categories of a page.
	 * 
	 * @param p Wiki page.
	 * @return Collection of page in the category namespace. 
	 */
	public Collection<IPage> getCategories(IPage p);
	
	/**
	 * Get the number of pagelinks pointing of a page.
	 * 
	 * @param p The page where the pagelinks point to. 
	 * @return The number of inlinks.
	 */
	public int getInlinkCount(IPage p);
	
	/**
	 * Get the number of pagelinks on page.
	 * 
	 * @param p The page that contains the links. 
	 * @return The number of outlinks.
	 */
	public int getOutlinkCount(IPage p);

	/**
	 * Get the current text of a page.
	 * 
	 * @param p Wiki Page.
	 * @return The text of the page.
	 */
	public String getText(IPage p);
	
	/**
	 * Get the language of the wikipedia database. 
	 * 
	 * @return The 2 letter language identifier.
	 */
	public Language getLanguage();
	
	/**
	 * Get the name of the wikipedia database.
	 * 
	 * @return Name of the current wikipedia database.
	 */
	public String getDbName();
	
	/**
	 * @return The maximum page id of this wikipedia database.
	 */
	public int getMaxPageId();
	
	/**
	 * Get an array containing all article ids in this
	 * Wikipedia database. Articles are pages in the default
	 * namespace, that are no redirect pages.
	 * 
	 * @return An array containing all article ids.
	 */
	public TIntArrayList getAllArticleIds();

	/**
	 * Get an array containing the specified number of article
	 * ids in this Wikipedia database. Articles are pages in the default
	 * namespace, that are no redirect pages.
	 * 
	 * @param number The number of articles. Set to <=0 for all articles.
	 * @return An array containing the article ids.
	 */
	public TIntArrayList getArticleIds( int number );
	
	/**
	 * Get an array containing all ids read from a specified table
	 * and column. 
	 * 
	 * @param targetLanguages A list of languages.
	 * @return An array containing the article ids.
	 */
	public TIntArrayList readArticleIds( String table, String column );
	
}

