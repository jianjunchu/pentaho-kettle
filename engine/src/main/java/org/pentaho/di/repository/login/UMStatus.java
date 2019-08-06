package org.pentaho.di.repository.login;

public enum UMStatus
{
    SUCCESS(0,"UserManager.Status.Success"),
    USER_NAME_EXIST(SUCCESS.statusCode+1,"UserManager.Status.UserNameExist"),
    USER_NOT_EXIST(USER_NAME_EXIST.statusCode+1,"UserManager.Status.UserNotExist"),
    UNKNOWN_ERROR(USER_NOT_EXIST.statusCode+1,"UserManager.Status.UnknownError"),
    DATABASE_EXCEPTION(UNKNOWN_ERROR.statusCode+1,"UserManager.Status.DatabaseException"),
    ROLE_NAME_EXIST(DATABASE_EXCEPTION.statusCode+1,"UserManager.Status.RoleNameExist"),
    WRONG_PASSWORD(ROLE_NAME_EXIST.statusCode+1,"UserManager.Status.WrongPassword"),
    NICK_NAME_EXIST(WRONG_PASSWORD.statusCode+1,"UserManager.Status.NickNameExist"),
    USER_NOT_ACTIVE(NICK_NAME_EXIST.statusCode+1,"UserManager.Status.UserNotActive");

    final int statusCode ;
    final String statusMessage ;

    UMStatus(int statusCode, String messageProperty)
    {
        this.statusCode = statusCode ;
        //this.statusMessage = Messages.getString(messageProperty)  ;
        this.statusMessage = messageProperty ;
    }

    public int getStatusCode()
    {
        return this.statusCode ;
    }

    public String getStatusMessage()
    {
        return statusMessage ;
    }
    public String toJsonString()
    {
        StringBuffer jsonStringBuffer = new StringBuffer(512) ;
        jsonStringBuffer.append("{\"statusCode\":\"").append(statusCode).append("\"").
                append(",\"statusMessage\":\"").append(getStatusMessage()).append("\"}") ;
        return jsonStringBuffer.toString() ;
    }
    public static UMStatus getStatus(int statusCode)
    {
        UMStatus[] status = UMStatus.values() ;
        for (int i = 0 ; i < status.length ; i ++)
        {
            if (status[i].statusCode == statusCode)
                return status[i] ;
        }
        return null ;
    }
}
