package org.pentaho.di.trans.steps.crawlerinput;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author
 * @version 1.0
 */
public class HTMLParser {
  String content;
  String startMark;
  String endMark;

  int nestedLevel;

  int offSet; //the offset of the start mark in the string
  
  private String encoding;

  public static int CATEGORY_YUMI = 1;
  public static int CATEGORY_SHUIDAO = 2;
  public static int CATEGORY_PUTONGXIAOMAI = 3;
  public static int CATEGORY_DADOU = 4;
  public static int CATEGORY_HUASHENG = 5;
  public static int CATEGORY_DABAICAI = 6;
  public static int CATEGORY_GANLANGXINGYOUCAI = 7;
  public static String[] categoryNamesForSearch =  new String[]{"玉米Zea","水稻Oryza","普通小麦Triticumaestivum","大豆Glycine","花生Arachis","高粱Sorghum","大麦属Hordeum","大白菜Brassica","甘蓝型油菜Brassica","棉属Gossypium","梨属Pyrus","南瓜Cucurbita","辣椒属Capsicum","菠菜Spinacia","小豆Vigna","菜豆Phaseolus","普通番茄Lycopersicon","马铃薯Solanum","茄子Solanum","大蒜Allium","甘蔗属Saccharum","猕猴桃属Actinidia","柑橘属Citrus","苹果属Malus","苎麻属Boehmeria","普通西瓜Citrullus","葡萄属Vitis","桃Prunus","香蕉Musa","芝麻Sesamum","百合属Lilium","兰属Cymbidium","非洲菊Gerbera","秋海棠属Begonia"};
  public static String[] categoryNames =  new String[]{"玉米","水稻","普通小麦","大豆","花生","高粱","大麦属","大白菜","甘蓝型油菜","棉属","梨属","南瓜","辣椒属","菠菜","小豆","菜豆","普通番茄","马铃薯","茄子","大蒜","甘蔗属","猕猴桃属","柑橘属","苹果属","苎麻属","普通西瓜","葡萄属","桃","香蕉","芝麻","百合属","兰属","非洲菊","秋海棠属"};

  public String[] categoryContent =  new String[categoryNames.length];

  public static String[] fieldsNameStr =  new String[]{"品种名称","申请日","申请号","授权日","品种权号","公告日","公告号","培育人","品种权人","品种权人地址","代理公司","代理公司地址","代理人","公告名称"};
//  public static String[] fieldsName =  new String[]{"variety_name","application_date","application_num","authorize_date","right_no","publish_date","publish_num","cultivate_people","right_owner_name","right_owner_address","agent_company","agent_company_address","agent_people","page_title"};

//  public HTMLParser(String urlString) throws IOException, Exception {
//    HTMLTractor tr = new HTMLTractor(urlString);
//    content = tr.getSource();
//  }

  private  Object[] getVariety(String item, String categoryName,String title) {
   // System.out.println(categoryID);
   // System.out.println(item);
    if(item.indexOf("品种名称")==-1)
      return null;
    Object[] row=new Object[fieldsNameStr.length+1];
    Variety variety = new Variety();
    int startIndex,endIndex;
    Pattern pattern = Pattern.compile("^\\d+[年]\\d+[月]\\d+[日]?$");
    for(int i=0;i<fieldsNameStr.length;i++) {
      startIndex = item.indexOf(fieldsNameStr[i]);
      if (startIndex > -1) {
        endIndex = getEndIndex(startIndex, item);
        row[i] = item.substring(startIndex+fieldsNameStr[i].length(), endIndex);
        Matcher match =  pattern.matcher(row[i].toString());
        if(match.find()) {
          row[i] = converDate(row[i].toString());
        }
      }
      if(i==fieldsNameStr.length-1)
      {
        row[i] = title;
      }
    }
    row[fieldsNameStr.length]=categoryName;
    //variety.variety_name= variety_name;
    return row;
  }

