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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.ListBucketsRequest;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;

/**
 * 
 * 未写完
 * 
 * @author Sun
 * @since 2019年8月21日
 * @version
 * 
 */
public class OBSFileObject extends AbstractFileObject<OBSFileSystem> {

  private static final Logger logger = LoggerFactory.getLogger(OBSFileObject.class);

  public static final String DELIMITER = "/";

  private final OBSFileSystem fs;

  protected String bucketName;

  protected String objectKey;

  protected ObsObject obsObject;

  protected OBSFileObject(final AbstractFileName name, final OBSFileSystem fs) {
    super(name, fs);

    this.fs = fs;
    this.bucketName = getObsBucketName();
    this.objectKey = getBucketRelativeObsPath();
  }

  protected OBSFileObject(final AbstractFileName name, final OBSFileSystem fs, final FileName rootName) {
    super(name, fs);

    this.fs = fs;
    this.bucketName = getObsBucketName();
    this.objectKey = getBucketRelativeObsPath();
  }

  @Override
  protected long doGetContentSize() throws Exception {
    // synchronized (getFileSystem()) {
    // return this.obsObject.getMetadata().getContentLength();
    // }
    // return getObsObject().getMetadata().getContentLength();
    return this.obsObject.getMetadata().getContentLength();
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("Accessing content {}", getQualifiedName());
    }
    return this.obsObject.getObjectContent();
  }

  @Override
  protected FileType doGetType() throws Exception {
    return super.getType();
  }

  @Override
  protected void doAttach() throws IOException {

    if (logger.isDebugEnabled()) {
      logger.debug("Attach called on {}", getQualifiedName());
    }
    injectType(FileType.IMAGINARY);

    if (isRootBucket()) {
      // cannot attach to root bucket
      injectType(FileType.FOLDER);
      return;
    }

    try {
      // 1. Is it an existing file?
      obsObject = getObsObject();
      injectType(getName().getType()); // if this worked then the automatically detected type is right
    } catch (ObsException e) {// OBS object doesn't exist
      // 2. Is it in reality a folder?
      handleAttachException(bucketName, objectKey);
    }
  }

  @SuppressWarnings("deprecation")
  private void handleAttachException(String bucketName, String objectKey) throws FileSystemException {
    String keyWithDelimiter = objectKey + DELIMITER;
    try {
      obsObject = getObsObject(bucketName, keyWithDelimiter);
      injectType(FileType.FOLDER);
      this.objectKey = keyWithDelimiter;
    } catch (ObsException e) {
      final ObsClient client = getAbstractFileSystem().getClient();
      try {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(keyWithDelimiter);
        listObjectsRequest.setDelimiter(DELIMITER);

        ObjectListing ol = client.listObjects(listObjectsRequest);

        if (!(ol.getCommonPrefixes().isEmpty() && ol.getObjectSummaries().isEmpty())) {
          injectType(FileType.FOLDER);
        } else {
          // Folders don't really exist - they will generate a "NoSuchKey" exception
          String errorCode = e.getErrorCode();
          // confirms key doesn't exist but connection okay
          if (!errorCode.equals("NoSuchKey")) {
            // bubbling up other connection errors
            logger.error("Could not get information on " + getQualifiedName(), e); // make sure this gets printed for the user
            throw new FileSystemException("vfs.provider/get-type.error", getQualifiedName(), e);
          }
        }
      } finally {
        getAbstractFileSystem().putClient(client);
      }
    }
  }

  @Override
  public FileObject[] getChildren() throws FileSystemException {
    try {
      if (doGetType() != FileType.FOLDER) {
        throw new FileNotFolderException(getName());
      }
    } catch (final Exception ex) {
      throw new FileNotFolderException(getName(), ex);
    }

    return super.getChildren();
  }

  @Override
  protected String[] doListChildren() throws Exception {

    List<String> childrenList = new ArrayList<String>();

    // only listing folders or the root bucket
    if (super.getType() == FileType.FOLDER || isRootBucket()) {
      childrenList = getObsObjectsFromVirtualFolder(bucketName, objectKey);
    }

    String[] children = new String[childrenList.size()];

    return childrenList.toArray(children);
  }

  // private final String relPath;

  @Override
  protected void doDelete() throws Exception {
    synchronized (getFileSystem()) {
      final DeleteObjectResult result;
      final ObsClient client = getAbstractFileSystem().getClient();
      try {
        result = client.deleteObject(bucketName, objectKey);
        System.out.println(result);
      } finally {
        getAbstractFileSystem().putClient(client);
      }
    }
  }

  @Override
  protected void doCreateFolder() throws Exception {

    if (isRootBucket()) {
      return;
      // throw new FileSystemException("vfs.provider/create-folder-not-supported.error");
    }

    final PutObjectResult result;
    final ObsClient client = getAbstractFileSystem().getClient();
    try {

      // create meta-data for your folder and set content-length to 0
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(0L);
      metadata.setContentType("binary/octet-stream");

      // create empty content
      InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

      // PutObjectRequest request = new PutObjectRequest(bucketName, objectKey + DELIMITER, emptyContent);
      // client.putObject(request);

      result = client.putObject(bucketName, objectKey + DELIMITER, emptyContent);
      result.getStatusCode();

    } /*
       * catch (ObsException e) {
       * 
       * }
       */ finally {
      getAbstractFileSystem().putClient(client);
    }

    // if (!ok.getStatusCode()) {
    // throw new FileSystemException("vfs.provider.ftp/create-folder.error", getName());
    // }
  }

  @Override
  protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
    return new OBSPipedOutputStream(this.fs, bucketName, objectKey);
    // final ObsClient client = getAbstractFileSystem().getClient();
    // try {
    // OutputStream out = null;
    // if (bAppend) {
    // out = client.appendFileStream(relPath);
    // } else {
    // out = client.storeFileStream(relPath);
    // }
    //
    // if (out == null) {
    // throw new FileSystemException("vfs.provider.ftp/output-error.debug", this.getName(), client.getReplyString());
    // }
    //
    // return new FtpOutputStream(client, out);
    // } catch (final Exception e) {
    // getAbstractFileSystem().putClient(client);
    // throw e;
    // }
  }

  protected String getObsBucketName() {
    String bucketName = getName().getPath();
    if (bucketName.indexOf(DELIMITER, 1) > 1) {
      // this file is a file, to get the bucket, remove the name from the path
      bucketName = bucketName.substring(1, bucketName.indexOf(DELIMITER, 1));
    } else {
      // this file is a bucket
      bucketName = bucketName.replaceAll(DELIMITER, "");
    }
    return bucketName;
  }

  protected List<String> getObsObjectsFromVirtualFolder(String bucketName, String objectKey) {
    List<String> bucketNames = new ArrayList<String>();

    // fix cases where the path doesn't include the final delimiter
    String realObjectKey = objectKey;
    if (!realObjectKey.endsWith(DELIMITER)) {
      realObjectKey += DELIMITER;
    }

    if ("".equals(bucketName) && "".equals(objectKey)) {
      final ObsClient client = getAbstractFileSystem().getClient();
      try {

        // Getting buckets in root folder
        ListBucketsRequest request = new ListBucketsRequest();
        request.setQueryLocation(true);
        List<ObsBucket> buckets = client.listBuckets(request);
        for (ObsBucket bucket : buckets) {
          bucketNames.add(bucket.getBucketName() + "/");
        }
      } finally {
        getAbstractFileSystem().putClient(client);
      }
    } else {
      getObjectsFromNonRootFolder(bucketName, objectKey, bucketNames, realObjectKey);
    }
    return bucketNames;
  }

  private void getObjectsFromNonRootFolder(String bucketName, String key, List<String> bucketNames, String realObjectKey) {
    // Getting files/folders in a folder/bucket
    String prefix = key.isEmpty() || key.endsWith(DELIMITER) ? key : key + DELIMITER;
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
    listObjectsRequest.setBucketName(bucketName);
    listObjectsRequest.setPrefix(prefix);
    listObjectsRequest.setDelimiter(DELIMITER);

    final ObsClient client = getAbstractFileSystem().getClient();
    try {

      ObjectListing ol = client.listObjects(listObjectsRequest);

      List<ObsObject> objects = new ArrayList<ObsObject>(ol.getObjects());
      ArrayList<String> commonPrefixes = new ArrayList<String>(ol.getCommonPrefixes());

      for (ObsObject object : objects) {
        if (!object.getObjectKey().equals(realObjectKey)) {
          bucketNames.add(object.getObjectKey().substring(prefix.length()));
        }
      }

      for (String commonPrefix : commonPrefixes) {
        if (!commonPrefix.equals(realObjectKey)) {
          bucketNames.add(commonPrefix.substring(prefix.length()));
        }
      }
    } finally {
      getAbstractFileSystem().putClient(client);
    }
  }

  protected String getBucketRelativeObsPath() {
    if (getName().getPath().indexOf(DELIMITER, 1) >= 0) {
      return getName().getPath().substring(getName().getPath().indexOf(DELIMITER, 1) + 1);
    } else {
      return "";
    }
  }

  @VisibleForTesting
  public ObsObject getObsObject() {
    return getObsObject(this.bucketName, this.objectKey);
  }

  protected ObsObject getObsObject(String bucketName, String objectKey) {
    if (obsObject != null && obsObject.getObjectContent() != null) {
      logger.debug("Returning exisiting object {}", getQualifiedName());
      return obsObject;
    } else {
      logger.debug("Getting object {}", getQualifiedName());
      final ObsClient client = getAbstractFileSystem().getClient();
      try {
        return client.getObject(bucketName, objectKey);
      } finally {
        getAbstractFileSystem().putClient(client);
      }
    }
  }

  @VisibleForTesting
  protected ObsObject activateContent() {
    if (obsObject != null) {
      // force it to re-create the object
      // obsObject.close();
      obsObject = null;
    }

    obsObject = getObsObject();
    return obsObject;
  }

  private boolean bucketExists(String bucketName) {
    boolean exists = false;
    final ObsClient client = getAbstractFileSystem().getClient();
    try {
      exists = client.headBucket(bucketName);
    } catch (ObsException e) {
      logger.debug("Exception checking if bucket exists", e);
    } finally {
      getAbstractFileSystem().putClient(client);
    }
    return exists;
  }

  protected boolean isRootBucket() {
    return objectKey.equals("");
  }

  protected String getQualifiedName() {
    return getQualifiedName(this);
  }

  protected String getQualifiedName(OBSFileObject obsFileObject) {
    return obsFileObject.bucketName + "/" + obsFileObject.objectKey;
  }

  // See if the first name on the path is actually a bucket. If not, it's probably an old-style path.
  protected SimpleEntry<String, String> fixFilePath(String bucket, String key) {
    String newBucket = bucket;
    String newKey = key;

    // see if the folder exists; if not, it might be from an old path and the real bucket is in the key
    if (!bucketExists(bucket)) {
      logger.debug("Bucket {} from original path not found, might be an old path from the old driver", bucket);
      if (key.split(DELIMITER).length > 1) {
        newBucket = key.split(DELIMITER)[0];
        newKey = key.replaceFirst(newBucket + DELIMITER, "");
      } else {
        newBucket = key;
        newKey = "";
      }
    }
    return new SimpleEntry<>(newKey, newBucket);
  }

}
