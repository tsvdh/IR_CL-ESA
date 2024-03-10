package edu.kit.aifb.wikipedia.mlc;

import org.junit.Before;

import edu.kit.aifb.TestContextManager;

public class MLCDatabaseTest {

	MLCDatabase mlcDb;
	
	@Before
	public void loadMlcDatabase() {
		mlcDb = (MLCDatabase)TestContextManager.getContext().getBean(
				"wp200909_mlc_articles" );
	}
	
}
