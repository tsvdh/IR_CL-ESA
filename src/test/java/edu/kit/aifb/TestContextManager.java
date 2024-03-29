package edu.kit.aifb;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestContextManager {

	static ApplicationContext context;
	
	public static ApplicationContext getContext() {
		if( context == null ) {
			context = new ClassPathXmlApplicationContext( "test_context.xml" );
		}
		return context;
	}		

}
