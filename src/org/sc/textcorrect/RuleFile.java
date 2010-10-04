package org.sc.textcorrect;

import java.io.*;

import java.util.*;
import java.util.regex.*;

public class RuleFile {
	
	private LinkedList<Rule> rules;

	public RuleFile(File f) throws IOException {  
		StringBuilder sb = new StringBuilder();
		
		int c=0;
		Reader r = new FileReader(f);
		while((c = r.read()) != -1) { 
			sb.append((char)c);
		}
		
		String contents = sb.toString();
		r.close();
		
		Matcher m = Rule.rulePattern.matcher(contents);
		int start = 0;
		rules = new LinkedList<Rule>();
		
		while(m.find(start)) { 
			String name = m.group(1), match = m.group(2), rewrite = m.group(3), desc = m.group(4);
			Rule rule = new Rule(name, match, rewrite, desc);
			rules.add(rule);
			start = m.end()+1;
		}
	}
	
	public boolean matches(String str) { 
		return matches(str, 0);
	}
	
	public boolean matches(String str, int start) { 
		for(Rule r : rules) { 
			if(r.findMatch(str, start) < str.length()) { 
				return true;
			}
		}
		return false;
	}

	public String rewrite(String str) {
		int start = 0;
		while(start < str.length() && matches(str, start)) { 
			//System.out.println(String.format("Matched: \"%s\"", str));
			
			int earliestMatch = str.length();
			Rule matcher = null;
			
			for(Rule r : rules) {
				int matchStart = r.findMatch(str, start);
				if(matchStart < earliestMatch) { 
					matcher = r;
					earliestMatch = matchStart;
				}
			}
			
			if(earliestMatch < str.length()) { 
				String newstr = matcher.rewrite(str);
				//System.out.println(String.format("\t%s : \"%s\" -> \"%s\"", matcher.getName(), str, newstr));
				str = newstr;
			}

			start = earliestMatch + 1;
		}
		return str;
	}
}
