/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://ir.dcs.gla.ac.uk/terrier 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
 * Information Retrieval Group
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
 * The Original Code is terrier.g.
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

/*
 * This is the lexer and parser definition for the 
 * query language of Terrier.
 * This specification was written for ANTLR-2.7.4.
 * authors: Vassilis Plachouras and Craig Macdonald
 * version $Revision: 1.16 $
 */

// ----------------------------------------------------------------------------
// the parser

header {
package org.terrier.querying.parser;
import antlr.TokenStreamSelector;
}
class TerrierQueryParser extends Parser;
options {
	k = 2; // two token lookahead
	importVocab=Main;
}

{
	private TokenStreamSelector selector;
	public void setSelector(TokenStreamSelector s)
	{
		selector = s;
	}
	
	public void recover( RecognitionException ex, BitSet tokenSet ) throws TokenStreamException 
	{
	  consume();
	  consumeUntil( tokenSet );
	}
}


query returns [Query q]
  {q=null;}
  : q=impliedMultiTermQuery
  | q=explicitMultiTermQuery
  | q=phraseQuery
  | q=fieldQuery
  | q=requirementQuery
  | q=singleTermQuery

  ;

/*notMultiQuery returns [Query q]
  {q=null;}
  : q=singleTermQuery
  | q=fieldQuery
  | q=requirementQuery
  ;*/
  
singleTermQuery returns [Query q]
  { q= null; SingleTermQuery stq = new SingleTermQuery(); }
  : qt:ALPHANUMERIC { stq.setTerm(qt.getText()); }
   (HAT {selector.push("numbers");/*System.out.println("Changed");*/}
		(w_f:NUM_FLOAT {stq.setWeight(Double.parseDouble(w_f.getText())); selector.pop();/*System.out.println("Changed back");*/}
		|w_i:NUM_INT {stq.setWeight(Double.parseDouble(w_i.getText())); selector.pop();/*System.out.println("Changed back");*/})
	)?
   {q = stq;}
  ;

/* fields can be applied to single terms, phrases or explicit multi-terms 
   (ie parenthesis) only */
fieldQuery returns [Query q]
  { q= null; FieldQuery fq = new FieldQuery(); Query child = null; }
  : f:ALPHANUMERIC {fq.setField(f.getText()); } COLON
    (   child = singleTermQuery 
      | child = phraseQuery 
      | child = explicitMultiTermQuery  
     ) {fq.setChild(child); }
    {q = fq;}
  ;
  
//a requirement query contains anything EXCEPT another query
requirementQuery returns [Query q]
  { q= null; RequirementQuery rq = new RequirementQuery(); Query child = null; }
  : (  REQUIRED | NOT_REQUIRED {rq.setRequired(false);})
    (   child = singleTermQuery 
      | child = phraseQuery 
      | child = fieldQuery
      | child = explicitMultiTermQuery  
     ) {rq.setChild(child); }
     { q = rq;}
  ; 
    
 
 
//phrase queries can only contain one of more single terms
//even though the PROXIMITY operator is followed by a FLOAT, 
//the number has to be an integer, so that it is parsed 
//correctly.
phraseQuery returns [Query q]
  { int prox = 0; 
	PhraseQuery pq = new PhraseQuery(); MultiTermQuery mq = new MultiTermQuery();
	Query child = null; q = mq;}  
  : QUOTE 
	(child = singleTermQuery {pq.add(child); mq.add(child);})+ 
	(endQ: QUOTE 
		(	
			PROXIMITY {selector.push("numbers");System.out.println("Changed");}
			p:NUM_INT { prox = Integer.parseInt(p.getText()); selector.pop();System.out.println("Changed back");}
		)?
	 )? {
			/* phrase has been closed, use phraseQuery, not multiTermQuery*/
			if (endQ != null) {q = pq;}
			else{/*WARN quote was emitted */}
		}
  { if (prox>0) pq.setProximityDistance(prox); }
  ;
  
// a list of terms
impliedMultiTermQuery returns [Query q]
  { q= null; MultiTermQuery mtq = new MultiTermQuery(); Query child = null;}
  : ((child=explicitMultiTermQuery
  |  child=singleTermQuery
  |  child=requirementQuery
  |  child=phraseQuery
  |  child=fieldQuery

)
     {mtq.add(child);})+
  { q= mtq;}
  ;

//a list of terms surrounded by parenthesis
explicitMultiTermQuery returns [Query q]
  { q= null; MultiTermQuery mtq = new MultiTermQuery(); Query child = null;}
  : OPEN_PAREN
    (( child=singleTermQuery
    |  child=fieldQuery
    |  child=requirementQuery
    |  child=phraseQuery
)
     {mtq.add(child);})+
	(closeP: CLOSE_PAREN)? {if (closeP == null){ /* WARN missing '(' */ }}
   { q= mtq;}
  ;



