package org.sc.annotator.adaptive;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
import org.sc.annotator.adaptive.exceptions.MatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an AdaptiveMatcher by keeping an in-memory data structure.  
 * 
 * This is the core implementation of the adaptive matcher -- other implementations (such as 
 * {@link SimpleFileAdaptiveMatcher}) are implemented as wrappers around this class.
 * 
 * @author Timothy Danford
 * @date   October, 2010
 */
public class SimpleInMemoryAdaptiveMatcher implements AdaptiveMatcher {
	
	private Map<String,Set<Match>> matches;
	
	private Logger logger;
	private boolean isGeneralizing, isSimplifying;
	
	public SimpleInMemoryAdaptiveMatcher(Logger logger) {
		this.logger = logger;
		matches = new TreeMap<String,Set<Match>>();
		isGeneralizing = false;
		isSimplifying = true;
	}
	
	public SimpleInMemoryAdaptiveMatcher() { 
		this(LoggerFactory.getLogger(SimpleInMemoryAdaptiveMatcher.class.getSimpleName()));
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
	
	/**
	 * @inheritDoc
	 */
	public void unregisterMatch(Match m) throws MatcherException { 
		String source = m.value();
		Context ctxt = m.context();
	
		if(matches.containsKey(source)) { 
			
			Set<Match> ms = matches.get(source);
			Iterator<Match> itr = ms.iterator();
			int found = 0, removed = 0;
			
			// For each match with the same source string...
			while(itr.hasNext()) { 

				Match extant = itr.next();
				Context extC = extant.context();
				
				// ... remove the match from the cache if it has a 'relevant' Context.
				// Note that identical context are *always* relevant, while sub-contexts 
				// are relevant iff the matcher is "simplifying."
				if(extC.isSubContext(ctxt) || 
					(isSimplifying && extC.isSubContext(ctxt))) { 
					
					itr.remove();
					removed += 1;
				}
				
				found += 1;
			}
			
			if(found==0) {
				// This is only triggered if the matches Map contains an empty set.
				throw new MatcherException(String.format("No matches for source string \"%s\"", 
						source));
			}
			
			logger.info(String.format("unregisterMatch: found %d matches, removed %d", found, removed));
	
			// finally, prune any empty sets.
			if(ms.isEmpty()) { 
				matches.remove(source);
			}
			
		} else { 
			throw new MatcherException(String.format("No source string \"%s\"",
					source));
		}
	}
	
	public void addMatch(Match m) { 
		if(!matches.containsKey(m.match())) { 
			matches.put(m.match(), new LinkedHashSet<Match>());
		}
		matches.get(m.match()).add(m);
		logger.info(String.format("Added: %s", m.toString()));			
	}
	
	public void removeMatch(Match m) { 
		matches.get(m.match()).remove(m);
		if(matches.get(m.match()).isEmpty()) { 
			matches.remove(m.match());
		}
	}
	
	public void removeMatches(Collection<Match> ms) { 
		for(Match m : ms) { 
			removeMatch(m);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Collection<Match> findMatches(Context c, String blockText) {
		logger.info(String.format("findMatches(%s, \"%s\")", c.toString(), blockText));
		
		LinkedList<Match> ms = new LinkedList<Match>();
		
		// We look *in* the given block-text, for corresponding pieces of source text.
		for(String k : matches.keySet()) { 
			
			if(blockText.contains(k)) {

				for(Match m : matches.get(k)) {
					
					Context mctxt = m.context();

					// Exact matches are always returned.
					// Sub-context matches are returned iff we are a "simplifying" matcher.
					if(c.equals(mctxt) 
							|| (isSimplifying && c.isSubContext(mctxt))
						) { 

						logger.info(String.format("Found Match %s", m.toString()));

						ms.add(m);				
					}
				}
			}
		}
		
		return ms;
	}

	/**
	 * @inheritDoc
	 */
	public Context registerMatch(Match m) {
		logger.info(String.format("registerMatch(%s)", m.toString()));
		
		String matchText = m.match();
		
		Iterator<Match> itr = matches.containsKey(matchText) ? 
				matches.get(matchText).iterator() : 
				new EmptyIterator<Match>();
		
		Match superMatch = null;
		Set<Match> subMatches = new HashSet<Match>();
		Set<Match> generalizable = new HashSet<Match>();
		
		while(itr.hasNext()) { 
			Match extant = itr.next();
			
			if(extant.value().equals(m.value())) { 

				if(m.context().isSubContext(extant.context())) {

					assert superMatch == null;

					superMatch = extant;
					
				} else if (extant.context().isSubContext(m.context())) {
					
					assert superMatch == null;
			
					subMatches.add(extant);
					
				} else {
					
					generalizable.add(extant);
				}
			}
		}
		
		if(superMatch != null) {
			
			assert subMatches.isEmpty();
			assert generalizable.isEmpty();

			m = null;  // don't add anything.
			
			return superMatch.context();
			
		} else { 

			removeMatches(subMatches);
			
			Context superCtxt = lub(m, generalizable);

			if(!superCtxt.isTopContext() && !generalizable.isEmpty()) { 
				boolean isImmediateParent = superCtxt.isParentOf(m.context());
			
				Iterator<Match> gitr = generalizable.iterator();
				while(!isImmediateParent && itr.hasNext()) { 
					isImmediateParent = isImmediateParent || superCtxt.isParentOf(gitr.next().context());
				}
				
				if(isImmediateParent) { 
					m = new Match(superCtxt, m.match(), m.value());
					removeMatches(generalizable);
				}
			}

			addMatch(m);

			return m.context();
		}
		
	}
	
	private Context lub(Match m, Collection<Match> ms) { 
		Context c = Match.lubContext(ms);
		return c != null ? c.leastUpperBound(m.context()) : m.context();
	}

	/**
	 * @inheritDoc
	 * 
	 * This method always succeeds, for the in-memory adaptive matcher.
	 */
	public void close() throws MatcherCloseException {
	} 
	
}