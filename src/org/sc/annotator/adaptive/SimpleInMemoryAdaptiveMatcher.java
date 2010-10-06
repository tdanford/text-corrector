package org.sc.annotator.adaptive;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
import org.sc.annotator.adaptive.exceptions.MatcherException;

public class SimpleInMemoryAdaptiveMatcher implements AdaptiveMatcher {
	
	private Map<String,Set<Match>> matches;
	
	public SimpleInMemoryAdaptiveMatcher() { 
		matches = new TreeMap<String,Set<Match>>();
	}

	public Collection<Match> findMatches(Context c, String blockText) {
		LinkedList<Match> ms = new LinkedList<Match>();
		
		for(String k : matches.keySet()) { 
			
			if(blockText.contains(k)) { 
			
				for(Match m : matches.get(k)) { 
				
					Context lub = c.leastUpperBound(m.context());
					Match nm = new Match(lub, m);
					ms.add(nm);
				}
			}
		}
		
		return ms;
	}

	public Context registerMatch(Match m) {
		String matchText = m.match();
		if(!matches.containsKey(matchText)) { matches.put(matchText, new LinkedHashSet<Match>()); }

		Iterator<Match> itr = matches.get(matchText).iterator();
		
		while(itr.hasNext()) { 
			Match extant = itr.next();
			
			if(extant.value().equals(m.value())) { 
				itr.remove();
				Context lub = m.context().leastUpperBound(extant.context());
				Match lubMatch = new Match(lub, matchText, m.value());
				m = lubMatch;
			}
		}
		
		matches.get(matchText).add(m);
		
		return m.context();
	}

	public void close() throws MatcherCloseException {
	} 
	
}