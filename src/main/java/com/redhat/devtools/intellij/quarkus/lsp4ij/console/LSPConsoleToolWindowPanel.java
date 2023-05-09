package com.redhat.devtools.intellij.quarkus.lsp4ij.console;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.CardLayoutPanel;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.quarkus.lsp4ij.LanguageServerWrapper;
import com.redhat.devtools.intellij.quarkus.lsp4ij.lifecycle.LanguageServerLifecycleListener;

import javax.swing.*;

public class LSPConsoleToolWindowPanel extends SimpleToolWindowPanel {

    private final Project project;

    private ConsolesPanel consoles;

    public LSPConsoleToolWindowPanel(Project project) {
        super(false, true);
        this.project = project;
        createUI();
    }

    private void createUI() {
        LanguageServerExplorer explorer = new LanguageServerExplorer(this);
        var scrollPane = new JBScrollPane(explorer);
        this.consoles = new ConsolesPanel();
        var splitPane = createSplitPanel(scrollPane, consoles);
        super.setContent(splitPane);
        super.revalidate();
        super.repaint();
    }

    private static JComponent createSplitPanel(JComponent left, JComponent right) {
        OnePixelSplitter splitter = new OnePixelSplitter(false, 0.15f);
        splitter.setShowDividerControls(true);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.setFirstComponent(left);
        splitter.setSecondComponent(right);
        return splitter;
    }

    public void selectConsole(LanguageServerProcessTreeNode processTreeNode) {
        if (consoles == null) {
            return;
        }
        consoles.select(processTreeNode, true);
    }

    public void showEmptyConsole() {
        consoles.showEmptyContent();
    }

    /**
     * A card-panel that displays panels for each language server instances.
     */
    private class ConsolesPanel extends CardLayoutPanel<LanguageServerProcessTreeNode, LanguageServerProcessTreeNode, LSPConsoleToolWindowPanel.ConsoleOrErrorPanel> {

        @Override
        protected LanguageServerProcessTreeNode prepare(LanguageServerProcessTreeNode key) {
            return key;
        }

        @Override
        protected LSPConsoleToolWindowPanel.ConsoleOrErrorPanel create(LanguageServerProcessTreeNode key) {
            return new LSPConsoleToolWindowPanel.ConsoleOrErrorPanel();
        }

        @Override
        public void dispose() {
            removeAll();
        }

        @Override
        protected void dispose(LanguageServerProcessTreeNode key, LSPConsoleToolWindowPanel.ConsoleOrErrorPanel value) {
            if (value != null) {
                value.dispose();
            }
        }

        public void showEmptyContent() {
        }
    }

    private class ConsoleOrErrorPanel extends SimpleCardLayoutPanel<JComponent> {

        private static final String NAME_VIEW_CONSOLE = "console";
        private static final String NAME_VIEW_EMPTY = "empty";

        private final ConsoleView consoleView;

        public ConsoleOrErrorPanel() {
            consoleView = createConsoleView(project);
            if (consoleView != null) {
                add(consoleView.getComponent(), NAME_VIEW_CONSOLE);
            }
            add(new JPanel(), NAME_VIEW_EMPTY);
            showConsole();
        }

        private void showConsole() {
            show(NAME_VIEW_CONSOLE);
        }

        public void showEmptyContent() {
            show(NAME_VIEW_EMPTY);
        }

        public void showMessage(String message) {
            consoleView.print(message, ConsoleViewContentType.SYSTEM_OUTPUT);
        }
    }

    private ConsoleView createConsoleView(Project project) {
        var builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.setViewer(true);
        return builder.getConsole();
    }

    public void showMessage(LanguageServerProcessTreeNode processTreeNode, String message) {
        var consoleOrErrorPanel = consoles.getValue(processTreeNode, false);
        if (consoleOrErrorPanel != null) {
            consoleOrErrorPanel.showMessage(message);
        }
    }

}
