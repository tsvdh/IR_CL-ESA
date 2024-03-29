package edu.kit.aifb.concept;


import org.terrier.sorting.HeapSort;

import gnu.trove.TIntDoubleHashMap;


public class TroveConceptVectorOrderedIterator implements IConceptIterator {

	private int[] index;
	private double[] values;
	
	private int currentPos;
	
	public TroveConceptVectorOrderedIterator( TIntDoubleHashMap valueMap ) {
		index = valueMap.keys();
		values = valueMap.getValues();
		HeapSort.heapSort( values, index );
		reset();
	}
	
	public int getId() {
		return index[currentPos];
	}

	public double getValue() {
		return values[currentPos];
	}

	public boolean next() {
		currentPos--;
		if( currentPos >= 0 ) {
			return true;
		}
		else {
			return false;
		}
	}

	public void reset() {
		currentPos = index.length;
	}

}
