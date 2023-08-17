/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.devtools.intellij.quarkus.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.redhat.devtools.intellij.quarkus.QuarkusConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User defined Quarkus Tools settings for:
 *
 * <ul>
 *     <li>quarkus.tools.starter.api</li>
 * </ul>
 */
@State(
        name = "QuarkusToolsSettingsState",
        storages = {@Storage("quarkusToolsProfileSettings.xml")}
)
public class UserDefinedQuarkusSettings implements PersistentStateComponent<UserDefinedQuarkusSettings.MyState> {

    private volatile MyState myState = new MyState();

    private final List<Runnable> myChangeHandlers = ContainerUtil.createConcurrentList();

    public static @NotNull UserDefinedQuarkusSettings getInstance() {
        return ApplicationManager.getApplication().getService(UserDefinedQuarkusSettings.class);
    }

    public void addChangeHandler(Runnable runnable) {
        myChangeHandlers.add(runnable);
    }

    public void removeChangeHandler(Runnable runnable) {
        myChangeHandlers.remove(runnable);
    }

    public void fireStateChanged() {
        for (Runnable handler : myChangeHandlers) {
            handler.run();
        }
    }

    public String getStarterApiUrl() {
        return myState.starterApiUrl;
    }

    public void setStarterApiUrl(String starterApiUrl) {
        myState.starterApiUrl = starterApiUrl;
    }

    @Nullable
    @Override
    public MyState getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull MyState state) {
        myState = state;
        for (Runnable handler : myChangeHandlers) {
            handler.run();
        }
    }

    static class MyState {
        @Tag("starterApiUrl")
        public String starterApiUrl = QuarkusConstants.QUARKUS_CODE_URL_PRODUCTION;

        MyState() {
        }

    }

}
