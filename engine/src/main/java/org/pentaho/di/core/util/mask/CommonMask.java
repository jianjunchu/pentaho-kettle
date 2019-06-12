package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class CommonMask extends BaseFunction {



    private static final long serialVersionUID = 1L;

    /**
     * @param
     * @return
     */
    public String evaluate(String source, int start, int end)  {
        String str = source;
        if (str == null) {
            return "";
        }
        if (start < 0 || end < 0 || end < start || end > source.length()) {
            return str;
        }

        if (str.length() < end - start) {
            return str;
        }
        String middle = "";
        for (int i = 0; i < end - start; i++) {
            middle += "*";
        }
        String ret = str.substring(0, start) + middle + str.substring(end);

        return ret;
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0].toString();
        int start = Integer.parseInt(objects[1].toString());
        int end = Integer.parseInt(objects[2].toString());
        if(o == null){
            return null;
        }else{
            return evaluate(o.toString(),start,end);
        }
    }

    public static void main(String[] args){
        AddressMask mask=new AddressMask();
        System.out.println(mask.evaluate("浙江省杭州市西湖区剑南街道190号翡翠小区405"));
    }
}
