package org.pentaho.di.core.util.mask;
import org.pentaho.di.core.util.function.BaseFunction;

public class MoneyMask  extends BaseFunction {



    private static final long serialVersionUID = 1L;



    public String evaluate(String money) {
        String str = money;
        if (str == null) {
            return "";
        }
        int index = str.indexOf(".");
        String ret = "";
        String last = "";
        if (str.length() < 2) {
            return money;
        }
        if (index != -1) {
            last = str.substring(index);
            str = str.substring(0, index);
            int len = str.length() / 2;
            for (int i = 0; i < len; i++) {
                ret += "*";
            }
            ret += str.substring(len);
            ret += last;
        } else {
            int len = str.length() / 2;
            for (int i = 0; i < len; i++) {
                ret += "*";
            }
            ret += str.substring(len);
            ret += last;
        }

        return ret;
    }



    public String evaluate(double money) {
        String str = ""+money;

        int index = str.indexOf(".");
        String ret = "";
        String last = "";
        if (str.length() < 2) {
            return str;
        }

        if (index != -1) {
            last = str.substring(index);
            str = str.substring(0, index);
            int len = str.length() / 2;
            for (int i = 0; i < len; i++) {
                ret += "*";
            }
            ret += str.substring(len);
            ret += last;
        } else {
            int len = str.length() / 2;

            for (int i = 0; i < len; i++) {
                ret += "*";
            }
            ret += str.substring(len);
            ret += last;
        }

        return ret;
    }

    public String evaluate(int money) {
        String str = String.valueOf(money);
        if (str == null) {
            return "";
        }
        int index = str.indexOf(".");
        String ret = "";
        String last = "";
        if (str.length() < 2) {
            return str;
        }

        if (index != -1) {
            last = str.substring(index);
            str = str.substring(0, index);
            int len = str.length() / 2;
            for (int i = 0; i < len; i++) {
                ret += "*";
            }
            ret += str.substring(len);
            ret += last;
        } else {
            int len = str.length() / 2;

            for (int i = 0; i < len; i++) {
                ret += "*";
            }
            ret += str.substring(len);
            ret += last;
        }

        return ret;
    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0].toString();
        if(o == null){
            return null;
        }else
            try{
            int i = Integer.parseInt(o.toString());
                return evaluate(i);
        }catch(Exception e){e.printStackTrace();}

        try{
            double d = Double.parseDouble(o.toString());
            return evaluate(d);
        }catch(Exception e){e.printStackTrace();}

        return evaluate(o.toString());
        }



    public static void main(String[] args) {
        MoneyMask mask = new MoneyMask();
        System.out.println(mask.evaluate("11055550"));
        System.out.println(mask.evaluate(1105550));
        System.out.println(mask.evaluate(11050.0));
    }

}
