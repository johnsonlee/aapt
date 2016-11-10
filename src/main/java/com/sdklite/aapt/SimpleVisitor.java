package com.sdklite.aapt;

public class SimpleVisitor implements ChunkVisitor {

    @Override
    public void visit(final StringPool chunk) {
    }

    @Override
    public void visit(final ResourceTable chunk) {
    }

    @Override
    public void visit(final ResourceTable.Package chunk) {
    }

    @Override
    public void visit(final ResourceTable.Type chunk) {
    }

    @Override
    public void visit(final ResourceTable.TypeSpec chunk) {
    }

    @Override
    public void visit(final ResourceTable.Library chunk) {
    }

    @Override
    public void visit(final Xml chunk) {
    }

    @Override
    public void visit(final Xml.Node chunk) {
    }

    @Override
    public void visit(final Xml.ResourceMap chunk) {
    }

}
