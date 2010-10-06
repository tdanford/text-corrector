package org.sc.annotator.adaptive;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.io.*;

import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
import org.sc.annotator.adaptive.exceptions.MatcherException;

/**
 * Implements an {@link AdaptiveMatcher} that is persistent by 
 * writing its matching rules to a backing file in a line-based text format. 
 * 
 * @author Timothy Danford
 *
 */
public class SimpleFileAdaptiveMatcher implements AdaptiveMatcher {
	
	private Map<String,Set<Match>> matches;
	private File backingFile;
	
	public SimpleFileAdaptiveMatcher(File f) throws IOException {
		backingFile = f;
		if(f.isDirectory()) { 
			throw new IllegalArgumentException(f.getAbsolutePath() + " is a directory.");
		}
		if(f.exists() && !f.canWrite()) { 
			throw new IllegalArgumentException(f.getAbsolutePath() + " is not writable.");
		}
		matches = new TreeMap<String,Set<Match>>();
		
		reload();
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
	
	public void reload() throws IOException { 
		BufferedReader reader = new BufferedReader(new FileReader(backingFile));
		String line;
		matches.clear();
		while((line = reader.readLine()) != null) { 
			String[] array = line.split("\t");
			String match = array[0];
			String value = array[1];
			String context = array[2];
			Match m = new Match(new Context(context), match, value);
			if(!matches.containsKey(match)) { 
				matches.put(match, new LinkedHashSet<Match>());
			}
			matches.get(match).add(m);
		}
		reader.close();
	}
	
	public void save(PrintWriter writer) { 
		for(String match : matches.keySet()) { 
			for(Match m : matches.get(match)) { 
				writer.println(String.format("%s\t%s\t%s", 
						m.match(),
						m.value(),
						m.context().toString()));
			}
		}
	}

	public void close() throws MatcherCloseException {
		PrintWriter writer = null;
		try { 
			writer = new PrintWriter(new FileWriter(backingFile));
			save(writer);
			
		} catch(IOException e) { 
			throw new MatcherCloseException(e);
			
		} finally { 
			writer.close();
		}
	} 
	
}