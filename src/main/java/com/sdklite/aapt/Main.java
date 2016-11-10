package com.sdklite.aapt;

import java.io.IOException;

/**
 * The Main class
 */
public class Main {

    public static final void main(final String[] args) throws IOException {
        if (args.length < 1) {
            printUsage();
            return;
        }

        final String cmd = args[0];
        if ("dump".equals(cmd) || "d".equals(cmd)) {
            if (args.length < 3) {
                printUsage();
                return;
            }

            final String type = args[1];
            final ChunkParser parser = new ChunkParser(args[2]);
            final ChunkVisitor visitor;

            if ("resources".equals(type)) {
                visitor = new ResourceTableVisitor(System.out, true);
            } else if ("strings".equals(type)) {
                visitor = new StringPoolVisitor(System.out, true);
            } else if ("xml".equals(type)) {
                visitor = new XmlVisitor(System.out, true);
            } else {
                visitor = new SimpleVisitor();
            }

            parser.parse().accept(visitor);
        } else if ("help".equals(cmd) || "h".equals(cmd) || "?".equals(cmd)) {
            printUsage();
        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: aapt <command> [args]");
        System.out.println();
        System.out.println("  Commands:");
        System.out.println("    - dump [resources, strings, xml]             Dump asset chunks");
        System.out.println();
    }
}
