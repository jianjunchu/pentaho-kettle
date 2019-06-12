package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class Left extends Operator{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 从左边取n位字符串
     * @param str
     * @param integer
     * @return
     */
    public static String evaluate(String str, Integer integer){
        if(str.length()>integer){
            str = str.substring(0,integer);
        }
        return str;
    }
    public static String evaluate(String str, java.lang.Long lon){
        if(str.length()>lon){
            str = str.substring(0,lon.intValue());
        }
        return str;
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        Object o1 = objects[1];
        if(o == null || o1 == null){
            return null;
        }
        if(o1 instanceof Integer){
            return evaluate(o.toString() , (Integer)o1);
        }else if(o1 instanceof Long){
            return evaluate(o.toString() , (Long)o1);
        }else{
            throw new Exception("Left function: Data type error["+o1.getClass()+"]");
        }
    }
}
