package org.pentaho.di.repository.login;

public class RepositoryBean {

    private int repositoryID;
    private String repositoryName;
    private String userName;
    private String password;
    private String version;
    private String dbHost;
    private String dbName;
    private String dbType;
    private String dbAccess;
    private String dbPort;
    private String rep_username ;
    private String rep_password ;
    private String url;
    private String driverClassName;
    private int orgId;

    public int getRepositoryID() {
        return repositoryID;
    }
    public void setRepositoryID(int repositoryID) {
        this.repositoryID = repositoryID;
    }
    public String getRepositoryName() {
        return repositoryName;
    }
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getDbHost() {
        return dbHost;
    }
    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }
    public String getDbName() {
        return dbName;
    }
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    public String getDbType() {
        return dbType;
    }
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
    public String getDbAccess() {
        return dbAccess;
    }
    public void setDbAccess(String dbAccess) {
        this.dbAccess = dbAccess;
    }
    public String getDbPort() {
        return dbPort;
    }
    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }
    public int getOrgId() {
        return orgId;
    }
    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getRep_username() {
        return rep_username;
    }

    public void setRep_username(String rep_username) {
        this.rep_username = rep_username;
    }

    public String getRep_password() {
        return rep_password;
    }

    public void setRep_password(String rep_password) {
        this.rep_password = rep_password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}
