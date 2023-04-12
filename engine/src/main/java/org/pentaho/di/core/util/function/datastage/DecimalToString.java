package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

public class DecimalToString extends Operator {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * str有多余0则补0，str无多余0则不处理
     * @param string1
     * @return
     */
    public static String evaluate(String string1) throws Exception {
        Double.valueOf(string1);

        if (string1.indexOf('0')==0 || string1.lastIndexOf('0')==string1.length()-1 ) {
            return evaluate(string1, "fix_zero");
        }

        return string1;

    }

    /**
     * str2为suppress_zero时，去掉左右多余的0,str2为fix_zero时，补0
     * @param string1
     * @param string2
     * @return
5     */
    public static String evaluate(String string1, String string2) throws Exception {
        int len = string1.length();
        int st = 0;
        char[] val = string1.toCharArray();    /* avoid getfield opcode */

        while ((st < len) && (val[st] == '0')) {
            st++;
        }
        while ((st < len) && (val[len - 1] == '0')) {
            len--;
        }
        string1= ((st > 0) || (len < val.length)) ? string1.substring(st, len) : string1;

        if("fix_zero".equals(string2.toLowerCase())){
            StringBuffer sb=new StringBuffer(string1);

            while(sb.indexOf(".")< 28){
                sb.insert(0,'0');
            }
            while(sb.length()-1-sb.indexOf(".")< 10){
                sb.append('0');
            }

            string1=sb.toString();

        }else if("suppress_zero".equals(string2.toLowerCase())){
            if(string1.indexOf(".") == string1.length()-1){
                string1 = string1.substring(0,string1.length()-1);
            }
        }else{
//            throw new Exception("DecimalToString function: Parameter2's value error["+string2.toString()+"], possible values: fix_zero, suppress_zero");
            throw new Exception("DecimalToString function: Parameter2's value error["+ string2 +"], possible values: fix_zero, suppress_zero");
        }
        return string1;
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        int length = objects.length;
        Object o = objects[0];
        if(o == null){
            return null;
        }else if (o instanceof Double){
            switch (length) {
                case 1:
                    return evaluate(o.toString());
                case 2:
                    return evaluate(o.toString(), String.valueOf(objects[1]));
                default:
                    return null;
            }
        }else {throw new Exception("DecimalToString function: Data type error["+o.getClass()+"]");}

    }

}