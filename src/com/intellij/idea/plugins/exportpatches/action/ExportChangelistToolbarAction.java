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
package com.intellij.idea.plugins.exportpatches.action;

import com.intellij.idea.plugins.exportpatches.ExportPatches;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: afedoreanu
 * Date: 19.05.2010
 * Time: 10:39:06
 * To change this template use File | Settings | File Templates.
 */
public class ExportChangelistToolbarAction extends AnAction {
  private static final Logger LOG = Logger.getLogger(ExportChangelistToolbarAction.class);
  
  public void actionPerformed(AnActionEvent e) {

    ExportPatches.showMessage(e);
  }
}