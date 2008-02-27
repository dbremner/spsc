package test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class SuperCompilerTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for SPSC");
		//$JUnit-BEGIN$
		suite.addTest(new JUnit4TestAdapter(spsc.SmallLanguageParsersTest.class));
		suite.addTest(new JUnit4TestAdapter(spsc.InterpreterTest.class));
		suite.addTest(new JUnit4TestAdapter(spsc.LazinessTest.class));
		//$JUnit-END$
		return suite;
	}

}