package org.spoofax.interpreter.library.language;

import static org.spoofax.interpreter.core.Tools.isTermAppl;

import java.util.Collection;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LANG_index_get_files_of extends AbstractPrimitive {

	private static String NAME = "LANG_index_get_files_of";
	
	private final SemanticIndexManager index;
	
	public LANG_index_get_files_of(SemanticIndexManager index) {
		super(NAME, 0, 1);
		this.index = index;
	}

	/**
	 * Returns [] if URI not in index.
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (isTermAppl(tvars[0])) {
			IStrategoAppl template = (IStrategoAppl) tvars[0];
			ISemanticIndex ind = index.getCurrent();
			Collection<SemanticIndexEntry> entries = ind.getEntries(template);
			IStrategoList files = env.getFactory().makeList();
			for(SemanticIndexEntry entry : entries) {
				IStrategoTerm file = entry.getFile().toTerm(env.getFactory());
				files = env.getFactory().makeListCons(file, files);
			}
			env.setCurrent(files);
			return true;
		} else {
			return false;
		}
	}
}
