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
package com.intellij.idea.plugins.exportpatches;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 19.05.2010
 * Time: 10:42:56
 * To change this template use File | Settings | File Templates.
 */
public class ExportPatches {
  private static final Logger LOG = Logger.getLogger(ExportPatches.class);

  public static void showMessage(AnActionEvent e) {
    Application application = ApplicationManager.getApplication();

    DataContext dataContext = e.getDataContext();
    Project project = (Project)dataContext.getData(DataConstants.PROJECT);
    Module module = (Module)dataContext.getData(DataConstants.MODULE);

    String projectName = project.getName();
    String projectPath = project.getBaseDir().getPath();

    //ActionMenuItem source = (ActionMenuItem)e.getInputEvent().getSource();
    //System.out.println("source = " + source.getActionCommand());

    //ChangeList changeListClicked = (ChangeList)dataContext.getData(DataConstants.SELECTED_ITEM);
    //System.out.println("changelistClicked = " + changeListClicked);

    List<String> changedFilenames = new ArrayList<String>();
    ChangeList[] changeListsClicked = (ChangeList[])dataContext.getData(DataConstants.CHANGE_LISTS);
    System.out.println("changelistsClicked.length = " + changeListsClicked.length);
    for (ChangeList changeList : changeListsClicked) {
      String changelistClickedName = changeList.getName();
      System.out.println("changelistClickedName = " + changelistClickedName);

      if (changeList != null) {
        Collection<Change> changeCollection = changeList.getChanges();

        for (Change change : changeCollection) {
          VirtualFile virtualFile = change.getVirtualFile();
          String name = virtualFile.getName();
          changedFilenames.add(name);
          String path = virtualFile.getPath();
          changedFilenames.add(path);
        }
      }
    }

    ChangeListManager changeListManager = ChangeListManager.getInstance(project);

    Messages.showMessageDialog(changedFilenames.toString(), projectName, Messages.getInformationIcon());

  }
}
