package com.sdklite.aapt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link Revision} which distinguishes between x and x.0, x.0.0, x.y.0, etc;
 * it basically keeps track of the precision of the revision string.
 * <p>
 * This is vital when referencing Gradle artifact numbers, since versions x.y.0
 * and version x.y are not the same.
 */
public class Revision implements Comparable<Revision> {
    public static final int MISSING_MAJOR_REV = 0;
    public static final int IMPLICIT_MINOR_REV = 0;
    public static final int IMPLICIT_MICRO_REV = 0;
    public static final int NOT_A_PREVIEW = 0;

    public static final Revision NOT_SPECIFIED = new Revision(MISSING_MAJOR_REV);

    public enum Precision {

        /** Only major revision specified: 1 term */
        MAJOR(1),

        /** Only major and minor revisions specified: 2 terms (x.y) */
        MINOR(2),

        /** Major, minor and micro revisions specified: 3 terms (x.y.z) */
        MICRO(3),

        /**
         * Major, minor, micro and preview revisions specified: 4 terms
         * (x.y.z-rcN)
         */
        PREVIEW(4);

        private final int termCount;

        Precision(final int termCount) {
            this.termCount = termCount;
        }

        int getTermCount() {
            return this.termCount;
        }
    }

    // 1=major 2=minor 3=micro 4=separator 5=previewType 6=preview
    private static final Pattern FULL_REVISION_PATTERN = Pattern.compile("\\s*([0-9]+)(?:\\.([0-9]+)(?:\\.([0-9]+))?)?([\\s-]*)?(?:(rc|alpha|beta)([0-9]+))?\\s*");

    protected static final String DEFAULT_SEPARATOR = " ";

    private final int major;
    private final int minor;
    private final int micro;
    private final int preview;
    private final Precision precision;
    private final String previewSeparator;

    /**
     * Parses a string of format "major.minor.micro rcPreview" and returns a new
     * {@link Revision} for it.
     *
     * <p>All the fields except major are optional.</p>
     *
     * @param revisionString
     *            A non-null revisionString to parse.
     * @param minimumPrecision
     *            Create a {@code Revision} with at least the given precision,
     *            regardless of how precise the {@code revisionString} is.
     * @return A new non-null {@link Revision}.
     * @throws NumberFormatException
     *             if the parsing failed.
     */
    public static Revision parseRevision(final String revisionString, final Precision minimumPrecision) throws NumberFormatException {
        Throwable cause = null;

        try {
            final Matcher m = FULL_REVISION_PATTERN.matcher(revisionString);
            if (m.matches()) {
                int major = Integer.parseInt(m.group(1));
                int minor = IMPLICIT_MINOR_REV;
                int micro = IMPLICIT_MICRO_REV;
                int preview = NOT_A_PREVIEW;
                Precision precision = Precision.MAJOR;
                String previewSeparator = " ";

                String s = m.group(2);
                if (s != null) {
                    minor = Integer.parseInt(s);
                    precision = Precision.MINOR;
                }

                s = m.group(3);
                if (s != null) {
                    micro = Integer.parseInt(s);
                    precision = Precision.MICRO;
                }

                s = m.group(6);
                if (s != null) {
                    preview = Integer.parseInt(s);
                    previewSeparator = m.group(4);
                    precision = Precision.PREVIEW;
                }

                if (minimumPrecision.compareTo(precision) >= 0) {
                    precision = minimumPrecision;
                }

                return new Revision(major, minor, micro, preview, precision, previewSeparator);
            }
        } catch (Throwable t) {
            cause = t;
        }

        final NumberFormatException n = new NumberFormatException("Invalid revision: " + revisionString);
        if (cause != null) {
            n.initCause(cause);
        }

        throw n;
    }

    /**
     * Parses a string of format "major.minor.micro rcPreview" and returns a new
     * {@code Revision} for it.
     *
     * <p>All the fields except major are optional.</p>
     *
     * @param revisionString
     *            A non-null revisionString to parse.
     * @return A new non-null {@link Revision}, with precision depending on the
     *         precision of {@code
     *         revisionString}.
     * @throws NumberFormatException
     *             if the parsing failed.
     */
    public static Revision parseRevision(final String revisionString) throws NumberFormatException {
        return parseRevision(revisionString, Precision.MAJOR);
    }

