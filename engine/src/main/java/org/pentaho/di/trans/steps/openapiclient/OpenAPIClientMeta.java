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

package org.pentaho.di.trans.steps.openapiclient;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Run open api .
 */

public class OpenAPIClientMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = OpenAPIClientMeta.class; // for i18n purposes, needed by Translator2!!

  private String APIServer;
  private String methodName;
  private String accessKey;
  private String secret;
  private String uid;
  private String bindId;
  private String boName;
  private String batchNumber;

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      APIServer = XMLHandler.getTagValue( stepnode, "apiserver" );
      methodName = XMLHandler.getTagValue( stepnode, "methodName" );
      accessKey = XMLHandler.getTagValue( stepnode, "accessKey" );
      secret = XMLHandler.getTagValue( stepnode, "secret" );
      uid = XMLHandler.getTagValue( stepnode, "uid" );
      bindId = XMLHandler.getTagValue( stepnode, "bindId" );
      boName = XMLHandler.getTagValue( stepnode, "boName" );
      batchNumber = XMLHandler.getTagValue( stepnode, "batchNumber" );

    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "GetSequenceMeta.Exception.ErrorLoadingStepInfo" ), e );
    }
  }

  @Override
  public void setDefault() {
    APIServer = "http://localhost:8088/portal/openapi";
    methodName = "bo.creates";
    accessKey = "";
    secret = "";
    bindId = "";
    uid = "";
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaInterface v = new ValueMetaString( "response" );
    v.setOrigin( name );
    row.addValueMeta( v );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "      " ).append( XMLHandler.addTagValue( "apiserver", APIServer ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "methodName", methodName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "accessKey", accessKey ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "secret", secret ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "uid", uid ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "bindId", bindId ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "boName", boName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "batchNumber", batchNumber ) );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      APIServer = rep.getStepAttributeString( id_step, "apiserver" );
      methodName = rep.getStepAttributeString( id_step, "methodName" );
      accessKey = rep.getStepAttributeString( id_step, "accessKey" );
      secret = rep.getStepAttributeString( id_step, "secret" );
      uid = rep.getStepAttributeString( id_step, "uid" );
      bindId = rep.getStepAttributeString( id_step, "bindId" );
      boName = rep.getStepAttributeString( id_step, "boName" );
      batchNumber = rep.getStepAttributeString( id_step, "batchNumber" );


    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetSequenceMeta.Exception.UnableToReadStepInfo" )
        + id_step, e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "apiserver", APIServer );
      rep.saveStepAttribute( id_transformation, id_step, "methodName", methodName );
      rep.saveStepAttribute( id_transformation, id_step, "accessKey", accessKey );
      rep.saveStepAttribute( id_transformation, id_step, "secret", secret );
      rep.saveStepAttribute( id_transformation, id_step, "uid", uid );
      rep.saveStepAttribute( id_transformation, id_step, "bindId", bindId );
      rep.saveStepAttribute( id_transformation, id_step, "boName", boName );
      rep.saveStepAttribute( id_transformation, id_step, "batchNumber", batchNumber );


    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetSequenceMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "GetSequenceMeta.CheckResult.StepIsReceving.Title" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "GetSequenceMeta.CheckResult.NoInputReceived.Title" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new OpenAPIClient( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new OpenAPIClientData();
  }

  /**
   * @return the valuename
   */
  public String getAPIServer() {
    return APIServer;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setAPIServer( String name ) {
    this.APIServer = name;
  }

  /**
   * @return the slaveServerName
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @param slaveServerName
   *          the slaveServerName to set
   */
  public void setMethodName( String slaveServerName ) {
    this.methodName = slaveServerName;
  }

  /**
   * @return the accessKey
   */
  public String getAccessKey() {
    return accessKey;
  }

  /**
   * @param key
   */
  public void setAccessKey( String key ) {
    this.accessKey = key;
  }

  /**
   * @return the secret
   */
  public String getSecret() {
    return secret;
  }

  /**
   * @param secret
   *          the secret to set
   */
  public void setSecret( String secret ) {
    this.secret = secret;
  }

  /**
   * @return the uid
   */
  public String getuid() {
    return uid;
  }

  /**
   * @param uid
   *          the uid to set
   */
  public void setuid( String uid ) {
    this.uid = uid;
  }

  /**
   * @return the bindid
   */
  public String getbindId() {
    return bindId;
  }

  /**
   * @param bindId
   *          the bindid to set
   */
  public void setbindId( String bindId ) {
    this.bindId = bindId;
  }

  /**
   * @return the boname
   */
  public String getBoName() {
    return boName;
  }

  /**
   * @param boName
   *          the Boname to set
   */
  public void setBoName( String boName ) {
    this.boName = boName;
  }


  /**
   * @return the batch number
   */
  public String getBatchNumber() {
    return batchNumber;
  }

  /**
   * @param b
   *          the batch number to set
   */
  public void setBatchNumber( String b ) {
    this.batchNumber = b;
  }


}
