package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class LicenceMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;

    public String evaluate(String name) {
        String str = name;
        if (str == null) {
            return "";
        }
        String ret = "";

        if (str.length() >= 14) {
            ret = str.substring(0, 6) + "********" + str.substring(14);
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
        LicenceMask mask = new LicenceMask();
        System.out.println(mask.evaluate("1234567890abcdefghi"));
    }

}