    /**
     * Creates a new {@code Revision} with the specified major revision and no
     * other revision components.
     */
    public Revision(final int major) {
        this(major, IMPLICIT_MINOR_REV, IMPLICIT_MICRO_REV, NOT_A_PREVIEW, Precision.MAJOR, DEFAULT_SEPARATOR);
    }

    /**
     * Creates a new {@code Revision} with the specified major and minor
     * revision components and no others.
     */
    public Revision(final int major, final int minor) {
        this(major, minor, IMPLICIT_MICRO_REV, NOT_A_PREVIEW, Precision.MINOR, DEFAULT_SEPARATOR);
    }

    /**
     * Creates a copy of the specified {@code Revision}.
     */
    public Revision(final Revision revision) {
        this(revision.getMajor(), revision.getMinor(), revision.getMicro(), revision.getPreview(), revision.precision,
                revision.getSeparator());
    }

    /**
     * Creates a new {@code Revision} with the specified major, minor, and micro
     * revision components and no preview component.
     */
    public Revision(final int major, final int minor, final int micro) {
        this(major, minor, micro, NOT_A_PREVIEW, Precision.MICRO, DEFAULT_SEPARATOR);
    }

    /**
     * Creates a new {@code Revision} with the specified components.
     */
    public Revision(final int major, final int minor, final int micro, final int preview) {
        this(major, minor, micro, preview, Precision.PREVIEW, DEFAULT_SEPARATOR);
    }

