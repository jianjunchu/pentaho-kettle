package org.pentaho.di.core.util.function.datastage;

import org.pentaho.di.core.util.function.BaseFunction;

public class Compare extends BaseFunction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 比较字符串大小，按照ascII比较方式
     * @param str1
     * @param str2
     * @param str3
     * @return
     */
    public static Integer evaluate(String str1, String str2, String str3){
        int max1 = str1.length();
        int max2 = str2.length();
        int result1 = 0;
        int result2 = 0;
        for(int i=0;i<max1;i++){
            char a = str1.charAt(i);
            int b = (int)a;
            result1 = result1+b;
        }
        for(int j=0;j<max2;j++){
            char c = str2.charAt(j);
            int d = (int)c;
            result2 = result2+d;
        }
        if(result1>result2){
            return 1;
        }else if(result1<result2){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public Integer executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        Object o1 = objects[1];
        Object o2 = objects[2];
        if(o == null || o1 == null || o2 == null){
            return null;
        }else{
            return evaluate(o.toString(),o1.toString(),o2.toString());
        }
    }
}
