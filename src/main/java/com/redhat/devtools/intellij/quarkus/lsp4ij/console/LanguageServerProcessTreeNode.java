package com.redhat.devtools.intellij.quarkus.lsp4ij.console;

import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServerWrapper;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class LanguageServerProcessTreeNode extends DefaultMutableTreeNode {

    private final LanguageServerWrapper languageServer;

    public LanguageServerProcessTreeNode(LanguageServerWrapper languageServer) {
        super("starting...");
        this.languageServer = languageServer;
    }

    public boolean isActive() {
        return (!getUserObject().equals("stopped"));
    }

    public LanguageServerWrapper getLanguageServer() {
        return languageServer;
    }
}
