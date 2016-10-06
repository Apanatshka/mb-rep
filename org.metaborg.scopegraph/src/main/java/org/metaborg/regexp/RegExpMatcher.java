package org.metaborg.regexp;

import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class RegExpMatcher<S> implements IRegExpMatcher<S> {

    private final IRegExp<S> state;
    private final Object2ObjectMap<IRegExp<S>,Object2ObjectMap<S,IRegExp<S>>> stateTransitions;
    private final ObjectSet<IRegExp<S>> canStepFrom;
    private final IAlphabet<S> alphabet;

    private RegExpMatcher(IRegExp<S> state,
            Object2ObjectMap<IRegExp<S>,Object2ObjectMap<S,IRegExp<S>>> stateTransitions, ObjectSet<IRegExp<S>> canStepFrom,
            IAlphabet<S> alphabet) {
        this.state = state;
        this.stateTransitions = stateTransitions;
        this.canStepFrom = canStepFrom;
        this.alphabet = alphabet;
    }

    @Override public RegExpMatcher<S> match(S symbol) {
        assert alphabet.contains(symbol);
        return new RegExpMatcher<>(stateTransitions.get(state).get(symbol), stateTransitions, canStepFrom, alphabet);
    }

    @Override public IRegExpMatcher<S> match(Iterable<S> symbols) {
        RegExpMatcher<S> matcher = this;
        for (S symbol : symbols) {
            matcher = matcher.match(symbol);
        }
        return matcher;
    }

    @Override public boolean isAccepting() {
        return state.isNullable();
    }

    @Override public boolean isStuck() {
        return !canStepFrom.contains(state);
    }

    public static <S> IRegExpMatcher<S> create(final IRegExp<S> initial, RegExpBuilder<S> builder) {
        assert initial.getAlphabet().equals(builder.getAlphabet());
        final List<Deriver<S>> derivers = Lists.newArrayList();
        for (S symbol : builder.getAlphabet()) {
            derivers.add(new Deriver<S>(symbol, builder));
        }

        final Object2ObjectMap<IRegExp<S>,Object2ObjectMap<S,IRegExp<S>>> stateTransitions = new Object2ObjectOpenHashMap<>();
        final Object2ObjectMap<IRegExp<S>,ObjectSet<IRegExp<S>>> reverseTransitions = new Object2ObjectOpenHashMap<>();
        final Stack<IRegExp<S>> worklist = new ObjectArrayList<>();
        worklist.push(initial);
        while (!worklist.isEmpty()) {
            final IRegExp<S> state = worklist.pop();
            final Object2ObjectMap<S,IRegExp<S>> transitions = new Object2ObjectOpenHashMap<>(derivers.size());
            if (!stateTransitions.containsKey(state)) {
                for (Deriver<S> deriver : derivers) {
                    final IRegExp<S> nextState = state.accept(deriver);
                    ObjectSet<IRegExp<S>> reverseStates;
                    if ((reverseStates = reverseTransitions.get(nextState)) == null) {
                        reverseTransitions.put(nextState, (reverseStates = new ObjectOpenHashSet<>()));
                    }
                    reverseStates.add(state);
                    transitions.put(deriver.getSymbol(), nextState);
                    worklist.push(nextState);
                }
                stateTransitions.put(state, transitions);
            }
        }

        final ObjectSet<IRegExp<S>> canStepFrom = new ObjectOpenHashSet<>();
        for (IRegExp<S> state : stateTransitions.keySet()) {
            if (state.isNullable()) {
                worklist.push(state);
            }
        }
        while (!worklist.isEmpty()) {
            final IRegExp<S> state = worklist.pop();
            if (!canStepFrom.contains(state)) {
                if (reverseTransitions.containsKey(state)) {
                    for (IRegExp<S> nextState : reverseTransitions.get(state)) {
                        canStepFrom.add(nextState);
                        worklist.push(nextState);
                    }
                }
            }
        }

        return new RegExpMatcher<>(initial, stateTransitions, canStepFrom, builder.getAlphabet());
    }

}