  /**
   * convert xxxx年x月x日 to yyyy-MM-dd
   *         xxxx年x月x  to yyyy-MM-dd
   * @param str
   * @return
   */
  private Object converDate(String str) {
    int yearIndex = str.indexOf("年");
    int monthIndex = str.indexOf("月");
    int dayIndex = str.indexOf("日");
    if (yearIndex < 4 || monthIndex < 1 || dayIndex < 1) {
      return null;
    }

    String yearStr = str.substring(yearIndex - 4, yearIndex);
    String monthStr,dayStr;
    Integer year,month,day;
    try {
       year = new Integer(yearStr).intValue();
    } catch (Exception e) {
      return null;
    }

    if (monthIndex > 1) {
      monthStr = str.substring(monthIndex - 2, monthIndex);
      try {
        month = new Integer(monthStr).intValue();
      } catch (Exception e) {
        monthStr = str.substring(monthIndex - 1, monthIndex);
        try {
          month = new Integer(monthStr).intValue();
        } catch (Exception e2) {
          return null;
        }
      }
    } else {
      monthStr = str.substring(monthIndex - 1, monthIndex);
      try {
        month = new Integer(monthStr).intValue();
      } catch (Exception e) {
        return null;
      }
    }

    if (dayIndex > 1) {
      dayStr = str.substring(dayIndex - 2, dayIndex);
      try {
        day = new Integer(dayStr).intValue();
      } catch (Exception e) {
        dayStr = str.substring(dayIndex - 1, dayIndex);
        try {
          day = new Integer(dayStr).intValue();
        } catch (Exception e2) {
          return null;
        }
      }
    } else if(dayIndex == 1)  {
      dayStr = str.substring(dayIndex - 1, dayIndex);
      try {
        day = new Integer(dayStr).intValue();
      } catch (Exception e) {
        return null;
      }
    }
    else if(dayIndex == -1) {//  xxxx年x月x 的格式
      String dayPart2 = str.substring(str.length() - 1, str.length());
      String dayPart1 = str.substring(str.length() - 2, str.length()-1);
      if(isDigital(dayPart2.charAt(0)) && isDigital(dayPart1.charAt(0)))
        day=new Integer(dayPart1+dayPart2).intValue();
      else if(isDigital(dayPart2.charAt(0)) && !isDigital(dayPart1.charAt(0)))
        day=new Integer(dayPart2).intValue();
      else
        day=1;
    }else
      day=1;

    return year.toString()+"-"+ (month<10?"0"+month.toString():month.toString())+"-"+ (day<10?"0"+day.toString():day.toString());

  }

  private int getEndIndex(int startIndex, String item) {
    int endIndex = item.length();
    for(int i =0;i<fieldsNameStr.length;i++)
    {
      int fieldsIndex = item.indexOf(fieldsNameStr[i]);
      if(fieldsIndex>-1 && fieldsIndex>startIndex && fieldsIndex<endIndex)
      {
        endIndex = fieldsIndex;
      }
    }
    return endIndex;
  }

