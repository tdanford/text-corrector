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

/**
 * 
 * An "adaptive matcher" is a service which allows its users to register "matches" (pairs of source
 * text and target terms), and then returns those matches again when a user indicates identical source 
 * text.  
 * 
 * The adaptive matcher therefore keeps track of previously-seen matches, and allows the user
 * to avoid reduplication of effort during annotation.
 * 
 * Since not all matches are valid for the same source text in all situations, the matcher organizes 
 * matches by a hierarchical value called the "context."  
 * 
 * @author Timothy Danford
 */
public interface AdaptiveMatcher {

	/**
	 * Returns all extant matches, against the given text argument (or a substring of this argument), 
	 * associated with the particular context.  All matches returned are guaranteed to be within a 
	 * context that is the same as, or a parent to, the given context.
	 * 
	 * @param c  The given context in which to search for matches.
	 * @param blockText  The raw text in which to identify matched values.
	 * @return A collection of {@link Match} objects; the {@link Context} of each {@link Match} is identical to the argument 'c'.
	 * @throws MatcherException
	 */
	public Collection<Match> findMatches(Context c, String blockText) throws MatcherException;

	/**
	 * Indicates a valid "match" to the adaptive matcher.
	 * 
	 * Matches which the matcher has already stored may have their contexts updated, to reflect a common parent
	 * with this Match's context.
	 * 
	 * @param m
	 * @return
	 * @throws MatcherException
	 */
	public Context registerMatch(Match m) throws MatcherException;
	
	/**
	 * 
	 * @param m
	 * @throws MatcherException
	 */
	public void unregisterMatch(Match m) throws MatcherException;

	/**
	 * Shuts down the Matcher.  Any underlying resources associated with the Matcher are released.
	 * 
	 * @throws MatcherCloseException
	 */
	public void close() throws MatcherCloseException;
}

