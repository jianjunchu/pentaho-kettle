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

package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public interface StepIOMetaInterface {

  boolean isInputAcceptor();

  boolean isOutputProducer();

  boolean isInputOptional();

  boolean isSortedDataRequired();

  List<StreamInterface> getInfoStreams();

  List<StreamInterface> getTargetStreams();

  String[] getInfoStepnames();

  String[] getTargetStepnames();

  /**
   * Replace the info steps with the supplied source steps.
   *
   * @param infoSteps
   */
  void setInfoSteps(StepMeta[] infoSteps);

  /**
   * Add a stream to the steps I/O interface
   *
   * @param stream
   *          The stream to add
   */
  void addStream(StreamInterface stream);

  /**
   * Set the general info stream description
   *
   * @param string
   *          the info streams description
   */
  void setGeneralInfoDescription(String string);

  /**
   * Set the general target stream description
   *
   * @param string
   *          the target streams description
   */
  void setGeneralTargetDescription(String string);

  /**
   * @return the generalTargetDescription
   */
  String getGeneralTargetDescription();

  /**
   * @return the generalInfoDescription
   */
  String getGeneralInfoDescription();

  /**
   * @return true if the output targets of this step are dynamic (variable)
   */
  boolean isOutputDynamic();

  /**
   * @param outputDynamic
   *          set to true if the output targets of this step are dynamic (variable)
   */
  void setOutputDynamic(boolean outputDynamic);

  /**
   * @return true if the input info sources of this step are dynamic (variable)
   */
  boolean isInputDynamic();

  /**
   * @param inputDynamic
   *          set to true if the input info sources of this step are dynamic (variable)
   */
  void setInputDynamic(boolean inputDynamic);

  StreamInterface findTargetStream(StepMeta targetStep);

  StreamInterface findInfoStream(StepMeta infoStep);
}
