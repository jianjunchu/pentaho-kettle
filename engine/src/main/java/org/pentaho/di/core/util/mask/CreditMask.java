package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class CreditMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;

    public String evaluate(String name) {
        String str = name;
        if (str == null) {
            return "";
        }
        String ret = "";

        if (str.length() >= 17) {
            ret = str.substring(0, 8) + "*********" + str.substring(17);
        } else {
            return str;
        }

        return ret;
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        String o = objects[0].toString();
        if(o == null){
            return null;
        }else{
            return evaluate(o);
        }
    }

    public static void main(String[] args) {
        CreditMask mask = new CreditMask();
        System.out.println(mask.evaluate("1234567890abcdefghi"));
    }

}
