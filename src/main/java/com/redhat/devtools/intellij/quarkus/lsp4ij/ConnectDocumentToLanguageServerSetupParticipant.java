/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.quarkus.lsp4ij;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Track file opened / closed to start language servers / disconnect file from language servers.
 */
public class ConnectDocumentToLanguageServerSetupParticipant implements ProjectComponent, FileEditorManagerListener {

    private final Project project;

    public ConnectDocumentToLanguageServerSetupParticipant(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }

    @Override
    public void projectClosed() {
        LanguageServiceAccessor.getInstance(project).shutdownAllDispatchers();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            // Force the start of all languages servers mapped with the given file
            LanguageServiceAccessor.getInstance(source.getProject())
                    .getLanguageServers(document, capabilities -> true);
        }
    }

}
