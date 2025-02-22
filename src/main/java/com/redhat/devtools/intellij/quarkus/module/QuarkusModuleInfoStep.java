/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.quarkus.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.quarkus.QuarkusConstants;
import com.redhat.devtools.intellij.quarkus.tool.ToolDelegate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Arrays;

public class QuarkusModuleInfoStep extends ModuleWizardStep implements Disposable {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusModuleInfoStep.class);

    private final JBLoadingPanel panel = new JBLoadingPanel(new BorderLayout(), this, 300);

    private ComboBox<QuarkusStream> streamComboBox;

    private ComboBox<ToolDelegate> toolComboBox;

    private JBCheckBox exampleField;

    private JBTextField groupIdField;

    private JBTextField artifactIdField;

    private JBTextField versionField;

    private JBTextField classNameField;

    private JBTextField pathField;

    private final WizardContext context;

    private QuarkusModel model;

    private QuarkusExtensionsModel extensionsModel;

    public QuarkusModuleInfoStep(WizardContext context) {
        this.context = context;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {
        context.putUserData(QuarkusConstants.WIZARD_TOOL_KEY, (ToolDelegate)toolComboBox.getModel().getSelectedItem());
        context.putUserData(QuarkusConstants.WIZARD_EXAMPLE_KEY, exampleField.isSelected());
        context.putUserData(QuarkusConstants.WIZARD_GROUPID_KEY, groupIdField.getText());
        context.putUserData(QuarkusConstants.WIZARD_ARTIFACTID_KEY, artifactIdField.getText());
        context.putUserData(QuarkusConstants.WIZARD_VERSION_KEY, versionField.getText());
        context.putUserData(QuarkusConstants.WIZARD_CLASSNAME_KEY, classNameField.getText());
        context.putUserData(QuarkusConstants.WIZARD_PATH_KEY, pathField.getText());
        if (extensionsModel != null) {
            context.putUserData(QuarkusConstants.WIZARD_EXTENSIONS_MODEL_KEY, extensionsModel);
        } else {
            throw new RuntimeException("Unable to get extensions");
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void _init() {
        panel.setBorder(JBUI.Borders.empty(20));

        ProgressIndicator indicator = new EmptyProgressIndicator() {
            @Override
            public void setText(String text) {
                SwingUtilities.invokeLater(() -> panel.setLoadingText(text));
            }
        };
        try {
            model = QuarkusModelRegistry.INSTANCE.load(context.getUserData(QuarkusConstants.WIZARD_ENDPOINT_URL_KEY), indicator);
            final FormBuilder formBuilder = new FormBuilder();
            final CollectionComboBoxModel<QuarkusStream> streamModel = new CollectionComboBoxModel<>(model.getStreams());
            streamModel.setSelectedItem(model.getStreams().stream().filter(QuarkusStream::isRecommended).findFirst().orElse(model.getStreams().get(0)));
            streamModel.addListDataListener(new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent e) {
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                    try {
                        loadExtensionsModel(streamModel, indicator);
                    } catch (IOException ex) {
                        LOGGER.warn(ex.getLocalizedMessage(), ex);
                    }
                }
            });
            loadExtensionsModel(streamModel, indicator);
            streamComboBox = new ComboBox<>(streamModel);
            streamComboBox.setRenderer(new ColoredListCellRenderer<QuarkusStream>() {
                @Override
                protected void customizeCellRenderer(@NotNull JList<? extends QuarkusStream> list, QuarkusStream stream, int index, boolean selected, boolean hasFocus) {
                    if (stream.isRecommended()) {
                        this.append(stream.getPlatformVersion(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, true);
                    } else {
                        this.append(stream.getPlatformVersion(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    }
                    if (stream.getStatus() != null) {
                        this.append(" ").append(stream.getStatus());
                    }
                }
            });
            formBuilder.addLabeledComponent("Quarkus stream:", streamComboBox);
            final CollectionComboBoxModel<ToolDelegate> toolModel = new CollectionComboBoxModel<>(Arrays.asList(ToolDelegate.getDelegates()));
            toolComboBox = new ComboBox<>(toolModel);
            toolComboBox.setRenderer(new ColoredListCellRenderer<ToolDelegate>() {
                @Override
                protected void customizeCellRenderer(@NotNull JList<? extends ToolDelegate> list, ToolDelegate toolDelegate, int index, boolean selected, boolean hasFocus) {
                    this.append(toolDelegate.getDisplay());
                }
            });
            formBuilder.addLabeledComponent("Tool:", toolComboBox);
            exampleField = new JBCheckBox("If selected, project will contain sample code from extensions that suppport codestarts.", true);
            formBuilder.addLabeledComponent("Example code:", exampleField);
            groupIdField = new JBTextField("org.acme");
            formBuilder.addLabeledComponent("Group:", groupIdField);
            artifactIdField = new JBTextField("code-with-quarkus");
            formBuilder.addLabeledComponent("Artifact:", artifactIdField);
            versionField = new JBTextField("1.0.0-SNAPSHOT");
            formBuilder.addLabeledComponent("Version:", versionField);
            classNameField = new JBTextField("org.acme.ExampleResource");
            formBuilder.addLabeledComponent("Class name:", classNameField);
            pathField = new JBTextField("/hello");
            formBuilder.addLabeledComponent("Path:", pathField);
            panel.add(ScrollPaneFactory.createScrollPane(formBuilder.getPanel(), true), "North");
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void loadExtensionsModel(CollectionComboBoxModel<QuarkusStream> streamModel, ProgressIndicator indicator) throws IOException {
        extensionsModel = model.getExtensionsModel(((QuarkusStream) streamModel.getSelectedItem()).getKey(), indicator);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (groupIdField.getText().isEmpty()) {
            throw new ConfigurationException("Group must be specified");
        }
        if (artifactIdField.getText().isEmpty()) {
            throw new ConfigurationException("Artifact must be specified");
        }
        if (versionField.getText().isEmpty()) {
            throw new ConfigurationException("Version must be specified");
        }
        if (extensionsModel == null) {
            throw new ConfigurationException("Unable to get extensions for this stream");
        }
        return true;
    }
}
