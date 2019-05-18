package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class AddressMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;

    /**
     * 地址混淆
     * @param
     * @return
     */
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
        Object o = objects[0];
        if(o == null){
            return null;
        }else{
            return evaluate(o.toString());
        }
    }

    public static void main(String[] args){
        AddressMask mask=new AddressMask();
        System.out.println(mask.evaluate("浙江省杭州市西湖区剑南街道190号翡翠小区405"));
    }
}
