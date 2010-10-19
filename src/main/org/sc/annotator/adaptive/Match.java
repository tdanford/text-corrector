package org.sc.annotator.adaptive;

import java.util.Collection;

public class Match {
	
	public static Context lubContext(Collection<Match> ms) { 
		Context c = null;
		for(Match m : ms) { 
			if(c == null) { 
				c = m.context();
			} else { 
				c = c.leastUpperBound(m.context());
			}
		}
		return c;
	}
	
	private Context context;
	private String match;
	private String value;
	
	public Match(Context c, String m, String v) { 
		context = c;
		match = m;
		value = v;
	}
	
	public Match(Context c, Match m) { 
		context = c;
		match = m.match;
		value = m.value;
	}
	
	public Context context() { return context; }
	public String match() { return match; }
	public String value() { return value; }
	
	public String toString() { 
		return String.format("\"%s\"->\"%s\"@%s", 
				match, value, context.toString());
	}
	
	public int hashCode() { 
		int code = 17;
		code += context.hashCode(); code *= 37;
		code += match.hashCode(); code *= 37;
		//code += value.hashCode(); code *= 37;
		return code;
	}
	
	public boolean equals(Object o) { 
		if(!(o instanceof Match)) { return false; }
		Match m = (Match)o;
		return context.equals(m.context) 
			&& match.equals(m.match) 
			//&& value.equals(m.value)
			;
	}
}
