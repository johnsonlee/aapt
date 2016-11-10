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

    Xml.Element element;

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
            final Xml.Element start = (Xml.Element) chunk;

            this.out.printf(getIndent(this.depth)).printf("<").printf(start.getName());

            if (start == chunk.getDocument().getDocumentElement()) {
                this.out.printf(" ").print(ns);
            }

            final Iterator<Xml.Attribute> i = start.attributes();
            if (i.hasNext()) {
                final Xml.Attribute first = i.next();
                if (i.hasNext()) {
                    this.out.printf("\n").printf(getIndent(this.depth + 1));
                } else {
                    this.out.printf(" ");
                }

                this.out.printf(ns.getPrefix()).printf(":").printf(first.getName()).printf("=\"").printf("%s", first.getValue()).printf("\"");

                while (i.hasNext()) {
                    final Xml.Attribute attr = i.next();
                    this.out.println();
                    this.out.printf(getIndent(this.depth + 1)).printf(ns.getPrefix()).printf(":").printf(attr.getName()).printf("=\"").printf("%s", attr.getValue()).printf("\"");
                }
            }

            this.element = start;
            break;
        }
        case ChunkType.XML_END_ELEMENT: {
            final Xml.Element end = (Xml.Element) chunk;
            if (null != this.element && this.element.getName().equals(end.getName())) {
                this.out.println(" />");
                this.element = null;
            } else {
                this.out.printf(getIndent(this.depth)).printf("</").printf(end.getName()).println(">");
            }

            this.depth--;
            break;
        }
        case ChunkType.XML_CDATA:
            this.out.print(chunk.toString());
            break;
        }
    }

    public Xml.Namespace getNamespace() {
        return this.namespaces.peek();
    }

    private static String getIndent(final int depth) {
        final char[] intent = new char[depth * 4];
        Arrays.fill(intent, ' ');
        return new String(intent);
    }

}
