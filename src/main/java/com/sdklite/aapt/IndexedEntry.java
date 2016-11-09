package com.sdklite.aapt;

/**
 * {@link IndexedEntry} hold the index and value of the specific entry
 * 
 * @author johnsonlee
 *
 * @param <T>
 *            The type of entry value
 */
final class IndexedEntry<T> {

    int index;

    T value;

    public IndexedEntry() {
    }

    public IndexedEntry(final int index, final T value) {
        this.index = index;
        this.value = value;
    }
}
