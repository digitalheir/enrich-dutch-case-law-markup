package org.leibnizcenter.rechtspraak.cfg;

/**
 * Used when a grammar is not
 * correctly defined
 */

public class MalformedGrammarException extends Error {
    public MalformedGrammarException() {
        super();
    }

    public MalformedGrammarException(String s) {
        super(s);
    }
}