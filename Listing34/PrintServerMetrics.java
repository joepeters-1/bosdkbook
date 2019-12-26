package bosdkbook;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import com.businessobjects.sdk.plugin.desktop.common.IMetric;
import com.businessobjects.sdk.plugin.desktop.common.IMetrics;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions
		.IMLDescriptions;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions
		.IMetricDescriptions;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions
		.IPropertyBagRenderTemplate;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions
		.IPropertyRenderTemplate;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions
		.ValueFormat;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.IServerMetrics;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;

public class PrintServerMetrics
{
	private IMetricDescriptions mds;
	private static final SimpleDateFormat sdf = 
				new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

	public static void main(String[] args) throws Exception
	{
		new ServerMetrics();
	}

	private ServerMetrics() 
	throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService("", "InfoStore");
		
		// Get an IMetricDescriptions object. Used to get render templates.
		mds = (IMetricDescriptions) infoStore.query("select * from ci_systemobjects where si_kind = 'metricdescriptions'").get(0);
		
		IInfoObjects ios = infoStore.query("select * from ci_systemobjects where si_kind = 'server'  ");

		// Iterate through each server
		for(Object o : ios)
		{
			IServer server = (IServer)o; 
			IServerMetrics serverMetrics = server.getMetrics();

			System.out.println("\nServer: " + server.getTitle());
			
			// Iterate through each service that this server supports
			@SuppressWarnings("unchecked")
			Iterator<String> itService = serverMetrics
					.getServiceInterfaceNames().iterator();
			while(itService.hasNext())
			{
				// Get the name and metrics for this service
				String serviceName = itService.next();
				IMetrics metrics = serverMetrics.getMetrics(serviceName);
				
				if(metrics.size() == 0)
					continue; // Don't print anything if service has no metrics to be displayed
				
				// A service may or may not have a friendly name. If not, use the SI_NAME value.
				String serviceFriendlyName =
					mds.getServiceFriendlyName(serviceName, 
					Locale.getDefault());
				
				if(serviceFriendlyName==null)
					serviceFriendlyName = serviceName;
				
				System.out.println("	Service: " + serviceFriendlyName);

				// IMLDescriptions contains the render templates for the service's top-level metrics
				IMLDescriptions metricDescriptions =
					mds.getMetricDescriptions(serviceName);
				
				// Iterate through each of the service's top-level metrics
				@SuppressWarnings("unchecked")
				Iterator<IMetric> itMetric = metrics.iterator();
				while(itMetric.hasNext())
				{
					IMetric metric = itMetric.next();
					if(metricDescriptions == null) 
					{
						// If this service has no render templates, just print the metric name and value
						System.out.println("		 " 
							+ metric.getName()
							+ ": " + metric.getValue().toString());
					}
					else
					{
						// Get the render template for this metric
						IPropertyRenderTemplate propRenderTemplate =
 							metricDescriptions
							.getPropertyRenderTemplate(metric.getName());
						// If there is no render template for this metric, or if it's a hidden metric, then skip
						if(propRenderTemplate==null 
							|| propRenderTemplate.getValueFormat() 
								== ValueFormat.HIDDEN)
							continue;
						// Display the metric label and value. Note that this may be a single value or collection
						printMetric(serverMetrics,
							metric.getValue(),propRenderTemplate);
					}
				}
			}
		}
		
	}
	
	void printMetric (IServerMetrics serverMetrics,Object value, IPropertyRenderTemplate propRenderTemplate) 
	{
		// Get the label and format for this metric
	    String label = propRenderTemplate.getLabel(Locale.getDefault());
	    ValueFormat valueFormat = propRenderTemplate.getValueFormat();
	    
	    // Print the metric's value, depending on whether it's a property bag or not
	    if (valueFormat == ValueFormat.PROPBAG) 
	    {
	        IProperties propertyBag =
					serverMetrics.resolveMetricString
					(value.toString());
	        System.out.println("		" + label + ":");
	        printMetricBag(propertyBag,propRenderTemplate
					.getRenderTemplateName(),"			");
	    }
	    else
	        System.out.println("		" + label + " (" 
					+ propRenderTemplate.getID()
					+ "): " + formatMetricValue(value, valueFormat,
					propRenderTemplate));
	}

	
	void printMetricBag(IProperties bag,String renderTemplateName,String indent)
	{
		// Get the render template for this property bag
		IPropertyBagRenderTemplate bagRenderTemplate =
			mds.getPropertyBagRenderTemplate(
			renderTemplateName);
		
		// Get the number of elements in the bag.  We first need to get the name of the property
		// (ex. SI_TOTAL), then get the value of the property having that name.
		int bagSize = bag.getInt(bagRenderTemplate.getTotalID());
		for(int x=1;x<=bagSize;x++)
		{
			// Get the properties within this bag
			IProperties ps = bag.getProperties(bagRenderTemplate
				.getPropNamePrefix() + x);
		
			// Iterate through the render templates within this bag. Each template will 
			// describe the structure of the metrics within the bag.
			@SuppressWarnings("unchecked")
			Iterator<IPropertyRenderTemplate> itSubTemplate =
					bagRenderTemplate
					.getPropertyRenderTemplates().iterator();
			while(itSubTemplate.hasNext())
			{
				// Get the render template of the metric in the bag
				IPropertyRenderTemplate subTemplate =
						itSubTemplate.next();
				
				// If the bag contains another bag, recurse into this method
				if(subTemplate.getValueFormat() == ValueFormat.PROPBAG)
				{
					IProperties subBag =
							ps.getProperties(subTemplate.getID());
					printMetricBag(subBag,subTemplate
						.getRenderTemplateName(),indent + "	");
				}
				// Otherwise, get the metric's name and value, then format the value to a string
				else
				{
					String label =
						subTemplate.getLabel(Locale.getDefault());
					Object subPropertyValue =
						ps.getProperty(subTemplate.getID()).getValue();
					String formattedMetricValue =
						formatMetricValue(subPropertyValue,
						subTemplate.getValueFormat(), subTemplate);
 					System.out.println(indent 
						+ label
						+ ": " 
						+ formattedMetricValue);
				}
			}
			if(x < bagSize)
				System.out.println(); // Don't print crlf for last entry in bag
		}
	}
	
	// Inspired by: https://answers.sap.com/questions/7487062/number-of-jobs-on-job-servergroup.html
	private String formatMetricValue(Object value,ValueFormat valueFormat,IPropertyRenderTemplate template)
	{
		if (valueFormat == ValueFormat.FLOAT 
			|| valueFormat == ValueFormat.PERCENT
			|| valueFormat == ValueFormat.SHIFTSIZE_KB
			|| valueFormat == ValueFormat.SHIFTSIZE_MB
			|| valueFormat == ValueFormat.SHIFTSIZE_GB) 
		{
			double val;
			double scale = 1.0;
	
			if(value instanceof String) 
				val = Double.parseDouble((String) value);
			else 
				val = ((Number) value).doubleValue();
	
			if(valueFormat == ValueFormat.PERCENT) {
				scale = 100.0;
			} else if(valueFormat == ValueFormat.SHIFTSIZE_KB) {
				scale = 1024.0;
			} else if(valueFormat == ValueFormat.SHIFTSIZE_MB) {
				scale = 1024.0 * 1024.0;
			} else if(valueFormat == ValueFormat.SHIFTSIZE_GB) {
				scale = 1024.0 * 1024.0 * 1024.0;
			}
	                    
			return "" + (val/scale);
		}
		else if(valueFormat == ValueFormat.DATETIME)
		{
			// A Date property can be a java.util.Date, IProperty, Float, or Double,
			// convert to a Date if necessary.
			Date date;
			if(value instanceof IProperty)
			{
				IProperty prop = (IProperty)value;
				date = (Date) prop.getValue();
			}
			else if(value instanceof Date)
			{
				date = (Date)value;
			}
			else // Not Date or IProperty; must be a Number, in the form of number of days since 11/30/1899
			{
				Number num = (Number)value;
				int days = num.intValue();
				double dayPortion =  
					(num.doubleValue() - days) * 86400000;
				Calendar cal = Calendar.getInstance();
				cal.set(1899, 11, 30, 0, 0, 0);
				cal.add(Calendar.DATE, days);
				cal.add(Calendar.MILLISECOND, (int)dayPortion);
				date = cal.getTime();
			}
			if(date.getTime() == -185544796348800000L)
				return null;
			
			return(sdf.format(date));
		}
		else if(valueFormat == ValueFormat.STRING_MAP)
		{
			// If the value is a mapped string, look up the value using the string map name and property value
			// If the string map is not found, then just return the property value itself
			String mappedValue =
				mds.getMLDescription(template.getStringMapName(),
				value.toString(), Locale.getDefault());
			if(mappedValue == null)
				return value.toString();
			else
				return mappedValue;
		}
		else
			return value.toString();
	}
}
