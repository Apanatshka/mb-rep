package org.spoofax.interpreter.library.language.spxlang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jdbm.PrimaryHashMap;
import jdbm.RecordListener;
import jdbm.SecondaryHashMap;
import jdbm.SecondaryKeyExtractor;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;

/**
 * SymbolTable for Spx Packages
 * 
 * @author Md. Adil Akhter
 */

public class SpxPackageLookupTable implements ICompilationUnitRecordListener{

	// Symbol table that stores package declarations
	private final PrimaryHashMap<IStrategoList, PackageDeclaration> _packageLookupTable;
	private final SecondaryHashMap<String, IStrategoList, PackageDeclaration> _uriMap;

	// Symbol table for language descriptor
	private final PrimaryHashMap<IStrategoList, LanguageDescriptor> _languageDescriptors;
	private final SecondaryHashMap<String, IStrategoList, LanguageDescriptor> _packagesByLangaugeName;

	private final String SRC = this.getClass().getSimpleName();
	private final ISpxPersistenceManager _manager;
	

    /**
     * Listeners which are notified about changes in records
     */
    protected List<RecordListener<IStrategoList, PackageDeclaration>> recordListeners = new ArrayList<RecordListener<IStrategoList, PackageDeclaration>>();

	/**
	 * Instantiates a lookup table for the base constructs (e.g. , packages and
	 * modules)of Spoofaxlang.
	 * 
	 * @param tableName
	 *            name of the table
	 * @param manager
	 *            an instance of {@link ISpxPersistenceManager}
	 */
	public SpxPackageLookupTable(ISpxPersistenceManager manager) {
		assert manager != null;

		String tableName = SRC + "_" + manager.getProjectName();

		_manager = manager;
		_packageLookupTable = manager.loadHashMap(manager.getProjectName()
				+ "._lookupPackageMap.idx");

		// readonly secondary view of the the lookup table .
		_uriMap = _packageLookupTable.secondaryHashMapManyToOne(
						tableName + "._urimap.idx",
						new SecondaryKeyExtractor<Iterable<String>, IStrategoList, PackageDeclaration>() {
							/**
							 * Returns the Secondary key of the primary lookup
							 * table.
							 * 
							 * @param key
							 *            current primary key
							 * @param value
							 *            value to be mapped using primary key
							 * @return secondary key to map the value with .
							 */
							public Iterable<String> extractSecondaryKey(
									IStrategoList key, PackageDeclaration value) {

								return value.getAllFilePaths();
							}
						});

		initListeners();

		// initializing language Descriptor for the package
		_languageDescriptors = manager.loadHashMap(tableName+ "._languageDescriptors.idx");

		_packagesByLangaugeName = _languageDescriptors
				.secondaryHashMapManyToOne( tableName + "._packagesByLangaugeName.idx",
						new SecondaryKeyExtractor<Iterable<String>, IStrategoList, LanguageDescriptor>() {
							/**
							 * Returns the Secondary keys as Language Name
							 * Strings
							 * 
							 * @param key
							 *            current primary key
							 * @param value
							 *            value to be mapped using primary key
							 * @return secondary key to map the value with .
							 */
							public Iterable<String> extractSecondaryKey(
									IStrategoList key, LanguageDescriptor value) {
								return value.asLanguageNameStrings();
							}
						});
	}

	/**
	 * adding a record listener to remove/cleanup symbol table and make it
	 * consistent in several scenario.
	 */
	private void initListeners() {
		_packageLookupTable
				.addRecordListener(new RecordListener<IStrategoList, PackageDeclaration>() {
					public void recordInserted(IStrategoList key,
							PackageDeclaration value) throws IOException {
						// do nothing
					}

					public void recordUpdated(IStrategoList key,
							PackageDeclaration oldValue,
							PackageDeclaration newValue) throws IOException {

						if (newValue.getAllFilePaths().size() == 0) {
							_manager.logMessage(SRC + ".recordUpdated", "Removing Package " + newValue + " from symbol table as it is not associated with any compunit.");

							// since there is no URI left for the Package
							// removing it from the table.
							remove(key);
							_languageDescriptors.remove(key);
						}
						else{
							if(!recordListeners.isEmpty()){	
								for( RecordListener<IStrategoList, PackageDeclaration> rl: recordListeners){
									rl.recordUpdated(key, oldValue, newValue);
								}
							}
						}
					}

					public void recordRemoved(IStrategoList key,
							PackageDeclaration value) throws IOException {

						// removing language descriptors
						_languageDescriptors.remove(key);

						_manager.logMessage(SRC + ".recordUpdated", "Removing Package " + key + ".");

						if(!recordListeners.isEmpty()){	
							for( RecordListener<IStrategoList, PackageDeclaration> rl: recordListeners){
								rl.recordRemoved(key, value);
							}
						}
					}
				});
	}

