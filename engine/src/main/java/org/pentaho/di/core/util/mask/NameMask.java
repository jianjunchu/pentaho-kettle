package org.pentaho.di.core.util.mask;

import org.pentaho.di.core.util.function.BaseFunction;

public class NameMask extends BaseFunction {

    private static final long serialVersionUID = 1L;

    public String evaluate(String name) {
        String str = name;
        if (str == null) {
            return "";
        }
        if (str.length() == 1) {
            return str;
        } else if (str.length() == 2) {
            return str.substring(0, 1) + "*";
        } else if (str.length() > 2) {
            return str.substring(0, 1) + "**" + str.substring(3);
        }
        return "";
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

    public static void main(String[] args) {
        NameMask mask = new NameMask();
        System.out.println(mask.evaluate("李海斌德"));
    }
}
