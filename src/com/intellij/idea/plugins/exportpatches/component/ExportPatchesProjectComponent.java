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

import com.intellij.idea.plugins.exportpatches.form.ExportPatchesConfigurationForm;
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
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 19.05.2010
 * Time: 15:35:33
 * To change this template use File | Settings | File Templates.
 */
public class ExportPatchesProjectComponent implements ProjectComponent, Configurable, JDOMExternalizable {
  private static final Logger LOG = Logger.getLogger(ExportPatchesProjectComponent.class);

  private String patchPath = "d:\\projects\\patches_test";
  private ExportPatchesConfigurationForm form;
  private Project project;

  public ExportPatchesProjectComponent(Project project) {
    setProject(project);
  }

  public void savePatch(ChangeList[] changeListsClicked) {

    List<String> changedFilenames = new ArrayList<String>();
    File destination = null;
    for (ChangeList changeList : changeListsClicked) {
      try {
        destination = new File(patchPath);
      }
      catch (Exception e) {
        Messages.showMessageDialog("Invalid path: " + patchPath + ". Set a valid path in the project settings!", "Error: Invalid path!",
                                   Messages.getErrorIcon());
        break;
      }

      String changeListClickedName = changeList.getName();


      Collection<Change> changeCollection = changeList.getChanges();
      final String systemSeparator = System.getProperty("file.separator");

      for (Change change : changeCollection) {
        final VirtualFile virtualFile = change.getVirtualFile();
        final String changedFilenameName = virtualFile.getName();
        final String changedFilenameExtension = virtualFile.getExtension();
        System.out.println("changedFilenameName = " + changedFilenameName);
        changedFilenames.add(changedFilenameName);
        String filepath = virtualFile.getPath();
        changedFilenames.add(filepath);

        // artifact code
        final Artifact[] artifacts = ArtifactManager.getInstance(getProject()).getArtifacts();
        for (Artifact artifact : artifacts) {
          final String artifactOutputPath = artifact.getOutputPath();
          File startingDir = new File(artifactOutputPath);
          final String filterFilename = changedFilenameName.substring(0, changedFilenameName.lastIndexOf("."));
          final FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
              if(name.contains(".")) {
                final String nameWithoutExtension = name.substring(0, name.lastIndexOf("."));
                if(nameWithoutExtension.equals(filterFilename)) {
                  return true;
                }
              }
              return false;
            }
          };
          File[] results = listFilesAsArray(startingDir, filenameFilter, true);

          for (File file : results) {
            String artifactFilePath = file.getAbsolutePath().replace(systemSeparator, "/");
            String s = artifactFilePath.replace(artifactOutputPath, "");

            try {

              String finalPath = patchPath.replace(systemSeparator, "/") + s;
              finalPath = finalPath.substring(0, finalPath.lastIndexOf("/"));
              finalPath += "/" + changedFilenameName;
              System.out.println("finalPath = " + finalPath);
              System.out.println("fromPath = " + filepath);
              copy(filepath, finalPath);
            }
            catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }

      Messages.showMessageDialog("Changelist '" + changeListClickedName + "' exported!", "Export ok", Messages.getInformationIcon());
    }
  }

  public static File[] listFilesAsArray(File directory, FilenameFilter filter, boolean recurse) {
    Collection<File> files = listFiles(directory, filter, recurse);
    //Java4: Collection files = listFiles(directory, filter, recurse);

    File[] arr = new File[files.size()];
    return files.toArray(arr);
  }

  public static Collection<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
    // List of files / directories
    Vector<File> files = new Vector<File>();
    // Java4: Vector files = new Vector();

    // Get files / directories in the directory
    File[] entries = directory.listFiles();

    // Go over entries
    for (File entry : entries) {
    // Java4: for (int f = 0; f < files.length; f++) {
    // Java4: 	File entry = (File) files[f];

      // If there is no filter or the filter accepts the
      // file / directory, add it to the list
      if (filter == null || filter.accept(directory, entry.getName())) {
        files.add(entry);
      }

      // If the file is a directory and the recurse flag
      // is set, recurse into the directory
      if (recurse && entry.isDirectory()) {
        files.addAll(listFiles(entry, filter, recurse));
      }
    }

    // Return collection of files
    return files;
  }

  public static void copy(String fromFileName, String toFileName) throws IOException {
    File fromFile = new File(fromFileName);
    File toFile = new File(toFileName);

    if (!fromFile.exists()) throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
    if (!fromFile.isFile()) throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
    if (!fromFile.canRead()) throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

    if (toFile.isDirectory()) toFile = new File(toFile, fromFile.getName());

    /* if (toFile.exists()) {
     if (!toFile.canWrite()) throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
     System.out.print("Overwrite existing file " + toFile.getName() + "? (Y/N): ");
     System.out.flush();
     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
     String response = in.readLine();
     if (!response.equals("Y") && !response.equals("y")) throw new IOException("FileCopy: " + "existing file was not overwritten.");
   }
   else {*/
    String parent = toFile.getParent();
    if (parent == null) parent = System.getProperty("user.dir");
    File dir = new File(parent);
    if (!dir.exists()) dir.mkdirs(); //throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
    if (dir.isFile()) throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
    if (!dir.canWrite()) throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
    //}

    FileInputStream from = null;
    FileOutputStream to = null;
    try {
      from = new FileInputStream(fromFile);
      to = new FileOutputStream(toFile);
      byte[] buffer = new byte[4096];
      int bytesRead;

      while ((bytesRead = from.read(buffer)) != -1) to.write(buffer, 0, bytesRead); // write
    }
    finally {
      if (from != null) {
        try {
          from.close();
        }
        catch (IOException e) {
          //
        }
      }
      if (to != null) {
        try {
          to.close();
        }
        catch (IOException e) {
          //
        }
      }
    }
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
}
