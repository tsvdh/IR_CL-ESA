package edu.kit.aifb.wikipedia.mlc;


public class MLCategoryTreeItem implements Comparable<MLCategoryTreeItem> {

	public int docId;
	public double score;

	public MLCategoryTreeItem( int docId, double score ) {
		this.docId = docId;
		this.score = score;
	}

	public int compareTo( MLCategoryTreeItem item ) {
		double diff = item.score - score;
		if( diff < 0 )
			return -1;
		else if ( diff > 0 )
			return 1;
		else
			return 0;
	}
	
	@Override
	public int hashCode() {
		return docId;
	}
	
	@Override
	public boolean equals( Object o ) {
		if( o instanceof MLCategoryTreeItem ) {
			return ((MLCategoryTreeItem)o).docId == docId;
		}
		return false;
	}
}
