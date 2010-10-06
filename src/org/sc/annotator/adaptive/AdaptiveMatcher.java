package org.sc.annotator.adaptive;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public interface AdaptiveMatcher {

	public Collection<Match> findMatches(Context c, String blockText);

	public Context registerMatch(Match m);
	
}

