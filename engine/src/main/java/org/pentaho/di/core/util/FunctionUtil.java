package org.pentaho.di.core.util;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.instruction.op.OperatorBase;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.FunctionPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.function.FunctionInterface;
import org.pentaho.di.core.util.function.datastage.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FunctionUtil {




	protected LogChannelInterface log;
	private static ExpressRunner runner = null;
	public FunctionUtil(LogChannelInterface log)  {
		this.log = log;
		try {
			runner = getRunner();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	

	public Object eval(String[] key , Object[] value , String exp) throws Exception {
		DefaultContext<String,Object> context = new DefaultContext<String,Object>();
		for(int i=0;i<key.length;i++)
		{
			context.put(key[i],value[i]);
		}
		
		return runner.execute(exp,context,null,true,false);
	}

	private ExpressRunner getRunner() throws Exception {
		if(runner ==null){
			runner = new ExpressRunner();
			runner.addOperatorWithAlias("If","if",null);
			runner.addOperatorWithAlias("Then","then",null);
			runner.addOperatorWithAlias("Else","else",null);

			runner.addFunction("AlNum",new AlNum());
			runner.addFunction("Alpha",new Alpha());
			runner.addFunction("Compare",new Compare());
			/*runner.addFunction("Char",new JavaChar());*/
			runner.addFunction("CompareNoCase",new CompareNoCase());
			runner.addFunction("CompareNum",new CompareNum());
			runner.addFunction("CompareNumNoCase",new CompareNumNoCase());
			/*runner.addFunction("Convert",new JavaConvert());*/
			runner.addFunction("Count",new Count());
			/*runner.addFunction("CurrentDate",new JavaCurrentDate());
			runner.addFunction("CurrentTime",new JavaCurrentTime());
			runner.addFunction("CurrentTimeMS",new JavaCurrentTimeMS());
			runner.addFunction("CurrentTimestamp",new JavaCurrentTimestamp());
			runner.addFunction("CurrentTimestampMS",new JavaCurrentTimestampMS());
			runner.addFunction("DateFromComponents",new JavaDateFromComponents());
			runner.addFunction("DateFromDaysSince",new JavaDateFromDaysSince());
			runner.addFunction("DateOffsetByComponents",new JavaDateOffsetByComponents());
			runner.addFunction("DateOffsetByDays",new JavaDateOffsetByDays());
			runner.addFunction("DateToString",new JavaDateToString());
			runner.addFunction("DaysInMonth",new JavaDaysInMonth());
			runner.addFunction("DaysInYear",new JavaDaysInYear());
			runner.addFunction("DaysSinceFromDate",new JavaDaysSinceFromDate());*/
			runner.addFunction("Dcount",new Dcount());
			/*runner.addFunction("DecDate",new JavaDecDate());*/
			runner.addFunction("DecimalToString",new DecimalToString());
			/*runner.addFunction("DecTime",new JavaDecTime());
			runner.addFunction("DecTimeStamp",new JavaDecTimeStamp());
			runner.addFunction("DFloatToDecimal",new JavaDFloatToDecimal());*/
			runner.addFunction("DownCase",new DownCase());
			/*runner.addFunction("DQuote",new JavaDQuote());
			runner.addFunction("Ereplace",new JavaEreplace());
			runner.addFunction("FileNorm",new JavaFileNorm());
			runner.addFunction("HoursFromTime",new JavaHoursFromTime());
			runner.addFunction("Index",new JavaIndex());*/
			runner.addFunction("Left",new Left());
			runner.addFunction("Len",new Len());
			/*runner.addFunction("MicroSecondsFromTime",new JavaMicroSecondsFromTime());
			runner.addFunction("MidnightSecondsFromTime",new JavaMidnightSecondsFromTime());
			runner.addFunction("MinutesFromTime",new JavaMinutesFromTime());
			runner.addFunction("MonthDayFromDate",new JavaMonthDayFromDate());
			runner.addFunction("MonthFromDate",new JavaMonthFromDate());
			runner.addFunction("NextWeekdayFromDate",new JavaNextWeekdayFromDate());
			runner.addFunction("NullToEmpty",new JavaNullToEmpty());*/
			runner.addFunction("Num",new Num());
			runner.addFunction("PadString",new PadString());
			/*runner.addFunction("PreviousWeekdayFromDate",new JavaPreviousWeekdayFromDate());*/
			runner.addFunction("Right",new Right());
			/*runner.addFunction("SecondsFromTime",new JavaSecondsFromTime());
			runner.addFunction("SecondsSinceFromTimestamp",new JavaSecondsSinceFromTimestamp());
			runner.addFunction("Seq",new JavaSeq());
			runner.addFunction("SetNull",new JavaSetNull());*/
			runner.addFunction("Space",new Space());
			/*runner.addFunction("Squote",new JavaSquote());
			runner.addFunction("Str",new JavaStr());
			runner.addFunction("StrClnsGBK",new JavaStrClnsGBK());
			runner.addFunction("StrClnsUTF8",new JavaStrClnsUTF8());
			runner.addFunction("StrDate",new JavaStrDate());
			runner.addFunction("StringToDate",new JavaStringToDate());
			runner.addFunction("StringToTime",new JavaStringToTime());
			runner.addFunction("StringToTimestamp",new JavaStringToTimestamp());*/
			runner.addFunction("StripWhiteSpace",new StripWhiteSpace());
			runner.addFunction("CompactWhiteSpace",new CompactWhiteSpace());
			/*runner.addFunction("StrNrNorm",new JavaStrNrNorm());
			runner.addFunction("StrTime",new JavaStrTime());
			runner.addFunction("StrTimeDec",new JavaStrTimeDec());
			runner.addFunction("StrTimeStamp",new JavaStrTimeStamp());
			runner.addFunction("TimeDate",new JavaTimeDate());
			runner.addFunction("TimeFromComponents",new JavaTimeFromComponents());
			runner.addFunction("TimeFromMidnightSeconds",new JavaTimeFromMidnightSeconds());
			runner.addFunction("TimestampFromDateTime",new JavaTimestampFromDateTime());
			runner.addFunction("TimestampFromSecondsSince",new JavaTimestampFromSecondsSince());
			runner.addFunction("TimestampFromTimet",new JavaTimestampFromTimet());
			runner.addFunction("TimestampToDate",new JavaTimestampToDate());
			runner.addFunction("TimestampToString",new JavaTimestampToString());
			runner.addFunction("TimestampToTime",new JavaTimestampToTime());
			runner.addFunction("TimetFromTimestamp",new JavaTimetFromTimestamp());
			runner.addFunction("TimeToDecimal",new JavaTimeToDecimal());
			runner.addFunction("TimeToString",new JavaTimeToString());
			runner.addFunction("Trim",new JavaTrim());
			runner.addFunction("TrimB",new JavaTrimB());
			runner.addFunction("TrimF",new JavaTrimF());
			runner.addFunction("TrimLeadingTrailing",new JavaTrimLeadingTrailing());*/
			runner.addFunction("UpCase",new UpCase());
			/*runner.addFunction("WeekdayFromDate",new JavaWeekdayFromDate());
			runner.addFunction("YeardayFromDate",new JavaYeardayFromDate());
			runner.addFunction("YearFromDate",new JavaYearFromDate());
			runner.addFunction("YearweekFromDate",new JavaYearweekFromDate());*/

			//load from xml files
//			runner.addFunction("AddressMask",new org.pentaho.di.core.util.mask.AddressMask());
//			runner.addFunction("CommonMask",new org.pentaho.di.core.util.mask.CommonMask());
//			runner.addFunction("CreditMask",new org.pentaho.di.core.util.mask.CreditMask());
//			runner.addFunction("EmployeeIdMask",new org.pentaho.di.core.util.mask.EmployeeIdMask());
//			runner.addFunction("HalfMask",new org.pentaho.di.core.util.mask.HalfMask());
//			runner.addFunction("IdCardMask",new org.pentaho.di.core.util.mask.IdCardMask());
//			runner.addFunction("LicenceMask",new org.pentaho.di.core.util.mask.LicenceMask());
//			runner.addFunction("MoneyMask",new org.pentaho.di.core.util.mask.MoneyMask());
//			runner.addFunction("NameMask",new org.pentaho.di.core.util.mask.NameMask());
//			runner.addFunction("PhoneMask",new org.pentaho.di.core.util.mask.PhoneMask());

            //load from plugins
			loadFuncitonPlugins();

		}
		return runner;
	}

	private void loadFuncitonPlugins() {

		PluginRegistry registry = PluginRegistry.getInstance();
		List<PluginInterface> plugins = registry.getPlugins(FunctionPluginType.class);
		for (PluginInterface plugin : plugins) {
			try {
				FunctionInterface functionInterface = (FunctionInterface)registry.loadClass(plugin);
				//OperatorBase operatorBase = (OperatorBase) Class.forName(plugin.getClassMap().get(plugin)).newInstance();
				runner.addFunction(plugin.getIds()[0], (OperatorBase)functionInterface);
			}  catch(Exception e) {
			    log.logError(e.getMessage());
				//logger.error("Error loading plugin: " + plugin, e); //$NON-NLS-1$
			}
		}
	}


	/**
	 * execute a IF Statement or a nested function
	 * @param statementExpress
	 * @param
	 * @param rowData
	 * @return
	 * @throws KettleException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object execStatement(String statementExpress, ValueMetaInterface valueMeta, Object rowData) throws KettleException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		statementExpress = statementExpress.trim();
		if(statementExpress.length() >= 2){
			String token = statementExpress.substring(0,2);
			if (token.equalsIgnoreCase("IF"))
			{
				StatementComponents components = parseStatement(statementExpress);
				if(evl(components.getCondition(),valueMeta,rowData))
				{
					return execFunction( components.getTrueFunction(),  valueMeta, rowData);
				}
				else
					return execFunction( components.getFalseFunction(),  valueMeta, rowData);
			}
			else
			{
				return execFunction( statementExpress,  valueMeta, rowData);
			}
		}else{
			return execFunction( statementExpress,  valueMeta, rowData);
		}

	}

	/**
	 * evaluate a condition express 
	 * @param condition
	 * @return true or false
	 * @throws KettleException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private static boolean evl(String condition, ValueMetaInterface rowMeta, Object rowData) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KettleException {
		//		Pattern p = Pattern.compile("[ ]*[>|<|=][ ]*");
		//edit by xt
		Pattern p = Pattern.compile("[>|<|=]");
		Matcher m = p.matcher(condition);
		//edit by xt
		if(!m.find()){
			throw new KettleException("error condition"+condition);
		}
		int startIdx = m.start();
		int endIdx=  m.end();
		if(startIdx>-1 && endIdx>-1)
		{
			String sign = condition.substring(startIdx,endIdx).trim();
			String left = condition.substring(0,startIdx).trim();
			//			String right = condition.substring(startIdx,endIdx);
			String right = condition.substring(endIdx+1).trim();
			if (sign.equals("="))
			{
				Object leftResult = execFunction(left,rowMeta,rowData);
				Object rightResult = execFunction(right,rowMeta,rowData);
				if(leftResult.toString().equals(rightResult.toString()))
					return true;
				else
					return false;
			}
			else if (sign.equals(">"))
			{
				Object leftResult = execFunction(left,rowMeta,rowData);
				Object rightResult = execFunction(right,rowMeta,rowData);
				if(new Double(leftResult.toString()).doubleValue() > new Double(rightResult.toString()).doubleValue())
					return true;
				else
					return false;
			}
			else if (sign.equals("<"))
			{
				Object leftResult = execFunction(left,rowMeta,rowData);
				Object rightResult = execFunction(right,rowMeta,rowData);
				if(new Double(leftResult.toString()).doubleValue() < new Double(rightResult.toString()).doubleValue())
					return true;
				else
					return false;
			}else
			{
				throw new KettleException("error condition["+condition+"] ,unsupport sign ＄1�7" + sign);
			}
		}else
		{
			throw new KettleException("error condition"+condition);
		}
	}
	
	private static boolean evl(String condition) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KettleException {
		//		Pattern p = Pattern.compile("[ ]*[>|<|=][ ]*");
		//edit by xt
		Pattern p = Pattern.compile("[>|<|=]");
		Matcher m = p.matcher(condition);
		//edit by xt
		if(!m.find()){
			throw new KettleException("error condition"+condition);
		}
		int startIdx = m.start();
		int endIdx=  m.end();
		if(startIdx>-1 && endIdx>-1)
		{
			String sign = condition.substring(startIdx,endIdx).trim();
			String left = condition.substring(0,startIdx).trim();
			//			String right = condition.substring(startIdx,endIdx);
			String right = condition.substring(endIdx+1).trim();
			if (sign.equals("="))
			{
				Object leftResult = execFunction(left);
				Object rightResult = execFunction(right);
				if(leftResult.toString().equals(rightResult.toString()))
					return true;
				else
					return false;
			}
			else if (sign.equals(">"))
			{
				Object leftResult = execFunction(left);
				Object rightResult = execFunction(right);
				if(new Double(leftResult.toString()).doubleValue() > new Double(rightResult.toString()).doubleValue())
					return true;
				else
					return false;
			}
			else if (sign.equals("<"))
			{
				Object leftResult = execFunction(left);
				Object rightResult = execFunction(right);
				if(new Double(leftResult.toString()).doubleValue() < new Double(rightResult.toString()).doubleValue())
					return true;
				else
					return false;
			}else
			{
				throw new KettleException("error condition["+condition+"] ,unsupport sign ＄1�7" + sign);
			}
		}else
		{
			throw new KettleException("error condition"+condition);
		}
	}
	
	/**
	 * parse a statement to 3 parts(condition, true function,false function)
	 * @param statementExpress
	 * @return
	 */
	private static StatementComponents parseStatement(String statementExpress) {
		int ifIdx  = statementExpress.toUpperCase().indexOf("IF");
		int thenIdx  = statementExpress.toUpperCase().indexOf("THEN");
		int elseIdx  = statementExpress.toUpperCase().indexOf("ELSE");
		String condition = statementExpress.substring(ifIdx+2,thenIdx);
		String trueFunction = statementExpress.substring(thenIdx+4,elseIdx);
		String falseFunction = statementExpress.substring(elseIdx+4,statementExpress.length());
		StatementComponents sc = new StatementComponents();
		sc.setCondition(condition.trim());
		sc.setTrueFunction(trueFunction.trim());
		sc.setFalseFunction(falseFunction.trim());
		return sc;
		//		int length = statementExpress.length();
		//		StringBuilder sb = new StringBuilder();
		//		for(int i=0;i<statementExpress.length();i++)
		//		{
		//			char c = statementExpress.charAt(i);
		//			while (c!=' ')
		//			{
		//				sb.append(c);
		//			}
		//			if (sb.toString().equalsIgnoreCase("IF"))
		//			{
		//				sb.delete(0, arg1)
		//			}
		//			else if
		//		}
		//		return null;
	}


		public static Object execFunction(String functionExpress, ValueMetaInterface rowMeta, Object rowData) throws KettleException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(functionExpress.trim().equals("''") || functionExpress.trim().equals("\"\""))
			return new String().trim();
		if(functionExpress.indexOf("(")>-1)
		{
			String methodName  = getMethodName(functionExpress);
			String parametersString = getParamString(functionExpress);
			Object[] parameters = parseParametersString(parametersString);
			Object[] newParameters = null;
			if(parameters != null){
				newParameters = new Object[parameters.length];
				for(int i=0; i<parameters.length;i++)
				{
					String parameter = parameters[i].toString().trim();
					newParameters[i] = getParameterValue(parameter);

				}
			}
			return execNestedFunction(methodName,newParameters, rowMeta,rowData);
		}else
		{
			return rowData;
		}
	}
		
		public static Object execFunction(String functionExpress) throws KettleException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if(functionExpress.trim().equals("''") || functionExpress.trim().equals("\"\""))
				return new String().trim();
			if(functionExpress.indexOf("(")>-1)
			{
				String methodName  = getMethodName(functionExpress);
				String parametersString = getParamString(functionExpress);
				Object[] parameters = parseParametersString(parametersString);
				Object[] newParameters = null;
				if(parameters != null){
					newParameters = new Object[parameters.length];
					for(int i=0; i<parameters.length;i++)
					{
						String parameter = parameters[i].toString().trim();
						newParameters[i] = getParameterValue(parameter);

					}
				}

				return execNestedFunction(methodName,newParameters);
			}else
			{
				return 1;
			}
		}
		

	public static Object getParameterValue(String parameter)
	{
		Object parameterVa = null;
		if(isInteger(parameter))
		{
			int intvalue = Integer.parseInt(parameter);
			parameterVa = intvalue;
		}
		else
			parameterVa = parameter;
		return parameterVa;
	}

	private static String getMethodName(String functionExpress){
		return functionExpress.substring(0,functionExpress.indexOf("(")).trim();
	}

	private static String getParamString(String functionExpress){
		return functionExpress.substring(functionExpress.indexOf("(")+1, functionExpress.lastIndexOf(")")).trim();
	}

	private static String removeParamWS(String parametersString){
		StringBuffer buffer = new StringBuffer();
		String[] paramArray = parametersString.split(",");
		for(int i=0 ;i<paramArray.length;i++){
			if(i == paramArray.length -1){
				buffer.append(paramArray[i].trim());
			}else{
				buffer.append(paramArray[i].trim()).append(",");
			}
		}
		return buffer.toString();
	}
	/**
	 * parse parameter string to get parameters
	 * @param parametersString
	 * @return
	 */
	private static Object[] parseParametersString(String parametersString) {
		parametersString = removeParamWS(parametersString);
		if("".equals(parametersString)){
			return null;
		}
		int length = parametersString.length();
		int i=0;
		ArrayList<Object> list = new ArrayList<Object>();
		int level = 0;
		int start = 0;
		while( i < length)
		{
			if(parametersString.charAt(i)==',' && level==0 )
			{
				list.add(getParameterValue(parametersString.substring(start,i)));
				start=i+1;

			}else if (parametersString.charAt(i)=='(' )
			{
				level++;
			}else if (parametersString.charAt(i)==')' )
			{
				level--;
			}
			i++;
		}
		list.add(getParameterValue(parametersString.substring(start,parametersString.length())));
		return list.toArray();
	}

	public static boolean isInteger(String parameter)
	{	
		
		return StringUtils.isNumeric(parameter);
		/*try{
			java.lang.Integer.parseInt(parameter);
			return true;
		}catch(Exception e)
		{
			return false;
		}*/
	}

	public static Object execNestedFunction(String methodName, Object[] parameters, ValueMetaInterface rowMeta, Object rowData) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KettleException
	{	
		
		if(!hasNested(parameters))
		{
			return execOneFunction(methodName,parameters, rowMeta, rowData);
		}
		else
		{
			Object[] newParameters = new Object[parameters.length];
			for (int i=0;i<parameters.length;i++)
			{	
				//update by lwj
				if(parameters[i] == null){
					return null;
				}
				String parameter= parameters[i].toString();
				Object parameterValue;
				if (parameter.indexOf("(")>-1)
				{
					String nestedMethodName = getMethodName(parameter);
					String nestedMethodParametersString = getParamString(parameter);
					Object[] nestedMethodParameter = parseParametersString(nestedMethodParametersString);
					parameterValue = execNestedFunction(nestedMethodName,nestedMethodParameter,rowMeta,rowData);
					if(parameterValue instanceof String)
						parameterValue = "\""+parameterValue+"\"";
					newParameters[i]=parameterValue;
				}
				else
				{
					newParameters[i]=getParameterValue(parameters[i].toString());
				}
			}
			return execNestedFunction(methodName,newParameters,rowMeta,rowData);
		}
	}
	
	public static Object execNestedFunction(String methodName, Object[] parameters) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KettleException
	{
		if(!hasNested(parameters))
		{
			String className= "org.pentaho.di.core.util.function."+"Java"+ StringUtil.initCap(methodName);
			
			Class<?> clazz = Class.forName(className);
		}
		else
		{
			Object[] newParameters = new Object[parameters.length];
			for (int i=0;i<parameters.length;i++)
			{	
				//update by lwj
				if(parameters[i] == null){
					return null;
				}
				String parameter= parameters[i].toString();
				Object parameterValue;
				if (parameter.indexOf("(")>-1)
				{
					String nestedMethodName = getMethodName(parameter);
					String nestedMethodParametersString = getParamString(parameter);
					Object[] nestedMethodParameter = parseParametersString(nestedMethodParametersString);
					parameterValue = execNestedFunction(nestedMethodName,nestedMethodParameter);
					if(parameterValue instanceof String)
						parameterValue = "\""+parameterValue+"\"";
					newParameters[i]=parameterValue;
				}
				else
				{
					newParameters[i]=getParameterValue(parameters[i].toString());
				}
			}
			return execNestedFunction(methodName,newParameters);
		}
		return 0;
	}
	
	
	/**
	 * execute one method
	 * @param methodName: a method string without nested method
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws KettleException
	 */
	public static Object execOneFunction(String methodName, Object[] parameters, ValueMetaInterface rowMeta, Object rowData) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, KettleException
	{
		String className= "org.pentaho.di.core.util.function."+"Java"+ StringUtil.initCap(methodName);
		
		Class<?> clazz = Class.forName(className);
		Class<?>[] parameterClass = null;
		Object[] realParameters = null;
		String log = "";
		//execute trans
		if(parameters!=null && parameters.length>0)
		{
			parameterClass = new Class[parameters.length];
			realParameters = new Object[parameters.length];
			for (int i=0;i<parameters.length;i++)
			{
				parameterClass[i]= parameters[i].getClass();
				if(parameters[i].toString().startsWith("\"") && parameters[i].toString().endsWith("\""))
				{
					realParameters[i]= parameters[i].toString().substring(1,parameters[i].toString().length()-1);
				}
				else if(parameters[i].toString().startsWith("'") && parameters[i].toString().endsWith("'")){
					realParameters[i]= parameters[i].toString().substring(1,parameters[i].toString().length()-1);
				}
				else if(isInteger(parameters[i].toString()))
				{
					realParameters[i]= parameters[i];
				}
				else
				{
					String columnName = parameters[i].toString();

					if("NullToEmpty".equals(methodName)){
						if(rowData == null){
							return "";
						}else{
							realParameters[i] = rowData;
							//获取函数值的类型  update by lwj
							parameterClass[i]=rowData.getClass();
						}
					}else if(rowData == null){
						return null;
					}else{
						realParameters[i] =rowData;
						//获取函数值的类型  update by lwj
						parameterClass[i]= rowData.getClass();
					}
				}
				log = log + (i+1) + "." + parameterClass[i].toString() + "   ";
			}
		}
		//允许传入参数类型的日志  update by lwj
//		if(clazz.getDeclaredMethod("decribe")!= null){
//			Method method2 = clazz.getDeclaredMethod("decribe");
//			method2.invoke(clazz);
//			LOG.logDebug("InputParType:"+log);
//		}
		Method method = clazz.getDeclaredMethod(methodName, parameterClass);
		Object result = method.invoke(clazz, realParameters);
//		Object result = method.invoke(object, realParameters);
		return result;
	}

	private static boolean hasNested(Object[] parameters) {
		if(parameters == null){
			return false;
		}
		for (int i=0;i<parameters.length;i++)
		{
			if(parameters[i] == null){
				return true;
			}
			String parameter= parameters[i].toString();
			if (parameter.indexOf("(")>-1 && parameter.indexOf("\"")!=0)
			{
				return true;
			}
		}
		return false;
	}

	public static class StatementComponents
	{

		private String condition;
		private String trueFunction;
		private String falseFunction;

		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
			this.condition = condition;
		}
		public String getTrueFunction() {
			return trueFunction;
		}
		public void setTrueFunction(String trueFunction) {
			this.trueFunction = trueFunction;
		}
		public String getFalseFunction() {
			return falseFunction;
		}
		public void setFalseFunction(String falseFunction) {
			this.falseFunction = falseFunction;
		}


	}
	
	
	public static Object checkFunction(String function)throws Exception {
		function = function.trim();
		if(function.length() >= 2){
			String token = function.substring(0,2);
			if (token.equalsIgnoreCase("IF"))
			{
				StatementComponents components = parseStatement(function);
				if(evl(components.getCondition()))
				{
					return execFunction( components.getTrueFunction());
				}
				else
					return execFunction( components.getFalseFunction());
			}
			else
			{
				return execFunction( function);
			}
		}else{
			return execFunction( function);
		}
	}
}


