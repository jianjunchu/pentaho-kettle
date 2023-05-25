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

package org.pentaho.di.trans.steps.valuemapperbatch;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Maps String values of a certain field to new values
 *
 * Created on 03-apr-2006
 */
@InjectionSupported( localizationPrefix = "ValueMapper.Injection.", groups = { "VALUES" } )
public class ValueMapperBatchMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ValueMapperBatchMeta.class; // for i18n purposes, needed by Translator2!!

//  @Injection( name = "FIELDNAME" )
//  private String fieldToUse;
//  @Injection( name = "TARGET_FIELDNAME" )
//  private String targetField;
//  @Injection( name = "NON_MATCH_DEFAULT" )
//  private String nonMatchDefault;

  @Injection( name = "FIELD", group = "VALUES" )
  private String[] fields;
  @Injection( name = "VALUE_MAPPERS", group = "VALUES" )
  private String[] valueMappers;
  @Injection( name = "DEFAULT", group = "VALUES" )
  private String[] defaultValues;

  public ValueMapperBatchMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFields() {
    return fields;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFields(String[] fieldName ) {
    this.fields = fieldName;
  }


  /**
   * @param defaultValues
   *          The fieldName to set.
   */
  public void setDefaultValues(String[] defaultValues ) {
    this.defaultValues = defaultValues;
  }

  /**
   * @return Returns the fieldValue.
   */
  public String[] getValueMappers() {
    return valueMappers;
  }

  public String[] getDefaultValues() {
    return defaultValues;
  }
  

  /**
   * @param fieldValue
   *          The fieldValue to set.
   */
  public void setValueMappers(String[] fieldValue ) {
    this.valueMappers = fieldValue;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    fields = new String[count];
    valueMappers = new String[count];
    defaultValues= new String[count];
  }

  @Override
  public Object clone() {
    ValueMapperBatchMeta retval = (ValueMapperBatchMeta) super.clone();

    int count = fields.length;

    retval.allocate( count );

    System.arraycopy(fields, 0, retval.fields, 0, count );
    System.arraycopy(valueMappers, 0, retval.valueMappers, 0, count );
    System.arraycopy(defaultValues, 0, retval.defaultValues, 0, count );
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {


      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        this.fields[i] = XMLHandler.getTagValue( fnode, "field_name" );
        valueMappers[i] = XMLHandler.getTagValue( fnode, "value_mapper" );
        defaultValues[i] = XMLHandler.getTagValue( fnode, "default_value" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "ValueMapperMeta.RuntimeError.UnableToReadXML.VALUEMAPPER0004" ), e );
    }
  }

  @Override
  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      fields[i] = "field" + i;
      valueMappers[i] = "";
      defaultValues[i] = "";

    }
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) {
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    <fields>" ).append( Const.CR );

    for (int i = 0; i < fields.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_name", fields[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "value_mapper", valueMappers[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "default_value", defaultValues[i] ) );

      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fields[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        valueMappers[i] = rep.getStepAttributeString( id_step, i, "value_mapper" );
        defaultValues[i] = rep.getStepAttributeString( id_step, i, "default_value" );

      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ValueMapperMeta.RuntimeError.UnableToReadRepository.VALUEMAPPER0005" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for (int i = 0; i < fields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", getNullOrEmpty( fields[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "value_mapper", getNullOrEmpty( valueMappers[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "default_value", getNullOrEmpty( defaultValues[i] ) );

      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ValueMapperMeta.RuntimeError.UnableToSaveRepository.VALUEMAPPER0006", "" + id_step ), e );
    }

  }

  private String getNullOrEmpty( String str ) {
    return str == null ? StringUtils.EMPTY : str;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.NotReceivingFieldsFromPreviousSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.ReceivingFieldsFromPreviousSteps", "" + prev.size() ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ValueMapperMeta.CheckResult.NotReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ValueMapperBatch( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new ValueMapperBatchData();
  }

  /**
   * @return Returns the fieldToUse.
   */
//  public String getFieldToUse() {
//    return fieldToUse;
//  }
//
//  /**
//   * @param fieldToUse
//   *          The fieldToUse to set.
//   */
//  public void setFieldToUse( String fieldToUse ) {
//    this.fieldToUse = fieldToUse;
//  }
//
//  /**
//   * @return Returns the targetField.
//   */
//  public String getTargetField() {
//    return targetField;
//  }
//
//  /**
//   * @param targetField
//   *          The targetField to set.
//   */
//  public void setTargetField( String targetField ) {
//    this.targetField = targetField;
//  }
//
//  /**
//   * @return the non match default. This is the string that will be used to fill in the data when no match is found.
//   */
//  public String getNonMatchDefault() {
//    return nonMatchDefault;
//  }
//
//  /**
//   * @param nonMatchDefault
//   *          the non match default. This is the string that will be used to fill in the data when no match is found.
//   */
//  public void setNonMatchDefault( String nonMatchDefault ) {
//    this.nonMatchDefault = nonMatchDefault;
//  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( fields == null ) ? -1 : fields.length;
    if ( nrFields <= 0 ) {
      return;
    }
    String[][] rtn = Utils.normalizeArrays( nrFields, valueMappers);
    valueMappers = rtn[ 0 ];
  }

}
