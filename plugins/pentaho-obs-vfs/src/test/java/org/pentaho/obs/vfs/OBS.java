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

package org.pentaho.obs.vfs;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ListBucketsRequest;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月26日
 * @version
 * 
 */
public class OBS {

  public static final String END_POINT = "obs.cn-north-1.myhuaweicloud.com";
  public static final String AK = "RW4X6LNKYMBSQTCQMHKP";
  public static final String SK = "3lJYCCLvkPf7ZnGjq59oqOaSLGpJ9fYDxZH6N9TJ";

  public static final String BUCKET_NAME = "obs-pentaho";

  public static final String OBJECT_KEY = "data-no.csv";

  public static final String OBJECT_KEY_NEW_1 = "obs-csv/data-no.csv";

  public static final String OBJECT_KEY_NEW_2 = "sun-obs-csv/data-no.csv";

  public static final String URI_ROOT = "obs://" + AK + ":" + SK + "@" + END_POINT;

  public static final String ROOT = URI_ROOT + "/" + BUCKET_NAME;

  public static final String filename = ROOT + "/" + OBJECT_KEY;

  public static final String new_filename_1 = ROOT + "/" + OBJECT_KEY_NEW_1;

  public static final String new_filename_2 = ROOT + "/" + OBJECT_KEY_NEW_2;

  public static void main(String[] args) throws Exception {
    test_exist();
  }

  static void test() {

    ObsClient client = null;
    try {
      client = new ObsClient(AK, SK, END_POINT);

      ObsObject obsObject = client.getObject(BUCKET_NAME, OBJECT_KEY);

      System.out.println(obsObject);
      System.out.println(obsObject.getMetadata());
    } finally {
      try {
        if (client != null)
          client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static void test_obs() {

    ObsClient client = null;
    try {
      client = new ObsClient(AK, SK, END_POINT);
      ListBucketsRequest requestBucket = new ListBucketsRequest();
      requestBucket.setQueryLocation(true);
      List<ObsBucket> buckets = client.listBuckets(requestBucket);
      for (ObsBucket bucket : buckets) {
        System.out.println(bucket.getBucketName());
      }

      System.out.println("|============================|");

      ListObjectsRequest request = new ListObjectsRequest();
      request.setBucketName(BUCKET_NAME);
      // request.setPrefix("/");
      request.setDelimiter(OBSFileObject.DELIMITER);

      ObjectListing ol = client.listObjects(request);
      List<ObsObject> objects = ol.getObjects();
      ArrayList<String> commonPrefixes = new ArrayList<String>(ol.getCommonPrefixes());
      for (ObsObject object : objects) {
        System.out.println(object.getObjectKey());
        // System.out.println(object.getMetadata());
        System.out.println("|----------------------------|");
      }
      for (String commonPrefixe : commonPrefixes) {
        System.out.println(commonPrefixe);
        System.out.println("|----------------------------|");
      }

      System.out.println("|============================|");

      try {
        ObsObject obsObject = client.getObject(BUCKET_NAME, OBJECT_KEY_NEW_1);
        System.out.println("Y");
      } catch (ObsException e) {
        System.out.println("N");
      }

      // System.out.println(obsObject);
      // System.out.println(obsObject.getMetadata());
    } finally {
      try {
        if (client != null)
          client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // try {
    // FileSystemManager manager = VFS.getManager();
    // FileObject file = manager.resolveFile("");
    // // if (file.exists()) {
    // // System.out.println("Y");
    // // } else {
    // // System.out.println("N");
    // // }
    //
    // FileObject[] children = file.getChildren();
    // System.out.println("Children of " + file.getName().getURI());
    //
    // for (FileObject child : children) {
    // String baseName = child.getName().getBaseName();
    // System.out.println("文件名：" + baseName + " -- " + baseName);
    // System.out.println("Type：" + child.getType());
    // }
    //
    // } catch (FileSystemException e) {
    // e.printStackTrace();
    // }
  }

  static void test_createFolder() {

    FileObject parentfolder = null;

    try {
      FileObject fileObject = KettleVFS.getFileObject(new_filename_2, null, null);
      System.out.print("1: ");
      System.out.println(fileObject);
      System.out.println(fileObject.getType());

      parentfolder = KettleVFS.getFileObject(new_filename_2).getParent();
      System.out.print("2: ");
      System.out.println(parentfolder);
      System.out.println(parentfolder.getType());

      if (parentfolder.exists()) {
        System.out.println("Y");
      } else {
        System.out.println("N");
        parentfolder.createFolder();
      }

      String name = "obs://RW4X6LNKYMBSQTCQMHKP:3lJYCCLvkPf7ZnGjq59oqOaSLGpJ9fYDxZH6N9TJ@obs.cn-north-1.myhuaweicloud.com/obs-pentaho/obs.csv";
      FileObject p = KettleVFS.getFileObject(name).getParent();
      System.out.println(p.exists());

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (parentfolder != null) {
        try {
          parentfolder.close();
        } catch (Exception ex) {
          // Ignore
        }
      }
    }

  }

  static void test_exist() throws KettleException {
    // KettleVFS.getFileObject( "" );
    boolean fileExist;
    try {
      fileExist = KettleVFS.getFileObject(filename).exists();
    } catch (FileSystemException e) {
      throw new KettleException(e);
    }

    System.out.println(fileExist);
  }

  static void test_delete() {
    try {
      FileSystemManager manager = VFS.getManager();

      FileObject fileObject = manager.resolveFile("");
      // Object s = fileObject.getType();

      fileObject.delete();
    } catch (FileSystemException e) {
      e.printStackTrace();
    }

  }

  static void test_2() throws KettleFileException, FileSystemException {
    FileSystemOptions opts = new FileSystemOptions();
    try {
      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, new StaticUserAuthenticator(null, AK, SK));
    } catch (FileSystemException e) {
      e.printStackTrace();
    }

    FileObject fo = KettleVFS.getFileObject("", opts);

    // FileObject parent = fo.getParent();
    //
    // if (!parent.exists()) {
    // parent.createFolder();
    // }

    String v = "1,2";

    OutputStream fos = KettleVFS.getOutputStream("", null, opts, false);

    OutputStream writer = new BufferedOutputStream(fos, 5000);

    try {
      writer.write(v.getBytes());

      writer.flush();

      writer.close();

      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println(fo);
  }
}
