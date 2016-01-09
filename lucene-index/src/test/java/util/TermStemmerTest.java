package util;

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;

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
