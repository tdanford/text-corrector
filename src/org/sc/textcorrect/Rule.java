package org.sc.textcorrect;

import java.util.*;
import java.util.regex.*;

import java.io.*;

public class Rule {

	public static Pattern rulePattern = Pattern.compile("\\s*" +
			"\\(" +  						// opening paren
			"\\s*" +
			"([^\\s]+)" +					// rule name  
			"\\s+" +
			"\"((?:[^\"]|(?:\\\\\"))+)\"" +	// rule match
			"\\s+" +
			"\"((?:[^\"]|(?:\\\\\"))+)\"" +   // rule rewrite
			"\\s+" +
			"\"((?:[^\"]|(?:\\\\\"))+)\"" +   // rule description
			"\\s*\\)");
	
	public static String rewriteGroupRegex = "((?:^|[^\\\\])\\$%d)";
	
	private String name, description;
	private Pattern pattern;
	private String rewrite;
	
	public Rule(String name, String match, String rewrite, String desc) { 
		//System.out.println(String.format("\"%s\"", match));
		this.name = name;
		this.description = desc;
		pattern = Pattern.compile(match);
		this.rewrite = rewrite;
	}
	
	public Rule(String ruleString) { 
		Matcher m = rulePattern.matcher(ruleString);
		if(!m.matches()) { throw new IllegalArgumentException(ruleString); }

		this.name = m.group(1);
		this.description = m.group(4);
		
		pattern = Pattern.compile(m.group(2));
		this.rewrite = m.group(3);
	}
	
	public int findMatch(String str, int start) { 
		Matcher m = pattern.matcher(str);
		if(m.find(start)) { 
			return m.start();
		} else { 
			return str.length();
		}
	}
	
	public String rewrite(String str) { 
		Matcher m = pattern.matcher(str);
		int start = 0;
		while(m.find(start)) { 
			str = replace(str, m.start(), m.end(), m);
			
			start = m.start() + 1;
			m = pattern.matcher(str);
		}
		
		return str;
	}
	
	private String replace(String str, int start, int end, Matcher m) { 
		String internal = rewrite;
		for(int i = 1; i <= m.groupCount(); i++) {
			String group = m.group(i).replaceAll("\\$", "\\\\$");
			internal = internal.replaceAll(String.format(rewriteGroupRegex, i), group);
		}
		return str.substring(0, start) + internal + str.substring(end, str.length());
	}

	public String getName() {
		return name;
	}
}
