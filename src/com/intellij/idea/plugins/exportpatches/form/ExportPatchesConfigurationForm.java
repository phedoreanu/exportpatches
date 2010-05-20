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
package com.intellij.idea.plugins.exportpatches.form;

import com.intellij.idea.plugins.exportpatches.component.ExportPatchesProjectComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 19.05.2010
 * Time: 15:28:20
 * To change this template use File | Settings | File Templates.
 */
public class ExportPatchesConfigurationForm {

  private JPanel rootComponent;
  private JTextField patchPath;
  private JLabel patchPathLabel;
  private JButton openFileChooserButton;
  private JCheckBox exportSources;

  public ExportPatchesConfigurationForm() {
    patchPathLabel.setLabelFor(patchPath);

    openFileChooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openFileChooserButton) {

          final JFileChooser fc = new JFileChooser();
          // todo project home
          //fc.setCurrentDirectory(new File(""));
          fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          int returnVal = fc.showOpenDialog(rootComponent);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            patchPath.setText(file.getPath());
          }
        }
      }
    });
  }

  public JComponent getRootComponent() {
    return rootComponent;
  }

  public void setData(ExportPatchesProjectComponent data) {
    patchPath.setText(data.getPatchPath());
    exportSources.setSelected(data.isExportSources());
  }

  public void getData(ExportPatchesProjectComponent data) {
    data.setPatchPath(patchPath.getText());
    data.setExportSources(exportSources.isSelected());
  }

  public boolean isModified(ExportPatchesProjectComponent data) {
    if (patchPath.getText() != null ? !patchPath.getText().equals(data.getPatchPath()) : data.getPatchPath() != null) return true;
    if (exportSources.isSelected() != data.isExportSources()) return true;
    return false;
  }
}
