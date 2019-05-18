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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.FunctionPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class Functions implements Cloneable {

  public static final String XML_TAG = "functions";

  protected List<FunctionInterface> functions;

  public Functions() {
    functions = new ArrayList<FunctionInterface>();
  }
  
  /**
   * Perform a deep clone
   * @return a deep copy of the all functions.
   */
  @Override
  public Functions clone() {

    Functions functions = new Functions();
    
    for (FunctionInterface function : this.functions) {
      functions.getFunctions().add( function.clone() );
    }
    
    return functions;
  }
  
//  public List<ImportValidationFeedback> verifyRules(Object subject) {
//    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();
//
//    for (FunctionInterface rule : rules) {
//      feedback.addAll(rule.verifyRule(subject));
//    }
//
//    return feedback;
//  }
  
  public void loadXML(Node functionsNode) throws KettleException {
    List<Node> functionNodes = XMLHandler.getNodes(functionsNode, BaseFunction.XML_TAG);
    for (Node functionNode : functionNodes) {
      String id = XMLHandler.getTagValue(functionNode, "id");

      PluginRegistry registry = PluginRegistry.getInstance();

      PluginInterface plugin = registry.findPluginWithId(FunctionPluginType.class, id);
      if (plugin==null) {
        throw new KettleException("The function of id '"+id+"' could not be found in the plugin registry.");
      }
      FunctionInterface function = (FunctionInterface) registry.loadClass(plugin);

      function.loadXML(functionNode);

      getFunctions().add(function);
    }
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    
    xml.append( XMLHandler.openTag(XML_TAG) ).append(Const.CR).append(Const.CR);
    
    for (FunctionInterface rule : getFunctions()) {
      
      PluginInterface plugin = PluginRegistry.getInstance().getPlugin(FunctionPluginType.class, rule.getId());
      xml.append("<!-- ").append(plugin.getName()).append(" : ").append(plugin.getDescription()).append(Const.CR).append(" -->").append(Const.CR);
      
      xml.append(rule.getXML());
      xml.append(Const.CR).append(Const.CR);
    }
    
    xml.append( XMLHandler.closeTag(XML_TAG) ) ;

    return xml.toString();
  }

  public List<FunctionInterface> getFunctions() {
    return functions;
  }

  public void setFunctions(List<FunctionInterface> rules) {
    this.functions = rules;
  }

}
