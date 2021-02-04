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

import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
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

}
