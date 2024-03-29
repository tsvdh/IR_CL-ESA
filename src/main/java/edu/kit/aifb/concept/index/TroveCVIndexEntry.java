package edu.kit.aifb.concept.index;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TroveCVIndexEntry implements Serializable {

	private static final long serialVersionUID = 1347532872147315675L;

	static Log logger = LogFactory.getLog( TroveCVIndexEntry.class );
	
	private TIntArrayList m_docIds;
	private TDoubleArrayList m_values;
	
	double m_valueSum;
	
	public TroveCVIndexEntry() {
		m_docIds = new TIntArrayList();
		m_values = new TDoubleArrayList();
		reset();
	}
	
	public void reset() {
		m_docIds.reset();
		m_values.reset();
		m_valueSum = 0;
		
		/*
		 * Marker at end of list
		 */
		m_docIds.add( -1 );
		m_values.add( 0 );
	}
	
	public int numberOfDocuments() {
		return m_docIds.size() - 1;
	}
	
	public void add( int docId, double value ) {
		if( value < 0 ) {
			logger.error( "Ignoring new CV entry with value < 0" );
			return;
		}
		else if( value == 0 ) {
			return;
		}
		
		int min = 0;
		int max = m_values.size() - 1;
		int index = ( max - min ) / 2 + min;
		
		while( min < max )
		{
			if( m_values.get( index ) >= value ) {
				min = index + 1;
			}
			else {
				max = index; 
			}
			index = ( max - min ) / 2 + min;
		}
		
		m_docIds.insert( index, docId );
		m_values.insert( index, value );
		m_valueSum += value;
	}
	
	public void merge( TroveCVIndexEntry entry ) {
		TIntArrayList newDocIds = new TIntArrayList();
		TDoubleArrayList newValues = new TDoubleArrayList();
		m_valueSum = 0;
		
		int indexThis = 0;
		int indexEntry = 0;
		while( indexThis < m_values.size() - 1 && indexEntry < entry.m_values.size() - 1 )
		{
			if( m_values.get( indexThis ) >= entry.m_values.get( indexEntry ) )
			{
				newDocIds.add( m_docIds.get( indexThis ) );
				newValues.add( m_values.get( indexThis ) );
				m_valueSum += m_values.get( indexThis );
				indexThis++;
			}
			else {
				newDocIds.add( entry.m_docIds.get( indexEntry ) );
				newValues.add( entry.m_values.get( indexEntry ) );
				m_valueSum += entry.m_values.get( indexEntry );
				indexEntry++;
			}
		}
		
		while( indexThis < m_values.size() - 1 ) {
			newDocIds.add( m_docIds.get( indexThis ) );
			newValues.add( m_values.get( indexThis ) );
			m_valueSum += m_values.get( indexThis );
			indexThis++;
		}

		while( indexEntry < entry.m_values.size() - 1 ) {
			newDocIds.add( entry.m_docIds.get( indexEntry ) );
			newValues.add( entry.m_values.get( indexEntry ) );
			m_valueSum += entry.m_values.get( indexEntry );
			indexEntry++;
		}

		newDocIds.add( -1 );
		newValues.add( 0 );
		
		m_docIds = newDocIds;
		m_values = newValues;
	}
	
	public void writeToDataOutput( DataOutput out ) throws IOException {
		for( int i=0; i<m_docIds.size(); i++ ) {
			out.writeInt( m_docIds.get( i ) );
			out.writeDouble( m_values.get( i ) );
		}
	}
	
	public static TroveCVIndexEntry readFromDataInput( DataInput in ) throws IOException {
		TroveCVIndexEntry entry = new TroveCVIndexEntry();
		entry.m_docIds.reset();
		entry.m_values.reset();
		
		int id = 0;
		double value;
		
		while( id >= 0 ) {
			id = in.readInt();
			value = in.readDouble();
			
			entry.m_docIds.add( id );
			entry.m_values.add( value );
			entry.m_valueSum += value;
		}
		
		return entry;
	}
	
	public class TroveCVIndexEntryIterator implements ICVIndexEntryIterator {
		private int m_index = -1;
		
		public int getDocId() {
			return m_docIds.get( m_index );
		}

		public double getValue() {
			return m_values.get( m_index );
		}

		public boolean next() {
			m_index++;
			if( m_index < m_docIds.size() - 1 ) {
				return true;
			}
			else {
				return false;
			}
		}

		public double getValueSum() {
			return m_valueSum;
		}
		
	}
	
	public ICVIndexEntryIterator iterator() {
		return new TroveCVIndexEntryIterator();
	}
}
