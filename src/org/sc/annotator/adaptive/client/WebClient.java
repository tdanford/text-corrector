package org.sc.annotator.adaptive.client;

import java.net.*;
import java.util.*;
import java.io.*;

import org.sc.annotator.adaptive.AdaptiveMatcher;
import org.sc.annotator.adaptive.Context;
import org.sc.annotator.adaptive.Match;

public class WebClient implements AdaptiveMatcher {
	
	private String base;
	
	public WebClient(String b) { 
		base = b;
	}

	public Collection<Match> findMatches(Context c, String blockText) {
		try {
			URL url = new URL(String.format("%s?context=%s&text=%s",
					base, 
					URLEncoder.encode(c.toString(), "UTF-8"),
					URLEncoder.encode(blockText, "UTF-8")));
			
			LinkedList<Match> matches = new LinkedList<Match>();
			
			HttpURLConnection cxn = (HttpURLConnection)url.openConnection();
			cxn.setRequestMethod("GET");
			cxn.connect();
			
			int status = cxn.getResponseCode();
			if(status == 200) { 
				BufferedReader reader = new BufferedReader(new InputStreamReader(cxn.getInputStream()));
				String line;
				while((line = reader.readLine()) != null) { 
					String value = line;
					Match m = new Match(c, blockText, value);
					matches.add(m);
				}
				reader.close();
				
			} else { 
				System.err.println(String.format("%d : %s", status, cxn.getResponseMessage()));
			}
			
			return matches;
			
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public Context registerMatch(Match m) {
		try {
			URL url = new URL(base);

			Context matched = null;
			
			HttpURLConnection cxn = (HttpURLConnection)url.openConnection();
			cxn.setRequestMethod("POST");
			cxn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			cxn.setDoOutput(true);
			
			OutputStream os = cxn.getOutputStream();
			PrintStream ps = new PrintStream(os);
			
			ps.print(String.format("context=%s", URLEncoder.encode(m.context().toString(), "UTF-8")));
			ps.print(String.format("&text=%s", URLEncoder.encode(m.match(), "UTF-8")));
			ps.print(String.format("&value=%s", URLEncoder.encode(m.value(), "UTF-8")));
			ps.println();
			
			cxn.connect();
			
			int status = cxn.getResponseCode();
			if(status == 200) { 
				BufferedReader reader = new BufferedReader(new InputStreamReader(cxn.getInputStream()));
				String line = reader.readLine();
				matched = new Context(line);
				
				reader.close();
				
			} else { 
				System.err.println(String.format("%d : %s", status, cxn.getResponseMessage()));
			}
			
			return matched;
			
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	
}
