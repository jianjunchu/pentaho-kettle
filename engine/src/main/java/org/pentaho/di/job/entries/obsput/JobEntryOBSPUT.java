package org.pentaho.di.job.entries.obsput;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.obs.services.ObsClient;
import com.obs.services.model.PutObjectResult;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;


@org.pentaho.di.core.annotations.JobEntry(id = "OBSPUT",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Utility",
        i18nPackageName = "org.pentaho.di.job.entries.obsput", image = "mllp-in.svg", name = "TooltipDesc.Name",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/HL7+MLLP+Input",
        description = "JobOBSPUT.TooltipDesc")
public class JobEntryOBSPUT extends JobEntryBase implements Cloneable, JobEntryInterface {
    private static Class<?> PKG = JobEntryOBSPUT.class; // for i18n purposes, needed by Translator2!!

    public static String getFileDir() {
        return fileDir;
    }

    public static void setFileDir(String fileDir) {
        JobEntryOBSPUT.fileDir = fileDir;
    }

    public static String getEndPoint() {
        return endPoint;
    }

    public static void setEndPoint(String endPoint) {
        JobEntryOBSPUT.endPoint = endPoint;
    }

    public static String getAk() {
        return ak;
    }

    public static void setAk(String ak) {
        JobEntryOBSPUT.ak = ak;
    }

    public static String getSk() {
        return sk;
    }

    public static void setSk(String sk) {
        JobEntryOBSPUT.sk = sk;
    }

    public static String getBucketName() {
        return bucketName;
    }

    public static void setBucketName(String bucketName) {
        JobEntryOBSPUT.bucketName = bucketName;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    public static void setMaxValue(int maxValue) {
        JobEntryOBSPUT.maxValue = maxValue;
    }

    public static int getStartValue() {
        return startValue;
    }

    public static void setStartValue(int startValue) {
        JobEntryOBSPUT.startValue = startValue;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        JobEntryOBSPUT.counter = counter;
    }

    public static String getSexFlag() {
        return sexFlag;
    }

    public static void setSexFlag(String sexFlag) {
        JobEntryOBSPUT.sexFlag = sexFlag;
    }

    private static String fileDir = "/mnt/kettle/photos/female";
    private static String endPoint = "sjclobs.fgwpt.com";
    private static String ak = "AC3C0C56748491C05D22";
    private static String sk = "GONZaHvEcNg75v1vJrfWlY+XagwAAAFvdISRwNQY";
    private static String bucketName;
    private static int maxValue;
    private static int startValue;
    private static int counter;
    private static String sexFlag;

    private static FileWriter fileWriter;
    private static ObsClient obsClient;


    public static class Task implements Callable<String> {
        public File file;
        public Task(File file){
            this.file = file;
        }

        public String call() throws Exception {
            try {
                if ( null == obsClient) {
                    debug(" start create obs client");
                    debug("ak="+ak);
                    debug("sk="+sk);
                    debug("endPoint="+endPoint);
                    obsClient = new ObsClient(ak, sk, endPoint);
                }
                String fileName = file.getName();
                String fileSuf = "";
                if(fileName.indexOf(".") != -1) {
                    fileSuf = fileName.substring(fileName.indexOf("."));
                    if (".jpg".equals(fileSuf.toLowerCase()) ||".png".equals(fileSuf.toLowerCase())||".jpeg".equals(fileSuf.toLowerCase()) ) {
                        String objectKey = null;
                        if(null == sexFlag || sexFlag.length() == 0) {
                            objectKey = fileName.substring(0, 1) + "_" + getNextValue() + fileSuf;
                        }else {
                            objectKey = sexFlag + "_" + getNextValue() + fileSuf;
                        }
                        if (maxValue == 0) {
                            if(null == sexFlag || sexFlag.length() == 0) {
                                objectKey = fileName.substring(0, 1) + "_" + generateRandomStr(13) + fileSuf;
                            }  else {
                                objectKey = sexFlag + "_" + generateRandomStr(13) + fileSuf;
                            }
                        }
                        debug(" start put");
                        debug("objectKey="+objectKey);
                        debug("bucketName="+bucketName);
                        debug("file="+file.getAbsolutePath());
                        PutObjectResult result = obsClient.putObject(bucketName, objectKey, file);
//                        debug(objectKey+" Success!");
                        save2File(objectKey);
                        //debug(" start close obs client");
                        //obsClient.close();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                System.exit(-1);
            }

            String tid = String.valueOf(Thread.currentThread().getId());
//            Thread.sleep(50);
            return tid;

        }
    }

    public JobEntryOBSPUT(String n) {
        super( n, "" );
        endPoint = "";
        bucketName = "";
    }
    public JobEntryOBSPUT() {
        this( "" );
    }

    public String getXML() {
        StringBuilder retval = new StringBuilder( 400 );

        retval.append( super.getXML() );

        retval.append( "      " ).append( XMLHandler.addTagValue( "servername", endPoint ) );
        retval.append( "      " ).append( XMLHandler.addTagValue( "bucketname", bucketName ) );
        retval.append( "      " ).append( XMLHandler.addTagValue( "username", ak ) );
        retval.append( "      " ).append(
                XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( sk ) ) );

        retval.append( "      " ).append( XMLHandler.addTagValue( "counter", counter ) );
        retval.append( "      " ).append( XMLHandler.addTagValue( "start_value", startValue ) );
        retval.append( "      " ).append( XMLHandler.addTagValue( "max_value", maxValue ) );
        retval.append( "      " ).append( XMLHandler.addTagValue( "sex_flag", sexFlag ) );

        return retval.toString();
    }

    public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
                        Repository rep, IMetaStore metaStore ) throws KettleXMLException {
        try {
            super.loadXML( entrynode, databases, slaveServers );
            endPoint = XMLHandler.getTagValue( entrynode, "servername" );
            bucketName = XMLHandler.getTagValue( entrynode, "bucketname" );
            ak = XMLHandler.getTagValue( entrynode, "username" );
            sk = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );

            counter = Const.toInt(XMLHandler.getTagValue( entrynode, "counter" ),0);
            startValue = Const.toInt(XMLHandler.getTagValue( entrynode, "start_value" ),0);
            maxValue = Const.toInt(XMLHandler.getTagValue( entrynode, "max_value" ),0);
            sexFlag = XMLHandler.getTagValue( entrynode, "sex_flag" );

        } catch ( KettleXMLException xe ) {
            throw new KettleXMLException( BaseMessages.getString( PKG, "JobFTPSPUT.Log.UnableToLoadFromXml" ), xe );
        }
    }

