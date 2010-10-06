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
import org.slf4j.Logger;

public class SimpleInMemoryAdaptiveMatcher implements AdaptiveMatcher {
	
	private Map<String,Set<Match>> matches;
	
	private Logger logger;
	
	public SimpleInMemoryAdaptiveMatcher(Logger logger) {
		this.logger = logger;
		matches = new TreeMap<String,Set<Match>>();
	}
	
	/**
	 * Package-only access to the internal Match store.
	 * 
	 * @return
	 */
	Map<String,Set<Match>> getMatches() { 
		return matches;
	}
	
	public void clear() { 
		matches.clear();
	}
	
	public void addMatch(Match m) { 
		if(!matches.containsKey(m.match())) { 
			matches.put(m.match(), new LinkedHashSet<Match>());
		}
		matches.get(m.match()).add(m);
	}
	
	public void removeMatch(Match m) { 
		matches.get(m.match()).remove(m);
		if(matches.get(m.match()).isEmpty()) { 
			matches.remove(m.match());
		}
	}

	/**
	 * @inheritDoc
	 */
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

	/**
	 * @inheritDoc
	 */
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

	/**
	 * @inheritDoc
	 * 
	 * This method always succeeds, for the in-memory adaptive matcher.
	 */
	public void close() throws MatcherCloseException {
	} 
	
}