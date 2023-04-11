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

package org.pentaho.di;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public interface ExecutionConfiguration extends Cloneable {

  Object clone();

  Repository connectRepository(RepositoriesMeta repositoriesMeta, String repositoryName, String username,
                               String password) throws KettleException;

  Map<String, String> getArguments();

  void setArguments(Map<String, String> arguments);

  String[] getArgumentStrings();

  void setArgumentStrings(String[] arguments);

  LogLevel getLogLevel();

  void setLogLevel(LogLevel logLevel);

  Map<String, String> getParams();

  void setParams(Map<String, String> params);

  Long getPassedBatchId();

  void setPassedBatchId(Long passedBatchId);

  Result getPreviousResult();

  void setPreviousResult(Result previousResult);

  SlaveServer getRemoteServer();

  void setRemoteServer(SlaveServer remoteServer);

  Date getReplayDate();

  void setReplayDate(Date replayDate);

  Repository getRepository();

  void setRepository(Repository repository);

  Map<String, String> getVariables();

  void setVariables(Map<String, String> variables);

  void setVariables(VariableSpace space);

  String getXML() throws IOException;

  boolean isClearingLog();

  void setClearingLog(boolean clearingLog);

  boolean isExecutingLocally();

  void setExecutingLocally(boolean localExecution);

  boolean isExecutingRemotely();

  void setExecutingRemotely(boolean remoteExecution);

  boolean isGatheringMetrics();

  void setGatheringMetrics(boolean gatheringMetrics);

  boolean isPassingExport();

  void setPassingExport(boolean passingExport);

  boolean isSafeModeEnabled();

  void setSafeModeEnabled(boolean usingSafeMode);

  String getRunConfiguration();

  void setRunConfiguration( String runConfiguration );
}
