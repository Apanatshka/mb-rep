package org.spoofax.interpreter.library.index.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class LANG_index_get_all_sources extends AbstractPrimitive {
	private static String NAME = "LANG_index_get_all_sources";

	public LANG_index_get_all_sources() {
		super(NAME, 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		final IIndex index = IndexManager.getInstance().getCurrent();
		final ITermFactory factory = env.getFactory();
		final Iterable<IStrategoTerm> sources = index.getAllSources();
		IStrategoList sourceList = factory.makeList();
		for(IStrategoTerm source : sources) {
			sourceList = factory.makeListCons(source, sourceList);
		}
		env.setCurrent(sourceList);
		return true;
	}
}
