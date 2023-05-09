package com.redhat.devtools.intellij.quarkus.lsp4ij.lifecycle;

import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServerWrapper;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

public interface LanguageServerLifecycleListener {

    void handleStartingProcess(LanguageServerWrapper languageServer);

    void handleStartedProcess(LanguageServerWrapper languageServer, Throwable exception);

    void handleStartedLanguageServer(LanguageServerWrapper languageServer, Throwable exception);

    void handleLSPMessage(Message message, LanguageServerWrapper languageServer);

    void handleStoppingLanguageServer(LanguageServerWrapper languageServer);

    void handleStoppedLanguageServer(LanguageServerWrapper languageServer, Throwable exception);

}
