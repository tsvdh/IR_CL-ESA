package edu.kit.aifb.concept.index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.nlp.Language;

public class IndexedFileCVIndexBuilder implements ICVIndexBuilder {

	static final String[] REQUIRED_PROPERTIES = {
		"concepts.base_dir",
		"concepts.builder_cache_size"
	};

	static Log logger = LogFactory.getLog( IndexedFileCVIndexBuilder.class );

	private TroveCVIndexEntry[] m_cvIndexEntries; 

	private IndexedFileCVData m_cvData;

	int m_tmpFileCount;
	long m_currentCacheByteSize;
	int m_maxCacheKByteSize;

	private String m_filePrefix;

	private ByteArrayOutputStream buffer;

	private String baseDir;
	private Language language;
	private String id;

	private boolean compressEntries = false;

	@Required
	public void setBaseDirectory( String baseDir ) {
		this.baseDir = baseDir;
		updateFilePrefix();
	}

	public void setLanguage( Language language ) {
		this.language = language;
		updateFilePrefix();
	}

	public void initialize( String id, int conceptCount ) {
		this.id = id;
		updateFilePrefix();

		m_cvIndexEntries = new TroveCVIndexEntry[conceptCount];
		for( int i=0; i<conceptCount; i++ ) {
			m_cvIndexEntries[i] = new TroveCVIndexEntry();
		}
	}

	@Required
	public void setCacheSize( int maxCacheKByteSize ) {
		this.m_maxCacheKByteSize = maxCacheKByteSize;
	}

	private void updateFilePrefix() {
		if( baseDir != null && id != null ) {
			m_filePrefix = baseDir + "/" + id;
		}
		if( m_filePrefix != null && language != null && language != Language.UNKNOWN ) {
			m_filePrefix += "_" + language;
		}
	}

	public void setCompressEntries( boolean compress ) {
		this.compressEntries = compress;
	}

	public IndexedFileCVIndexBuilder() {
		m_cvData = new IndexedFileCVData(); 

		m_tmpFileCount = 0;
		m_currentCacheByteSize = 0;

		buffer = new ByteArrayOutputStream();
	}

	public boolean indexExists() {
		File conceptFile = new File( m_filePrefix + ".concepts" );
		File postionFile = new File( m_filePrefix + ".positions" );
		File cvDataFile = new File( m_filePrefix + ".cv_data" );
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
			IndexedFilePositions positions, long currentOffset,
			boolean compressSingleEntries ) throws IOException
			{
		buffer.reset();
		DataOutputStream dataOut;
		if( compressSingleEntries ) {
			dataOut = new DataOutputStream( new GZIPOutputStream( buffer ) );
		}
		else {
			dataOut = new DataOutputStream( buffer );
		}
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
			OutputStream tmpFileOut = new GZIPOutputStream( new BufferedOutputStream( new FileOutputStream (
					m_filePrefix + ".concepts." + m_tmpFileCount ) ) );
			logger.info( "Building temporary concept index file " + m_filePrefix + ".concepts." + m_tmpFileCount );

			/*
			 * Write concept entries
			 */
			for( int i=0; i<m_cvIndexEntries.length; i++ ) {
				writeEntries( tmpFileOut, m_cvIndexEntries[i], i, null, 0, false );
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
				tmpFileDataIn[i] = new DataInputStream( new GZIPInputStream( new BufferedInputStream( new FileInputStream (
						m_filePrefix + ".concepts." + (i+1) ) ) ) );
			}

			logger.info( "Building concept index file " + m_filePrefix + ".concepts" );
			OutputStream conceptFileOut = new FileOutputStream (
					m_filePrefix + ".concepts" );

			IndexedFilePositions positions = new IndexedFilePositions( m_cvIndexEntries.length );
			long currentOffset = 0;

			for( int conceptId = 0; conceptId<m_cvIndexEntries.length; conceptId++ )
			{
				TroveCVIndexEntry entry = TroveCVIndexEntry.readFromDataInput( tmpFileDataIn[0] );
				for( int i=1; i<tmpFileDataIn.length; i++ ) {
					entry.merge( TroveCVIndexEntry.readFromDataInput( tmpFileDataIn[i] ) );
				}

				currentOffset = writeEntries(
						conceptFileOut, entry, conceptId, positions, currentOffset, compressEntries );
			}

			conceptFileOut.close();

			/*
			 * Write positions
			 */
			logger.info( "Saving positions" );
			positions.saveToFile( new File( m_filePrefix + ".positions" ) );

			/*
			 * Write concept vector data
			 */
			logger.info( "Saving concept vector data" );
			m_cvData.saveToFile( new File( m_filePrefix + ".cv_data" ) );

			/*
			 * Remove temporary files
			 */
			logger.info( "Removing temporary files" );
			for( int i=0; i<m_tmpFileCount; i++ ) {
				File tmpFile = new File( m_filePrefix + ".concepts." + (i+1) );
				tmpFile.delete();
			}
		}
		catch( IOException e ) {
			logger.error( e );
		}
	}


}
