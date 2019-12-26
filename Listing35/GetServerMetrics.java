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
import com.crystaldecisions.sdk.properties.IProperty;

public class GetServerMetrics
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

			System.out.println("\n" + server.getTitle() + ": ");
			System.out.println("Auditing events: " 
				+ getMetricValue(server,"Auditing Metrics",
					"Current Number of Auditing Events in the Queue"));
			System.out.println("PID: " 
				+ getMetricValue(server,
					"Common Server Metrics","PID"));
		}
	}
	
	String getMetricValue(IServer server,String requestedServiceName,String requestedMetricName) 
	throws SDKException
	{
		IServerMetrics serverMetrics = server.getMetrics();
		
		// Iterate through each service that this server supports
		@SuppressWarnings("unchecked")
		Iterator<String> itService =
			serverMetrics.getServiceInterfaceNames().iterator();
		while(itService.hasNext())
		{
			// Get the name of this service
			String serviceName = itService.next();
			
			// A service may or may not have a friendly name. If not, use the SI_NAME value.
			String serviceFriendlyName =
				mds.getServiceFriendlyName(serviceName,
					Locale.getDefault());
			
			// Continue if this is not the service we're looking for
			if(! (requestedServiceName.equals(serviceName) 
				|| requestedServiceName.equals(serviceFriendlyName)))
				continue;
					
			IMetrics metrics = serverMetrics.getMetrics(serviceName);
			if(metrics == null || metrics.size() == 0)
				return "N/A"; // Don't print anything if service has no metrics to be displayed

			// IMLDescriptions contains the render templates for the service's top-level metrics
			IMLDescriptions metricDescriptions = mds.getMetricDescriptions(serviceName);
			
			// Iterate through each of the service's top-level metrics
			@SuppressWarnings("unchecked")
			Iterator<IMetric> itMetric = metrics.iterator();
			while(itMetric.hasNext())
			{
				IMetric metric = itMetric.next();
				
				String metricName = metric.getName();
				String metricLabel = ""; // will adjust below if render template available
				String metricValue;
				if(metricDescriptions == null) 
				{
					// If this service has no render templates, just take the metric's string value
					metricValue = metric.getValue().toString();
				}
				else
				{
					// Get the render template for this metric
					IPropertyRenderTemplate propRenderTemplate = metricDescriptions.getPropertyRenderTemplate(metric.getName());
					
					// If there is no render template for this metric, or if it's a hidden metric, then continue
					if(propRenderTemplate==null)
						continue;

					// Get the label, value, and format
					MetricLabel =
						propRenderTemplate.getLabel(Locale.getDefault());
					Object value = metric.getValue();
				    ValueFormat valueFormat =
						propRenderTemplate.getValueFormat();
				    
				    // If the metric is a bag, then just return N/A
				    if (valueFormat == ValueFormat.PROPBAG) 
				    	metricValue = "N/A";
				    
				    metricValue = formatMetricValue(value, 
						valueFormat, propRenderTemplate);
				}
				
				// Check the metric name and label to see if it 
				// is the one we want.
				if(requestedMetricName.equals(metricName) 
					|| requestedMetricName.equals(metricLabel))
					return metricValue;
			}
		}
		return ""; // metric not found
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
