package org.sc.annotator.adaptive;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.junit.*;
import org.sc.annotator.adaptive.*;
import org.sc.annotator.adaptive.client.WebClient;
import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
import org.sc.annotator.adaptive.exceptions.MatcherException;

import static org.junit.Assert.*;

public class AdaptiveMatcherTest {

	private AdaptiveMatcher matcher;
	
	private Match m1;
	
	@Before 
	public void setup() { 
		matcher = new WebClient("http://localhost:8080/matcher");
		
	}
	
	@Test 
	public void test1() throws IOException, MatcherException {  
		m1 = new Match(new Context("foo bar"), "xxx", "yyy");
		
		Context c = matcher.registerMatch(m1);
		assertEquals(c, m1.context());

		Collection<Match> matches = matcher.findMatches(new Context("foo bar"), "xxx");
		assertTrue(matches.contains(m1));
		
		Match m2 = new Match(new Context("foo grok"), "xxx", "yyy");
		
		Context c2 = matcher.registerMatch(m2);
		assertEquals(c2, new Context("foo"));
		
		matches = matcher.findMatches(new Context("foo quux"), "xxx");
	}	
	
	@After 
	public void teardown() { 
		try {
			matcher.close();
		} catch (MatcherCloseException e) {
			e.printStackTrace(System.err);
		}
	}
}
