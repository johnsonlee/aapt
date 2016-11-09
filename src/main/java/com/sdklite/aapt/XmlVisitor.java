package com.sdklite.aapt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

public class XmlVisitor extends SimpleVisitor {

    final PrintWriter out;

    final Stack<Xml.Namespace> namespaces = new Stack<Xml.Namespace>();

    int depth = -1;

    Xml.StartElement element;

    public XmlVisitor(final PrintWriter out) {
        this.out = out;
    }

    public XmlVisitor(final PrintStream out) {
        this(new PrintWriter(out));
    }

    public XmlVisitor(final PrintStream out, final boolean autoFlush) {
        this(new PrintWriter(out, autoFlush));
    }

    public XmlVisitor(final OutputStream out) {
        this(new PrintStream(out));
    }

    public XmlVisitor(final OutputStream out, final boolean autoFlush) {
        this(new PrintStream(out), autoFlush);
    }

    public XmlVisitor(final File file) throws FileNotFoundException {
        this(new PrintStream(file));
    }

    public XmlVisitor(final String file) throws FileNotFoundException {
        this(new PrintStream(file));
    }

    @Override
    public void visit(final Xml chunk) {
        this.out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        for (final Xml.Node node : chunk.chunks) {
            visit(node);
        }
    }

    @Override
    public void visit(final Xml.Node chunk) {
        switch (chunk.type) {
        case ChunkType.XML_START_NAMESPACE: {
            this.namespaces.push((Xml.Namespace) chunk);
            break;
        }
        case ChunkType.XML_END_NAMESPACE:
            this.namespaces.pop();
            break;
        case ChunkType.XML_START_ELEMENT: {
            if (null != this.element) {
                this.out.println(">");
            }

            this.depth++;

            final Xml.Namespace ns = getNamespace();
            final Xml.StartElement start = (Xml.StartElement) chunk;
            this.out.printf(getIndent()).printf("<").printf(start.getName());
            for (final Iterator<Xml.Attribute> i = start.attributes(); i.hasNext();) {
                final Xml.Attribute attr = i.next();
                this.out.printf(" ").printf(ns.getPrefix()).printf(":").printf(attr.getName()).printf("=\"").printf("%s", attr.getValue()).printf("\"");
            }

            this.element = start;
            break;
        }
        case ChunkType.XML_END_ELEMENT: {
            final Xml.EndElement end = (Xml.EndElement) chunk;
            if (null != this.element && this.element.getName().equals(end.getName())) {
                this.out.println(" />");
                this.element = null;
            } else {
                this.out.printf(getIndent()).printf("</").printf(end.getName()).println(">");
            }

            this.depth--;
            break;
        }
        case ChunkType.XML_CDATA:
            this.out.print(chunk.toString());
            break;
        }
    }

    private String getIndent() {
        final char[] intent = new char[this.depth * 2];
        Arrays.fill(intent, ' ');
        return new String(intent);
    }

    public Xml.Namespace getNamespace() {
        return this.namespaces.peek();
    }

}
