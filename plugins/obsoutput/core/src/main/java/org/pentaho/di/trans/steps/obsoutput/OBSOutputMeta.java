/*******************************************************************************
 *
 *
 *
 * Copyright (C) 2011-2019 by Sun : http://www.kingbase.com.cn
 *
 *******************************************************************************
 *
 *
 *    Email : snj1314@163.com
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.obsoutput;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.obs.services.ObsClient;

/**
 * 
 * 
 * @author Sun
 * @since 2019年2月25日
 * @version
 * 
 */
@Step(id = "OBSOutput", image = "OBSO.svg", i18nPackageName = "org.pentaho.di.trans.steps.obsoutput", name = "OBSOutput.Name", description = "OBSOutput.Description", categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output", documentationUrl = "http://wiki.pentaho.com/display/EAI/OBSOutput")
public class OBSOutputMeta extends TextFileOutputMeta /* BaseStepMeta implements StepMetaInterface */ {

  // private static Class<?> PKG = OBSOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String ENDPOINT_TAG = "endpoint";

  private static final String ACCESS_KEY_TAG = "access_key";

  private static final String SECRET_KEY_TAG = "secret_key";

  private static final String BUCKET_TAG = "bucket";

  private static final String OBJECT_KEY_TAG = "object_key";

  private static final String FILE_TAG = "file";

  private static final String NAME_TAG = "name";

  private static final Pattern OLD_STYLE_FILENAME = Pattern.compile("^[s|S]3:\\/\\/([0-9a-zA-Z]{20}):(.+)@(.+)$");

  private String endPoint;

  private String accessKey;

  private String secretKey;

  private String bucket;

  private String objectKey;
  public int previoudfileNameIndex;
  public int previoudfileContentIndex;

  public OBSOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
    readData(stepnode);
  }

  @Override
  public void setDefault() {
    // call the base classes method
    super.setDefault();

    // now set the default for the
    // filename to an empty string
    setFileName("");
  }

  @Override
  public void readData(Node stepnode) throws KettleXMLException {
    try {

      super.readData(stepnode);

      endPoint = XMLHandler.getTagValue(stepnode, ENDPOINT_TAG);
      accessKey = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, ACCESS_KEY_TAG));
      secretKey = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, SECRET_KEY_TAG));
      bucket = XMLHandler.getTagValue(stepnode, BUCKET_TAG);
      objectKey = XMLHandler.getTagValue(stepnode, OBJECT_KEY_TAG);

      setFileAsCommand(false); // command cannot be executed in S3 file system; PDI-4707, 4655
      String filename = XMLHandler.getTagValue(stepnode, FILE_TAG, NAME_TAG);
      processFilename(filename);

    } catch (Exception e) {
      throw new KettleXMLException("Unable to load step info from XML", e);
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder(512);

    retval.append(super.getXML());

    retval.append("    ").append(XMLHandler.addTagValue(ENDPOINT_TAG, endPoint));
    retval.append("    ").append(XMLHandler.addTagValue(ACCESS_KEY_TAG, Encr.encryptPasswordIfNotUsingVariables(accessKey)));
    retval.append("    ").append(XMLHandler.addTagValue(SECRET_KEY_TAG, Encr.encryptPasswordIfNotUsingVariables(secretKey)));
    retval.append("    ").append(XMLHandler.addTagValue(BUCKET_TAG, bucket));
    retval.append("    ").append(XMLHandler.addTagValue(OBJECT_KEY_TAG, objectKey));

    return retval.toString();
  }

  @Override
  public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
    try {

      super.readRep(rep, metaStore, id_step, databases);

      endPoint = rep.getStepAttributeString(id_step, ENDPOINT_TAG);
      accessKey = Encr.decryptPasswordOptionallyEncrypted(rep.getStepAttributeString(id_step, ACCESS_KEY_TAG));
      secretKey = Encr.decryptPasswordOptionallyEncrypted(rep.getStepAttributeString(id_step, SECRET_KEY_TAG));
      bucket = rep.getStepAttributeString(id_step, BUCKET_TAG);
      objectKey = rep.getStepAttributeString(id_step, OBJECT_KEY_TAG);

      setFileAsCommand(false); // commands cannot be executed in S3 file system; PDI-4707, 4655
      String filename = rep.getStepAttributeString(id_step, "file_name");
      processFilename(filename);

    } catch (Exception e) {
      throw new KettleException("Unexpected error reading step information from the repository", e);
    }
  }

  @Override
  public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    try {

      super.saveRep(rep, metaStore, id_transformation, id_step);

      rep.saveStepAttribute(id_transformation, id_step, ENDPOINT_TAG, endPoint);
      rep.saveStepAttribute(id_transformation, id_step, ACCESS_KEY_TAG, Encr.encryptPasswordIfNotUsingVariables(accessKey));
      rep.saveStepAttribute(id_transformation, id_step, SECRET_KEY_TAG, Encr.encryptPasswordIfNotUsingVariables(secretKey));
      rep.saveStepAttribute(id_transformation, id_step, BUCKET_TAG, bucket);
      rep.saveStepAttribute(id_transformation, id_step, OBJECT_KEY_TAG, objectKey);

    } catch (Exception e) {
      throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
    }
  }

  @Override
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    return new OBSOutput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  // @Override
   public StepDataInterface getStepData() {
   return new OBSOutputData();
   }

  public ObsClient getObsClient(VariableSpace space) {

    // Try to connect to OBS first
    //
    String realEndPoint = space.environmentSubstitute(endPoint);
    String realAccessKey = Encr.decryptPasswordOptionallyEncrypted(space.environmentSubstitute(accessKey));
    String realSecretKey = Encr.decryptPasswordOptionallyEncrypted(space.environmentSubstitute(secretKey));
    ObsClient client = new ObsClient(realAccessKey, realSecretKey, realEndPoint);

    return client;
  }

  public String getEndPoint() {
    return endPoint;
  }

  public void setEndPoint(String endPoint) {
    this.endPoint = endPoint;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getObjectKey() {
    return objectKey;
  }

  public void setObjectKey(String objectKey) {
    this.objectKey = objectKey;
  }

  /**
   * New filenames obey the rule obs://<any_string>/<obs_bucket_name>/<path>. <br>
   * However, we maintain old filenames obs://<access_key>:<secret_key>@endpoint/<obs_bucket_name>/<path>
   * 
   * @param filename
   * @return
   */
  protected void processFilename(String filename) throws Exception {
    if (Utils.isEmpty(filename)) {
      setFileName(filename);
      return;
    }
    // it it's an old-style filename - use and then remove keys from the filename
    Matcher matcher = OLD_STYLE_FILENAME.matcher(filename);
    if (matcher.matches()) {
      // old style filename is URL encoded
      accessKey = decodeAccessKey(matcher.group(1));
      secretKey = decodeAccessKey(matcher.group(2));
      setFileName("obs://" + matcher.group(3));
    } else {
      setFileName(filename);
    }
  }

  protected String decodeAccessKey(String key) {
    if (Utils.isEmpty(key)) {
      return key;
    }
    return key.replaceAll("%2B", "\\+").replaceAll("%2F", "/");
  }

}
