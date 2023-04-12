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

package org.pentaho.di.repository;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public interface RepositoryImportFeedbackInterface {

  void addLog(String line);

  void setLabel(String labelText);

  void updateDisplay();

  boolean transOverwritePrompt(TransMeta transMeta);

  boolean jobOverwritePrompt(JobMeta jobMeta);

  void showError(String title, String message, Exception e);

  boolean askContinueOnErrorQuestion(String title, String message);

  boolean isAskingOverwriteConfirmation();
}
