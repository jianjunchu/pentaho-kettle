package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class PhoneMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;

    public String evaluate(String phone) {// 电话号码加密
        String str = phone;
        if (str == null) {
            return "";
        }
        if (str.length() >= 7) {
            return str.substring(0, str.length() - 4) + "****";
        }
        return "";
    }


    @Override
    public String executeInner(Object[] objects) throws Exception {
        String o = objects[0].toString();
        if(o == null){
            return null;
        }else{
            return evaluate(o.toString());
        }
    }

    public static void main(String[] args) {
        PhoneMask mask = new PhoneMask();
        System.out.println(mask.evaluate("15020006666"));
    }
}
