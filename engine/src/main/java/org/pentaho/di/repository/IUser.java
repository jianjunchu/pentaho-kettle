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

/**
 * Repository User object
 *
 * @author rmansoor
 *
 */
public interface IUser {
  /**
   * Set the login for the user
   *
   * @param login
   */
  void setLogin(String login);

  /**
   * Get the login for a the user
   *
   * @return user login
   */
  String getLogin();

  /**
   * Set the password for the
   *
   * @param password
   */
  void setPassword(String password);

  /**
   * Get the password for the user
   *
   * @return user password
   */
  String getPassword();

  /**
   * Set the user name for the user
   *
   * @param username
   */
  void setUsername(String username);

  /**
   * Get the user name for the user
   *
   * @return user name
   */
  String getUsername();

  /**
   * Set the description of the user
   *
   * @param description
   */
  void setDescription(String description);

  /**
   * Get the user's description
   *
   * @return user description
   */
  String getDescription();

  /**
   * Make the user enabled or disabled
   *
   * @param enabled
   */
  void setEnabled(boolean enabled);

  /**
   * Check if the user is enabled or not
   *
   * @return the enabled
   */
  boolean isEnabled();

  /**
   * Get the object id
   *
   * @return Object Id
   */
  ObjectId getObjectId();

  /**
   * Set the object id of this user
   *
   * @param object
   *          id
   */
  void setObjectId(ObjectId id);

  /**
   * The name of the user maps to the login id
   *
   * @return name
   */
  String getName();

  /**
   * Set the name of the user.
   *
   * @param name
   *          The name of the user maps to the login id.
   */
  void setName(String name);

  /**
   * Check if the user is admin or not
   *
   * @return admin
   */
  default Boolean isAdmin() {
    return null;
  }

  /**
   * Make the user admin or not
   * @param admin
   */
  default void setAdmin(Boolean admin) {
    // Default implementation does nothing
  }

}
