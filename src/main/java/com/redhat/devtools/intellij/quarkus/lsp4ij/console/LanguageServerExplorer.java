/******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.quarkus.lsp4ij.console;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServerWrapper;
import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServersRegistry;
import com.redhat.devtools.intellij.quarkus.lsp4ij.lifecycle.LanguageServerLifecycleListener;
import com.redhat.devtools.intellij.quarkus.lsp4ij.lifecycle.LanguageServerLifecycleManager;
import com.redhat.devtools.intellij.quarkus.lsp4ij.settings.ServerTrace;
import com.redhat.devtools.intellij.quarkus.lsp4ij.settings.UserDefinedLanguageServerSettings;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.util.HashMap;
import java.util.Map;

import static com.redhat.devtools.intellij.quarkus.lsp4ij.utils.UIHelper.executeInUI;

/**
 * Language server explorer which shows language servers and their process.
 *
 * @author Angelo ZERR
 */
public class LanguageServerExplorer extends SimpleToolWindowPanel {

    private final LSPConsoleToolWindowPanel panel;

    private final Tree tree;
    private final LanguageServerLifecycleListener listener;

    public LanguageServerExplorer(LSPConsoleToolWindowPanel panel) {
        super(true, false);
        this.panel = panel;
        listener = createListener();
        LanguageServerLifecycleManager.getInstance().addLanguageServerLifecycleListener(listener);
        tree = buildTree();
        this.setContent(tree);
    }

    private LanguageServerLifecycleListener createListener() {
        return new LanguageServerLifecycleListener() {

            @Override
            public void handleStartingProcess(LanguageServerWrapper languageServer) {
                executeInUI(() -> {
                    LanguageServerTreeNode node = findLanguageServerTreeNode(languageServer);
                    if (node == null) {
                        return;
                    }
                    var processTreeNode = new LanguageServerProcessTreeNode(languageServer);
                    node.add(processTreeNode);
                    ((DefaultTreeModel)tree.getModel()).reload(node);

                    final TreePath treePath = new TreePath(processTreeNode.getPath());
                    tree.setSelectionPath(treePath);
                    if (!tree.isExpanded(treePath)) {
                        tree.expandPath(treePath);
                    }
                });
            }

            @Override
            public void handleStartedProcess(LanguageServerWrapper languageServer, Throwable exception) {
                executeInUI(() -> {
                    LanguageServerTreeNode node = findLanguageServerTreeNode(languageServer);
                    if (node != null) {
                        LanguageServerProcessTreeNode process = node.getActiveProcessTreeNode();
                        process.setUserObject("started process");
                        ((DefaultTreeModel)tree.getModel()).reload(process);
                    }
                });
            }

            @Override
            public void handleStartedLanguageServer(LanguageServerWrapper languageServer, Throwable exception) {
                executeInUI(() -> {
                    LanguageServerTreeNode node = findLanguageServerTreeNode(languageServer);
                    if (node != null) {
                        LanguageServerProcessTreeNode process = node.getActiveProcessTreeNode();
                        process.setUserObject("started");
                        ((DefaultTreeModel)tree.getModel()).reload(process);
                    }

                });
            }

            private final Map<String, String> methods = new HashMap<>(10);

            @Override
            public void handleLSPMessage(Message message, LanguageServerWrapper languageServer) {
                UserDefinedLanguageServerSettings.LanguageServerDefinitionSettings settings = UserDefinedLanguageServerSettings.getInstance().getLanguageServerSettings(languageServer.serverDefinition.id);
                ServerTrace traceLevel = settings.getServerTrace();
                if (ServerTrace.off.equals(traceLevel)) {
                    return;
                }
                StringBuilder formattedMessage = new StringBuilder("[Trace]");
                if (message instanceof RequestMessage) {
                    // [Trace - 12:27:33 AM] Sending request 'initialize - (0)'.
                    //  Params: {
                    String id = ((RequestMessage) message).getId();
                    String method = ((RequestMessage) message).getMethod();
                    methods.put(id, method);
                    formattedMessage.append(" Sending request '").append(method).append(" - (").append(id).append(")'.");
                    formattedMessage.append("\n");
                } else if (message instanceof ResponseMessage) {
                    // [Trace - 12:27:35 AM] Received response 'initialize - (0)' in 1921ms.
                    String id = ((ResponseMessage) message).getId();
                    String method = methods.remove(id);
                    formattedMessage.append(" Received response '").append(method == null?"<unknown>":method).append(" - (").append(id).append(")'.");
                    formattedMessage.append("\n");
                } else if (message instanceof NotificationMessage) {
                    // [Trace - 12:27:35 AM] Sending notification 'initialized'.
                    // Params: {}
                    formattedMessage.append(" Sending notification '").append(((NotificationMessage) message).getMethod()).append("'.");
                    formattedMessage.append("\n");
                }
                if (ServerTrace.verbose.equals(traceLevel)) {
                    formattedMessage.append(message.toString());
                    formattedMessage.append("\n");
                }
                formattedMessage.append("\n");

                executeInUI(() -> {
                    showMessage(languageServer, formattedMessage.toString());
                });
            }

            @Override
            public void handleStoppingLanguageServer(LanguageServerWrapper languageServer) {
                executeInUI(() -> {
                    LanguageServerTreeNode node = findLanguageServerTreeNode(languageServer);
                    if (node != null) {
                        LanguageServerProcessTreeNode process = node.getActiveProcessTreeNode();
                        process.setUserObject("stopping...");
                        ((DefaultTreeModel)tree.getModel()).reload(process);
                    }
                });
            }

            @Override
            public void handleStoppedLanguageServer(LanguageServerWrapper languageServer, Throwable exception) {
                executeInUI(() -> {
                    LanguageServerTreeNode node = findLanguageServerTreeNode(languageServer);
                    if (node != null) {
                        LanguageServerProcessTreeNode process = node.getActiveProcessTreeNode();
                        process.setUserObject("stopped");
                        ((DefaultTreeModel)tree.getModel()).reload(process);
                    }
                });
            }
        };
    }

