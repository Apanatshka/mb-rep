package org.spoofax.interpreter.library.index.legacy;

import org.spoofax.interpreter.library.index.primitives.IndexLibrary;
import org.spoofax.interpreter.library.index.primitives.LANG_index_reset;
import org.spoofax.interpreter.library.index.primitives.LANG_index_clear_partition;
import org.spoofax.interpreter.library.index.primitives.LANG_index_get_all_in_partition;
import org.spoofax.interpreter.library.index.primitives.LANG_index_get_all_partitions;
import org.spoofax.interpreter.library.index.primitives.LANG_index_get_partitions_of;
import org.spoofax.interpreter.library.index.primitives.LANG_index_persist;

public class LegacyIndexLibrary extends IndexLibrary {
	public LegacyIndexLibrary() {
		super();

		add(new LANG_legacy_index_setup());

		add(new RedirectAbstractPrimitive("LANG_index_commit", new LANG_index_persist()));
		add(new RedirectAbstractPrimitive("LANG_index_clear_file", new LANG_index_clear_partition()));
		add(new RedirectAbstractPrimitive("LANG_index_clear_all", new LANG_index_reset()));
		add(new RedirectAbstractPrimitive("LANG_index_get_all_files", new LANG_index_get_all_partitions()));
		add(new RedirectAbstractPrimitive("LANG_index_get_all_in_file", new LANG_index_get_all_in_partition()));
		add(new RedirectAbstractPrimitive("LANG_index_get_files_of", new LANG_index_get_partitions_of()));

		add(new NoopAbstractPrimitive("LANG_index_remove", 0, 2));
		add(new NoopAbstractPrimitive("LANG_index_remove_one", 0, 1));
		add(new NoopAbstractPrimitive("LANG_index_remove_all", 0, 1));
		add(new NoopAbstractPrimitive("LANG_index_get_current_partition", 0, 0));
		add(new NoopAbstractPrimitive("LANG_index_get_current_file", 0, 0));
		add(new NoopAbstractPrimitive("LANG_index_set_current_partition", 0, 1));
		add(new NoopAbstractPrimitive("LANG_index_set_current_file", 0, 1));
		add(new NoopAbstractPrimitive("LANG_index_start_transaction", 0, 0));
		add(new NoopAbstractPrimitive("LANG_index_end_transaction", 0, 0));
		add(new NoopAbstractPrimitive("LANG_index_get_file_revision", 0, 1));
		add(new NoopAbstractPrimitive("LANG_index_get_files_newer_than", 0, 1));
		add(new NoopAbstractPrimitive("LANG_index_get_with_partitions", 0, 1));
	}
}
