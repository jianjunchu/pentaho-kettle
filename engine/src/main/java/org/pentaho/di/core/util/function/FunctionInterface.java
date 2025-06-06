/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.util.function;

import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface FunctionInterface extends Cloneable {


  /**
   * @return The import function plugin ID
   */
  String getId();

  /**
   * @param string Sets the plugin ID of this import function
   */
  void setId(String string);
  
  /**
   * @return True if this function is enabled.  When not enabled this function will not be looked at.  Neither an approval nor an error will be given during validation.
   */
  //public boolean isEnabled();


  void loadXML(Node functionNode) throws KettleException;
  String getXML();
  
 // public String getCompositeClassName();
  
  FunctionInterface clone();

}

