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

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface RepositoryMeta {

  String XML_TAG = "repository";

  String getDialogClassName();

  String getRevisionBrowserDialogClassName();

  void loadXML(Node repnode, List<DatabaseMeta> databases) throws KettleException;

  String getXML();

  /**
   * @return the id
   */
  String getId();

  /**
   * @param id
   *          the id to set
   */
  void setId(String id);

  /**
   * @return the name
   */
  String getName();

  /**
   * @param name
   *          the name to set
   */
  void setName(String name);

  /**
   * @return the description
   */
  String getDescription();

  /**
   * @param description
   *          the description to set
   */
  void setDescription(String description);

  /**
   * @return the isDefault
   */
  Boolean isDefault();

  /**
   * @param isDefault
   *          the isDefault to set
   */
  void setDefault(Boolean isDefault);

  /**
   * Describes the capabilities of the repository
   *
   * @return The repository capabilities object
   */
  RepositoryCapabilities getRepositoryCapabilities();

  RepositoryMeta clone();

  void populate(Map<String, Object> properties, RepositoriesMeta repositoriesMeta);

  JSONObject toJSONObject();

  String getRep_username();

  void setRep_username(String rep_username) ;

  String getRep_password() ;

  void setRep_password(String rep_password) ;

}
