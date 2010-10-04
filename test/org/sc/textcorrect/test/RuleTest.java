package org.sc.textcorrect.test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.junit.*;
import org.sc.textcorrect.*;

import static org.junit.Assert.*;

public class RuleTest {
	
	@Test 
	public void testRule1() throws IOException {  
		RuleFile rules = new RuleFile(new File("rules/protein.rls"));
		
		String s1 = " a.a. 100-200";
		String s2 = " aa 100-200";
		assertEquals(rules.rewrite(s1), s2);
		
		String s3 = "C-Term aa. 4300";
		String s4 = "C-terminus aa 4300";
		assertEquals(rules.rewrite(s3), s4);
	}	
}
