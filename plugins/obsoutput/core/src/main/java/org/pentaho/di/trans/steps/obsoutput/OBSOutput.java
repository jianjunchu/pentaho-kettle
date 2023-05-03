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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.CharsetToolkit;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;

/**
 * 
 * 
 * @author Sun
 * @since 2019年2月25日
 * @version
 * 
 */
public class OBSOutput extends TextFileOutput /* BaseStep implements StepInterface */ {

  // private static Class<?> PKG = OBSOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private FileSystemOptions fsOptions;

  private OBSOutputMeta meta;
  private OBSOutputData data;
  private static Class<?> PKG = OBSOutputMeta.class; //for i18n purposes, needed by Translator2!!

  // private OBSOutputMeta meta;
  // private OBSOutputData data;

  public OBSOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  @Override
  protected FileObject getFileObject(String vfsFilename) throws KettleFileException {
    return KettleVFS.getFileObject(vfsFilename, getFsOptions());
  }

  @Override
  protected FileObject getFileObject(String vfsFilename, VariableSpace space) throws KettleFileException {
    return KettleVFS.getFileObject(vfsFilename, space, getFsOptions());
  }

  @Override
  protected OutputStream getOutputStream(String vfsFilename, VariableSpace space, boolean append) throws KettleFileException {
    return KettleVFS.getOutputStream(vfsFilename, space, getFsOptions(), append);
  }

  public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if(true)
      return processRowBinary(smi,sdi);
    else
      return super.processRow(smi,sdi);
  }

  public synchronized boolean processRowBinary(StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] row = getRow();
    meta = (OBSOutputMeta)smi;
    data = (OBSOutputData)sdi;

    if (row==null) {
      setOutputDone();
      return false; // nothing to see here, move along...
    }
    if (first) {

      first = false;
      if ( getInputRowMeta() != null ) {
        data.outputRowMeta = getInputRowMeta().clone();
      } else {
        data.outputRowMeta = new RowMeta();
      }
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
      String filenameField = environmentSubstitute(meta.getFileNameField());
      meta.previoudfileNameIndex = getInputRowMeta().indexOfValue(filenameField);
      String fileContentField = meta.getOutputFields()[0].getName();
      meta.previoudfileContentIndex = getInputRowMeta().indexOfValue(fileContentField);
    }

    String binaryFileName =row[meta.previoudfileNameIndex].toString();
    byte[] binaryFileContent =(byte[])row[meta.previoudfileContentIndex];
    Object[] outputRowData  = new Object[getInputRowMeta().size()+1];

    for (int i =0;i<getInputRowMeta().size();i++)
    {
      outputRowData[i]=row[i];
    }
    if(binaryFileName!=null && binaryFileContent!=null) {
        saveBinaryFile(binaryFileName,binaryFileContent);
    }

    putRow(data.outputRowMeta, outputRowData); // copy row to possible alternate rowset(s).
    if (checkFeedback(getLinesInput())) {
      if (log.isBasic()) {
        logBasic(BaseMessages.getString(PKG, "OBSOutput.Log.LineNumber", Long.toString(getLinesInput()))); //$NON-NLS-1$
      }
    }
    return true;
  }

  protected FileSystemOptions createFileSystemOptions() throws KettleFileException {
    try {
      FileSystemOptions opts = new FileSystemOptions();
      OBSOutputMeta obsMeta = (OBSOutputMeta) meta;
      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, //
          new StaticUserAuthenticator(null, //
              Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(obsMeta.getAccessKey())),
              Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(obsMeta.getSecretKey()))//
          )//
      );
      return opts;
    } catch (FileSystemException e) {
      throw new KettleFileException(e);
    }
  }

  protected FileSystemOptions getFsOptions() throws KettleFileException {
    if (fsOptions == null) {
      fsOptions = createFileSystemOptions();
    }
    return fsOptions;
  }

  private boolean saveBinaryFile(String binaryFileName,byte[] content)  {
    try {

      // Close the previous file...
      //
      if (data.fos != null) {
        data.fos.close();
      }
      data.object = null;
      data.fos = null;
      if (data.client == null)
          data.client = meta.getObsClient(this);
      if(content!=null)
          data.client.putObject(meta.getBucket(),binaryFileName,new ByteArrayInputStream(content));
      else
          data.client.putObject(meta.getBucket(),binaryFileName,(InputStream) null);
        return true;
    } catch (Exception e) {
        logBasic("OBSOutput_Error:"+binaryFileName); //$NON-NLS-1$
    }
    return true;
  }

}
