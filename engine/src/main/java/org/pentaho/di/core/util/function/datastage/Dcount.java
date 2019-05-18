package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Dcount extends Operator{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int evaluate(String str1, String str2){
        Matcher matcher= Pattern.compile(str2).matcher(str1);
        int i=0;
        while (matcher.find()) {
            i++;
        }
        return i;
    }


    @Override
    public Integer executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        Object o1 = objects[1];
        if(o == null || o1==null){
            return null;
        }else{
            return evaluate(o.toString(),o1.toString());
        }
    }
}

