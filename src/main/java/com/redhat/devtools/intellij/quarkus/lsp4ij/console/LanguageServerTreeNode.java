package com.redhat.devtools.intellij.quarkus.lsp4ij.console;

import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServersRegistry;

import javax.swing.tree.DefaultMutableTreeNode;

public class LanguageServerTreeNode extends DefaultMutableTreeNode  {

    private final LanguageServersRegistry.LanguageServerDefinition serverDefinition;

    public LanguageServerTreeNode(LanguageServersRegistry.LanguageServerDefinition serverDefinition) {
        super(serverDefinition.getDisplayName());
        this.serverDefinition = serverDefinition;
    }

    public LanguageServersRegistry.LanguageServerDefinition getServerDefinition() {
        return serverDefinition;
    }

    public LanguageServerProcessTreeNode getActiveProcessTreeNode() {
        for (int i = 0; i < super.getChildCount(); i++) {
            LanguageServerProcessTreeNode node = (LanguageServerProcessTreeNode) super.getChildAt(i);
            if (node.isActive()) {
                return node;
            }
        }
        return null;
    }
}
