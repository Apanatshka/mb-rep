package org.spoofax.interpreter.library.language;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SemanticIndexManager {
	
	private final AtomicLong revisionProvider = new AtomicLong();

	private ISemanticIndex current;
	
	private URI currentProject;
	
	private String currentLanguage;
	
	private SemanticIndexFile currentFile;
	
	/**
	 * Indices by language and project. Access requires a lock on {@link #getSyncRoot}
	 */
	private static Map<String, Map<URI, WeakReference<ISemanticIndex>>> asyncIndexCache =
		new HashMap<String, Map<URI, WeakReference<ISemanticIndex>>>();
	
	public ISemanticIndex getCurrent() {
		if (!isInitialized())
			throw new IllegalStateException("No semantic index has been set-up, use index-setup(|language, project-paths) to set up the index before use.");
		
		return current;
	}
	
	public SemanticIndexFile getCurrentFile() {
		if (!isInitialized())
			throw new IllegalStateException("No semantic index has been set-up, use index-setup(|language, project-paths) to set up the index before use.");
		
		return currentFile;
	}
	
	public void setCurrentFile(SemanticIndexFile currentFile) {
		this.currentFile = currentFile;
	}
	
	private static Object getSyncRoot() {
		return SemanticIndexManager.class;
	}
	
	public AtomicLong getRevisionProvider() {
		return revisionProvider;
	}
	
	public boolean isInitialized() {
		return current != null;
	}
	
	public static boolean isKnownIndexingLanguage(String language) {
		synchronized (getSyncRoot()) {
			return asyncIndexCache.containsKey(language);
		}
	}
	
	public void loadIndex(String language, URI project, ITermFactory factory, IOAgent agent) {
		synchronized (getSyncRoot()) {
			Map<URI, WeakReference<ISemanticIndex>> indicesByProject =
					asyncIndexCache.get(language);
			if (indicesByProject == null) {
				indicesByProject = new HashMap<URI, WeakReference<ISemanticIndex>>();
				asyncIndexCache.put(language, indicesByProject);
			}
			WeakReference<ISemanticIndex> indexRef = indicesByProject.get(project);
			ISemanticIndex index = indexRef == null ? null : indexRef.get();
			if (index == null) {
				index = tryReadFromFile(getIndexFile(project, language), factory, agent);
			}
			if (index == null) {
				index = new SemanticIndex();
				NotificationCenter.notifyNewProject(project);
			}
			indicesByProject.put(project, new WeakReference<ISemanticIndex>(index));
			current = index;
			currentLanguage = language;
			currentProject = project;
		}
	}
	
	public ISemanticIndex tryReadFromFile(File file, ITermFactory factory, IOAgent agent) {
		try {
			IStrategoTerm term = new TermReader(factory).parseFromFile(file.toString());
			return SemanticIndex.fromTerm(term, factory, agent, true); // TODO: Move to other class
		} catch (IOException e) {
			return null;
		}
	}
	
	public void storeCurrent() throws IOException {
		File file = getIndexFile(currentProject, currentLanguage);
		IStrategoTerm stored = getCurrent().toTerm(true);
		Writer writer = new BufferedWriter(new FileWriter(file));
		try {
			stored.writeAsString(writer, IStrategoTerm.INFINITE);
		} finally {
			writer.close();
		}
	}

	private File getIndexFile(URI project, String language) {
		File container = new File(new File(project), ".cache");
		container.mkdirs();
		return new File(container, language + ".idx");
	}
}
