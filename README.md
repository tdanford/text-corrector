
Abstracting application-specific text correction and rewriting rules.

Rules File
==========

Text correction rules are collected in 'rules files' -- each rules file contains one or more annotated rules for text correction.

Each rules file is imported en masse, therefore, rules which appear in the same file are guaranteed to be applied at the same time.

Each rule has the following format: 
    ( <rule-name> 
      <rule-match>
      <rule-rewrite> 
      <rule-description> )

The format for rule-match is: 
    "([^"]|(\"))+"

The rule-match is interpreted as a Java regular expression.

The rule-rewrite is a double-quote delimited string ("...", again with escaped internal double quotes).  The rule-rewrite is interpreted as a literal string and replaces any occurrence of the rule-match within the target string. The one exception to this are strings of the form "$N", where N is any digit; these are replaced with the matched groups from the rule-match regexp.  (Escaped dollar-signs, "\$", are interpreted as literal "$" in the output, without group matching.) 