    Revision(final int major, final int minor, final int micro, final int preview, final Precision precision, final String separator) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.preview = preview;
        this.previewSeparator = separator;
        this.precision = precision;
    }

    /**
     * Creates a new {@code Revision} with the specified components. The
     * precision will be exactly sufficient to include all non-null components.
     */
    public Revision(final int major, final Integer minor, final Integer micro, final Integer preview) {
        this(major,
            minor == null ? IMPLICIT_MINOR_REV : minor, micro == null ? IMPLICIT_MICRO_REV : micro,
            preview == null ? NOT_A_PREVIEW : preview,
            preview != null ? Precision.PREVIEW : micro != null ? Precision.MICRO : minor != null ? Precision.MINOR : Precision.MAJOR,
            DEFAULT_SEPARATOR);
    }

    /**
     * Returns the version in a fixed format major.minor.micro with an optional
     * "rc preview#". For example it would return "18.0.0", "18.1.0" or
     * "18.1.2 rc5".
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getMajor());

        if (this.precision.compareTo(Precision.MINOR) >= 0) {
            sb.append('.').append(getMinor());
            if (this.precision.compareTo(Precision.MICRO) >= 0) {
                sb.append('.').append(getMicro());
                if (this.precision.compareTo(Precision.PREVIEW) >= 0 && isPreview()) {
                    sb.append(getSeparator()).append("rc").append(getPreview());
                }
            }
        }

        return sb.toString();
    }

    /**
     * Returns an {@code int[]} containing the Major, Minor, and Micro (and
     * optionally Preview) components (if specified) of this revision
     *
     * @param includePreview
     *            If false, the preview component of this revision will be
     *            ignored.
     * @return An array exactly long enough to include the components specified
     *         in this revision. For example, if only Major and Minor revisions
     *         are specified the array will be of length 2. If a preview
     *         component is specified and {@code includePreview} is true, the
     *         result will always be of length 4.
     */

    public int[] toIntArray(final boolean includePreview) {
        final int[] result;
        if (precision.compareTo(Precision.PREVIEW) >= 0) {
            if (includePreview) {
                result = new int[precision.getTermCount()];
                result[3] = getPreview();
            } else {
                result = new int[precision.getTermCount() - 1];
            }
        } else {
            result = new int[precision.getTermCount()];
        }

        result[0] = getMajor();

        if (precision.compareTo(Precision.MINOR) >= 0) {
            result[1] = getMinor();
            if (precision.compareTo(Precision.MICRO) >= 0) {
                result[2] = getMicro();
            }
        }

        return result;
    }

    /**
     * Returns {@code true} if this revision is equal, <b>including in
     * precision</b> to {@code rhs}. That is,
     * {@code (new Revision(20)).equals(new Revision(20, 0, 0)} will return
     * {@code false}.
     */
    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) {
            return true;
        }

        if (rhs == null) {
            return false;
        }

        if (!(rhs instanceof Revision)) {
            return false;
        }

        final Revision other = (Revision) rhs;
        if (major != other.major) {
            return false;
        }
        if (minor != other.minor) {
            return false;
        }
        if (micro != other.micro) {
            return false;
        }
        if (preview != other.preview) {
            return false;
        }
        return precision == other.precision;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getMicro() {
        return micro;
    }

    protected String getSeparator() {
        return previewSeparator;
    }

    public boolean isPreview() {
        return preview > NOT_A_PREVIEW;
    }

    public int getPreview() {
        return preview;
    }

    /**
     * Returns the version in a dynamic format "major.minor.micro rc#". This is
     * similar to {@link #toString()} except it omits minor, micro or preview
     * versions when they are zero. For example it would return "18 rc1" instead
     * of "18.0.0 rc1", or "18.1 rc2" instead of "18.1.0 rc2".
     */

    public String toShortString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(major);
        if (minor > 0 || micro > 0) {
            sb.append('.').append(minor);
        }
        if (micro > 0) {
            sb.append('.').append(micro);
        }
        if (preview != NOT_A_PREVIEW) {
            sb.append(previewSeparator).append("rc").append(preview);
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + micro;
        result = prime * result + preview;
        result = prime * result + precision.getTermCount();
        return result;
    }

    /**
     * Trivial comparison of a version, e.g 17.1.2 &lt; 18.0.0.
     *
     * Note that preview/release candidate are released before their final
     * version, so "18.0.0 rc1" comes below "18.0.0". The best way to think of
     * it as if the lack of preview number was "+inf": "18.1.2 rc5" =&gt;
     * "18.1.2.5" so its less than "18.1.2.+INF" but more than "18.1.1.0" and
     * more than "18.1.2.4"
     *
     * @param rhs
     *            The right-hand side {@link Revision} to compare with.
     * @return &lt;0 if lhs &lt; rhs; 0 if lhs==rhs; &gt;0 if lhs &gt; rhs.
     */
    @Override
    public int compareTo(final Revision rhs) {
        return compareTo(rhs, PreviewComparison.COMPARE_NUMBER);
    }

    /**
     * Trivial comparison of a version, e.g 17.1.2 &lt; 18.0.0.
     *
     * Note that preview/release candidate are released before their final
     * version, so "18.0.0 rc1" comes below "18.0.0". The best way to think of
     * it as if the lack of preview number was "+inf": "18.1.2 rc5" =&gt;
     * "18.1.2.5" so its less than "18.1.2.+INF" but more than "18.1.1.0" and
     * more than "18.1.2.4"
     *
     * @param rhs
     *            The right-hand side {@link Revision} to compare with.
     * @param comparePreview
     *            How to compare the preview value.
     * @return &lt;0 if lhs &lt; rhs; 0 if lhs==rhs; &gt;0 if lhs &gt; rhs.
     */
    public int compareTo(final Revision rhs, final PreviewComparison comparePreview) {
        int delta = major - rhs.major;
        if (delta != 0) {
            return delta;
        }

        delta = minor - rhs.minor;
        if (delta != 0) {
            return delta;
        }

        delta = micro - rhs.micro;
        if (delta != 0) {
            return delta;
        }

        int p1, p2;
        switch (comparePreview) {
        case IGNORE:
            // Nothing to compare.
            break;

        case COMPARE_NUMBER:
            p1 = preview == NOT_A_PREVIEW ? Integer.MAX_VALUE : preview;
            p2 = rhs.preview == NOT_A_PREVIEW ? Integer.MAX_VALUE : rhs.preview;
            delta = p1 - p2;
            break;

        case COMPARE_TYPE:
            p1 = preview == NOT_A_PREVIEW ? 1 : 0;
            p2 = rhs.preview == NOT_A_PREVIEW ? 1 : 0;
            delta = p1 - p2;
            break;
        }
        return delta;
    }

    /**
     * Indicates how to compare the preview field in
     * {@link Revision#compareTo(Revision, PreviewComparison)}
     */
    public enum PreviewComparison {
        /**
         * Both revisions must have exactly the same preview number.
         */
        COMPARE_NUMBER,
        /**
         * Both revisions must have the same preview type (both must be previews
         * or both must not be previews, but the actual number is irrelevant.)
         * This is the most typical choice used to find updates of the same
         * type.
         */
        COMPARE_TYPE,
        /**
         * The preview field is ignored and not used in the comparison.
         */
        IGNORE
    }

}
