package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class PadString extends Operator{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param string1
     * @param string2
     * @param integer
     * @return
     */
    public static String evaluate(String string1, String string2, Integer integer){
        StringBuffer stringBuffer=new StringBuffer(string1);

        if (!string2.isEmpty()) {
            string2=string2.substring(0,1);
            for (int i=0;i<integer;i++) {
                stringBuffer.append(string2);
            }
        }
        return  stringBuffer.toString();
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        Object o1 = objects[1];
        Object o2 = objects[2];
        if(o == null || o1==null || o2==null){
            return null;
        }else if (o2 instanceof Integer){
            return evaluate(o.toString(),o1.toString(),(Integer)o2);
        }else {throw new Exception("PadString function: Data type error["+o2.getClass()+"]");}
    }
}