    public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
                         List<SlaveServer> slaveServers ) throws KettleException {
        try {
            endPoint = rep.getJobEntryAttributeString( id_jobentry, "servername" );
            bucketName = rep.getJobEntryAttributeString( id_jobentry, "bucketname" );
            ak = rep.getJobEntryAttributeString( id_jobentry, "username" );
            sk =
                    Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );


            counter = (int)rep.getJobEntryAttributeInteger( id_jobentry, "counter" );
            startValue = (int)rep.getJobEntryAttributeInteger( id_jobentry, "start_value" );
            maxValue = (int)rep.getJobEntryAttributeInteger( id_jobentry, "max_value" );
            sexFlag = rep.getJobEntryAttributeString( id_jobentry, "sex_flag" );
        } catch ( KettleException dbe ) {
            throw new KettleException( BaseMessages.getString( PKG, "JobFTPSPUT.UnableToLoadFromRepo", String
                    .valueOf( id_jobentry ) ), dbe );
        }
    }

    public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
        try {
            rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", endPoint );
            rep.saveJobEntryAttribute( id_job, getObjectId(), "bucketname", bucketName );
            rep.saveJobEntryAttribute( id_job, getObjectId(), "username", ak );
            rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
                    .encryptPasswordIfNotUsingVariables( sk ) );

            rep.saveJobEntryAttribute( id_job, getObjectId(), "counter", counter );
            rep.saveJobEntryAttribute( id_job, getObjectId(), "start_value", startValue );
            rep.saveJobEntryAttribute( id_job, getObjectId(), "max_value", maxValue );
            rep.saveJobEntryAttribute( id_job, getObjectId(), "sex_flag", sexFlag );

        } catch ( KettleDatabaseException dbe ) {
            throw new KettleException( BaseMessages.getString( PKG, "JobFTPSPUT.UnableToSaveToRepo", String
                    .valueOf( id_job ) ), dbe );
        }
    }


    private static void putFiles(String ak, String sk, String endPoint, String bucketName, String middle_ffix_url, String fileDir) {
        if ( null == obsClient) {
            debug(" start create obs client");
            debug("ak="+ak);
            debug("sk="+sk);
            debug("endPoint="+endPoint);
            obsClient = new ObsClient(ak, sk, endPoint);
        }

        File[] files = new File(fileDir).listFiles();
        System.out.println("total files:"+files.length);
        for(int i=0; i<files.length;i++)
        {
            File file = files[i];
            String filename = file.getName();
            if(filename.indexOf("_")==-1)
            {
                System.out.println("_ not found in "+filename);
                continue;
            }

            int startIndex = filename.indexOf("_")+1;
            int endIndex = filename.indexOf("_")+19;
            String id=filename.substring(startIndex,endIndex);
            String fileSuf = filename.substring(filename.indexOf("."));
            String id_suffix = id.substring(12,18);
            String objectKey;
            int sexFlag = new Integer(id.substring(16,17)).intValue();
            String sexFlagStr;
            if(sexFlag % 2==0)
                sexFlagStr = "2";
            else
                sexFlagStr = "1";
            objectKey =  sexFlagStr+ "_" + middle_ffix_url+"_" + id_suffix+fileSuf;
            System.out.println(objectKey);
            PutObjectResult result = obsClient.putObject(bucketName, objectKey, file);
        }
    }



    /**
     * ∏¥÷∆±æµÿŒƒº˛µΩobs
     * @param copyCount ∏¥÷∆Œƒº˛µƒ¥Œ ˝
     * @param fileDir “™∏¥÷∆Œƒº˛µƒƒø¬º
     * @param bucketn obsÕ∞√˚≥∆
     * @param start obs key÷ÿ√¸√˚µƒø™ º ˝
     * @param max obs key÷ÿ√¸√˚µƒ◊Ó¥Û ˝
     * @param threadNum ‘À––œﬂ≥Ã ˝
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void putFiles(int copyCount,String fileDir,String bucketn,int start,int max,int threadNum) throws InterruptedException, ExecutionException{
        long st=System.currentTimeMillis();
        System.out.println("begin: copyCount="+copyCount+",fileDir="+fileDir+",bucketn="+bucketn+",start="+start+",max="+max+",threadNum="+threadNum);
        bucketName  = bucketn;
        startValue =start;
        maxValue= max;
        if (threadNum == 0){
            threadNum = 8;
        }
        File[] files = new File(fileDir).listFiles();
        System.out.println("total files:"+files.length);
        List<Future<String>> results = new ArrayList<Future<String>>();
        ExecutorService es = Executors.newFixedThreadPool(threadNum);

        for(int i=0; i<files.length;i++)
            for(int j=0; j<copyCount;j++) {
                results.add(es.submit(new Task(files[i])));
            }
        results.add(es.submit(new Task(files[0])));
//        es.shutdown();
//        while(true) {
//            try {
//                if (es.isTerminated()) {
//                    if (null != obsClient) obsClient.close();
//                    long cost = System.currentTimeMillis() - st;
//                    System.out.println("put files cost=" + cost + "ms");
//                    if (null != fileWriter) fileWriter.close();
//                    break;
//                }
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        System.out.println("end putFiles");
    }

    public static void putFiles(int copyCount,String fileDir,int start,int max,int threadNum) throws InterruptedException, ExecutionException{
        long st=System.currentTimeMillis();


        String bucketn = null;

        ResourceBundle resourceBundle = null;
        try{
            String proFilePath = System.getProperty("user.home") + "/.kettle/kettle.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(proFilePath));
            resourceBundle = new PropertyResourceBundle(in);
            bucketn = resourceBundle.getString("bucketName");
            ak = resourceBundle.getString("ak");
            sk = resourceBundle.getString("sk");
            endPoint = resourceBundle.getString("endPoint");

        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("begin: copyCount="+copyCount+",fileDir="+fileDir+",bucketn="+bucketn+",start="+start+",max="+max+",threadNum="+threadNum);
        bucketName  = bucketn;
        startValue =start;
        maxValue= max;
        if (threadNum == 0){
            threadNum = 8;
        }
        File[] files = new File(fileDir).listFiles();
        System.out.println("total files:"+files.length);
        List<Future<String>> results = new ArrayList<Future<String>>();
        ExecutorService es = Executors.newFixedThreadPool(threadNum);

        for(int i=0; i<files.length;i++)
            for(int j=0; j<copyCount;j++) {
                results.add(es.submit(new Task(files[i])));
            }
        results.add(es.submit(new Task(files[0])));
        System.out.println("end putFiles");
    }
    /**
     *
     * @param ak_
     * @param sk_
     * @param endPoint_
     * @param bucketName_
     * @param copyCount
     * @param fileDir
     * @param start
     * @param max
     * @param threadNum
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void putFiles(String ak_,String sk_,String endPoint_,String bucketName_,
                                int copyCount,String fileDir,int start,int max,int threadNum) throws InterruptedException, ExecutionException{
        long st=System.currentTimeMillis();
        String bucketn = null;
        ResourceBundle resourceBundle = null;
        try{
            String proFilePath = System.getProperty("user.home") + "/.kettle/kettle.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(proFilePath));
            resourceBundle = new PropertyResourceBundle(in);
            if(null == ak_ || ak_.length() == 0)  {
                ak = resourceBundle.getString("ak");
                System.out.println(" load parameters from kettle.propers file ak");
                save2File(" load parameters from kettle.propers file ak");
            }else{
                ak = ak_;
            }
            if(null == sk_ || sk_.length() == 0)  {
                sk = resourceBundle.getString("sk");
                System.out.println(" load parameters from kettle.propers file sk");
                save2File(" load parameters from kettle.propers file sk");
            }else{
                sk = sk_;
            }
            if(null == endPoint_ || endPoint_.length() == 0)  {
                endPoint = resourceBundle.getString("endPoint");
                System.out.println(" load parameters from kettle.propers file endPoint_");
                save2File(" load parameters from kettle.propers file endPoint");
            }else{
                endPoint = endPoint_;
            }
            if(null == bucketName_ || bucketName_.length() == 0) {
                bucketn = resourceBundle.getString("bucketName");
                System.out.println(" load parameters from kettle.propers file bucketName");
                save2File(" load parameters from kettle.propers file bucketName");
            }else {
                bucketn = bucketName_;
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("begin: " +
                " ak="+ak+",sk="+sk+",endPoint="+endPoint+",bucketn="+bucketn+
                ",copyCount="+copyCount+",fileDir="+fileDir+",bucketn="+bucketn+
                ",start="+start+",max="+max+",threadNum="+threadNum
        );
        bucketName  = bucketn;
        startValue =start;
        maxValue= max;
        if (threadNum == 0){
            threadNum = 8;
        }
        File[] files = new File(fileDir).listFiles();
        System.out.println("total files:"+files.length);
        List<Future<String>> results = new ArrayList<Future<String>>();
        ExecutorService es = Executors.newFixedThreadPool(threadNum);

        for(int i=0; i<files.length;i++)
            for(int j=0; j<copyCount;j++) {
                results.add(es.submit(new Task(files[i])));
            }
        results.add(es.submit(new Task(files[0])));
        System.out.println("end putFiles");
    }

    public static void putFiles(String ak_,String sk_,String endPoint_,String bucketName_,
                                int copyCount,String fileDir,int start,int max,int threadNum,String sexFlag_) throws InterruptedException, ExecutionException{
        sexFlag = sexFlag_;
        System.out.println("sexFlag:0-man,1-feman");
        putFiles( ak_, sk_, endPoint_, bucketName_,
                copyCount, fileDir, start, max, threadNum);
    }
    /**
     * …˙≥…≤ª÷ÿ∏¥ÀÊª˙◊÷∑˚¥Æ∞¸¿®◊÷ƒ∏ ˝◊÷
     *
     * @param len ≥§∂»
     * @return
     */
    public static String generateRandomStr(int len) {
        //◊÷∑˚‘¥£¨ø…“‘∏˘æ›–Ë“™…æºı
        String generateSource = "0123456789abcdefghigklmnopqrstuvwxyz";
        String rtnStr = "";
        for (int i = 0; i < len; i++) {
            //—≠ª∑ÀÊª˙ªÒµ√µ±¥Œ◊÷∑˚£¨≤¢“∆◊ﬂ—°≥ˆµƒ◊÷∑˚
            String nowStr = String.valueOf(generateSource.charAt((int) Math.floor(Math.random() * generateSource.length())));
            rtnStr += nowStr;
            generateSource = generateSource.replaceAll(nowStr, "");
        }
        return rtnStr;
    }
    public static synchronized int getNextValue()
    {
        if(counter>maxValue)
            counter=0;
        return startValue+counter++;
    }

    public static synchronized  void save2File(String f)
    {
        if(fileWriter==null)
        {
            try {
                File file = new File("./logs");
                if (!file.exists()) file.mkdirs();
                fileWriter=new FileWriter("./logs/log"+generateRandomStr(6)+".txt",true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fileWriter.write(f);
            fileWriter.write(System.getProperty("line.separator"));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ;
    }

    public static void debug (String s)
    {
        if(isDebug && s!=null)
            System.out.println(s);
    }

    static boolean isDebug=false;

    public Result execute( Result previousResult, int nr ) {
        Result result = previousResult;
        result.setResult( false );
        long filesput = 0;

        if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.Starting" ) );
        }
        return null;
//        FTPClient ftpclient = null;
//        try {
//            // Create ftp client to host:port ...
//            ftpclient = createAndSetUpFtpClient();
//
//            // login to ftp host ...
//            String realUsername = environmentSubstitute( userName );
//            String realPassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
//            ftpclient.connect();
//            ftpclient.login( realUsername, realPassword );
//
//            // set BINARY
//            if ( binaryMode ) {
//                ftpclient.setType( FTPTransferType.BINARY );
//                if ( log.isDetailed() ) {
//                    logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.BinaryMode" ) );
//                }
//            }
//
//            // Remove password from logging, you don't know where it ends up.
//            if ( log.isDetailed() ) {
//                logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.Logged", realUsername ) );
//            }
//
//            // Fix for PDI-2534 - add auxilliary FTP File List parsers to the ftpclient object.
//            this.hookInOtherParsers( ftpclient );
//
//            // move to spool dir ...
//            String realRemoteDirectory = environmentSubstitute( remoteDirectory );
//            if ( !Utils.isEmpty( realRemoteDirectory ) ) {
//                ftpclient.chdir( realRemoteDirectory );
//                if ( log.isDetailed() ) {
//                    logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.ChangedDirectory", realRemoteDirectory ) );
//                }
//            }
//
//            String realLocalDirectory = environmentSubstitute( localDirectory );
//            if ( realLocalDirectory == null ) {
//                throw new FTPException( BaseMessages.getString( PKG, "JobFTPPUT.LocalDir.NotSpecified" ) );
//            } else {
//                // handle file:/// prefix
//                if ( realLocalDirectory.startsWith( "file:" ) ) {
//                    realLocalDirectory = new URI( realLocalDirectory ).getPath();
//                }
//            }
//
//            final List<String> files;
//            File localFiles = new File( realLocalDirectory );
//            File[] children = localFiles.listFiles();
//            if ( children == null ) {
//                files = Collections.emptyList();
//            } else {
//                files = new ArrayList<String>( children.length );
//                for ( File child : children ) {
//                    // Get filename of file or directory
//                    if ( !child.isDirectory() ) {
//                        files.add( child.getName() );
//                    }
//                }
//            }
//            if ( log.isDetailed() ) {
//                logDetailed( BaseMessages.getString(
//                        PKG, "JobFTPPUT.Log.FoundFileLocalDirectory", "" + files.size(), realLocalDirectory ) );
//            }
//
//            String realWildcard = environmentSubstitute( wildcard );
//            Pattern pattern;
//            if ( !Utils.isEmpty( realWildcard ) ) {
//                pattern = Pattern.compile( realWildcard );
//            } else {
//                pattern = null;
//            }
//
//            for ( String file : files ) {
//                if ( parentJob.isStopped() ) {
//                    break;
//                }
//
//                boolean toBeProcessed = true;
//
//                // First see if the file matches the regular expression!
//                if ( pattern != null ) {
//                    Matcher matcher = pattern.matcher( file );
//                    toBeProcessed = matcher.matches();
//                }
//
//                if ( toBeProcessed ) {
//                    // File exists?
//                    boolean fileExist = false;
//                    try {
//                        fileExist = ftpclient.exists( file );
//                    } catch ( Exception e ) {
//                        // Assume file does not exist !!
//                    }
//
//                    if ( log.isDebug() ) {
//                        if ( fileExist ) {
//                            logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.FileExists", file ) );
//                        } else {
//                            logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.FileDoesNotExists", file ) );
//                        }
//                    }
//
//                    if ( !fileExist || !onlyPuttingNewFiles ) {
//                        if ( log.isDebug() ) {
//                            logDebug( BaseMessages.getString(
//                                    PKG, "JobFTPPUT.Log.PuttingFileToRemoteDirectory", file, realRemoteDirectory ) );
//                        }
//
//                        String localFilename = realLocalDirectory + Const.FILE_SEPARATOR + file;
//                        ftpclient.put( localFilename, file );
//
//                        filesput++;
//
//                        // Delete the file if this is needed!
//                        if ( remove ) {
//                            new File( localFilename ).delete();
//                            if ( log.isDetailed() ) {
//                                logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.DeletedFile", localFilename ) );
//                            }
//                        }
//                    }
//                }
//            }
//
//            result.setResult( true );
//            if ( log.isDetailed() ) {
//                logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.WeHavePut", "" + filesput ) );
//            }
//        } catch ( Exception e ) {
//            result.setNrErrors( 1 );
//            logError( BaseMessages.getString( PKG, "JobFTPPUT.Log.ErrorPuttingFiles", e.getMessage() ) );
//            logError( Const.getStackTracker( e ) );
//        } finally {
//            if ( ftpclient != null && ftpclient.connected() ) {
//                try {
//                    ftpclient.quit();
//                } catch ( Exception e ) {
//                    logError( BaseMessages.getString( PKG, "JobFTPPUT.Log.ErrorQuitingFTP", e.getMessage() ) );
//                }
//            }
//
//            FTPClient.clearSOCKS();
//        }
//
//        return result;
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int copyCount = 1;
        fileDir = "E:\\felix\\demos\\test\\src\\main\\resources\\obsPutFileDir";

        if (args.length == 4){
            copyCount = Integer.parseInt(args[0]);
            fileDir = args[1];
            bucketName = args[2];
            int threadNum = Integer.parseInt(args[3]);
            //putFiles(copyCount,fileDir,bucketName,threadNum);
        }
        else if(args.length == 5){
            copyCount = Integer.parseInt(args[0]);
            fileDir = args[1];
            int start = Integer.parseInt(args[2]);
            int max = Integer.parseInt(args[3]);
            int threadNum = Integer.parseInt(args[4]);
            putFiles(copyCount,fileDir,start,max,threadNum);
        }
        else if(args.length == 6){
            String ak_= args[0];
            String sk_= args[1];
            String endPoint_= args[2];
            String bucketName_= args[3];
            String middle_ffix_url = args[4];
            fileDir = args[5];
            putFiles(ak_, sk_, endPoint_, bucketName_,middle_ffix_url,fileDir);
        }
        else if(args.length == 9){
            String ak_= args[0];
            String sk_= args[1];
            String endPoint_= args[2];
            String bucketName_= args[3];
            copyCount = Integer.parseInt(args[4]);
            fileDir = args[5];
            int start = Integer.parseInt(args[6]);
            int max = Integer.parseInt(args[7]);
            int threadNum = Integer.parseInt(args[8]);
            putFiles(ak_, sk_, endPoint_, bucketName_,
                    copyCount, fileDir, start, max, threadNum);
        }
        else if(args.length == 10){
            String ak_= args[0];
            String sk_= args[1];
            String endPoint_= args[2];
            String bucketName_= args[3];
            copyCount = Integer.parseInt(args[4]);
            fileDir = args[5];
            int start = Integer.parseInt(args[6]);
            int max = Integer.parseInt(args[7]);
            int threadNum = Integer.parseInt(args[8]);
            String sexFlag_ = args[9];
            putFiles(ak_, sk_, endPoint_, bucketName_,
                    copyCount, fileDir, start, max, threadNum,sexFlag_);
        }
    }

}

