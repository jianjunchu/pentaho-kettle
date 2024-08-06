package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;
@DatabaseMetaPlugin( type = "aliyunOdps", typeDescription = "Aliyun Odps" )
public class AliyunOdpsDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface{
    @Override
    public String getFieldDefinition(ValueMetaInterface valueMetaInterface, String s, String s1, boolean b, boolean b1, boolean b2) {
        return null;
    }

    @Override
    public int[] getAccessTypeList() {
        return new int[] {
                DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
    }

    @Override
    public String getDriverClass() {
        return "com.aliyun.odps.jdbc.OdpsDriver";
    }

    @Override
    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException {
        return "jdbc:odps:" + hostname + "?project=" + databaseName;
    }

    @Override
    public boolean isUsingConnectionPool() {
        return false;
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public String getAddColumnStatement(String s, ValueMetaInterface valueMetaInterface, String s1, boolean b, String s2, boolean b1) {
        return "";
    }

    @Override
    public String getModifyColumnStatement(String s, ValueMetaInterface valueMetaInterface, String s1, boolean b, String s2, boolean b1) {
        return "";
    }

    @Override
    public String[] getUsedLibraries() {
        return new String[0];
    }
}
