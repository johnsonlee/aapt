package com.sdklite.aapt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Internal utilities
 * 
 * @author johnsonlee
 *
 */
abstract class Internal {

    /**
     * Element filter in collection
     * 
     * @param <T>
     *            The element type
     */
    public interface Filter<T> {

        /**
         * Test whether the element is acceptable
         * 
         * @param it
         *            The element in collection
         * @return true if the element is acceptable
         */
        public boolean accept(final T it);

    }

    public interface Consumer<T> {
        public void consume(final T it);
    }

    public interface Mapper<E, T> {
        public T map(final E it);
    }

    public static final <E, T> List<T> map(final E[] c, final Mapper<E, T> mapper) {
        return map(new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<E>() {

                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return this.index < c.length;
                    }

                    @Override
                    public E next() {
                        return c[this.index++];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }, mapper);
    }
    public static final <E, T> List<T> map(final Iterable<E> c, final Mapper<E, T> mapper) {
        final List<T> result = new ArrayList<T>();

        for (final E e : c) {
            result.add(mapper.map(e));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T toArray(final Collection<? extends Number> c, final Class<?> componentType) {
        int i = 0;

        final Object array = Array.newInstance(componentType, c.size());
        
        if (componentType == byte.class) {
            for (final Number num : c) {
                Array.setByte(array, i++, num.byteValue());
            }
        } else if (componentType == short.class) {
            for (final Number num : c) {
                Array.setShort(array, i++, num.shortValue());
            }
        } else if (componentType == int.class) {
            for (final Number num : c) {
                Array.setInt(array, i++, num.intValue());
            }
        } else if (componentType == float.class) {
            for (final Number num : c) {
                Array.setFloat(array, i++, num.floatValue());
            }
        } else if (componentType == long.class) {
            for (final Number num : c) {
                Array.setLong(array, i++, num.longValue());
            }
        } else if (componentType == double.class) {
            for (final Number num : c) {
                Array.setDouble(array, i++, num.doubleValue());
            }
        }

        return (T) array;
    }

    /**
     * Find element from collection with the specified matcher
     * 
     * @param c
     *            The collection to search
     * @param filter
     *            The element filter
     * @return the first acceptable element
     */
    public static final <T> T find(final Iterable<T> c, final Filter<? super T> filter) {
        for (final T t : c) {
            if (filter.accept(t)) {
                return t;
            }
        }

        return null;
    }
    public static final <T> T find(final Enumeration<T> c, final Filter<? super T> filter) {
        while (c.hasMoreElements()) {
            final T t = c.nextElement();

            if (filter.accept(t)) {
                return t;
            }
        }

        return null;
    }

    public static final <T> List<T> findAll(final Iterable<T> c, final Filter<? super T> filter) {
        final List<T> result = new ArrayList<T>();

        for (final T t : c) {
            if (filter.accept(t)) {
                result.add(t);
            }
        }

        return result;
    }

    public static final <T> List<T> findAll(final Enumeration<T> c, final Filter<? super T> filter) {
        final List<T> result = new ArrayList<T>();

        while (c.hasMoreElements()) {
            final T t = c.nextElement();

            if (filter.accept(t)) {
                result.add(t);
            }
        }

        return result;
    }

    public boolean isValidId(final int resId) {
        return resId != 0;
    }

    public static int getType(int resId) {
        return ((resId >> 16) & 0xff);
    }

    public static int getPackage(final int resId) {
        return ((resId >> 24) & 0xff);
    }

    public static int getEntry(final int resId) {
        return (resId & 0xffff);
    }

    public static int makeId(final int packageId, final int typeId, final int entryId) {
        return (((packageId) & 0xff) << 24) | (((typeId) & 0xff) << 16) | (entryId & 0xffff);
    }

    public static String hexlify(final byte[] value) {
        return _format("0x%02x", value);
    }

    public static String hexlify(final short[] value) {
        return _format("0x%04x", value);
    }

    public static String hexlify(final int[] value) {
        return _format("0x%08x", value);
    }

    public static String hexlify(final long[] value) {
        return _format("0x%16x", value);
    }

    private static final String _format(final String format, final Object array) {
        final StringBuilder builder = new StringBuilder("[");

        for (int i = 0, n = Array.getLength(array); i < n; i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(String.format(format, Array.get(array, i)));
        }

        return builder.append("]").toString();
    }

    public static boolean isVisibleCharacter(final byte c) {
        return (c >= 0x20 && c < 0x7f) || (c >= 0x80 && c <= 0xff);
    }

    private Internal() {
    }

}
