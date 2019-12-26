package bosdkbook;

import java.util.Iterator;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.CePropertyID;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;

public class ListProps {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects ios = infoStore.query("select top 10 * from ci_systemobjects");
		
		for(int x = 0;x < ios.size(); x++)
		{
			IInfoObject io = (IInfoObject) ios.get(x);
			System.out.println(io.getTitle());
			displayBag(io.properties(),"");
			System.out.println("\n");
		}
		System.out.println("Done!");
		session.logoff();
	}
	
	private static void displayBag(IProperties props,String indent)
	{
		Iterator iPropIDs = props.keySet().iterator();
		while(iPropIDs.hasNext())
		{
			IProperty prop = props.getProperty(iPropIDs.next());
			String propName = CePropertyID.idToName(prop.getID());
			Object propValue = prop.getValue();
			
			if(prop.isContainer())
			{
				System.out.println(indent + propName + " (Bag)");
				displayBag((IProperties)propValue,indent + "\t");
			}
			else
			{
				String propDatatype = propValue.getClass().getSimpleName();
				System.out.println(indent 
					+ propName + " ("
					+ propDatatype + "): "
					+ propValue);
			}
		}
	}
}
