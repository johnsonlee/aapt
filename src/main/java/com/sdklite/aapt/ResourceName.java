package com.sdklite.aapt;

/**
 * The resource name is consists of three parts:
 * <ul>
 * <li>The package name</li>
 * <li>The resource type name</li>
 * <li>The entry name</li>
 * </ul>
 * 
 * @author johnsonlee
 *
 */
public final class ResourceName {

    /**
     * The package name of resource
     */
    public final String packageName;

    /**
     * The type name of resource
     */
    public final String typeName;

    /**
     * The name of resource
     */
    public final String name;

    private final String string;

    /**
     * Instantialize with package name, resource type name and entry name
     * 
     * @param packageName
     *            The package name
     * @param typeName
     *            The resource type name
     * @param name
     *            The resource entry name
     */
    public ResourceName(final String packageName, final String typeName, final String name) {
        this.packageName = packageName;
        this.typeName = typeName;
        this.name = name;
        this.string = packageName + ":" + typeName + "/" + name;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ResourceName)) {
            return false;
        }

        final ResourceName rn = (ResourceName) obj;
        return this.string.equals(rn.string);
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @Override
    public String toString() {
        return this.string;
    }
}
