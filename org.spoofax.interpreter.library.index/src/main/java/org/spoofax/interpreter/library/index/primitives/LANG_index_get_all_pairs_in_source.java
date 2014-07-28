package org.spoofax.interpreter.library.index.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexEntry;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class LANG_index_get_all_pairs_in_source extends AbstractPrimitive {
	private static String NAME = "LANG_index_get_all_pairs_in_source";

	public LANG_index_get_all_pairs_in_source() {
		super(NAME, 0, 1);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		final IIndex index = IndexManager.getInstance().getCurrent();
		final IStrategoTerm source = tvars[0];
		final Iterable<IndexEntry> entries = index.getInSource(source);
		env.setCurrent(index.entryFactory().toPairTerms(entries));
		return true;
	}
}
