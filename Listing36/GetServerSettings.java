package bosdkbook;

import java.util.Iterator;
import java.util.Locale;

import com.businessobjects.sdk.plugin.desktop.common.IConfigProperties;
import com.businessobjects.sdk.plugin.desktop.common.IConfigProperty;
import com.businessobjects.sdk.plugin.desktop.common.IConfiguredService;
import com.businessobjects.sdk.plugin.desktop.service.IService;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

public class GetServerSettings
{
	public static void main(String[] args) throws Exception
	{
		new GetServerSettings();
	}

	private GetServerSettings() 
	throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService("", "InfoStore");
		
		IInfoObjects ios = infoStore.query("select * from ci_systemobjects where si_kind = 'server' and si_name like '%WebIntelligenceProcessingServer%'");

		for(Object o : ios)
		{
			IServer server = (IServer)o; 
			
			@SuppressWarnings("unchecked")
			Iterator<IConfiguredService> itCS =
				server.getHostedServices().iterator();
			while(itCS.hasNext())
			{
				IConfiguredService cs = itCS.next();
				System.out.println("Configuration settings for: " 
					+ getServiceByID(infoStore, cs.getID())
					.getDescription());
				
				IConfigProperties configProps = cs.getConfigProps();
				displayProperties(configProps,"  ");
				System.out.println();
			}
		}
	}

	private void displayProperties(IConfigProperties configProps,String indent) 
	throws SDKException
	{
		for(String propName : configProps.getPropNames())
		{
			IConfigProperty configProperty =
				configProps.getProp(propName);
			String label =
				configProperty.getDisplayName(Locale.getDefault());
			Object propValue = configProperty.getValue();
			if(propValue instanceof IConfigProperties)
			{
				System.out.println(indent + label + ":");
				displayProperties((IConfigProperties) propValue,
					indent + "  ");
			}
			else
				System.out.println(indent + label + ": " 
					+ configProperty.getValue());
		}
	}
	
	private IService getServiceByID(IInfoStore infoStore,Integer id)
	throws SDKException
	{
		IInfoObjects ios = infoStore.query("select * from ci_systemobjects where si_id = " + id);
		if(ios.size()==0)
			return null;
		return (IService) ios.get(0);
	}
}
