package org.pentaho.di.core.util.function.datastage;

import org.pentaho.di.core.util.function.BaseFunction;

import java.util.regex.Pattern;

public class Alpha extends BaseFunction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param str
     * @return
     */
    public static Integer evaluate(String str){
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        if(pattern.matcher(str).matches()){
            return 1;
        }else{
            return -1;
        }
    }

    @Override
    public Integer executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if(o == null ){
            return null;
        }else{
            return evaluate(o.toString());
        }
    }
}
