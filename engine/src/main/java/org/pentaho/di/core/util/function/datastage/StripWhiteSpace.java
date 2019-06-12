package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class StripWhiteSpace extends Operator{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param string
     * @return
     */
    public static String evaluate(String string){
        return  string.replace(" ","");
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if(o == null){
            return null;
        }else{
            return evaluate(o.toString());
        }
    }
}

