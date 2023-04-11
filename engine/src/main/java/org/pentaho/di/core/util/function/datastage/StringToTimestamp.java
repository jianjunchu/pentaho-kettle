package org.pentaho.di.core.util.function.datastage;


import com.ql.util.express.Operator;

import java.util.Calendar;
import java.util.Date;

public class StringToTimestamp extends Operator{

    private static final long serialVersionUID = 1L;

    /**
     * StringToTimestamp('19261212 090102',"%yyyy%mm%dd %hh%nn%ss")
     * @param string
     * @param format
     * @return date
     * @throws Exception
     */
    public Date stringToTimestamp(String string, String format) throws Exception {
        Date date = new Date();
        int year=0;
        int length=string.length();
        //int formatIndex=0;
        int status=0;
        //char c = format.charAt(formatIndex);
        StringBuffer segBuffer = new StringBuffer();
        String previousSeg;
        String[] segments = format.split("[%|$]");
        Integer offset=0;
        Calendar calendar = Calendar.getInstance();
        for(int i=0;i<segments.length;i++)
        {
            String segment = segments[i];
            if(segment.length()==0)
                continue;
            StringBuffer tagBuffer = new StringBuffer();
            StringBuffer optionBuffer = new StringBuffer();
            StringBuffer literalBuffer = new StringBuffer();
            if (segment.indexOf("(")>-1)
            {
                if(segment.indexOf(",")>-1) {
                    tagBuffer.append(segment, segment.indexOf("(") + 1, segment.indexOf(","));
                    optionBuffer.append(segment, segment.indexOf(",")+1, segment.indexOf(")"));
                }
                else
                    tagBuffer.append(segment, segment.indexOf("(")+1, segment.indexOf(")"));
                //now only support one option!
                if(segment.length() > segment.indexOf("(")+1)
                {
                    literalBuffer = new StringBuffer();
                    literalBuffer.append(segment.substring(segment.indexOf(")")+1));
                }
            }else
            {
                if(segment.startsWith(dateTags.yyyy.name()))
                {
                    tagBuffer.append(dateTags.yyyy.name());
                }
                else if(segment.startsWith(dateTags.yy.name()))
                {
                    tagBuffer.append(dateTags.yy.name());
                }
                else if(segment.startsWith(dateTags.eeee.name()))
                {
                    tagBuffer.append(dateTags.eeee.name());
                }else if(segment.startsWith(dateTags.eee.name()))
                {
                    tagBuffer.append(dateTags.eee.name());
                }
                else if(segment.startsWith(dateTags.NNNNyy.name()))
                {
                    tagBuffer.append(dateTags.NNNNyy.name());
                }
                else {// other tags
                    int index = 0;
                    char c = segment.charAt(index);
                    String tempString = "";
                    tempString += c;
                    while (c > 0 && ++index < segment.length() && isTag(tempString)) {
                        c = segment.charAt(index);
                        tempString += c;
                    }

                    if (isTag(tempString))
                        tagBuffer.append(tempString);
                    else
                        tagBuffer.append(tempString, 0, tempString.length() - 1);
                }
                literalBuffer.append(segment.substring(tagBuffer.length()));

            }
            dateTags tag = dateTags.valueOf(tagBuffer.toString());

            switch(tag)
            {
                case d:
                {
                    if (optionBuffer!=null& optionBuffer.length()>0)
                    {
                        dateOptions option = dateOptions.valueOf(optionBuffer.toString());
                        if(option.name().equals("s"))
                        {
                            skipLeftSpace(string,offset); //offset move to first non space char.
                        }
                    }
                    if(isDigital(string.charAt(offset)) && isDigital(string.charAt(offset+1)))
                    {
                        calendar.set(Calendar.DAY_OF_MONTH,new Integer(string.substring(offset,offset+2)));
                        offset=offset+2;
                    }
                    else {
                        calendar.set(Calendar.DAY_OF_MONTH, new Integer(string.substring(offset, offset + 1)));
                        offset++;
                    }
                    break;
                }
                case dd:
                {
                    if (optionBuffer!=null&& optionBuffer.length()>0)
                    {
                        dateOptions option = dateOptions.valueOf(optionBuffer.toString());
                        if(option.name().equals("s"))
                        {
                            offset = skipLeftSpace(string,offset); //offset move to first non space char.
                        }
                    }
                    if(isDigital(string.charAt(offset)) && isDigital(string.charAt(offset+1)))
                    {
                        calendar.set(Calendar.DAY_OF_MONTH,new Integer(string.substring(offset,offset+2)));
                        offset=offset+2;
                    }
                    break;
                }
                case ddd:
                    break;
                case m:
                {
                    if (optionBuffer!=null& optionBuffer.length()>0)
                    {
                        dateOptions option = dateOptions.valueOf(optionBuffer.toString());
                        if(option.name().equals("s"))
                        {
                            skipLeftSpace(string,offset); //offset move to first non space char.
                        }
                    }
                    if(isDigital(string.charAt(offset)) && offset+1<string.length() && isDigital(string.charAt(offset+1)))
                    {
                        calendar.set(Calendar.MONTH,new Integer(string.substring(offset,offset+2))-1);
                        offset=offset+2;
                    }
                    else {
                        calendar.set(Calendar.MONTH, new Integer(string.substring(offset, offset + 1))-1);
                        offset++;
                    }
                    break;
                }
                case mm:
                {
                    if (optionBuffer!=null && optionBuffer.length()>0)
                    {
                        dateOptions option = dateOptions.valueOf(optionBuffer.toString());
                        if(option.name().equals("s"))
                        {
                            offset = skipLeftSpace(string,offset); //offset move to first non space char.
                        }
                    }
                    if(isDigital(string.charAt(offset)) && isDigital(string.charAt(offset+1)))
                    {
                        calendar.set(Calendar.MONTH,new Integer(string.substring(offset,offset+2))-1);
                        offset=offset+2;
                    }
                    break;
                }
                case mmm:
                    break;
                case mmmm:
                    break;
                case yy:
                    break;
                case yyyy:
                {
                    if (optionBuffer!=null && optionBuffer.length()>0)
                    {
                        dateOptions option = dateOptions.valueOf(optionBuffer.toString());
                        if(option.name().equals("s"))
                        {
                            offset = skipLeftSpace(string,offset); //offset move to first non space char.
                        }
                    }
                    if(isDigital(string.charAt(offset)) && isDigital(string.charAt(offset+1) ) &&  isDigital(string.charAt(offset+2))  && isDigital(string.charAt(offset+3)))
                    {
                        calendar.set(Calendar.YEAR,new Integer(string.substring(offset,offset+4)));
                        offset=offset+4;
                    }else
                    {
                        throw new Exception("parse error, year:"+string.substring(offset,offset+4));
                    }
                    break;
                }
                case NNNNyy:
                case e:
                    break;
                case E:
                    break;
                case eee:
                    break;
                case eeee:
                    break;
                case W:
                    break;
                case WW:
                    break;
            }
            //deal with literals
            offset = skipLiteral(string,offset,literalBuffer);
        }
        return calendar.getTime();
    }