  public HTMLParser(URL url) throws IOException, Exception {
    this(url,"utf-8");
  }

  
  public HTMLParser(final URL url,final String encoding) throws IOException, Exception {
	  this.encoding = encoding;
	  Thread thread = new Thread()
	  {
		public void run()
		{
			try{
			HTMLTractor tr = new HTMLTractor(url.toString());
		    content = tr.getSource(encoding);
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	  };
	  thread.start();
	  int count=0;
	  while(count<60 && thread.isAlive()) //wait 60 second
	  {
	      Thread.sleep(1000);  
	      if(count>=5 && count % 5 ==0)
	      count++;
	  }
	  if(thread.isAlive())
		  thread.stop();
	  
  }
  

  public HTMLParser(URL url, Properties p, String httpeHeadContent) throws
      IOException {
    HTMLTractor tr = new HTMLTractor(url.toString(), p,false);
    content = tr.getSourceFromPost(httpeHeadContent);
  }

  public HTMLParser(String c) {
    content = c;
  }

  public HTMLParser() {

  }

  public void setNestedLevel(int nestedLevel) {
    this.nestedLevel = nestedLevel;
  }

  public void setStartMark(String startMark) {
    this.startMark = startMark;
  }

  public void setString(String string) {
    this.content = string;
  }

  public void setEndMark(String endMark) {
    this.endMark = endMark;
  }

  public void setOffSet(int offSet) {
    this.offSet = offSet;
  }

  /**
       * get the trunked string according to the start mark ,end mark and nested level
   * @return String
   */
  public String getTrunkedString() {
    int startIndex = getTunkedStringStartIndex();
    int endIndex;
    if (this.nestedLevel == 0) {
      endIndex = content.indexOf(this.endMark, startIndex) + endMark.length();
      return content.substring(startIndex, endIndex);
    }
    else {
      endIndex = startIndex;
      for (int i = 0; i < this.nestedLevel; i++) {
        endIndex = content.indexOf(this.endMark, endIndex) + endMark.length();
      }
      return content.substring(startIndex, endIndex) + endMark;
    }
  }

  /**
   * �����ĳ������ַ���֮�������, ����ַ����ͻ�õ��ַ�����ͬһ��TAG��
   * @return String mark ����ַ���
   * @return int offset �ı����ж������ַ���ʱ,ָ���ı���ַ�����˳���.���ֻ��һ��,��ò���Ϊ0.
   */
  public String getTrunkedStringNextMark(String mark, int offset) {
    int index = getIndexOfMark(mark, offset);
    if (index == -1) {
      return ""; //û�иñ�־,���ؿ��ַ���
    }
    int begin = index + mark.length();
    return content.substring(begin, content.indexOf('<', begin));
  }

  /**
   * �����ĳ������ַ���֮��� Tag �������, Tag �� startMark �� endMark ����
   * @return String mark ����ַ���
   * @return int offset �ı����ж������ַ���ʱ,ָ���ı���ַ�����˳���.���ֻ��һ��,��ò���Ϊ0.
   */
  public String getTrunkedStringAfterMark(String mark, int offset) {
    int index = getIndexOfTagContent(mark, offset);
    String result;
    if (index == -1) {
      return ""; //
    }
    index++;
    int beginIndex = content.indexOf(this.startMark, index);
    int endIndex = content.indexOf(this.endMark, beginIndex);

    if (endIndex > beginIndex + startMark.length()) {
      result = content.substring(content.indexOf('>',
                                                 beginIndex + startMark.length()) +
                                 1, endIndex);
    }
    else {
      result = content.substring(index + mark.length(),
                                 beginIndex + startMark.length());
    }
    return deleteAllTags(result);
  }

  /** mark is part content of an element, as the follong fomat
   *  "... > mark other chars < ... "
   * this method can get the index of  "mark" in the content
   * @param mark
   * @param offset: the offset of mark in the string
   * @return -1 if no mark in the string
   */
  public int getIndexOfMark(String mark, int offset) {
    int index = -1;
    int i = 0;
    for (i = 0; i <= offset; i++) {
      index = content.indexOf(mark, index + 1);
    }
    return index;
  }

  /** mark is the content of an element, as the follong fomat
   *  "... > mark < ... "
   * this method can get the index of  "mark" in the content
   * @param tagContent
   * @param index: the offset of mark in the string
   * @return -1 if no mark in the string
   */
  public int getIndexOfTagContent(String tagContent, int index) {
    int offset = -1;
    int i = 0;
    Pattern p = Pattern.compile(">" + "[\\s]*" + tagContent + "[:]{0,1}[\\s]*" + "<");
    Matcher matcher = p.matcher(content);
    while (matcher.find()) {
      if (i++ == index) {
        offset = matcher.start();
        break;
      }
      else {
        continue;
      }
    } 
    return offset;
  }

  /**
   *
   * @param mark: һ������ַ������磺 "�� Ȧ" , "�� ַ"
   * @param order: the order of tag after the mark, start from 0
   * @param tag: html tag without "<" or ">" ,such as "TD"
   * @return "" if no mark in the string
   *
   * <td>��ַ</td> <td ><font size = 12>�йش�</td>
   *
   * mark �� ����ַ��
   * order �� 0
   * tag �� td
   *
   */
  public String getTagContentAfterMark(String mark, int order, String tag) {
    int offset = getIndexOfTagContent(mark, 0);

    if (offset == -1) {
      return "";
    }
    int tagIndex = getTagIndexIgnoreCase(tag,order, offset + mark.length()+1);
    return getTagContent(tagIndex, tag);
  }

  /**
   * ��� mark ������ڵ� tag ��ǩ������ݡ�
   *
   * @param mark: һ������ַ������磺 "�� Ȧ" , "�� ַ"
   * @param offset: the offset of mark in the string��������ַ����е�λ��
   * @param tag: html tag without "<" or ">" ,such as "TD"
   * @return "" if no mark in the string
   *
   * �� �ַ���
   * <td font size = 12>��ַ�� �йش� </td>
   *
   * ����
   * mark �� ����ַ��
   * offset �� 0
   * tag �� td
   *
   * ���� ��ַ�� �йش�
   */
  public String getTagContentOfMark(String mark, int offset, String tag) {
    int index = getIndexOfMark(mark, offset);
    if (index == -1) {
      return "";
    }
    int tagIndex = getLastTagIndexIgnoreCase(tag, index + mark.length());
    return getTagContent(tagIndex, tag);
  }

  /**
   * get the Nth tag after offset
   * @param tag
   * @param order: the sequence of the tag after offset
   * @param offset
   * @return
   */
  public int getTagIndexIgnoreCase(String tag,int order, int offset) {
    int tagOffset = offset;
    for (int i = 1;i<= order;i++)
    tagOffset =  content.toUpperCase().indexOf(tag.toUpperCase(), tagOffset);
    return tagOffset;
  }

  public int getLastTagIndexIgnoreCase(String tag, int offset) {
    return content.toUpperCase().lastIndexOf(tag.toUpperCase(), offset);
  }

  /**
   * get the index of a tag which the offset belongs to
   * @param offset
   * @return
   */
  public int getTagIndex(int offset) {
    char c = content.charAt(offset);
    while (c != '<') {
      c = content.charAt(--offset);
    }
    return offset;
  }

  /**
   * ����ָ����ǩ��Ĳ������ݣ���������ǩ����������
   * @param cont
   * @param tag
   * @return
   */
//  public String getTagContentBySegment (String seg, String tag)
//  {
//
//  }

  /**
   * ���ָ��һ�Ա�ǩ�������
   * @param index ��ʼ������λ��
   * @param tag
   * @return
   *
   * �� <TD FONT = 12 >13261217005 <A>look up the phone</A></TD>
   * para:     tag = TD
   * para:     index �� 0 �� 14 ֮���һ������ 0 ���� "<" ,14 ���� ">"
   * return:   13261217005 <A>look up the phone</A>
   */
  public String getTagContent(int index, String tag) {

    char c = content.charAt(index);
    if (c != '<') { // index ��tag ��, Ӧ��ʹ��һ�����������.
      index = getTagIndex(index);
    }
    c = content.charAt(index);
    boolean tagBegin = false, flag = true;
    int begin = 0, end = 0;
    while (flag) {
      switch (c) {
        case '<':
          if (content.substring(index + 1, index + tag.length() + 1).
              equalsIgnoreCase(tag)) {
            tagBegin = true;
          }
          c = content.charAt(++index);
          break;
        case '>':
          if (tagBegin) {
            begin = index + 1;
            tagBegin = false;
          }
          if (content.substring(index - tag.length() - 1, index).
              equalsIgnoreCase("/" + tag) && begin != 0) {
            end = index - tag.length() - 2;
            flag = false;
          }
          if(content.length() == index+1)
          {
        	  return "";
          }
          c = content.charAt(++index);
          break;
        default:
          c = content.charAt(++index);
          break;
      }
    }
    return content.substring(begin, end);
  }

  
  /**
   *
   * @return
   *
   * �� <TD> phone </TD> <TD FONT = 12 >13261217005 <A>look up the phone</A></TD>
   * para:     mark = phone 
   * return:   13261217005 <A>look up the phone</A>
   */
  public String getNextTagContent(String mark) {
	  int index = this.getIndexOfTagContent(mark, 0);
	  if(index==-1)
		  return null;
	  Pattern StartTagPattern =  Pattern.compile("[<]{1}[^/]{1}[a-z|A-Z|\\s|\\\"|/|=]*[>]{1}");
	  Matcher matcher = StartTagPattern.matcher(content);
	  int startIndexOfNextTag;
	  int endIndexOfNextTag;
	  
	  if( matcher.find(index))
		  {startIndexOfNextTag = matcher.start();
		   endIndexOfNextTag = matcher.end();
		  }
	  else
		  return null;
	  
	  String tag = content.substring(startIndexOfNextTag+1 ,endIndexOfNextTag-1 ).trim();
	  if(tag.indexOf(" ")>-1)
		  tag = tag.substring(0,tag.indexOf(" "));
	  return getTagContent(startIndexOfNextTag, tag);
  }
  
  /**
   * ���mark��� tag��ǩ��
   * @param mark
   * @param offset
   * @param tag
   * @return  ֻ���ؿ�ʼ��ǩ�������ؽ�����ǩ
   *
   * �� �ַ���
   * <td>��ַ</td> <td ><font size = 12>�йش�</font></td>
   *
   * ����
   * mark �� ����ַ��
   * offset �� 0
   * tag �� font
   *
   * ���� <font size = 12>
   */
  public String getTagAfterMark(String mark, int offset, String tag) {
    int index = getIndexOfMark(mark, offset);
    if (index == -1) {
      return "";
    }
    if (!tag.startsWith("<")) {
      tag = "<" + tag;
    }
    int tagStartIndex = this.content.indexOf(tag, index);
    int tagEndIndex = this.content.indexOf(">", tagStartIndex);
    return content.substring(tagStartIndex, tagEndIndex + 1);
  }

  /**
   * delete All Tags of a html htmlContent
   * @return String
   */
  public static String deleteAllTags(String htmlContent) {
    StringBuffer result = new StringBuffer();
    char[] array = htmlContent.toCharArray();
    //boolean tag = false;
    int level = 0;
    for (int i = 0; i < array.length; i++) {
      switch (array[i]) {
        case '<':
          level++;
          break;
        case '>':
          level--;
          break;
        default:
          if (level == 0) {
            result.append(array[i]);
          }
          break;
      }
    }
    return result.toString().replaceAll("&nbsp;", " ").trim();
  }

  /**
   * remove specified tag and content in the tag from a html string
   * @param htmlContent
   * @param tagToRemove
   * @return
   */
  private String deleteSpecifiedTags(String htmlContent, String tagToRemove) {
    int startIndex = htmlContent.indexOf("<"+tagToRemove);
    if(startIndex==-1)//not found tag in the content
      return htmlContent;
    else {
      String endTag = "</"+tagToRemove+">";
      int endIndex = htmlContent.indexOf(endTag);
      int tagLength = endTag.length();
      return htmlContent.substring(0,startIndex)+deleteSpecifiedTags(htmlContent.substring(endIndex+tagLength,htmlContent.length()),tagToRemove);
    }
  }

  /**
   * getTunkedStringIndex
   *
   * @return int
   */
  private int getTunkedStringStartIndex() {
    int index = 0;
    for (int i = 0; i < offSet; i++) {
      index = content.indexOf(this.startMark, index) + startMark.length();
    }
    return index - startMark.length();
  }

  /**
   *
   * @param tag String: the <a> tag such as "<a href=shou.asp>"
   * @return String
   */
  public String getURLFromTagA(String tag) {
    if (tag.trim().equals("") || tag == null) {
      return null;
    }
    int begin = tag.indexOf("href");
    int end = tag.indexOf(" ", begin) == -1 ? tag.indexOf(">", begin) :
        tag.indexOf(" ", begin);
    if(end==-1)
    	end = tag.length();
    String comparativeURL = tag.substring(begin + 4, end).trim();
    comparativeURL = comparativeURL.substring(comparativeURL.indexOf("=") + 1);
    if ( (comparativeURL.startsWith("\"") && comparativeURL.endsWith("\"")) ||
        (comparativeURL.startsWith("'") && comparativeURL.endsWith("'"))) {
      comparativeURL = comparativeURL.substring(1,
                                                comparativeURL.length() - 1);

    }
    return comparativeURL.replaceAll("&amp;", "&"); //��&amp;�滻Ϊ&
  }

  /**
   * getTunkedStringIndex
   *
   * @return int
   */
  private int getTunkedStringEndIndex() {
    int index = 0;
    for (int i = 0; i < offSet; i++) {
      index = content.indexOf(this.endMark, index);
    }
    return index;
  }

  public static String removeAllWhiteSpace(String str)
  {
    char[] result = new char[str.length()];
    int count=0;
    //ArrayList<Char> result = new ArrayList();
    for(int index =0;index<str.length();index++)
    {
      char c = str.charAt(index);
      if(c>32 && c!=160 ) //160 is "nbsp"
        result[count++]=c;
    }
    return new String(result).substring(0,count);

  }

    public static void main(String[] args) {
//      File file = new File("/Users/jianjunchu/seed_authorization/16.html");
//      File outputFile = new File("/Users/jianjunchu/seed_authorization/16.txt");
//      HTMLParser parser = new HTMLParser();
//      try {
//        parser.parseFile(file,outputFile);
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }

      File dir = new File("/Users/jianjunchu/seed_authorization/");
      File[] files = dir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          if(name.indexOf("html")>-1)
            return true;
          else
            return false;
        }
      });
      for(int i=0;i<files.length;i++)
      {
        File file = files[i];
        String destFileName;
        try {
           destFileName = file.getCanonicalPath().replace("html","txt");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        File outputFile = new File(destFileName);
        HTMLParser parser = new HTMLParser();
        try {
          parser.parseFile(file,outputFile);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    public void parseFile(File file,File outputFile) throws IOException {
      if(!outputFile.exists())
        outputFile.createNewFile();
      FileOutputStream fos = new FileOutputStream(outputFile);
      FileReader reader = null;
      StringWriter writer = new StringWriter();
      try {
        reader = new FileReader(file);
      }
      catch (FileNotFoundException ex) {
      }
      char[] buffer = new char[1024];

      int length;
      try {
        length = reader.read(buffer, 0, 1024);
        while (length != -1) {
          writer.write(buffer, 0, length);
          length = reader.read(buffer, 0, 1024);
        }
      }
      catch (IOException ex1) {
      }
      ArrayList<Object[]> result = new ArrayList();
      String pageHtmlContent = writer.toString();
      content = pageHtmlContent;
      this.setOffSet(7);
      this.setNestedLevel(1);
      this.setStartMark("<h3");
      this.setEndMark("</h3>");
      String content = this.getTrunkedString();
      String title =  HTMLParser.deleteAllTags(content);

      this.setOffSet(7);
      this.setNestedLevel(1);
      this.setStartMark("<body");
      this.setEndMark("</body>");
      String pageContent =  this.getTrunkedString();
      String pageContent2 =  this.deleteSpecifiedTags(pageContent,"script");//remove script nodes
      String pageContent3 =  removeAllWhiteSpace(HTMLParser.deleteAllTags(pageContent2));//remove script nodes
      String pageContent4;
      if(pageContent3.indexOf("关于我们")>-1)
         pageContent4=  pageContent3.substring(0,pageContent3.indexOf("关于我们"));
      else
         pageContent4=  pageContent3;
      //System.out.println(pageContent4);
      for(int i = 0; i< categoryNamesForSearch.length; i++)
      {
        String categoryName = categoryNamesForSearch[i];
        int startIndex = pageContent4.indexOf(categoryName);
        if(startIndex>-1)
        {
          int endIndex = pageContent4.length();
          for(int j = 0; j< categoryNamesForSearch.length; j++)
          {
            int nextCategoryIndex =pageContent4.indexOf(categoryNamesForSearch[j]);
            if(nextCategoryIndex>startIndex && nextCategoryIndex< endIndex)
              endIndex=nextCategoryIndex;
          }
          categoryContent[i] = pageContent4.substring(startIndex,endIndex);
        }
      }
        for(int i=0;i<categoryContent.length;i++)
        {
          if(categoryContent[i]!=null)
          {
            String[] items = categoryContent[i].split("[*][*][*][*][*]");
            for (int j =0;j<items.length;j++)
            {
              Object[] row = getVariety(items[j].replaceAll("[*]",""),categoryNames[i],title);
              if(row != null)
                result.add(row);
            }
          }
        }
      fos.write("variety_name;application_date;application_num;authorize_date;right_no;publish_date;publish_num;cultivate_people;right_owner_name;right_owner_address;agent_company;agent_company_address;agent_people;page_title".getBytes());
      fos.write("\n".getBytes());
        for(int i=0;i<result.size();i++)
        {
          Object[] row = result.get(i);
          if(row !=null) {
            for (int j = 0; j < row.length; j++) {
              if(row[j]!=null) {
                if(j== row.length-1){ //last column without ';'
                  System.out.print(row[j].toString() );
                  fos.write((row[j].toString() ).getBytes());
                }else {
                  System.out.print(row[j].toString() + ";");
                  fos.write((row[j].toString() + ";").getBytes());
                }
              }
              else {
                System.out.print( ";");
                fos.write(";".getBytes());
              }
            }
            fos.write(( "\n").getBytes());
            System.out.println();
          }
        }
      fos.flush();
  }



  public static boolean isDigital(char c) {
    return c <= 57 && c >= 48;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

public String getEncoding() {
	return encoding;
}

public void setEncoding(String encoding) {
	this.encoding = encoding;
}


public boolean includeTag(String value) {
	  Pattern StartTagPattern =  Pattern.compile("[<]{1}[^/]{1}[a-z|A-Z|\\s|\\\"|/|=]*[>]{1}");
	  Matcher matcher = StartTagPattern.matcher(value);
	  if(matcher.find())
		  return true;
	  else
		  return false;
}

  public class Variety
  {
    public String variety_name,application_date,application_num,authorize_date,right_no,publish_date,publish_num,cultivate_people,right_owner_name,right_owner_address,agent_company,agent_company_address,agent_people,page_title;
  }

}