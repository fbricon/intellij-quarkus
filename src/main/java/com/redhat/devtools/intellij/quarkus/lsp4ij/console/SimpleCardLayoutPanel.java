/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 * based on com.intellij.ui.CardLayoutPanel
 ******************************************************************************/

package com.redhat.devtools.intellij.quarkus.lsp4ij.console;

import com.intellij.openapi.Disposable;
import com.intellij.util.ui.JBInsets;

import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JPanel;

abstract class SimpleCardLayoutPanel<V extends JComponent> extends JPanel implements Disposable {

    protected volatile boolean isDisposed = false;

    private CardLayout cardLayout;

    public SimpleCardLayoutPanel() {
        this(new CardLayout());
    }

    public SimpleCardLayoutPanel(CardLayout cardLayout) {
        super(cardLayout);
        this.cardLayout = cardLayout;
    }

    private Component visibleComponent() {
        for (var component : getComponents()) {
            if (component.isVisible()) return component;
        }
        return null;
    }

    public void show(String name) {
        cardLayout.show(this, name);
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            removeAll();
        }
    }

    @Override
    public void doLayout() {
        var bounds = new Rectangle(getWidth(), getHeight());
        JBInsets.removeFrom(bounds, getInsets());
        for (var component : getComponents()) {
            component.setBounds(bounds);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        var component = isPreferredSizeSet() ? null : visibleComponent();
        if (component == null) {
            return super.getPreferredSize();
        }
        // preferred size of a visible component plus border insets of this panel
        var size = component.getPreferredSize();
        JBInsets.addTo(size, getInsets()); // add border of this panel
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        var component = isMinimumSizeSet() ? null : visibleComponent();
        if (component == null) {
            return super.getMinimumSize();
        }
        // minimum size of a visible component plus border insets of this panel
        var size = component.getMinimumSize();
        JBInsets.addTo(size, getInsets());
        return size;
    }
}