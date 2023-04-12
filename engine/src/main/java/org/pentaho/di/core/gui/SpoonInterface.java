/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.gui;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public interface SpoonInterface extends OverwritePrompter {

  int STATE_CORE_OBJECTS_NONE = 1; // No core objects
  int STATE_CORE_OBJECTS_CHEF = 2; // Chef state: job entries
  int STATE_CORE_OBJECTS_SPOON = 3; // Spoon state: steps

  String XUL_FILE_MENUBAR = "ui/menubar.xul";

  String XUL_FILE_MENUS = "ui/menus.xul";

  String XUL_FILE_MENU_PROPERTIES = "ui/menubar.properties";

  boolean addSpoonBrowser(String name, String urlString);

  void addTransGraph(TransMeta transMeta);

  void addJobGraph(JobMeta jobMeta);

  Object[] messageDialogWithToggle(String dialogTitle, Object image, String message, int dialogImageType,
                                   String[] buttonLabels, int defaultIndex, String toggleMessage, boolean toggleState);

  boolean messageBox(String message, String text, boolean allowCancel, int type);
}
