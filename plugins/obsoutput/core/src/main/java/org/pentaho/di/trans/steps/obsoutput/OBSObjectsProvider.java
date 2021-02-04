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

import org.pentaho.di.i18n.BaseMessages;

import com.obs.services.ObsClient;
import com.obs.services.model.GetObjectRequest;
import com.obs.services.model.ListBucketsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月18日
 * @version
 * 
 */
public class OBSObjectsProvider {

  private static Class<?> PKG = OBSOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private ObsClient client;

  public OBSObjectsProvider(ObsClient client) {
    super();
    this.client = client;
  }

  /**
   * @return
   */
  private List<ObsBucket> getBuckets() {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

    List<ObsBucket> result;
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

      ListBucketsRequest request = new ListBucketsRequest();
      request.setQueryLocation(true);

      result = client.listBuckets(request);

    } finally {
      Thread.currentThread().setContextClassLoader(currentClassLoader);
    }

    return result;
  }

  public String[] getBucketsNames() {
    return getBuckets().stream().map(b -> b.getBucketName()).toArray(String[]::new);
  }

  public ObsBucket getBucket(String bucketName) {
    if (client.headBucket(bucketName)) {
      ObsBucket bucket = new ObsBucket();
      bucket.setBucketName(bucketName);
      return bucket;
    }

    return null;
  }

  private ObjectListing getObsObjects(ObsBucket bucket) {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

      return client.listObjects(bucket.getBucketName());
    } finally {
      Thread.currentThread().setContextClassLoader(currentClassLoader);
    }
  }

  public String[] getObsObjectsNames(String bucketName) throws Exception {
    ObsBucket bucket = getBucket(bucketName);
    if (bucket == null) {
      throw new Exception(BaseMessages.getString(PKG, "OBSDefaultService.Exception.UnableToFindBucket.Message", bucketName));
    }
    // return getObsObjects(bucket).getObjectSummaries().stream().map(b -> b.getObjectKey()).toArray(String[]::new);
    return getObsObjects(bucket).getObjects().stream().map(b -> b.getObjectKey()).toArray(String[]::new);
  }

  public ObsObject getObsObject(ObsBucket bucket, String objectKey, Long byteRangeStart, Long byteRangeEnd) {
    if (byteRangeStart != null && byteRangeEnd != null) {
      GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucket.getBucketName(), objectKey);
      rangeObjectRequest.setRangeStart(byteRangeStart);
      rangeObjectRequest.setRangeEnd(byteRangeEnd);
      return client.getObject(rangeObjectRequest);
    } else {
      return client.getObject(bucket.getBucketName(), objectKey);
    }
  }

  public ObsObject getObsObject(ObsBucket bucket, String objectKey) {
    return getObsObject(bucket, objectKey, null, null);
  }

  private ObjectMetadata getObsObjectDetails(ObsBucket bucket, String objectKey) {
    return client.getObjectMetadata(bucket.getBucketName(), objectKey);
  }

  public long getObsObjectContentLenght(ObsBucket bucket, String objectKey) {
    return getObsObjectDetails(bucket, objectKey).getContentLength();
  }

}
