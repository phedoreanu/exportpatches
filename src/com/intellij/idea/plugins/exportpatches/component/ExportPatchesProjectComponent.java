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

import com.intellij.idea.plugins.exportpatches.ExportPatchesConfigurationForm;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 19.05.2010
 * Time: 15:35:33
 * To change this template use File | Settings | File Templates.
 */
public class ExportPatchesProjectComponent implements ProjectComponent, Configurable, JDOMExternalizable {
  private static final Logger LOG = Logger.getLogger(ExportPatchesProjectComponent.class);

  private String patchPath;
  private ExportPatchesConfigurationForm form;

  public ExportPatchesProjectComponent(Project project) {
  }

  public void savePatch(List<ChangeList> changeListsClicked) {

    List<String> changedFilenames = new ArrayList<String>();
    File file = null;
    for (ChangeList changeList : changeListsClicked) {
      try {
        file = new File(patchPath);
        if(file.isFile()) throw new IllegalArgumentException("is file");
        if(!file.isDirectory()) throw new IllegalArgumentException("!is directory");

      } catch (Exception e) {
        Messages.showMessageDialog("Invalid path: " + patchPath + " " + e.getMessage() + ". Set a valid path in the project settings!",
                                   "Error: Invalid path!",
                                   Messages.getErrorIcon());
        break;
      }

      String changeListClickedName = changeList.getName();

      Collection<Change> changeCollection = changeList.getChanges();

      for (Change change : changeCollection) {
        VirtualFile virtualFile = change.getVirtualFile();
        String name = virtualFile.getName();
        changedFilenames.add(name);
        String filepath = virtualFile.getPath();
        changedFilenames.add(filepath);
      }

      Messages.showMessageDialog(changedFilenames.toString(), changeListClickedName + " : " + file.getPath(), Messages.getInformationIcon());
    }
  }

  public void initComponent() {
  }

  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Export Patches";
  }

  @Override
  public Icon getIcon() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getHelpTopic() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public JComponent createComponent() {
    if (form == null) {
      form = new ExportPatchesConfigurationForm();
    }
    return form.getRootComponent();
  }

  @Override
  public boolean isModified() {
    return form != null && form.isModified(this);
  }

  @Override
  public void apply() throws ConfigurationException {
    if (form != null) {
      form.getData(this);
    }
  }

  @Override
  public void reset() {
    if (form != null) {
      form.setData(this);
    }
  }

  @Override
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
}
