package org.pentaho.di.core.util.function.datastage;

import org.pentaho.di.core.util.function.BaseFunction;

import java.util.regex.Pattern;

public class AlNum extends BaseFunction {
    private static final long serialVersionUID = 1L;


    public static Integer evaluate(String str){
        Pattern pattern = Pattern.compile("\\w*");
        if(pattern.matcher(str).matches() && !str.isEmpty()){
            return 1;
        }else{
            return -1;
        }
    }

    @Override
    public Integer executeInner(Object[] objects) throws Exception {
        String o = objects[0].toString();
        if(o == null){
            return null;
        }else{
            return evaluate(o);
        }
    }

}
