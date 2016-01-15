package test.internal.static_util;

import internal.static_util.QueryUtils;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/** 
* QueryUtils Tester. 
* 
* @author <Authors name> 
* @since <pre>Jan 15, 2016</pre> 
* @version 1.0 
*/ 
public class QueryUtilsTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: mustContainWords(String... words) 
* 
*/ 
@Test
public void testMustContainWords() throws Exception {
    Query q = QueryUtils.mustContainWords("winning", "abc", "test");
    assertEquals("+contents:\"winning\" +contents:\"abc\" +contents:\"test\"", q.toString());
} 


} 
