package org.metaborg.regexp;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class FiniteAlphabet<S> implements IAlphabet<S> {

    private final ImmutableList<S> symbols;

    public FiniteAlphabet(ImmutableSet<S> alphabet) {
        this.symbols = ImmutableList.copyOf(alphabet);
    }

    @Override public Iterator<S> iterator() {
        return symbols.iterator();
    }

    @Override public boolean contains(S s) {
        return symbols.contains(s);
    }

    @Override public int indexOf(S s) {
        if (!contains(s)) {
            throw new IllegalArgumentException("Symbol not in alphabet.");
        }
        return symbols.indexOf(s);
    }

}
