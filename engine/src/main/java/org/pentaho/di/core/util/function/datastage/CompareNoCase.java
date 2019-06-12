package org.pentaho.di.core.util.function.datastage;

import org.pentaho.di.core.util.function.BaseFunction;

public class CompareNoCase extends BaseFunction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 比较字符串大小，按照ascII比较方式
     * @param str1
     * @param str2
     * @return
     */
    public static Integer evaluate(String str1, String str2){
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
        if(o == null || o1 == null){
            return null;
        }else{
            return evaluate(o.toString(),o1.toString());
        }
    }
}