	private boolean containsUri(String absPath) {
		return _uriMap.containsKey(absPath);
	}

	/**
	 * Returns no of entries in this symbol table.
	 * 
	 * @return {@link Integer}
	 */
	public int size() {
		return _packageLookupTable.size();
	}

	/**
	 * Defines {@code PackageDeclaration} in current symbol table
	 * 
	 * @param packageDeclaration
	 *            an Instance of {@link PackageDeclaration}
	 */
	public void definePackageDeclaration(PackageDeclaration packageDeclaration) {
		assert packageDeclaration != null;
		assert packageDeclaration.getId() != null;

		_packageLookupTable.put(packageDeclaration.getId(), packageDeclaration);
		
		_manager.logMessage(SRC + ".definePackageDeclaration", "Indexed/Reindexed package declaration : " + packageDeclaration);
	}

	/**
	 * Defines {@link LanguageDescriptor} for the Spx Package with
	 * {@code packageId}
	 * 
	 * @param packageId
	 *            Qualified ID of the package
	 * @param newDesc
	 *            {@link LanguageDescriptor} of package with ID -
	 *            {@code newDesc}
	 */
	public void defineLanguageDescriptor(IStrategoList packageId, LanguageDescriptor newDesc) {
		if (containsPackage(packageId)) {
			this._languageDescriptors.put(packageId, newDesc);
		} else
			throw new IllegalArgumentException("Unknown Package ID : "
					+ packageId.toString());
	}

	/**
	 * Adds a SPX Package Declaration location
	 * 
	 * @param packageId
	 *            Package ID represented by {@link IStrategoList}
	 * @param absPath
	 * @return
	 */
	public boolean addPackageDeclarationLocation(IStrategoList packageId, String absPath) {
		assert packageId != null && absPath != null;

		PackageDeclaration decl = PackageDeclaration.newInstance(_packageLookupTable.get(packageId));

		if (decl != null) {
			
			_manager.logMessage(SRC + ".addPackageDeclarationLocation", "adding "+ absPath + " to following package : "+ packageId );
			
			decl.addFileUri(absPath);
			this.definePackageDeclaration(decl); // redefining package 
			return true;
		}
		return false;
	}

	public void removePackageDeclarationLocation(IStrategoList pId,String absPath) {
		assert pId != null && absPath != null;

		PackageDeclaration decl = PackageDeclaration.newInstance(_packageLookupTable.get(pId));

		if (decl != null) {
			
			decl.removeFileUri(absPath);
			this.definePackageDeclaration(decl); // redefining packagedeclaration
			_manager.logMessage(SRC + ".removePackageDeclarationLocation", "removed "+ absPath + " from following package : "+ decl );
		}
		else
			throw new IllegalArgumentException("Unknown PackageID : "+ pId);
		
	}

	void removeImportedToReferences(PackageDeclaration decl) {
		assert decl != null;
		PackageDeclaration packageDecl;

		for (IStrategoList id : decl.getImortedToPackageReferences()) {
			packageDecl = this.getPackageDeclaration(id);
			if (packageDecl != null) {
				packageDecl.removeImportedToPackageReference(decl);
			}
		}

	}

	public PackageDeclaration getPackageDeclaration(IStrategoList id) {
		return _packageLookupTable.get(id);
	}

	public Iterable<PackageDeclaration> getPackageDeclarations() {
		return _packageLookupTable.values();
	}

	/**
	 * Returns language descriptor associated with id
	 * 
	 * @param id
	 *            package id whose language descriptor is to be returned
	 * @return {@link LanguageDescriptor}
	 */
	public LanguageDescriptor getLangaugeDescriptor(IStrategoList id) {
		return _languageDescriptors.get(id);
	}

	/**
	 * Removes a PackageDeclaration from the table
	 * 
	 * @param id
	 *            Package Id whose language descriptor is to be returned and
	 *            removed from the table.
	 * @return associated {@link PackageDeclaration}
	 * @throws IOException 
	 */
	public PackageDeclaration remove(IStrategoList id) throws IOException {
		_manager.logMessage(SRC + ".remove", "Removing Package " + id + " from symbol table.");

		PackageDeclaration decl = _packageLookupTable.remove(id);
		
		this.removeImportedToReferences(decl);
		
		_manager.logMessage(SRC + ".remove", "Removed Package " + id + " from symbol table.");
		return decl;
	}

