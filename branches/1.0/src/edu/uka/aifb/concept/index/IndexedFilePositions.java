package edu.uka.aifb.concept.index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class IndexedFilePositions implements Serializable {
		
	private static final long serialVersionUID = 1708445290823969368L;

	private long[] m_fileOffsets;
	private int[] m_byteSizes;

	public IndexedFilePositions( int size ) {
		m_fileOffsets = new long[size];
		m_byteSizes = new int[size];
	}

	public void set( int conceptId, long fileOffset, int byteSize ) {
		m_fileOffsets[conceptId] = fileOffset;
		m_byteSizes[conceptId] = byteSize;
	}

	public long getFileOffset( int conceptId ) {
		return m_fileOffsets[conceptId];
	}
	
	public int getByteSize( int conceptId ) {
		return m_byteSizes[conceptId];
	}
	
	public int size() {
		return m_fileOffsets.length;
	}
	
	public void saveToFile( File file ) throws IOException {
		ObjectOutputStream objectOut = new ObjectOutputStream(
				new BufferedOutputStream( new FileOutputStream (
						file ) ) );
		
		objectOut.writeObject( this );
		
		objectOut.close();
	}
	
	static public IndexedFilePositions readFromFile( File file ) throws IOException, ClassNotFoundException {
		ObjectInputStream objectIn = new ObjectInputStream( 
				new BufferedInputStream( new FileInputStream( 
						file ) ) );
		
		IndexedFilePositions positions = (IndexedFilePositions)objectIn.readObject();
		objectIn.close();
		
		return positions;
	}
	
}
