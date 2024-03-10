package edu.uka.aifb.api.wikipedia;


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
public interface IPage extends Comparable<IPage> {

	public int getId();
	
	public int getNamespace() throws PropertyNotInitializedException;

	public void setNamespace(int namespace);
	
	public String getTitle() throws PropertyNotInitializedException;

	public void setTitle(String title);
	
	public boolean isRedirect() throws PropertyNotInitializedException;

	public void setIsRedirect(boolean isRedirect);
	
	public boolean isInitialized();
	
}
