package org.sc.annotator.adaptive.client;

import java.util.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.sc.annotator.adaptive.*;
import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
import org.sc.annotator.adaptive.exceptions.MatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalGUI extends JFrame {
	
	public static void main(String[] args) {
		
		Logger logger = //LoggerFactory.getLogger(LocalGUI.class);
			LoggerFactory.getLogger("LocalGUI");

		AdaptiveMatcher matcher = 
			args.length > 0 ? new WebClient(args[0]) : new SimpleInMemoryAdaptiveMatcher(logger);
			
		new LocalGUI(matcher);
	}

	private AdaptiveMatcher matcher;
	
	private DefaultListModel matchList;
	private ListSelectionModel matchListSelection;
	
	private JTextArea matchText;
	private JTextField contextField, valueField;
	private JButton registerButton, listMatchesButton;
	
	public LocalGUI(AdaptiveMatcher m) {
		super("Matcher GUI");
		matcher = m;
		
		Container c = (Container)getContentPane();
		c.setLayout(new BorderLayout());

		matchList = new DefaultListModel();
		matchListSelection = new DefaultListSelectionModel();
		matchText = new JTextArea();
		contextField = new JTextField();
		valueField  = new JTextField();
		registerButton = new JButton(registerMatchAction());
		listMatchesButton = new JButton(listMatchesAction());
		
		valueField.setColumns(40);
		contextField.setColumns(40);
		matchText.setColumns(60);
		
		JPanel listPanel = new JPanel(new BorderLayout());
		JList list = new JList(matchList);
		list.setSelectionModel(matchListSelection);
		listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
		
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(new JScrollPane(matchText), BorderLayout.CENTER);
		
		JPanel contextPanel = new JPanel(new FlowLayout());
		contextPanel.add(new JLabel("Context"));
		contextPanel.add(contextField);
		contextPanel.add(listMatchesButton);
		
		JPanel registerPanel = new JPanel(new FlowLayout());
		registerPanel.add(new JLabel("Match Value"));
		registerPanel.add(valueField);
		registerPanel.add(registerButton);
		
		JPanel north = contextPanel;
		
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(textPanel);
		center.add(listPanel);
		
		textPanel.setBorder(new TitledBorder("Text to Search"));
		listPanel.setBorder(new TitledBorder("Match List"));
		
		JPanel south = registerPanel;
		
		c.add(north, BorderLayout.NORTH);
		c.add(center, BorderLayout.CENTER);
		c.add(south, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addWindowListener(new WindowAdapter() {

			public void windowClosed(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				try {
					matcher.close();
				} catch (MatcherCloseException e1) {
					e1.printStackTrace(System.err);
				}
			}
		});
		
		setVisible(true);
		pack();
	}
	
	public void registerMatch() {
		int selected = matchListSelection.getLeadSelectionIndex();
		Context c = null;

		try {
			if(selected != -1) { 
				MatchWrapper wrapper = (MatchWrapper)matchList.elementAt(selected);
				Match m = wrapper.match;
				c = matcher.registerMatch(m);

			} else { 
				String selection = matchText.getSelectedText();
				String valueText = valueField.getText();

				if(selection != null && selection.length() > 0 && valueText.length() > 0) { 
					Match m = new Match(getContext(), selection, valueText);
					c = matcher.registerMatch(m);
				}
			}
			System.out.println(String.format("Context: %s", c.toString()));
		} catch (MatcherException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public Context getContext() { 
		return new Context(contextField.getText().trim());
	}
	
	public void listMatches() {
		matchList.clear();
		try {
			Collection<Match> ms = matcher.findMatches(new Context(contextField.getText().trim()), matchText.getText());
			for(Match m : ms) { 
				matchList.addElement(new MatchWrapper(m));
			}
			
		} catch (MatcherException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public Action registerMatchAction() { 
		return new AbstractAction("Register Match") { 
			public void actionPerformed(ActionEvent e) { registerMatch(); }
		};
	}
	
	public Action listMatchesAction() { 
		return new AbstractAction("List Matches") { 
			public void actionPerformed(ActionEvent e) { listMatches(); }
		};
	}
	
	private class MatchWrapper { 
		public Match match;
		
		public MatchWrapper(Match m) { match = m; }
		public String toString() { return match.value(); }
	}
}
