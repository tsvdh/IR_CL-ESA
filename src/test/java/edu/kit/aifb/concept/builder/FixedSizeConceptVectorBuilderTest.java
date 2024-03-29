package edu.kit.aifb.concept.builder;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.kit.aifb.concept.IConceptVector;

public class FixedSizeConceptVectorBuilderTest {

	FixedSizeConceptVectorBuilder builder;
	
	@Before
	public void initialize() {
		builder = new FixedSizeConceptVectorBuilder();
		builder.setSize( 2 );
	}
	
	@Test
	public void simple() {
		int ids[] = new int[] { 0, 1, 2, 9 };
		double values[] = new double[] { 1, .9, .8, .7 };
		
		builder.reset( "test", 10 );
		builder.addScores( ids, values, 4 );
		IConceptVector cv = builder.getConceptVector();
		
		Assert.assertEquals( 2, cv.count() );
		for( int index : new int[] { 0, 1 } ) {
			Assert.assertEquals( values[index], cv.get( ids[index] ), .001 );
		}
		for( int index : new int[] { 2, 3 } ) {
			Assert.assertEquals( 0d, cv.get( ids[index] ), .001 );
		}
	}

	@Test
	public void reverse() {
		int ids[] = new int[] { 0, 1, 2, 9 };
		double values[] = new double[] { .7, .8, .9, 1 };
		
		builder.reset( "test", 10 );
		builder.addScores( ids, values, 4 );
		IConceptVector cv = builder.getConceptVector();
		
		Assert.assertEquals( 2, cv.count() );
		for( int index : new int[] { 2, 3 } ) {
			Assert.assertEquals( values[index], cv.get( ids[index] ), .001 );
		}
		for( int index : new int[] { 0, 1 } ) {
			Assert.assertEquals( 0d, cv.get( ids[index] ), .001 );
		}
	}

	@Test
	public void missing() {
		int ids[] = new int[] { 0, 1, 2, 9 };
		double values[] = new double[] { .7, .8, .9, 1 };
		
		builder.reset( "test", 10 );
		builder.addScores( ids, values, 3 );
		IConceptVector cv = builder.getConceptVector();
		
		Assert.assertEquals( 2, cv.count() );
		for( int index : new int[] { 1, 2 } ) {
			Assert.assertEquals( values[index], cv.get( ids[index] ), .001 );
		}
		for( int index : new int[] { 0, 3 } ) {
			Assert.assertEquals( 0d, cv.get( ids[index] ), .001 );
		}
	}

	@Test
	public void random() {
		int ids[] = new int[] { 0, 1, 2, 9 };
		double values[] = new double[] { .4, .2, .3, .7 };
		
		builder.reset( "test", 10 );
		builder.addScores( ids, values, 4 );
		IConceptVector cv = builder.getConceptVector();
		
		Assert.assertEquals( 2, cv.count() );
		for( int index : new int[] { 0, 3 } ) {
			Assert.assertEquals( values[index], cv.get( ids[index] ), .001 );
		}
		for( int index : new int[] { 1, 2 } ) {
			Assert.assertEquals( 0d, cv.get( ids[index] ), .001 );
		}
	}

}
