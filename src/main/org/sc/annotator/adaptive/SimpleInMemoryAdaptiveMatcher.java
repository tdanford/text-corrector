package org.sc.annotator.adaptive;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
	
	private Map<String,Map<String,TreeSet<Context>>> matches;
	
	private Logger logger;
	private boolean isGeneralizing, isSimplifying;
	
	public SimpleInMemoryAdaptiveMatcher(Logger logger) {
		this.logger = logger;
		matches = new TreeMap<String,Map<String,TreeSet<Context>>>();
		isGeneralizing = true;
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
	Map<String,Map<String,TreeSet<Context>>> getMatches() { 
		return matches;
	}
	
	public void clear() { 
		matches.clear();
	}
	
	public void reset() throws MatcherException { 
		clear();
	}
	
	/**
	 * @inheritDoc
	 */
	public void unregisterMatch(Match m) throws MatcherException { 
		String source = m.match();
		String target = m.value();
		Context ctxt = m.context();
	
		if(matches.containsKey(source) && matches.get(source).containsKey(target)) { 
			
			Set<Context> ms = matches.get(source).get(target);
			Iterator<Context> itr = ms.iterator();
			int found = 0, removed = 0;
			
			// For each match with the same source string...
			while(itr.hasNext()) { 

				Context extC = itr.next();
				
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
			if(matches.get(source).get(target).isEmpty()) { 
				matches.get(source).remove(target);
			}
			
			if(matches.get(source).isEmpty()) { 
				matches.remove(source);
			}
			
		} else { 
			throw new MatcherException(String.format("No source string \"%s\"",
					source));
		}
	}
	
	public void addMatch(Match m) { 
		if(!matches.containsKey(m.match())) { 
			matches.put(m.match(), new TreeMap<String,TreeSet<Context>>());
		}
		if(!matches.get(m.match()).containsKey(m.value())) { 
			matches.get(m.match()).put(m.value(), new TreeSet<Context>());
		}
		matches.get(m.match()).get(m.value()).add(m.context());
		logger.info(String.format("Added: %s", m.toString()));			
	}
	
	public void removeMatches(String source, String target, Collection<Context> ms) { 
		matches.get(source).get(target).removeAll(ms);
		if(matches.get(source).get(target).isEmpty()) { 
			matches.get(source).remove(target);
		}
		if(matches.get(source).isEmpty()) { 
			matches.remove(source);
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

				for(String target : matches.get(k).keySet()) { 

					for(Context mctxt : matches.get(k).get(target)) {

						// Exact matches are always returned.
						// Sub-context matches are returned iff we are a "simplifying" matcher.
						if(c.equals(mctxt) 
								|| (isSimplifying && c.isSubContext(mctxt))
							) { 
							
							Match m = new Match(mctxt, k, target);

							logger.info(String.format("Found Match %s", m.toString()));

							ms.add(m);				
						}
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
		
		String source = m.match();
		String target = m.value();
		
		if(!matches.containsKey(source) || !matches.get(source).containsKey(target)) {
			addMatch(m);
			return m.context();
		}

		Iterator<Context> itr =  
				matches.get(source).get(target).iterator();
		
		Context superMatch = null;
		Set<Context> subMatches = new HashSet<Context>();
		Set<Context> generalizable = new HashSet<Context>();

		while(itr.hasNext()) { 
			Context extant = itr.next();


			if(m.context().isSubContext(extant)) {

				// this is the simple case... our new match context is subsumed by 
				// a context for a match already in the database.

				assert superMatch == null;

				superMatch = extant;

			} else if (extant.isSubContext(m.context())) {

				assert superMatch == null;

				subMatches.add(extant);

			} else {

				generalizable.add(extant);
			}
		}
		
		if(superMatch != null) {
			
			assert subMatches.isEmpty();
			//assert generalizable.isEmpty();

			m = null;  // don't add anything.
			
			return superMatch;
			
		} else { 

			removeMatches(source, target, subMatches);
			
			Context superCtxt = lub(m, generalizable);

			if(isGeneralizing && !superCtxt.isTopContext() && !generalizable.isEmpty()) { 
				boolean isImmediateParent = superCtxt.isParentOf(m.context());
			
				Iterator<Context> gitr = generalizable.iterator();
				while(!isImmediateParent && itr.hasNext()) { 
					isImmediateParent = isImmediateParent || superCtxt.isParentOf(gitr.next());
				}
				
				if(isImmediateParent) { 
					m = new Match(superCtxt, m.match(), m.value());
					removeMatches(source, target, generalizable);
				}
			}

			addMatch(m);

			return m.context();
		}
		
	}
	
	private Context lub(Match m, Collection<Context> ms) { 
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