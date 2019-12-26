package bosdkbook;

import java.util.Date;

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

public class ListUsers {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects users = infoStore.query("select si_name,si_creation_time from ci_systemobjects where si_kind = 'user'");
		
		if(users.size() == 0)
		{
			System.out.println("No users found!");
			session.logoff();
			return;
		}
		for(int x = 0;x < users.size(); x++)
		{
			IInfoObject user = (IInfoObject) users.get(x);
			IProperties props = user.properties();
			IProperty creationTimeProp = props.getProperty(CePropertyID.SI_CREATION_TIME);
			Date creationTime = (Date)creationTimeProp.getValue();
			System.out.println("Name: " + user.getTitle() + "\r\nCreated: " + creationTime + "\r\n");
		}
		System.out.println("Done!");
		session.logoff();
	}
}
