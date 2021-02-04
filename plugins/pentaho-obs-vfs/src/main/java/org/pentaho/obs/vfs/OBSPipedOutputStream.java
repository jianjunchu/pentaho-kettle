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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.obs.services.ObsClient;
import com.obs.services.model.AbortMultipartUploadRequest;
import com.obs.services.model.CompleteMultipartUploadRequest;
import com.obs.services.model.InitiateMultipartUploadRequest;
import com.obs.services.model.InitiateMultipartUploadResult;
import com.obs.services.model.PartEtag;
import com.obs.services.model.UploadPartRequest;
import com.obs.services.model.UploadPartResult;

/**
 * 
 * 
 * @author Sun
 * @since 2019年8月26日
 * @version
 * 
 */
public class OBSPipedOutputStream extends PipedOutputStream {

  private static int PART_SIZE = 1024 * 1024 * 5;

  private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

  private boolean initialized = false;

  private boolean blockedUntilDone = true;

  private PipedInputStream pipedInputStream;

  private OBSAsyncTransferRunner obsAsyncTransferRunner;

  private OBSFileSystem fs;

  private Future<Boolean> result = null;

  private String bucketName;

  private String objectKey;

  public OBSPipedOutputStream(OBSFileSystem fs, String bucketName, String objectKey) {

    this.pipedInputStream = new PipedInputStream();
    try {
      this.pipedInputStream.connect(this);
    } catch (IOException e) {
      // FATAL, unexpected
      throw new RuntimeException(e);
    }

    this.obsAsyncTransferRunner = new OBSAsyncTransferRunner();
    this.bucketName = bucketName;
    this.objectKey = objectKey;
    this.fs = fs;
  }

  private void initializeWrite() {
    if (!initialized) {
      initialized = true;
      result = this.executor.submit(obsAsyncTransferRunner);
    }
  }

  public boolean isBlockedUntilDone() {
    return blockedUntilDone;
  }

  public void setBlockedUntilDone(boolean blockedUntilDone) {
    this.blockedUntilDone = blockedUntilDone;
  }

  @Override
  public void write(int b) throws IOException {
    initializeWrite();
    super.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    initializeWrite();
    super.write(b, off, len);
  }

  @Override
  public void close() throws IOException {
    super.close();

    if (initialized && isBlockedUntilDone()) {
      while (!result.isDone()) {
        try {
          Thread.sleep((long) (0.1 * 1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
          Thread.currentThread().interrupt();
        }
      }
    }
    this.executor.shutdown();
  }

  class OBSAsyncTransferRunner implements Callable<Boolean> {

    @Override
    public Boolean call() throws Exception {
      boolean returnVal = true;
      List<PartEtag> partETags = new ArrayList<PartEtag>();

      // Step 1: Initialize
      // FileSystemOptions fsOpts = fs.getFileSystemOptions();

      InitiateMultipartUploadRequest initRequest;
      // ObjectMetadata objectMetadata;
      initRequest = new InitiateMultipartUploadRequest(bucketName, objectKey);

      InitiateMultipartUploadResult initResponse = null;

      final ObsClient client = fs.getClient();

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(PART_SIZE); BufferedInputStream bis = new BufferedInputStream(pipedInputStream, PART_SIZE)) {
        initResponse = client.initiateMultipartUpload(initRequest);
        // Step 2: Upload parts.
        byte[] tmpBuffer = new byte[PART_SIZE];
        int read = 0;
        long offset = 0;
        long totalRead = 0;
        int partNum = 1;

        OBSWindowedSubstream s3is;

        while ((read = bis.read(tmpBuffer)) >= 0) {

          // if something was actually read
          if (read > 0) {
            baos.write(tmpBuffer, 0, read);
            totalRead += read;
          }

          if (totalRead > PART_SIZE) { // do we have a minimally accepted chunk above 5Mb?
            s3is = new OBSWindowedSubstream(baos.toByteArray());

            UploadPartRequest uploadRequest = new UploadPartRequest();
            uploadRequest.setBucketName(bucketName);
            uploadRequest.setObjectKey(objectKey);
            uploadRequest.setUploadId(initResponse.getUploadId());
            uploadRequest.setPartNumber(partNum++);
            uploadRequest.setOffset(offset);
            uploadRequest.setPartSize(totalRead);
            uploadRequest.setInput(s3is);

            // Upload part and add response to our list.
            UploadPartResult uploadPartResult = client.uploadPart(uploadRequest);

            partETags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));

            offset += totalRead;
            totalRead = 0; // reset part size counter
            baos.reset(); // reset output stream to 0
          }
        }

        // Step 2.1 upload last part
        s3is = new OBSWindowedSubstream(baos.toByteArray());

        UploadPartRequest uploadRequest = new UploadPartRequest();//
        uploadRequest.setBucketName(bucketName);
        uploadRequest.setObjectKey(objectKey);
        uploadRequest.setUploadId(initResponse.getUploadId());
        uploadRequest.setPartNumber(partNum++);
        uploadRequest.setOffset(offset);
        uploadRequest.setPartSize(totalRead);
        uploadRequest.setInput(s3is);
        // uploadRequest.withLastPart(true);

        UploadPartResult uploadPartResult = client.uploadPart(uploadRequest);

        partETags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));

        // Step 3: Complete.
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, objectKey, initResponse.getUploadId(), partETags);

        client.completeMultipartUpload(compRequest);
      } catch (Exception e) {
        e.printStackTrace();
        if (initResponse == null) {
          close();
        } else {
          client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, objectKey, initResponse.getUploadId()));
        }
        returnVal = false;
      } finally {
        fs.putClient(client);
      }

      return returnVal;
    }

  }
}
