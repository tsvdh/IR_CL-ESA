package edu.uka.aifb.concept.index;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.uka.aifb.api.concept.IConceptIterator;
import edu.uka.aifb.api.concept.IConceptVector;
import edu.uka.aifb.api.concept.index.ICVIndexBuilder;
import edu.uka.aifb.nlp.Language;
import edu.uka.aifb.tools.ConfigurationManager;

public class IndexedFileCVIndexBuilder implements ICVIndexBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.base_dir",
		"concepts.builder_cache_size"
	};
	
	static Logger logger = Logger.getLogger( IndexedFileCVIndexBuilder.class );
	
	private TroveCVIndexEntry[] m_cvIndexEntries; 
	
	private IndexedFileCVData m_cvData;
	
	int m_tmpFileCount;
	long m_currentCacheByteSize;
	int m_maxCacheKByteSize;
	
	private String m_pathPrefix;
	
	private ByteArrayOutputStream buffer;
	
	public IndexedFileCVIndexBuilder( Configuration config, int size, String name, Language language ) {
		ConfigurationManager.checkProperties( config, REQUIRED_PROPERTIES );
		
		m_cvIndexEntries = new TroveCVIndexEntry[size];
		for( int i=0; i<size; i++ ) {
			m_cvIndexEntries[i] = new TroveCVIndexEntry();
		}

		m_cvData = new IndexedFileCVData(); 
		
		m_pathPrefix = config.getString( "concepts.base_dir" ) + "/" + name;
		if( language != null && language != Language.UNKNOWN ) {
			m_pathPrefix += "_" + language;
		}
 
		m_tmpFileCount = 0;
		m_currentCacheByteSize = 0;
		
		m_maxCacheKByteSize = config.getInt( "concepts.builder_cache_size" );
		
		buffer = new ByteArrayOutputStream();
	}
	
	public boolean indexExists() {
		File conceptFile = new File( m_pathPrefix + ".concepts" );
		File postionFile = new File( m_pathPrefix + ".positions" );
		File cvDataFile = new File( m_pathPrefix + ".cv_data" );
		return conceptFile.exists() && postionFile.exists() && cvDataFile.exists();
	}

	public void add( IConceptVector cv ) {
		int docId = m_cvData.add( new PersistantCVData( cv.getData() ) );
		
		IConceptIterator it = cv.iterator();
		while( it.next() ) {
			m_cvIndexEntries[it.getId()].add( docId, it.getValue() );
			m_currentCacheByteSize += 12;
		}
		
		if( m_currentCacheByteSize / 1024 > m_maxCacheKByteSize ) {
			buildTmpIndex();
		}
	}

	private long writeEntries(
			OutputStream out, 
			TroveCVIndexEntry entry, int conceptId,
			IndexedFilePositions positions, long currentOffset ) throws IOException
	{
		buffer.reset();
		
		DataOutputStream dataOut = new DataOutputStream( buffer );
		entry.writeToDataOutput( dataOut );
		dataOut.close();
		
		int byteSize = buffer.size();
		
		buffer.writeTo( out );
		
		if( positions != null ) {
			positions.set( conceptId, currentOffset, byteSize );
		}
		return currentOffset + byteSize;
	}
	
	private void buildTmpIndex() {
		try {
			m_tmpFileCount++;
			OutputStream tmpFileOut = new FileOutputStream (
					m_pathPrefix + ".concepts." + m_tmpFileCount );
			logger.info( "Building temporary concept index file " + m_pathPrefix + ".concepts." + m_tmpFileCount );
			
			/*
			 * Write concept entries
			 */
			for( int i=0; i<m_cvIndexEntries.length; i++ ) {
				writeEntries( tmpFileOut, m_cvIndexEntries[i], i, null, 0 );
			}
			
			tmpFileOut.close();
			
			/*
			 * Reset index entries
			 */
			for( TroveCVIndexEntry entry : m_cvIndexEntries ) {
				entry.reset();
			}
			m_currentCacheByteSize = 0;
		}
		catch( IOException e ) {
			logger.error( e );
		}
	}
	
	public void buildIndex() {
		buildTmpIndex();
		
		try {
			DataInputStream[] tmpFileDataIn = new DataInputStream[m_tmpFileCount];
			for( int i=0; i<m_tmpFileCount; i++ ) {
				tmpFileDataIn[i] = new DataInputStream( new BufferedInputStream( new FileInputStream (
						m_pathPrefix + ".concepts." + (i+1) ) ) );
			}
			
			logger.info( "Building concept index file " + m_pathPrefix + ".concepts" );
			OutputStream conceptFileOut = new FileOutputStream (
					m_pathPrefix + ".concepts" );

			IndexedFilePositions positions = new IndexedFilePositions( m_cvIndexEntries.length );
			long currentOffset = 0;

			for( int conceptId = 0; conceptId<m_cvIndexEntries.length; conceptId++ )
			{
				TroveCVIndexEntry entry = TroveCVIndexEntry.readFromDataInput( tmpFileDataIn[0] );
				for( int i=1; i<tmpFileDataIn.length; i++ ) {
					entry.merge( TroveCVIndexEntry.readFromDataInput( tmpFileDataIn[i] ) );
				}
				
				currentOffset = writeEntries( conceptFileOut, entry, conceptId, positions, currentOffset );
				
			}
			
			conceptFileOut.close();
			
			 /*
          * Write positions
          */
         logger.info( "Saving positions" );
			positions.saveToFile( new File( m_pathPrefix + ".positions" ) );
         
         /*
          * Write concept vector data
          */
         logger.info( "Saving concept vector data" );
			m_cvData.saveToFile( new File( m_pathPrefix + ".cv_data" ) );
         
         /*
          * Remove temporary files
          */
         logger.info( "Removing temporary files" );
			for( int i=0; i<m_tmpFileCount; i++ ) {
				File tmpFile = new File( m_pathPrefix + ".concepts." + (i+1) );
				tmpFile.delete();
			}
		}
		catch( IOException e ) {
			logger.error( e );
		}
	}


}
