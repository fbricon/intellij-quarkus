/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.devtools.intellij.lsp4mp4ij.psi.core;

import com.intellij.psi.PsiModifierListOwner;
import com.intellij.util.Query;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;

import java.util.List;

import static org.eclipse.lsp4mp.commons.metadata.ItemMetadata.CONFIG_PHASE_RUN_TIME;

/**
 * Properties provider for Environment variables and System properties.
 */
public class SysEnvPropertiesProvider extends AbstractPropertiesProvider {
    @Override
    protected String[] getPatterns() {
        return null;
    }

    @Override
    protected Query<PsiModifierListOwner> createSearchPattern(SearchContext context, String pattern) {
        return null;
    }

    @Override
    public void collectProperties(PsiModifierListOwner match, SearchContext context) {
        List<String[]> sysProps = System.getProperties().entrySet().stream().map(e -> {
            var tuple = new String[2];
            tuple[0] = e.getKey().toString();
            tuple[1] = e.getValue().toString();
            return tuple;
        }).toList();
        var sysPropsMetadata = getMetadata("System property", sysProps);
        context.getCollector().merge(sysPropsMetadata, IPropertiesCollector.MergingStrategy.IGNORE_IF_EXISTS);


        List<String[]> envVars = System.getenv().entrySet().stream().map(e -> {
            var tuple = new String[2];
            String key = e.getKey();
            tuple[0] = key;
            // Poor-man obfuscation of env var keys (*_KEY) and secrets (*_SECRET)
            // Maybe later add configuration for suffixes and/or actual keys to obfuscate
            tuple[1] = obfuscate(key, e.getValue());
            return tuple;
        }).toList();

        var envVarsMetadata = getMetadata("Environment variable", envVars);
        context.getCollector().merge(envVarsMetadata, IPropertiesCollector.MergingStrategy.IGNORE_IF_EXISTS);

    }

    private String obfuscate(String key, String value) {
        var upKey = key.toUpperCase().replace(".", "_");
        return upKey.endsWith("_KEY") || upKey.endsWith("_SECRET") ? "*********" : value;
    }

    private ConfigurationMetadata getMetadata(String source, List<String[]> properties) {
        ConfigurationMetadata metadata = new ConfigurationMetadata();
        List<ItemMetadata> itemProperties = properties.stream().map(kv -> {
            var item = new ItemMetadata();
            item.setExtensionName(source);
            item.setPhase(CONFIG_PHASE_RUN_TIME);
            item.setName(kv[0]);
            item.setDefaultValue(kv[1]);
            item.setType("java.lang.String");
            item.setDescription(source);
            return item;
        }).toList();

        metadata.setProperties(itemProperties);
        return metadata;
    }

}
