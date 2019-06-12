package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class Space extends Operator{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param integer
     * @return
     */
    public static String evaluate(Integer integer){
        StringBuffer stringBuffer=new StringBuffer();
        for (int i=0;i<integer;i++) {
            stringBuffer.append(" ");
        }
        return  stringBuffer.toString();
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if(o == null){
            return null;
        }else if (o instanceof Integer){
            return evaluate((Integer) o);
        }else {
            throw new Exception("Space function: Data type error["+o.getClass()+"]");
        }
    }
}

