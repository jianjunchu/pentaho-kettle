package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class HalfMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;

    public String evaluate(String name) {
        String str = name;
        if (str == null) {
            return "";
        }
        if (str.length() < 2) {
            return str;
        }
        String ret = "";
        int len = str.length() / 2;
        ret = str.substring(0, len);
        for (int i = 0; i < str.length() - len; i++) {
            ret += "*";
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
        HalfMask mask = new HalfMask();
        System.out.println(mask.evaluate("北京傲飞商智软件有限公司"));
    }
}
