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
import org.slf4j.Logger;

/**
 * Implements an {@link AdaptiveMatcher} that is persistent by 
 * writing its matching rules to a backing file in a line-based text format. 
 * 
 * @author Timothy Danford
 *
 */
public class SimpleFileAdaptiveMatcher implements AdaptiveMatcher {
	
	private SimpleInMemoryAdaptiveMatcher matcher;
	private File backingFile;
	private Logger logger;
	
	/**
	 * Creates a new adaptive matcher, out of the given backing file.  
	 * 
	 * @param f The name of the file, which need not exist at the moment this constructor is called.  
	 * @param logger An SLF4J {@link Logger} object, used to output debugging messages.
	 * @throws IOException If the file is a directory, or is not writable.
	 */
	public SimpleFileAdaptiveMatcher(File f, Logger logger) throws IOException {
		this.logger = logger;
		backingFile = f;
		if(f.isDirectory()) { 
			throw new IllegalArgumentException(f.getAbsolutePath() + " is a directory.");
		}
		if(f.exists() && !f.canWrite()) { 
			throw new IllegalArgumentException(f.getAbsolutePath() + " is not writable.");
		}
		matcher = new SimpleInMemoryAdaptiveMatcher(logger);
		
		reload();
	}

	/**
	 * @inheritDoc
	 */
	public Collection<Match> findMatches(Context c, String blockText) {
		Collection<Match> matches = matcher.findMatches(c, blockText);
		return matches;
	}

	/**
	 * @inheritDoc
	 */
	public Context registerMatch(Match m) {
		logger.debug(String.format("registerMatch(%s)", m.toString()));
		
		Context c = matcher.registerMatch(m);
		
		logger.debug(String.format("registerMatch() returning: %s", c.toString()));

		return c;
	}
	
	public void unregisterMatch(Match m) throws MatcherException { 
		matcher.unregisterMatch(m);
	}
	
	/**
	 * Clears the internal state of the matcher, and replaces it with the matches read from the backing file.
	 * 
	 * @throws IOException If the backing file cannot be read, or another I/O error occurs.
	 */
	public void reload() throws IOException {
		logger.info(String.format("Opening: %s", backingFile.getAbsolutePath()));

		matcher.clear();
		if(backingFile.exists()) { 
			BufferedReader reader = new BufferedReader(new FileReader(backingFile));
			String line;
			while((line = reader.readLine()) != null) { 
				String[] array = line.split("\t");
				String match = array[0];
				String value = array[1];
				String context = array[2];
				Match m = new Match(new Context(context), match, value);
				matcher.addMatch(m);
			}
			reader.close();
		}
	}
	
	/**
	 * Writes the matches which have been accumulated out to an underlying writer.
	 * 
	 * The {@link close} method will call this, with a writer created from the underlying backing file.
	 * 
	 * @param writer
	 */
	public void save(PrintWriter writer) {
		Map<String,Set<Match>> matches = matcher.getMatches();
		for(String match : matches.keySet()) { 
			for(Match m : matches.get(match)) { 
				writer.println(String.format("%s\t%s\t%s", 
						m.match(),
						m.value(),
						m.context().toString()));
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void close() throws MatcherCloseException {
		PrintWriter writer = null;
		try {
			logger.info(String.format("Saving to %s", backingFile.getAbsolutePath()));
			writer = new PrintWriter(new FileWriter(backingFile));
			save(writer);
			
		} catch(IOException e) { 
			throw new MatcherCloseException(e);
			
		} finally { 
			writer.close();
		}
	} 
	
}