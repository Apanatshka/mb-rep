package org.spoofax.interpreter.library.index;

import static org.spoofax.interpreter.core.Tools.isTermAppl;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class LANG_index_get_with_partitions extends AbstractPrimitive {
	private static String NAME = "LANG_index_get_with_partitions";

	public LANG_index_get_with_partitions() {
		super(NAME, 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if(isTermAppl(tvars[0])) {
			IStrategoAppl template = (IStrategoAppl) tvars[0];
			IIndex ind = IndexManager.getInstance().getCurrent();
			IIndexEntryIterable entries = ind.get(template);
			env.setCurrent(IndexEntry.toTermsWithPartition(env.getFactory(), entries));
			return true;
		} else {
			return false;
		}
	}
}