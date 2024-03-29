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
 * The Original Code is BlockDirectIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.terrier.compression.BitIn;
import org.terrier.structures.postings.BlockIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.FieldScore;
/**
 * Extended direct index that saves both block 
 * and field information about the terms that 
 * appear in a document.
 * @author Douglas Johnson, Vassilis Plachouras
 * @version $Revision: 1.33 $
 */
public class BlockDirectIndex extends DirectIndex {
    protected int DocumentBlockCountDelta = 1;
    
	/** The logger used */
	private static Logger logger = Logger.getRootLogger();
	/**
	 * Constructs an instance of the class with 
	 * the given index, using the specified structure name.
	 * @param index The index to be used
	 * @param structureName the name of this direct index
	 * @throws IOException 
	 */
	public BlockDirectIndex(Index index, String structureName) throws IOException {
		super(index, structureName, BlockIterablePosting.class);
	}
	
	public BlockDirectIndex(Index index, String structureName,
			Class<? extends IterablePosting> postingClass) throws IOException 
	{
		super(index, structureName, postingClass);
	}
	
	/**
	 * Returns a five dimensional array containing the 
	 * term ids and the term frequencies for the given document. 
	 * @return int[][] a five dimensional array containing 
	 *         the term ids, frequencies, field scores, 
	 *         block frequencies and the containing the block ids.
	 * @param docid the id of the document whose terms we are looking for.
	 */
	public int[][] getTerms(int docid) {
		try {
			DocumentIndexEntry de = docIndex.getDocumentEntry(docid);
			if (de == null)
				return null;
			if (de.getNumberOfEntries() == 0)
				return null;
			ArrayList<int[]> temporaryTerms = new ArrayList<int[]>();
			ArrayList<int[]> temporaryBlockids = new ArrayList<int[]>();
			final BitIn file = this.file[de.getFileNumber()].readReset(de.getOffset(), de.getOffsetBits());
			//boolean hasMore = false;
			int blockCount = 0;
			final int fieldTags = FieldScore.FIELDS_COUNT;
			final boolean loadTagInformation = FieldScore.USE_FIELD_INFORMATION;
			int[][] documentTerms;
			if (loadTagInformation) {
				//while (((file.getByteOffset() + startOffset.Bytes) < endOffset.Bytes)
				//		|| (((file.getByteOffset() + startOffset.Bytes) == endOffset.Bytes)
				//			&& (file.getBitOffset() < endOffset.Bits))) {
				for(int j=0;j<de.getNumberOfEntries();j++)
				{
						int[] tmp = new int[4];
						tmp[0] = file.readGamma();
						tmp[1] = file.readUnary();
						tmp[2] = file.readBinary(fieldTags);
						int blockfreq = file.readUnary() - DocumentBlockCountDelta;
						tmp[3] = blockfreq;
						final int[] tmp2 = new int[blockfreq];
						if (blockfreq > 0)
						{
							for (int i = 0; i < blockfreq; i++) {
								tmp2[i] = file.readGamma();
								blockCount++;
							}
						}
						temporaryTerms.add(tmp);
						temporaryBlockids.add(tmp2);
					}
					documentTerms = new int[5][];
					documentTerms[0] = new int[temporaryTerms.size()];
					documentTerms[1] = new int[temporaryTerms.size()];
					documentTerms[2] = new int[temporaryTerms.size()];
					documentTerms[3] = new int[temporaryTerms.size()];
					documentTerms[4] = new int[blockCount];
					documentTerms[0][0] = ((int[]) temporaryTerms.get(0))[0] - 1;
					documentTerms[1][0] = ((int[]) temporaryTerms.get(0))[1];
					documentTerms[2][0] = ((int[]) temporaryTerms.get(0))[2];
					documentTerms[3][0] = ((int[]) temporaryTerms.get(0))[3];
					int[] blockids = ((int[]) temporaryBlockids.get(0));
					documentTerms[4][0] = blockids[0] - 1;
					for(int i=1; i<blockids.length; i++){
						documentTerms[4][i]= blockids[i] + documentTerms[4][i-1];
					}
					int blockindex = blockids.length;
					if (documentTerms[0].length > 1) {
						for (int i = 1; i < documentTerms[0].length; i++) {
							int[] tmpMatrix = (int[]) temporaryTerms.get(i);
							documentTerms[0][i] = tmpMatrix[0] + documentTerms[0][i - 1];
							documentTerms[1][i] = tmpMatrix[1];
							documentTerms[2][i] = tmpMatrix[2];
							documentTerms[3][i] = tmpMatrix[3];
							blockids = ((int[]) temporaryBlockids.get(i));
							if (blockids.length > 0)
							{
								documentTerms[4][blockindex] = blockids[0] - 1;
								blockindex++;
								for (int j = 1; j < blockids.length; j++) {
									documentTerms[4][blockindex] =
										blockids[j] + documentTerms[4][blockindex - 1];
									blockindex++;
								}
							}
						}
					}
			} else {
				for(int j=0;j<de.getNumberOfEntries();j++)
				{
				//while (((file.getByteOffset() + startOffset.Bytes) < endOffset.Bytes)
				//		|| (((file.getByteOffset() + startOffset.Bytes) == endOffset.Bytes)
				//			&& (file.getBitOffset() < endOffset.Bits))) {
						int[] tmp = new int[4];
						tmp[0] = file.readGamma();
						tmp[1] = file.readUnary();
						//tmp[2] = file.readBinary(ApplicationSetup.FIELD_TAGS);
						int blockfreq = file.readUnary() - DocumentBlockCountDelta;
						tmp[3] = blockfreq;
						int[] tmp2 = new int[blockfreq];
						if (blockfreq > 0)
						{
							for (int i = 0; i < blockfreq; i++) {
								tmp2[i] = file.readGamma();
								blockCount++;
							}
						}
						temporaryTerms.add(tmp);
						temporaryBlockids.add(tmp2);
					}
					documentTerms = new int[5][];
					documentTerms[0] = new int[temporaryTerms.size()];
					documentTerms[1] = new int[temporaryTerms.size()];
					//documentTerms[2] = new int[temporaryTerms.size()];
					documentTerms[2] = null;
					documentTerms[3] = new int[temporaryTerms.size()];
					documentTerms[4] = new int[blockCount];
					documentTerms[0][0] = ((int[]) temporaryTerms.get(0))[0] - 1;
					documentTerms[1][0] = ((int[]) temporaryTerms.get(0))[1];
					//documentTerms[2][0] = ((int[]) temporaryTerms.get(0))[2];
					documentTerms[3][0] = ((int[]) temporaryTerms.get(0))[3];
					
					int[] blockids = ((int[]) temporaryBlockids.get(0));
					if (blockids.length > 0)
					{
						documentTerms[4][0] = blockids[0] - 1;
						for(int i=1; i<blockids.length; i++){
							documentTerms[4][i]= blockids[i] + documentTerms[4][i-1];
						}
					}
					
					int blockindex = blockids.length;
					if (documentTerms[0].length > 1) {
						for (int i = 1; i < documentTerms[0].length; i++) {
							int[] tmpMatrix = (int[]) temporaryTerms.get(i);
							documentTerms[0][i] =
								tmpMatrix[0] + documentTerms[0][i - 1];
							documentTerms[1][i] = tmpMatrix[1];
							//documentTerms[2][i] = tmpMatrix[2];
							documentTerms[3][i] = tmpMatrix[3];
							blockids = ((int[]) temporaryBlockids.get(i));
							if (blockids.length > 0)
							{
								documentTerms[4][blockindex] = blockids[0] - 1;
								blockindex++;
								for (int j = 1; j < blockids.length; j++) {
									documentTerms[4][blockindex] =
										blockids[j] + documentTerms[4][blockindex - 1];
									blockindex++;
								}
							}
						}
					}
			}
			return documentTerms;
		} catch (IOException ioe) {
			logger.fatal(
				"Input/Output exception while fetching the term ids for a given document. Stack trace follows.",ioe);
		}
		return null;
	}
}
