package com.redhat.devtools.intellij.quarkus.lsp4ij.lifecycle;

import com.intellij.openapi.components.ServiceManager;
import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServerWrapper;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LanguageServerLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageServerLifecycleManager.class);//$NON-NLS-1$

    private final Queue<LanguageServerLifecycleListener> listeners;

    public LanguageServerLifecycleManager() {
        this.listeners = new ConcurrentLinkedQueue<>();
    }

    public static LanguageServerLifecycleManager getInstance() {
        return ServiceManager.getService(LanguageServerLifecycleManager.class);
    }

    public void addLanguageServerLifecycleListener(LanguageServerLifecycleListener listener) {
        this.listeners.add(listener);
    }

    public void removeLanguageServerLifecycleListener(LanguageServerLifecycleListener listener) {
        this.listeners.remove(listener);
    }

    public void onStartingProcess(LanguageServerWrapper languageServer) {
            for (LanguageServerLifecycleListener listener: this.listeners) {
                try {
                    listener.handleStartingProcess(languageServer);
                }
                catch(Exception e) {
                    LOGGER.error("Error while tracking starting process", e);
                }
            }
    }

    public void onStartedProcess(LanguageServerWrapper languageServer, Exception exception) {
        synchronized (this.listeners) {
            for (LanguageServerLifecycleListener listener: this.listeners) {
                try {
                    listener.handleStartedProcess(languageServer, exception);
                }
                catch(Exception e) {
                    LOGGER.error("Error while tracking started process", e);
                }
            }
        }
    }

    public void onStartedLanguageServer(LanguageServerWrapper languageServer, Throwable exception) {
            for (LanguageServerLifecycleListener listener: this.listeners) {
                try {
                    listener.handleStartedLanguageServer(languageServer, exception);
                }
                catch(Exception e) {
                    LOGGER.error("Error while tracking started language server", e);
                }
            }
    }

    public void logLSPMessage(Message message, LanguageServerWrapper languageServer) {
            for (LanguageServerLifecycleListener listener: this.listeners) {
                try {
                    listener.handleLSPMessage(message, languageServer);
                }
                catch(Exception e) {
                    LOGGER.error("Error while tracking LSP message", e);
                }
            }
    }

    public void onStoppingLanguageServer(LanguageServerWrapper languageServer) {
            for (LanguageServerLifecycleListener listener: this.listeners) {
                try {
                    listener.handleStoppingLanguageServer(languageServer);
                }
                catch(Exception e) {
                    LOGGER.error("Error while tracking stopping language server", e);
                }
            }

    }

    public void onStoppedLanguageServer(LanguageServerWrapper languageServer, Exception exception) {
            for (LanguageServerLifecycleListener listener: this.listeners) {
                try {
                    listener.handleStoppedLanguageServer(languageServer, exception);
                }
                catch(Exception e) {
                    LOGGER.error("Error while tracking stopped language server", e);
                }
            }
    }
}
