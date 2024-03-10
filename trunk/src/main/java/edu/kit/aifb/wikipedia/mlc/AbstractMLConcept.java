package edu.kit.aifb.wikipedia.mlc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.wikipedia.sql.IPage;
import edu.kit.aifb.wikipedia.sql.Page;

abstract public class AbstractMLConcept implements IMLConcept {

	Log logger;
	
	int id;
	MLCFactory factory;
	String pageTable;
	
	public AbstractMLConcept( MLCFactory factory, int id, String pageTable ) {
		logger = LogFactory.getLog( this.getClass() );
		
		this.factory = factory;
		this.id = id;
		this.pageTable = pageTable;
	}
	
	public int getId() {
		return id;
	}

	public Collection<IPage> getPages( Language lang ) throws SQLException {
		PreparedStatement pst = factory.getJdbcFactory().prepareStatement(
				"select mlc_page from "
				+ pageTable
				+ " where mlc_id=? and mlc_lang=?;" );
		try {
			pst.setInt( 1, id );
			pst.setString( 2, lang.toString() );

			Collection<IPage> pages = new ArrayList<IPage>();
			ResultSet rs = pst.executeQuery();
			try {
				while( rs.next() ) {
					pages.add( new Page( rs.getInt( 1 ) ) );
				}
				return pages;
			}
			finally {
				rs.close();
			}
		}
		finally {
			pst.close();
		}
	}

	public Map<Language, Collection<IPage>> getPages() throws SQLException {
		PreparedStatement pst = factory.getJdbcFactory().prepareStatement(
				"select mlc_page, mlc_lang from "
				+ pageTable
				+ " where mlc_id=?;" );
		try {
			pst.setInt( 1, id );

			Map<Language,Collection<IPage>> pages = new HashMap<Language,Collection<IPage>>();
			ResultSet rs = pst.executeQuery();
			try {
				while( rs.next() ) {
					IPage p = new Page( rs.getInt( 1 ) );
					Language l = Language.getLanguage( rs.getString( 2 ) );
					if( !pages.containsKey( l ) ) {
						pages.put( l, new ArrayList<IPage>() );
					}
					pages.get( l ).add( p );
				}
				return pages;
			}
			finally {
				rs.close();
			}
		}
		finally {
			pst.close();
		}
	}

	@Override
	public boolean equals( Object o ) {
		if( o instanceof IMLConcept ) {
			return id == ((IMLConcept)o).getId();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "MLC#" + id ;
	}
}
