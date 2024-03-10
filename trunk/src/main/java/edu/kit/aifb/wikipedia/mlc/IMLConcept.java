package edu.kit.aifb.wikipedia.mlc;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.wikipedia.sql.IPage;

public interface IMLConcept {

	public int getId();

	public Collection<IPage> getPages( Language lang ) throws SQLException;
	
	public Map<Language,Collection<IPage>> getPages() throws SQLException;

}
