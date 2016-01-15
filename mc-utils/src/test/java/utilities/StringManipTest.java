package test.utilities;

import com.google.common.collect.ImmutableSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utilities.StringManip;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** 
* StringManip Tester. 
* 
* @author <Authors name> 
* @since <pre>Jan 15, 2016</pre> 
* @version 1.0 
*/ 
public class StringManipTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: removeStopwords(String original, Set<String> stopwords) 
* 
*/ 
@Test
public void testRemoveStopwords() throws Exception {
    Set<String> stop = ImmutableSet.of("test", "thing", "win");
    String result = StringManip.removeStopwords("test of thing and need to win", stop);
    assertEquals("of and need to", result);
} 

/** 
* 
* Method: splitSentences(String text) 
* 
*/ 
@Test
public void testSplitSentences() throws Exception {
    String[] sentences = {"Test 1.", "Test 2!", "Test 4?"};
    String combinedSentences = Arrays.asList(sentences).stream().collect(Collectors.joining(" "));
    String[] splitSentences = StringManip.splitSentences(combinedSentences);
    Arrays.asList(sentences).forEach(s -> assertTrue(Arrays.asList(splitSentences).contains(s)));
}

/**
 *
 * Method: removeNumbers(String s)
 *
 */
@Test
public void testRemoveNumbers() throws Exception {
    Assert.assertEquals("abc", StringManip.removeNumbers("abc 123"));
    Assert.assertEquals("abc", StringManip.removeNumbers("123 abc"));
    Assert.assertEquals("abc abc", StringManip.removeNumbers("abc 123 abc"));
    Assert.assertEquals("abc abc", StringManip.removeNumbers("abc abc"));
}


} 
