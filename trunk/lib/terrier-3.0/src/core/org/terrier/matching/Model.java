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
 * The Original Code is Model.java.
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching;
/** This interface encapsulates all the common functionality between
  * standard IR models (including DFR models, TF/IDF, BM25 etc), and
  * Language Modelling models (Terrier includes PonteCroft Language
  * Modelling model).
  * @see org.terrier.matching.models.WeightingModel
  * @author Craig Macdonald
  * @version $Revision: 1.12 $
  */
public interface Model
{
	/** Returns a model dependant string, such that runs can be identified
	  * from TREC results. Example <tt>PL2c2.1</tt> would imply PL2 model, with the
	  * c parameter set to 2.1. 
	  * @return The string description of the current instantiation of the weighting
	  * model. */
	public String getInfo();
	
	/** Many models require 1 parameter to be set - this is often dependant on the
	  * corpus and the model, so should be set for each Terrier instance. 
	  * @param param double the parameter value. */
	public void setParameter(double param);
	/**
	 * Set the number of unique terms in the collection.
	 * @param n double The number of unique terms in the collection.
	 */
	public void setNumberOfUniqueTerms (double n);
	/**
	 * Set the number of tokens in the whole collection.
	 * @param n The number of tokens in the whole collection.
	 */
	public void setNumberOfTokens (double n);
	/**
	 * Set the average document length in the collection.
	 * @param n The average document length in the collection.
	 */
	public void setAverageDocumentLength (double n);
	/**
	 * Set the number of documents in the collection.
	 * @param n The number of documents in the collection.
	 */
	public void setNumberOfDocuments(double n);
	public void setNumberOfPointers(double n);
	/** Returns the current value of the parameter set using setParameter() method. */
	public double getParameter();
}