    private  int skipLiteral(String string, Integer offset, StringBuffer literalBuffer) throws Exception {
        if(literalBuffer==null || literalBuffer.length()==0)
            return offset;
        if(offset>=string.length())
            return offset;
        char c = literalBuffer.charAt(0);
        Integer startVaule = offset;
        int count;
        while(c==string.charAt(offset))
        {
            offset++;
        }
        if(offset-startVaule==literalBuffer.length())
            return offset;
        else
            throw new Exception(" Error occured on pos:"+offset+" when parse "+string);
    }

    private static boolean isTag(String tempString) {
        return findTag(tempString) > -1;
    }

    private static int skipLeftSpace(String str, Integer beginOffset) {
        char c = str.charAt(beginOffset);
        while (c<=0x20)
        {
            beginOffset++;
            c = str.charAt(beginOffset);
        }
        return beginOffset;
    }

    private static boolean isDigital(char c) {
        return (c>=48 && c<=57 );
    }

    public Date stringToTimestamp(String str) throws Exception {
        return stringToTimestamp(str,"%yyyy-%mm-%dd %hh:%nn:%ss");
    }

    @Override
    public Date executeInner(Object[] objects) throws Exception {
        Object o = objects[0];
        if(o == null ){
            return null;
        }
        int length = objects.length;
        switch (length){
            case 1 :
                return stringToTimestamp(o.toString());
            case 2 :
                Object o1 = objects[1];
                if(o1 == null ){
                    return null;
                }
                return stringToTimestamp(o.toString(),o1.toString());
            default: return null;
        }
    }


    public enum dateTags{
        d,//Day_of_month_variable_width,
        dd,//Day_of_month_fixed_width,
        ddd,//Day_of_year,
        m,//Month_of_year_variable_width,
        mm,//Month_of_year_fixed_width,
        mmm,//Month_of_year_short_name,
        mmmm,//Month_of_year_full_name,
        yy,//Year_of_century,
        yyyy,//Four_digit_year,
        NNNNyy,//Cutoff_year_plus_year_of_century,
        e,//Day_of_week_Sunday_1,
        E,//Day_of_week_Monday_1,
        eee,//Weekday_short_name,
        eeee,//Weekday_long_name,
        W,//Week_of_year_variable_width,
        WW,//Week_of_year_fixed_width,
    }

    public enum dateOptions{
        s,
        v,
        u,
        w,
        t,
        minusN,
        plusN,
    }

    /**
     *
     * find tag
     */
    private static int findTag(String tag) {
        dateTags[] allTag = dateTags.values();
        for (dateTags aTag : allTag) {
            if(tag.equals(aTag.name()))
                return aTag.ordinal();
        }
        return -1;
    }

    /**
     *
     * find option
     */
    private static int findOption(String option) {
        dateOptions[] allOpitons = dateOptions.values();
        for (dateOptions aOption : allOpitons) {
            if(option.equals(aOption.name()));
            return aOption.ordinal();
        }
        return -1;
    }

    public static void main(String[] args)
    {
        StringToTimestamp stringToTimestamp= new StringToTimestamp();
        try {
            Date date1 = stringToTimestamp.stringToTimestamp("15-1","%dd-%m");
            System.out.println(date1);
            Date date2 = stringToTimestamp.stringToTimestamp("16-01","%dd-%mm");
            System.out.println(date2);
            Date date3 = stringToTimestamp.stringToTimestamp("17-   01","%(dd)-%(mm,s)");
            System.out.println(date3);
            Date date4 = stringToTimestamp.stringToTimestamp("   19-   01","%(dd,s)-%(mm,s)");
            System.out.println(date4);
            Date date5 = stringToTimestamp.stringToTimestamp("   19-   01-2015","%(dd,s)-%(mm,s)-%yyyy");
            System.out.println(date5);
            Date date6 = stringToTimestamp.stringToTimestamp("19261212","%yyyy%mm%dd");
            System.out.println(date6);
            Date date7 = stringToTimestamp.stringToTimestamp("1980-02-12","%yyyy-%mm-%dd");
            System.out.println(date7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
