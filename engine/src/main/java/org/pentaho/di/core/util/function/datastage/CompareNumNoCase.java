package org.pentaho.di.core.util.function.datastage;

import org.pentaho.di.core.util.function.BaseFunction;

public class CompareNumNoCase extends BaseFunction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 比较传入的参数，如果绝对相同为0，不相等为-1.比较两个字符串中的前 n 个字符。
     * @param str1
     * @param str2
     * @param in
     * @return
     */
    public static Integer evaluate(String str1, String str2, Integer in){
        if(str1.length()>in){
            str1 = str1.substring(0, in);
        }
        if(str2.length()>in){
            str2 = str2.substring(0, in);
        }

        if(str1.equals(str2)){
            return 0;
        }else{
            return -1;
        }
    }

    @Override
    public Integer executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        Object o1 = objects[1];
        Object o2 = objects[2];
        if(o == null || o1 == null || o2 == null){
            return null;
        }else if(o2 instanceof Integer){
            return evaluate(o.toString(),o1.toString(),(Integer) o2);
        }else{
            throw new Exception("CompareNum function: Data type error["+o2.getClass()+"]");
        }

    }
}
