package com.redhat.devtools.intellij.quarkus.lsp4ij.console;

import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServerWrapper;

public class LanguageServerDefinitionLabelAdapter {

    public final LanguageServerWrapper languageServer;

    public LanguageServerDefinitionLabelAdapter(LanguageServerWrapper languageServer) {
        this.languageServer = languageServer;
    }

    @Override
    public String toString() {
        return languageServer.serverDefinition.getDisplayName();
    }
}
