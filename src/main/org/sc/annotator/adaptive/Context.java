package org.sc.annotator.adaptive;

import java.util.*;

public class Context implements Comparable<Context> {

	private String[] address;
	
	public Context() { 
		address = new String[0];
	}
	
	public Context(String str) { 
		this(str.split("\\s+"));
	}
	
	public Context(String[] a) { 
		address = a.clone();
		for(String aa : address) { 
			if(aa.trim().length() == 0) { 
				throw new IllegalArgumentException(String.valueOf(Arrays.asList(a)));
			}
		}
	}
	
	public Context(Context c, String a) { 
		address = new String[c.address.length+1];
		for(int i = 0; i < c.address.length; i++) { 
			address[i] = c.address[i];
		}
		address[c.address.length] = a;
	}
	
	public boolean isTopContext() { return address.length==0; }
	
	public int depth() { return address.length; }
	
	public Context leastUpperBound(Context c) { 
		int count = 0;
		for(int i = 0; i < Math.min(address.length, c.address.length) && address[i].equals(c.address[i]); i++, count++) 
			;
		String[] lub = new String[count];
		for(int i = 0; i < lub.length; i++) { 
			lub[i] = address[i];
		}
		return new Context(lub);
	}
	
	public String toString() { 
		StringBuilder sb = new StringBuilder();
		for(String a : address) { 
			if(sb.length() > 0) { sb.append(" "); }
			sb.append(a);
		}
		return String.format("[%s]", sb.toString());
	}
	
	public int hashCode() { 
		int code = 17;
		for(String a : address) { 
			code += a.hashCode(); code *= 37;
		}
		return code;
	}
	
	public boolean equals(Object o) { 
		if(!(o instanceof Context)) { return false; }
		Context c = (Context)o;
		if(address.length != c.address.length) { return false; }
		for(int i = 0; i < address.length; i++) { 
			if(!address[i].equals(c.address[i])) { 
				return false;
			}
		}
		return true;
	}
	
	public boolean isSubContext(Context c) { 
		if(address.length < c.address.length) { return false; }
		for(int i = 0; i < c.address.length; i++) { 
			if(!address[i].equals(c.address[i])) { return false; }
		}
		return true;
	}
	
	public int compareTo(Context c) { 
		for(int i = 0; i < Math.min(address.length, c.address.length); i++) { 
			if(!address[i].equals(c.address[i])) { 
				return address[i].compareTo(c.address[i]);
			}
		}
		if(address.length < c.address.length) { return -1; }
		if(address.length > c.address.length) { return 1; }
		return 0;
	}

	public boolean isParentOf(Context context) {
		if(context.address.length != address.length + 1) { return false; }
		return context.isSubContext(this);
	}
}
