/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.psi.internal.quarkus.renarde.java;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.redhat.devtools.intellij.lsp4mp4ij.psi.core.utils.PsiTypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.redhat.devtools.intellij.lsp4ij.LSPIJUtils.getProject;


/**
 * Utilities for working with Renarde applications.
 */
public class RenardeUtils {

	private RenardeUtils() {
	}

	/**
	 * Returns true if the given class extends Renarde's <code>Controller</code>
	 * class and false otherwise.
	 *
	 * @param psiClass the class to check
	 * @return true if the given class extends Renarde's <code>Controller</code>
	 *         class and false otherwise
	 */
	public static boolean isControllerClass(@Nullable PsiClass psiClass) {
		if (psiClass == null) {
			return false;
		}
		Module project = ModuleUtilCore.findModuleForPsiElement(psiClass);
		return isControllerClass(project, psiClass);
	}


	/**
	 * Returns true if the given class extends Renarde's <code>Controller</code>
	 * class and false otherwise.
	 *
	 * @param project  the project that the class to check is in
	 * @param typeRoot the class (compilation unit or class file) to check
	 * @param monitor  the progress monitor
	 * @return true if the given class extends Renarde's <code>Controller</code>
	 *         class and false otherwise
	 */
	public static boolean isControllerClass(Module project, PsiFile typeRoot, ProgressIndicator monitor) {
		PsiClass type = PsiTreeUtil.getChildOfType(typeRoot, PsiClass.class);
		return isControllerClass(project, type);
	}

	public static boolean isControllerClass(@NotNull Module project, @Nullable PsiClass type) {
		if (type == null || type.isEnum() || type.isInterface() || type.isAnnotationType() || type.isRecord()) {
			return false;
		}
		PsiClass renardeControllerType = PsiTypeUtils.findType(project, RenardeConstants.CONTROLLER_FQN);
		if (renardeControllerType == null) {
			// The project is not a renarde project
			return false;
		}
		// Check if the current type extends "io.quarkiverse.renarde.Controller".
		return type.isInheritor(renardeControllerType,true);
	}

	/**
	 * Returns a set of all classes in the given project that extend Renarde's
	 * <code>Controller</code> class.
	 *
	 * @param project the project to search in
	 * @param monitor the progress monitor
	 * @return a set of all classes in the given project that extend Renarde's
	 *         <code>Controller</code> class
	 */
	public static Set<PsiClass> getAllControllerClasses(Module project, ProgressIndicator monitor) {
		// TODO: implement when LSP4IJ will support workspace symbols
		return Collections.emptySet();
	}

}