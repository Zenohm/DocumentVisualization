package internal.lucene_intf;

import internal.static_util.TermStemmer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/** 
* TermStemmer Tester. 
* 
* @author <Authors name> 
* @since <pre>Jan 5, 2016</pre> 
* @version 1.0 
*/ 
public class TermStemmerTest { 

@Before
public void before() throws Exception {
    org.apache.log4j.BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.INFO);
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: stemTerm(String term) 
* 
*/ 
@Test
public void testStemTerm() throws Exception { 
    assertEquals("visual", TermStemmer.stemTerm("visualization"));
    assertEquals("test", TermStemmer.stemTerm("test"));
    assertEquals("virtual", TermStemmer.stemTerm("virtualization"));
}
} 
