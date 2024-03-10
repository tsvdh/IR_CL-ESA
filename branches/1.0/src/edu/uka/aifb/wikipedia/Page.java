package edu.uka.aifb.wikipedia;

import edu.uka.aifb.api.wikipedia.IPage;
import edu.uka.aifb.api.wikipedia.PropertyNotInitializedException;

/**
 * This class models a wikipedia page. 
 * 
 * The following properties are stored:
 * <ul>
 *   <li>page_id</li>
 *   <li>page_namespace</li>
 *   <li>page_title</li>
 *   <li>page_is_redirect</li>
 * </ul>
 * 
 * @author pso
 *
 */
public class Page implements IPage {

	public static final int ARTICLE_NAMESPACE = 0;
	public static final int CATEGORY_NAMESPACE = 14;
	
	int id;

	int namespace = -1;
	boolean isRedirect = false;
	String title = null;
	
	boolean isInitialized = false;
	
	/**
	 * Constructor for page stub.  
	 * 
	 * <code>id</code> is the only valid property.
	 * Reading any other property will throw an exception.
	 * 
	 * @param id
	 */
	public Page(int id) {
		this.id = id;
	}
	
	/**
	 * Constructor for a generic page.
	 * 
	 * @param id
	 * @param namespace
	 * @param title
	 * @param isRedirect
	 */
	public Page(int id, int namespace, String title, boolean isRedirect) {
		this.id = id;
		this.namespace = namespace;
		this.title = title;
		this.isRedirect = isRedirect;
		isInitialized = true;
	}
	
	public int getId() {
		return id;
	}
	
	public int getNamespace() throws PropertyNotInitializedException {
		if (isInitialized) {
			return namespace;
		} else {
			throw new PropertyNotInitializedException("Property 'namespace' is not initialized for this page.");
		}
	}
	public void setNamespace(int namespace) {
		this.namespace = namespace;
		if (title != null) {
			isInitialized = true;
		}
	}
	
	public String getTitle() throws PropertyNotInitializedException {
		if (isInitialized) {
			return title;
		} else {
			throw new PropertyNotInitializedException("Property 'title' is not initialized for this page.");
		}
	}
	public void setTitle(String title) {
		this.title = title;
		if (namespace >= 0) {
			isInitialized = true;
		}
	}
	
	public boolean isRedirect() throws PropertyNotInitializedException {
		if (isInitialized) {
			return isRedirect;
		} else {
			throw new PropertyNotInitializedException("Property 'isRedirect' is not initialized for this page.");
		}
	}
	public void setIsRedirect(boolean isRedirect) {
		this.isRedirect = isRedirect;
	}
	
	public boolean isInitialized() {
		return isInitialized;
	}
	
	public boolean equals(IPage p) {
		return p.getId() == id;
	}
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	public int compareTo(IPage arg0) {
		return (int)(id - arg0.getId());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if( isInitialized ) {
			sb.append( "[" ).append( getId() ).append( ":" ).append( title ).append( "]" );
			sb.append( "{ns=" ).append( namespace );
			if( isRedirect ) {
				sb.append( ",redirect" );
			}
			sb.append( "}" );
		}
		else {
			sb.append( "[" ).append( getId() ).append( "]" );
			sb.append( "{uninitialized}" );
		}
		return sb.toString();
	}
}
