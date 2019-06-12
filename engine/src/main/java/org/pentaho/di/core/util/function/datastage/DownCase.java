package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class DownCase extends Operator{
    /**·
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 所有字符转换成大写
     * @param str
     * @return
     */
    public static String evaluate(String str){
        return str.toLowerCase();
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if(o == null){
            return null;
        }
        else{
            return evaluate(o.toString());
        }
    }
}
