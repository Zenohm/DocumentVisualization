package test.internal.static_util;

import internal.static_util.StringFilters;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** 
* StringFilters Tester. 
* 
* @author <Authors name> 
* @since <pre>Jan 13, 2016</pre> 
* @version 1.0 
*/ 
public class StringFiltersTest { 

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
* Method: isNumeric(String str) 
* 
*/ 
@Test
public void testIsNumeric() throws Exception {
    assertTrue(StringFilters.isNumeric("42"));
    assertTrue(StringFilters.isNumeric("-1"));
    assertTrue(StringFilters.isNumeric("10000"));
    assertTrue(StringFilters.isNumeric("123"));
    assertFalse(StringFilters.isNumeric("abd"));
    assertFalse(StringFilters.isNumeric("abc123"));
    assertFalse(StringFilters.isNumeric("abc1"));
} 

/** 
* 
* Method: removeNumbers(String s) 
* 
*/ 
@Test
public void testRemoveNumbers() throws Exception {
    assertEquals("abc", StringFilters.removeNumbers("abc 123"));
    assertEquals("abc", StringFilters.removeNumbers("123 abc"));
    assertEquals("abc abc", StringFilters.removeNumbers("abc 123 abc"));
    assertEquals("abc abc", StringFilters.removeNumbers("abc abc"));
} 


} 
