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
package com.intellij.idea.plugins.exportpatches.controller;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 08.07.2010
 * Time: 15:22:14
 * To change this template use File | Settings | File Templates.
 */
public class ExportPatchesController implements Runnable {

  private ChangeList[] changeListsClicked;
  private Project project;
  public String patchPath;
  public boolean exportSources;

  public ExportPatchesController(ChangeList[] changeListsClicked, Project project, String patchPath, boolean exportSources) {
    this.changeListsClicked = changeListsClicked;
    this.project = project;
    this.patchPath = patchPath;
    this.exportSources = exportSources;
  }

  public void run() {
    savePatches();
  }

  public void savePatches() {
    Map<String, Boolean> confirmations = new HashMap<String, Boolean>();
    File destination = null;
    for (ChangeList changeList : changeListsClicked) {

      if (StringUtils.isEmpty(patchPath)) {
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              Messages.showMessageDialog("Invalid path: " + patchPath + ". Set a valid path in the project settings!", "Error: Invalid path!",
                                         Messages.getErrorIcon());
            }
          });
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        break;
      }

      final String changeListClickedName = changeList.getName();

      final String projectBaseDir = project.getBaseDir().getPath();
      final VirtualFile compilerOutputVirtualFile = CompilerProjectExtension.getInstance(project).getCompilerOutput();
      final String compilerOutputPath = compilerOutputVirtualFile.getPath();

      final Collection<Change> changeCollection = changeList.getChanges();
      final String systemSeparator = System.getProperty("file.separator");

      for (Change change : changeCollection) {
        final VirtualFile changedFile = change.getVirtualFile();
        final String changedFileName = changedFile.getName();
        final String changedFileExtension = changedFile.getExtension();
        final String changedFileFilepath = changedFile.getPath();
        final String changedFileParent = changedFile.getParent().getName();
        //System.out.println("changedFileName = " + changedFileName);
        final String filterFilename = changedFile.getNameWithoutExtension();
        confirmations.put(filterFilename, true);

        final Artifact[] artifacts = ArtifactManager.getInstance(project).getArtifacts();
        for (Artifact artifact : artifacts) {
          final String artifactOutputPath = artifact.getOutputPath();

          final FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
              final String name = file.getName();
              final String parent = StringUtils.substringAfterLast(file.getParent(), systemSeparator);
              if (name.contains(".")) {
                if (parent.equals(changedFileParent)) {
                  final String nameWithoutExtension = name.substring(0, name.lastIndexOf("."));
                  if ("java".equals(changedFileExtension) && nameWithoutExtension.startsWith(filterFilename)) return true;

                  if (name.equals(changedFileName)) return true;
                }
                else if (!"jsp".equals(changedFileExtension) && name.equals(changedFileName)) {
                  return true;
                }
              }
              return false;
            }
          };

          File startingDir = new File(artifactOutputPath);
          File[] results = listFilesAsArray(startingDir, fileFilter, true);
          for (File file : results) {
            boolean isJavaFile = false;
            String outputPrefix = "";
            String artifactFilePath = file.getPath().replace(systemSeparator, "/");
            if (artifactFilePath.contains(compilerOutputPath)) outputPrefix = artifactFilePath.replace(compilerOutputPath, "");
            outputPrefix = outputPrefix.substring(0, outputPrefix.lastIndexOf("/"));

            if ("java".equals(changedFileExtension)) {
              outputPrefix += "/" + file.getName();
              isJavaFile = true;
            }
            else {
              outputPrefix += "/" + changedFileName;
            }

            String destinationPath = patchPath.replace(systemSeparator, "/") + outputPrefix;

            if (exportSources) {
              if (patchPath.endsWith(systemSeparator)) patchPath = patchPath.substring(0, patchPath.length() - 1);
              String sourcesDestinationPath = patchPath.replace(systemSeparator, "/") + "_src";
              final File sourcesPath = new File(sourcesDestinationPath);
              if (!sourcesPath.exists()) sourcesPath.mkdirs();

              String sourcePrefix = "";
              if (changedFileFilepath.contains(projectBaseDir)) sourcePrefix = changedFileFilepath.replaceAll(projectBaseDir, "");

              sourcesDestinationPath += sourcePrefix;

              try {
                copy(changedFileFilepath, sourcesDestinationPath);
              }
              catch (IOException e) {
                //
              }
            }

            //System.out.println("changedFileFilepath = " + changedFileFilepath);
            //System.out.println("destinationPath = " + destinationPath);

            try {
              if (isJavaFile) {
                copy(artifactFilePath, destinationPath, true, confirmations);
              }
              else {
                copy(changedFileFilepath, destinationPath, true, confirmations);
              }
            }
            catch (IOException e) { /**/
            }
          }
        }
      }

      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            Messages.showMessageDialog("Changelist '" + changeListClickedName + "' exported!", "Export ok", Messages.getInformationIcon());
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static File[] listFilesAsArray(File directory, FileFilter filter, boolean recurse) {
    Collection<File> files = listFiles(directory, filter, recurse);
    //Java4: Collection files = listFiles(directory, filter, recurse);

    File[] arr = new File[files.size()];
    return files.toArray(arr);
  }

  public static Collection<File> listFiles(File directory, FileFilter filter, boolean recurse) {
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
      if (filter == null || filter.accept(entry)) {
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

  public void copy(String fromFileName, String toFileName) throws IOException {
    copy(fromFileName, toFileName, false, null);
  }

  public void copy(final String fromFileName, final String toFileName, boolean promtForOveright, Map<String, Boolean> confirmations)
    throws IOException {
    final File fromFile = new File(fromFileName);
    File toFile = new File(toFileName);

    if (!fromFile.exists()) throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
    if (!fromFile.isFile()) throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
    if (!fromFile.canRead()) throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

    if (toFile.isDirectory()) toFile = new File(toFile, fromFile.getName());

    final String toFilename = toFile.getName();
    final String absolutePath = toFile.getAbsolutePath();

    Boolean showConfirmation = false;
    if (confirmations != null) {
      final String sourceFilename = fromFile.getName();
      final String sourceFilenameWithoutExtension = sourceFilename.substring(0, sourceFilename.lastIndexOf("."));
      showConfirmation = confirmations.get(sourceFilenameWithoutExtension);
      if (showConfirmation != null && showConfirmation) confirmations.put(sourceFilenameWithoutExtension, false);
    }

    if (toFile.exists() && promtForOveright && showConfirmation != null && showConfirmation) {
      if (!toFile.canWrite()) throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);

      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            final int i = Messages
              .showOkCancelDialog("Overwrite existing file '" + toFilename + " ?", "Overwrite existing file ?", Messages.getQuestionIcon());

            // 1 = CANCEL
            if (i == 1) {
              return;
            }

            try {
              copy(new File(absolutePath), fromFile);
            }
            catch (IOException e) {
              Messages
                .showOkCancelDialog("Error copying '" + toFilename + " !", "Error copying file", Messages.getErrorIcon());
            }
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      copy(toFile, fromFile);
    }
  }

  public void copy(File toFile, File fromFile) throws IOException {
    //else {
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
        catch (IOException e) { /**/}
      }
      if (to != null) {
        try {
          to.close();
        }
        catch (IOException e) { /**/}
      }
    }
  }
}
