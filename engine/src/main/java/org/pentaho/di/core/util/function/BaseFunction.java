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

import com.ql.util.express.Operator;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.FunctionPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public abstract class BaseFunction extends Operator implements FunctionInterface {

  public static String XML_TAG = "function";

  private String id;
  //private boolean           enabled;

//  public BaseFunction() {
//    this.enabled=false;
//  }
  
  public FunctionInterface clone() {
    try {
      return (FunctionInterface)super.clone();
    } catch(CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone function", e);
    }
  }

  //public abstract List<ImportValidationFeedback> verifyRule(Object subject);

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    
    xml.append(XMLHandler.addTagValue("id", id));
    
    return xml.toString();
  }

  public void loadXML(Node ruleNode) throws KettleException {
    id = XMLHandler.getTagValue(ruleNode, "id");
  }

  @Override
  public String toString() {
    // The function name
    String pluginId = PluginRegistry.getInstance().getPluginId(this);
    PluginInterface plugin = PluginRegistry.getInstance().findPluginWithId(FunctionPluginType.class, pluginId);
    return plugin.getName();
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * This returns the expected name for the composite that allows a base import
   * rule to be edited.
   * 
   * The expected name is in the org.pentaho.di.ui tree and has a class name
   * that is the name of the job entry with 'Composite' added to the end.
   * 
   * e.g. if the import rule class name is:
   * "org.pentaho.di.imp.rules.DatabaseConfigurationImportRule" the composite
   * then the composite class name would be:
   * "org.pentaho.di.ui.imp.rules.DatabaseConfigurationImportRuleComposite"
   * 
   * If the composite class for a job entry does not match this pattern it
   * should override this method and return the appropriate class name
   * 
   * @return full class name of the composite class
   */
  public String getCompositeClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst("\\.di\\.", ".di.ui.");
    className += "Composite";
    return className;
  }

}
