package com.sdklite.aapt;

import static com.sdklite.aapt.Internal.find;
import static com.sdklite.aapt.Internal.findAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.sdklite.aapt.Internal.Filter;

import android.util.TypedValue;

/**
 * Represents Android binary XML resource
 * 
 * @author johnsonlee
 *
 */
public class Xml extends ChunkHeader {

    /**
     * Represents a mapping from strings in the string pool back to resource
     * identifiers
     * 
     * @author johnsonlee
     *
     */
    public final class ResourceMap extends ChunkHeader {

        final List<Integer> ids = new ArrayList<Integer>();

        public ResourceMap() {
            super(ChunkType.XML_RESOURCE_MAP);
        }

        @Override
        public void accept(final ChunkVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * Basic XML tree node. A single item in the XML document
     * 
     * @author johnsonlee
     *
     */
    public abstract class Node extends ChunkHeader {
        int lineNumber;
        int commentIndex;

        protected Node(final short type) {
            super(type);
        }

        /**
         * Returns the owner XML document
         * 
         * @return the owner XML document
         */
        public Xml getDocument() {
            return Xml.this;
        }

        @Override
        public void accept(final ChunkVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * CDATA section
     * 
     * @author johnsonlee
     *
     */
    public final class CharData extends Node {

        int data;

        final ResourceValue typedData = new ResourceValue();

        /**
         * Default constructor
         */
        public CharData() {
            super(ChunkType.XML_CDATA);
        }

        @Override
        public String toString() {
            return pool.getStringAt(this.data);
        }
    }

    /**
     * Namespace section
     * 
     * @author johnsonlee
     *
     */
    public final class Namespace extends Node {

        int prefix;
        int uri;

        /**
         * Instantialize with namespace type
         * 
         * @param type
         *            The namespace type
         * @see ChunkType#XML_START_NAMESPACE
         * @see ChunkType#XML_END_NAMESPACE
         */
        public Namespace(final short type) {
            super(type);
        }

        /**
         * Returns the prefix
         * 
         * @return the prefix
         */
        public String getPrefix() {
            return pool.getStringAt(this.prefix);
        }

        /**
         * Returns the URI
         * 
         * @return the URI
         */
        public String getUri() {
            return pool.getStringAt(this.uri);
        }

        @Override
        public String toString() {
            return "xmlns:" + this.getPrefix() + "=\"" + this.getUri() + "\"";
        }
    }

    /**
     * XML element
     * 
     * @author johnsonlee
     *
     */
    public class Element extends Node {

        int ns;
        int name;
        short attributeStart;
        short attributeSize;
        short idIndex;
        short classIndex;
        short styleIndex;

        final List<Attribute> attributes = new ArrayList<Attribute>();

        /**
         * Instantialize with element type
         * 
         * @param type
         *            The element type
         * @see ChunkType#XML_START_ELEMENT
         * @see ChunkType#XML_END_ELEMENT
         */
        public Element(final short type) {
            super(type);
        }

        /**
         * Returns the namespace
         * 
         * @return the namespace
         */
        public String getNamespace() {
            return pool.getStringAt(this.ns);
        }

        /**
         * Returns the name
         * 
         * @return the name
         */
        public String getName() {
            return pool.getStringAt(this.name);
        }

        /**
         * Returns an iterator of attributes
         * 
         * @return an iterator of attributes
         */
        public Iterator<Attribute> attributes() {
            return this.attributes.iterator();
        }

        /**
         * Returns the {@code android:id} attribute value
         * 
         * @return the {@code android:id} attribute value
         */
        public int getId() {
            for (final Attribute attr : this.attributes) {
                if ("id".equals(attr.getName())) {
                    return attr.rawValue;
                }
            }

            return -1;
        }

        /**
         * Appends the specified attribute to the end of the attribute list
         * 
         * @param attr
         *            The attribute to be appended
         * @throws DuplicateAttributeException
         *             if the specified attribute already exists
         */
        public void addAttribute(final Attribute attr) throws DuplicateAttributeException {
            final Attribute duplicated = find(this.attributes, new Filter<Attribute>() {
                @Override
                public boolean accept(final Attribute it) {
                    return it.name == attr.name;
                }
            });

            if (null != duplicated) {
                throw new DuplicateAttributeException(attr.getName());
            }

            this.attributes.add(attr);
        }

        /**
         * Remote the specified attribute
         * 
         * @param attr
         *            The attribute to be removed
         * @return true if the attribute exists
         */
        public boolean removeAttribute(final Attribute attr) {
            final Attribute duplicated = find(this.attributes, new Filter<Attribute>() {
                @Override
                public boolean accept(final Attribute it) {
                    return it.name == attr.name;
                }
            });

            if (duplicated != null) {
                return this.attributes.remove(duplicated);
            }

            return false;
        }

        /**
         * Merge the specified attribute into the existing attribute or add it
         * into the attribute list
         * 
         * @param attr
         *            The attribute to be merged
         */
        public void mergeAttribute(final Attribute attr) {
            final Attribute duplicated = find(this.attributes, new Filter<Attribute>() {
                @Override
                public boolean accept(final Attribute it) {
                    return it.name == attr.name;
                }
            });

            if (duplicated != null) {
                duplicated.ns = attr.ns;
                duplicated.name = attr.name;
                duplicated.rawValue = attr.rawValue;
                duplicated.typedValue.dataType = attr.typedValue.dataType;
                duplicated.typedValue.data = attr.typedValue.data;
            } else {
                this.attributes.add(attr);
            }
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<");
            buffer.append(pool.getStringAt(this.name));
            buffer.append("\n");

            for (final Attribute attr : this.attributes) {
                buffer.append(attr.toString());
                buffer.append("\n");
            }

            buffer.append(">");

            return buffer.toString();
        }

        public String getAttribute(final String name) {
            final Attribute attr = find(this.attributes, new Filter<Attribute>() {
                @Override
                public boolean accept(final Attribute it) {
                    return it.getName().equals(name);
                }
            });

            if (null != attr) {
                return attr.getValue();
            }

            return null;
        }
    }

    /**
     * Attribute of element
     * 
     * @author johnsonlee
     *
     */
    public final class Attribute {

        int ns;

        int name;

        int rawValue;

        final ResourceValue typedValue = new ResourceValue();

        Attribute() {
        }

        /**
         * Returns the attribute name
         * 
         * @return the attribute name
         */
        public String getName() {
            return pool.getStringAt(this.name);
        }

        /**
         * Returns the attribute value
         * 
         * @return the attribute value
         */
        public String getValue() {
            switch (this.typedValue.dataType) {
            case ValueType.STRING:
                return pool.getStringAt(this.rawValue);
            default:
                return TypedValue.coerceToString(this.typedValue.dataType, this.typedValue.data);
            }
        }

        @Override
        public String toString() {
            return this.getName() + "=\"" + getValue() + "\"";
        }
    }

    StringPool pool;

    ResourceMap resources;

    final List<Node> chunks = new ArrayList<Node>();

    public Xml() {
        super(XML);
    }

    /**
     * Returns the document element
     * 
     * @return the document element
     */
    public Element getDocumentElement() {
        return (Element) find(this.chunks, new Filter<Node>() {
            @Override
            public boolean accept(final Node it) {
                return ChunkType.XML_START_ELEMENT == it.type;
            }
        });
    }

    /**
     * Returns all the elements corresponding to the specified name
     * 
     * @param name
     *            The element name
     * @return a collection of XML element
     */
    public List<Element> getElementsByName(final String name) {
        final List<Node> nodes = findAll(this.chunks, new Filter<Node>() {
            @Override
            public boolean accept(final Node it) {
                return it.type == ChunkType.XML_START_ELEMENT && ((Element) it).getName().equals(name);
            }
        });
        return Arrays.asList(nodes.toArray(new Element[nodes.size()]));
    }

    /**
     * Returns the element with the specified id
     * 
     * @param id
     *            The value of {@code android:id} attribute
     * @return the element with the specified id or null if not found
     */
    public Element getElementById(final int id) {
        return (Element) find(this.chunks, new Filter<Node>() {
            @Override
            public boolean accept(final Node it) {
                return it.type == ChunkType.XML_START_ELEMENT && ((Element) it).getId() == id;
            }
        });
    }

    @Override
    public void accept(final ChunkVisitor visitor) {
        visitor.visit(this);
    }

}