    private LanguageServerTreeNode findLanguageServerTreeNode(LanguageServerWrapper languageServer) {
        DefaultMutableTreeNode top = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i < top.getChildCount(); i++) {
            LanguageServerTreeNode node = (LanguageServerTreeNode) top.getChildAt(i);
            if (node.getServerDefinition().equals(languageServer.serverDefinition)) {
                return node;
            }
        }
        return null;
    }

    private void showMessage(LanguageServerWrapper languageServer, String message) {
        LanguageServerTreeNode node = findLanguageServerTreeNode(languageServer);
        if (node == null) {
            return;
        }
        var processTreeNode = node.getActiveProcessTreeNode();
        if (processTreeNode == null) {
            processTreeNode = new LanguageServerProcessTreeNode(languageServer);
            node.add(processTreeNode);
            processTreeNode.setUserObject("started");
        }
        panel.showMessage(processTreeNode, message);
    }

    private void onLanguageServerSelected(LanguageServerProcessTreeNode processTreeNode) {
        panel.selectConsole(processTreeNode);
    }


    private void showEmptyConsole() {
        panel.showEmptyConsole();
    }

    /**
     * Builds the Language server tree
     *
     * @return Tree object of all language servers
     */
    private Tree buildTree() {

        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Language servers");

        Tree tree = new Tree(top);
        tree.setRootVisible(false);

        // Fill tree will all language server definitions
        LanguageServersRegistry.getInstance().getAllDefinitions()
                .forEach(serverDefinition -> top.add(new LanguageServerTreeNode(serverDefinition)));

        tree.addTreeSelectionListener(l -> {
            TreePath selectionPath = tree.getSelectionPath();
            Object selectedItem = selectionPath != null ? selectionPath.getLastPathComponent() : null;
            if (selectedItem instanceof LanguageServerProcessTreeNode) {
                LanguageServerProcessTreeNode node = (LanguageServerProcessTreeNode) selectedItem;
                onLanguageServerSelected(node);
            } else {
                showEmptyConsole();
            }
        });

        ((DefaultTreeModel)tree.getModel()).reload(top);
        return tree;
    }

    public Tree getTree() {
        return tree;
    }

    public void dispose() {
        LanguageServerLifecycleManager.getInstance().removeLanguageServerLifecycleListener(listener);
    }
}
