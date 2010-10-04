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
			start = m.end();
		}
	}
	
	public boolean matches(String str) { 
		for(Rule r : rules) { 
			if(r.matches(str)) { 
				return true;
			}
		}
		return false;
	}
	
	public String rewrite(String str) { 
		for(Rule r : rules) { 
			if(r.matches(str)) { 
				str = r.rewrite(str);
			}
		}
		return str;
	}
}
