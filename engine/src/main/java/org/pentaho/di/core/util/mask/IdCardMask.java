package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class IdCardMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;


    public String evaluate(String id) {
        String str = id;
        if (str == null) {
            return "";
        }
        if (str.length() < 6) {
            return str;
        }
        if (str.length() >= 6) {
            return str.substring(0, str.length() - 6) + "******";
        }
        return "";
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
        IdCardMask mask = new IdCardMask();
        System.out.println(mask.evaluate("362202198802109999"));
    }
}
