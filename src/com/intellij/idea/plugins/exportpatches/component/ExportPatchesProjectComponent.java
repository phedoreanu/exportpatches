/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.idea.plugins.exportpatches.component;

import com.intellij.idea.plugins.exportpatches.controller.ExportPatchesController;
import com.intellij.idea.plugins.exportpatches.form.ExportPatchesConfigurationForm;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vcs.changes.ChangeList;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 19.05.2010
 * Time: 15:35:33
 * To change this template use File | Settings | File Templates.
 */
public class ExportPatchesProjectComponent implements ProjectComponent, Configurable, JDOMExternalizable {
  private static final Logger LOG = Logger.getLogger(ExportPatchesProjectComponent.class);

  //private String patchPath = "d:\\projects\\patches_test";
  public String patchPath;
  public boolean exportSources;

  private ExportPatchesConfigurationForm form;
  private Project project;

  public ExportPatchesProjectComponent(Project project) {
    setProject(project);
  }

  public void savePatch(ChangeList[] changeListsClicked) {

    final ExportPatchesController exportPatchesController = new ExportPatchesController(changeListsClicked, project, patchPath, exportSources);
    
    new Thread(exportPatchesController).start();
  }

  public void initComponent() {
  }

  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  @Nls
  public String getDisplayName() {
    return "Export Patches";
  }

  public Icon getIcon() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getHelpTopic() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public JComponent createComponent() {
    if (form == null) {
      form = new ExportPatchesConfigurationForm();
    }
    return form.getRootComponent();
  }

  public boolean isModified() {
    return form != null && form.isModified(this);
  }

  public void apply() throws ConfigurationException {
    if (form != null) {
      form.getData(this);
    }
  }

  public void reset() {
    if (form != null) {
      form.setData(this);
    }
  }

  public void disposeUIResources() {
    form = null;
  }

  @NotNull
  public String getComponentName() {
    return "ExportPatchesProjectComponent";
  }

  public void projectOpened() {

  }

  public void projectClosed() {
    // called when project is being closed
  }

  public String getPatchPath() {
    return patchPath;
  }

  public void setPatchPath(final String patchPath) {
    this.patchPath = patchPath;
  }

  public void readExternal(Element element) throws InvalidDataException {
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public boolean isExportSources() {
    return exportSources;
  }

  public void setExportSources(final boolean exportSources) {
    this.exportSources = exportSources;
  }
}
