package edu.kit.aifb.wikipedia.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LanglinksApiLanglinksMap implements ILanglinksMap {

	static Log logger = LogFactory.getLog( LanglinksApiLanglinksMap.class );
	
	ILanglinksApi llApi;
	
	@Autowired
	public void setLanglinksApi( ILanglinksApi llApi ) {
		this.llApi = llApi;;
	}

	public int map( int sourceId ) {
		return llApi.getTargetPageId( sourceId );
	}

}
