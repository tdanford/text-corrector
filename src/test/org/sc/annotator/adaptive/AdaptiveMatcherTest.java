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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

public class AdaptiveMatcherTest {

	private AdaptiveMatcher matcher;
	
	@Before 
	public void setup() { 
		//matcher = new WebClient("http://localhost:8080/matcher");
		matcher = new SimpleInMemoryAdaptiveMatcher();
	}
	
	@Test 
	public void test1() throws IOException, MatcherException {  
		Match m1 = new Match(new Context("foo bar"), "xxx", "yyy");
		Context c = matcher.registerMatch(m1);
		
		assertThat(c, is(equalTo(m1.context())));

		Collection<Match> matches = matcher.findMatches(new Context("foo bar"), "xxx");

		assertThat(matches, hasItem(m1));
		
		Match m2 = new Match(new Context("foo grok"), "xxx", "yyy");
		Context c2 = matcher.registerMatch(m2);
		
		assertThat(c2, is(equalTo(new Context("foo"))));
		
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
