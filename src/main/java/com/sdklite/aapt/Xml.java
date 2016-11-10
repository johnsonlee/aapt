package com.sdklite.aapt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.util.TypedValue;

/**
 * Binary XML resource
 * 
 * @author johnsonlee
 *
 */
public class Xml extends ChunkHeader {

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

    public abstract class Node extends ChunkHeader {
        int lineNumber;
        int commentIndex;

        protected Node(final short type) {
            super(type);
        }

        public Xml getDocument() {
            return Xml.this;
        }

        @Override
        public void accept(final ChunkVisitor visitor) {
            visitor.visit(this);
        }
    }

    public final class CharData extends Node {

        int data;

        final ResourceValue typedData = new ResourceValue();
        
        public CharData() {
            super(ChunkType.XML_CDATA);
        }

        @Override
        public String toString() {
            return pool.getStringAt(this.data);
        }
    }

    public abstract class Namespace extends Node {

        int prefix;
        int uri;

        public Namespace(final short type) {
            super(type);
        }

        public String getPrefix() {
            return pool.getStringAt(this.prefix);
        }

        public String getUri() {
            return pool.getStringAt(this.uri);
        }

        @Override
        public String toString() {
            return "xmlns:" + this.getPrefix() + "=\"" + this.getUri() + "\"";
        }
    }

    public final class StartNamespace extends Namespace {

        public StartNamespace() {
            super(ChunkType.XML_START_NAMESPACE);
        }

    }

    public final class EndNamespace extends Namespace {

        public EndNamespace() {
            super(ChunkType.XML_END_NAMESPACE);
        }

    }

    public abstract class Element extends Node {

        int ns;
        int name;

        public Element(short type) {
            super(type);
        }

        public String getNamespace() {
            return pool.getStringAt(this.ns);
        }

        public String getName() {
            return pool.getStringAt(this.name);
        }

        public abstract Iterator<Attribute> attributes();
    }

    public final class StartElement extends Element {

        short attributeStart;
        short attributeSize;
        short idIndex;
        short classIndex;
        short styleIndex;

        final List<Attribute> attributes = new ArrayList<Attribute>();
        
        public StartElement() {
            super(ChunkType.XML_START_ELEMENT);
        }

        public void insertOrReplaceAttribute(final Attribute attr) {
            for (final Iterator<Attribute> i = this.attributes.iterator(); i.hasNext();) {
                final Attribute attribute = i.next();
                if (attribute.name == attr.name) {
                    i.remove();
                }
            }

            this.attributes.add(attr);
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

        @Override
        public Iterator<Attribute> attributes() {
            return this.attributes.iterator();
        }
    }

    public final class EndElement extends Element {

        public EndElement() {
            super(ChunkType.XML_END_ELEMENT);
        }

        @Override
        public Iterator<Attribute> attributes() {
            return Collections.emptyIterator();
        }

    }

    public final class Attribute {

        int ns;

        int name;

        int rawValue;

        final ResourceValue typedValue = new ResourceValue();

        Attribute() {
        }

        public String getName() {
            return pool.getStringAt(this.name);
        }

        public String getValue() {
            switch (this.typedValue.dataType) {
            case ValueType.STRING:
                return pool.getStringAt(this.rawValue);
            default:
                return TypedValue.coerceToString(this.typedValue.dataType, this.typedValue.data);
            }
        }
    }

    StringPool pool;

    ResourceMap resources;

    final List<Node> chunks = new ArrayList<Node>();

    public Xml() {
        super(XML);
    }

    public List<StartElement> getElementsByName(final String name) {
        final List<StartElement> elements = new ArrayList<StartElement>();
        for (final ChunkHeader chunk : this.chunks) {
            if (!(chunk instanceof StartElement)) {
                continue;
            }

            final StartElement element = (StartElement) chunk;
            if (name.equals(element.getName())) {
                elements.add(element);
            }
        }

        return elements;
    }

    @Override
    public void accept(final ChunkVisitor visitor) {
        visitor.visit(this);
    }

}
