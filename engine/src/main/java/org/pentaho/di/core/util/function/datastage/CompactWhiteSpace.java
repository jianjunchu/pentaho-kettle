package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

import java.util.regex.Pattern;

public class CompactWhiteSpace extends Operator {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param string1
     * @return
     */
    public static String evaluate(String string1) {
        return Pattern.compile("\\s+").matcher(string1).replaceAll(" ");

    }

    @Override
    public String executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if (o == null) {
            return null;
        } else {
            return evaluate(o.toString());
        }
    }
}

