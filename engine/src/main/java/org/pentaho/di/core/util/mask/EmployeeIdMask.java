package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class EmployeeIdMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;



    @Override
    public String executeInner(Object[] objects) throws Exception {
        String o = objects[0].toString();
        int start = Integer.parseInt(objects[1].toString());
        int end = Integer.parseInt(objects[2].toString());
        if(o == null){
            return null;
        }else{
            return evaluate(o);
        }
    }

    public String evaluate(String name) {
        String str = name;
        if (str == null) {
            return "";
        }
        if (str.length() < 4) {
            return str;
        } else {
            return str.substring(0, str.length()-4) + "****";
        }
    }

    public static void main(String[] args) {
        EmployeeIdMask mask = new EmployeeIdMask();
        System.out.println(mask.evaluate("E1177777"));
    }
}
