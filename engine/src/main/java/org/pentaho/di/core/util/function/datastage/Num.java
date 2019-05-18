package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class Num extends Operator{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param str
     * @return
     */
    public static int evaluate(String str){
        try {
            Double.valueOf(str);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }


    @Override
    public Integer executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if(o == null){
            return null;
        }else{
            return evaluate(o.toString());
        }
    }
}

