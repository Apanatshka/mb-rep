package org.metaborg.scopegraph.indices;

import org.metaborg.scopegraph.context.IScopeGraphContext;
import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_metadata extends ScopeGraphPrimitive {
 
    public SG_get_ast_metadata() {
        super(SG_get_ast_metadata.class.getSimpleName(), 0, 1);
    }


    @Override public boolean call(IScopeGraphContext<?> context, IContext env,
            Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        ITermIndex index = TermIndex.get(env.current());
        if(index == null) {
            throw new InterpreterException("Term has no AST index.");
        }
        IScopeGraphUnit unit = context.unit(index.resource());
        if(unit == null) {
            return false;
        }
        IStrategoTerm value = unit.metadata(index.nodeId(), terms[0]); 
        if(value == null) {
            return false;
        }
        env.setCurrent(value);
        return true;
    }

}
