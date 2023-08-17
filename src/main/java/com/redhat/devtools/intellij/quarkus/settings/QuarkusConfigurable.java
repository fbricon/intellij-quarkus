package com.redhat.devtools.intellij.quarkus.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.NlsContexts;

import javax.swing.*;
import java.util.Objects;

public class QuarkusConfigurable extends NamedConfigurable<UserDefinedQuarkusSettings> {

    private QuarkusView myView;

    @Override
    public UserDefinedQuarkusSettings getEditableObject() {
        return UserDefinedQuarkusSettings.getInstance();
    }

    @Override
    public @NlsContexts.DetailedDescription String getBannerSlogan() {
        return null;
    }

    @Override
    public JComponent createOptionsPanel() {
        if (myView == null) {
            myView = new QuarkusView();
        }
        return myView.getComponent();
    }

    @Override
    public void setDisplayName(String name) {
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return QuarkusBundle.message("quarkus.tools");
    }

    @Override
    public void reset() {
        if (myView == null) return;
        UserDefinedQuarkusSettings settings = UserDefinedQuarkusSettings.getInstance();
        myView.setStarterApiUrl(settings.getStarterApiUrl());
    }

    @Override
    public boolean isModified() {
        if (myView == null) return false;
        UserDefinedQuarkusSettings settings = UserDefinedQuarkusSettings.getInstance();
        return !Objects.equals(settings.getStarterApiUrl(), myView.getStarterApiUrl());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (myView == null) return;
        UserDefinedQuarkusSettings settings = UserDefinedQuarkusSettings.getInstance();
        settings.setStarterApiUrl(myView.getStarterApiUrl());
        settings.fireStateChanged();
    }
}
