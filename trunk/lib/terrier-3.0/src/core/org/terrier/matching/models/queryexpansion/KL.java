/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://ir.dcs.gla.ac.uk/terrier 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is KL.java.
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models.queryexpansion;

import org.terrier.matching.models.Idf;

/** 
 * This class implements the Kullback-Leibler divergence for
 * query expansion. See G. Amati's PhD Thesis.
 * @author Gianni Amati, Ben He
 * @version $Revision: 1.20 $
 */
public class KL extends QueryExpansionModel {
    /** A default constructor.*/
    public KL() {
		super();
    }
    
    /**
     * Returns the name of the model.
     * @return the name of the model
     */
    public final String getInfo() {
    	if (PARAMETER_FREE)
			return "KLbfree";
		return "KLb"+ROCCHIO_BETA;
    }
    /**
     * This method computes the normaliser of parameter-free query expansion.
     * @return The normaliser.
     */
    public final double parameterFreeNormaliser(){	
    	return (maxTermFrequency) * Math.log(collectionLength/totalDocumentLength)/
			(Math.log(2d)*totalDocumentLength);
		//return  maxTermFrequency * idf.log(collectionLength/totalDocumentLength)/ idf.log (totalDocumentLength);
	}
	
    /**
     * This method computes the normaliser of parameter-free query expansion.
     * @param maxTermFrequency The maximum of the term frequency of the query terms.
     * @param collectionLength The number of tokens in the collections.
     * @param totalDocumentLength The sum of the length of the top-ranked documents.
     * @return The normaliser.
     */
	public final double parameterFreeNormaliser(double maxTermFrequency, double collectionLength, double totalDocumentLength){
		return (maxTermFrequency) * Math.log(collectionLength/totalDocumentLength)/
		(Math.log(2d)*totalDocumentLength);
	}
    
    /** This method implements the query expansion model.
     *  @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
     *  @param termFrequency double The term frequency in the collection.
     *  @return double The query expansion weight using he complete 
     *          Kullback-Leibler divergence.
     */
    public final double score(double withinDocumentFrequency, double termFrequency) {
        if (withinDocumentFrequency / this.totalDocumentLength < termFrequency / this.collectionLength)
            return 0;
        else
            return withinDocumentFrequency / this.totalDocumentLength * 
                    Idf.log(withinDocumentFrequency / this.totalDocumentLength, 
                        termFrequency / this.collectionLength);
    } 
    
    /**
	 * This method implements the query expansion model.
	 * @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
     * @param termFrequency double The term frequency in the collection.
     * @param totalDocumentLength double The sum of length of the X top-retrieved documents.
     * @param collectionLength double The number of tokens in the whole collection.
     * @param averageDocumentLength double The average document length in the collection.
	 * @return double The score returned by the implemented model.
	 */
	public final double score(
        double withinDocumentFrequency, 
        double termFrequency,
        double totalDocumentLength, 
        double collectionLength, 
        double averageDocumentLength
    ){
        if (withinDocumentFrequency / totalDocumentLength < termFrequency / collectionLength)
            return 0;
        else
            return withinDocumentFrequency / totalDocumentLength * 
                    Idf.log(withinDocumentFrequency / totalDocumentLength, 
                        termFrequency / collectionLength);
    }
}
