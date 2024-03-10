package edu.uka.aifb.api.concept.search;

import java.util.List;

import uk.ac.gla.terrier.matching.ResultSet;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.ir.IMatch;

/**
 * This specifies a class for concept based search. Based on a ceratin scoring model
 * all matches for a specified query are returned.
 * 
 * @author pso
 *
 */
/**
 * @author pso
 *
 */
/**
 * @author pso
 *
 */
public interface IConceptMatcher {
	
	/**
	 * This sets the scoring model for the concept based retrieval.
	 * 
	 * @param className The name of the IScorer class that implements the
	 * concept based retrieval model.
	 */
	public void setScorerClass( String className );
	
	/**
	 * This sets the scoring model for the concept based retrieval.
	 * 
	 * @param scorerClass The scorer class that implements IScorer interface.
	 */
	public void setScorerClass( Class<? extends IScorer> scorerClass );
	
	/**
	 * This function performs a complete retrieval step for a specified query which
	 * is represented by a concept vector.  
	 * 
	 * @param queryCV The concept vector of the query that should be searched for.
	 */
	public void match( IConceptVector queryCV );

	
	/**
	 * This function returns a sorted list of matches found for a previous call of match().
	 * 
	 * @return A sorted list of matches containing relevant documents and their scores.
	 */
	public List<IMatch> getMatches();

	public ResultSet getResultSet();
}
