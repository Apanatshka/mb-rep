package org.metaborg.solver;

import java.util.Collection;
import java.util.Collections;

import org.metaborg.solver.constraints.IConstraint;

public class SolveResult {

    public final Collection<IConstraint> constraints;
    public final ISolution solution;

    public SolveResult(ISolution solution) {
        this(solution, Collections.<IConstraint> emptyList());
    }

    public SolveResult(ISolution solution, Collection<IConstraint> constraints) {
        this.constraints = constraints;
        this.solution = solution;
    }

}