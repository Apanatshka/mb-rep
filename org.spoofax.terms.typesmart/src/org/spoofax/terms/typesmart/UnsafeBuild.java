package org.spoofax.terms.typesmart;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.Term;

/**
 * Builds an application term using the given term factory. The constructor name
 * is expected as a term argument, the argument list is expected as the current
 * term.
 * 
 * @author seba
 */
public class UnsafeBuild extends AbstractPrimitive {

	private ITermFactory factory;

	public UnsafeBuild() {
		super("SUGARJ_unsafe_build", 0, 1);
	}

	@Override
	public boolean call(IContext context, Strategy[] svars,
			IStrategoTerm[] tvars) throws InterpreterException {
		if (factory == null) {
			ITermFactory smartFactory = context.getFactory();
			if (smartFactory instanceof TypesmartTermFactory) {
				factory = ((TypesmartTermFactory) smartFactory)
						.getUnsafeFactory();
			} else {
				factory = smartFactory;
			}
		}

		String ctr = Term.javaString(tvars[0]);

		if (!(context.current() instanceof StrategoList))
			return false;

		StrategoList args = (StrategoList) context.current();

		IStrategoTerm[] argArray = args.getAllSubterms();
		IStrategoTerm unsafeTerm = factory.makeAppl(
				factory.makeConstructor(ctr, argArray.length), argArray);

		context.setCurrent(unsafeTerm);
		return true;
	}
}