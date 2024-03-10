package edu.kit.aifb.wikipedia.mlc;

import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.aifb.TestContextManager;

public class MLCFactoryTest {

	static MLCFactory mlcFactory;
	
	@BeforeClass
	static public void initialize() {
		mlcFactory = (MLCFactory)TestContextManager.getContext().getBean(
				MLCFactory.class );
	}
	
	@Test
	public void rootCategory() throws SQLException {
		MLCategory rootCat = mlcFactory.getRootCat();
		Assert.assertEquals( 80, rootCat.getId() );
		Assert.assertEquals( 27, rootCat.getSubCategories().size() );
	}
	
}
