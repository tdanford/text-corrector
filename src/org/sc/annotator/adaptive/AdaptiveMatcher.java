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

public interface AdaptiveMatcher {

	public Collection<Match> findMatches(Context c, String blockText) throws MatcherException;

	public Context registerMatch(Match m) throws MatcherException;

	public void close() throws MatcherCloseException;
}