	/**
	 * Returns Package located in the uri specified by {@code absUri}
	 * 
	 * @param absUri
	 * @return {@link Iterable}
	 */
	public Iterable<PackageDeclaration> packageDeclarationsByUri(String absUri) {
		Set<PackageDeclaration> ret = new HashSet<PackageDeclaration>();
		Iterable<IStrategoList> retList = _uriMap.get(absUri);
		if (retList != null) {
			for (IStrategoList l : retList)
				ret.add(_uriMap.getPrimaryValue(l));
		}

		return ret;
	}

	void verifyUriExists(String uri) {
		if (!containsUri(uri)) {
			throw new IllegalArgumentException("Unknown Package Uri " + uri);
		}
	}

	void verifyPackageIDExists(IStrategoList packageId) {
		if (!containsPackage(packageId)) {
			throw new IllegalArgumentException("Unknown Package ID : "
					+ packageId);
		}
	}

	/**
	 * Removes all packages located in the {@code absUri}
	 * 
	 * @param absUri
	 *            Absolute Path of the File
	 */
	public void removePackageDeclarationsByUri(String absUri) {
		_manager.logMessage(SRC + ".removePackageDeclarationsByUri", "Removing following Uri for all the package declarations :"+ absUri);
		
		ArrayList<IStrategoList> list = new ArrayList<IStrategoList>();
		Iterable<IStrategoList> toRemove = _uriMap.get(absUri);

		if (toRemove != null) {
			// constructing a temporary list to be removed from
			// the symbol table.
			for (IStrategoList l : toRemove) {
				list.add(l);
			}
		}

		_manager.logMessage(SRC + ".removePackageDeclarationsByUri", "Found entries  "+ list + " to update." );


		// removing the package declaration from the lookup table.
		for (IStrategoList id : list)
			removePackageDeclarationLocation(id, absUri);
	}

	/**
	 * Clears this symbol table.
	 * @throws IOException 
	 */
	public synchronized void clear() throws IOException {
		_manager.logMessage(SRC + ".clear", "Removing " + this.size() + " entries from symbol table ");

		Iterator<IStrategoList> keyIter = _packageLookupTable.keySet().iterator();
		if(keyIter != null) {
			while (keyIter.hasNext())
				remove(keyIter.next());
		}
	}

	/**
	 * Checks whether the package with packageId exists in the symbol table.
	 * 
	 * @param packageId
	 * @return
	 */
	public boolean containsPackage(IStrategoList packageId) {
		return _packageLookupTable.containsKey(packageId);
	}

	/**
	 * Returns the packages indexed using languageName
	 * 
	 * @param langaugeName
	 * @return
	 */
	public Iterable<IStrategoList> getPackageIdsByLangaugeName(
			String langaugeName) {
		return _packagesByLangaugeName.get(langaugeName);
	}

	public Iterable<IStrategoList> getPackageIdsByLangaugeName(
			IStrategoString langaugeName) {
		return getPackageIdsByLangaugeName(Tools.asJavaString(langaugeName));
	}

	public RecordListener<String, SpxCompilationUnitInfo> getCompilationUnitRecordListener() {
		return new RecordListener<String, SpxCompilationUnitInfo>() {
			public void recordUpdated(String key,
					SpxCompilationUnitInfo oldValue,
					SpxCompilationUnitInfo newValue) throws IOException {

				if (oldValue.getVersionNo() != newValue.getVersionNo()) {
					// Whenever compilation unit version no is updated ,
					// remove all the related module declaration
					// from the symbol table , since it is obsolete now.
					recordRemoved(key, oldValue);
				}

			}

			public void recordRemoved(String key, SpxCompilationUnitInfo value)
					throws IOException {
				removePackageDeclarationsByUri(key);

			}

			public void recordInserted(String key, SpxCompilationUnitInfo value)
					throws IOException {
				// do nothing
			}
		};
	}

	public void addRecordListener( final IPackageDeclarationRecordListener rl){
		this.recordListeners.add(rl.getPackageDeclarationRecordListener());
	}

	public void removeRecordListener( final IPackageDeclarationRecordListener rl){
		this.recordListeners.remove(rl.getPackageDeclarationRecordListener());
	}
}


interface IPackageDeclarationRecordListener
{
	public RecordListener<IStrategoList, PackageDeclaration> getPackageDeclarationRecordListener();
}
