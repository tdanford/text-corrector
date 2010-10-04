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
			"\"((?:[^\"]|(?:\\\"))+)\"" +	// rule match
			"\\s+" +
			"\"((?:[^\"]|(?:\\\"))+)\"" +   // rule rewrite
			"\\s+" +
			"\"((?:[^\"]|(?:\\\"))+)\"" +   // rule description
			"\\s*\\)");
	
	public static String rewriteGroupRegex = "((?:^|[^\\\\])\\$%d)";
	
	private String name, description;
	private Pattern pattern;
	private String rewrite;
	
	public Rule(String name, String match, String rewrite, String desc) { 
		this.name = name;
		this.description = desc;
		
		pattern = Pattern.compile(rewrite);
		this.rewrite = rewrite; 
	}
	
	public boolean matches(String str) { 
		return pattern.matcher(str).find();
	}
	
	public String rewrite(String str) { 
		Matcher m = pattern.matcher(str);
		int start = 0;
		while(m.find(start)) { 
			str = replace(str, m.start(), m.end(), m);
			
			m = pattern.matcher(str);
			start = m.start() + 1;
		}
		
		return str;
	}
	
	private String replace(String str, int start, int end, Matcher m) { 
		String internal = rewrite;
		for(int i = 1; i <= m.groupCount(); i++) {
			String group = m.group(i).replaceAll("\\$", "\\\\$");
			internal = internal.replaceAll(String.format(rewriteGroupRegex, i), group);
		}
		return str.substring(0, start) + internal + str.substring(0, end);
	}
}
