package com.sdklite.aapt;

/**
 * The resource name
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

    public ResourceName(final String packageName, final String typeName, final String name) {
        this.packageName = packageName;
        this.typeName = typeName;
        this.name = name;
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
        return this.packageName.equals(rn.packageName)
                && this.typeName.equals(rn.typeName)
                && this.name.equals(rn.name);
    }

    @Override
    public int hashCode() {
        return (this.packageName + ":" + this.typeName + "/" + this.name).hashCode();
    }
}
