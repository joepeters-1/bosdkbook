package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;


public class ProvokeError {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects infoObjects = infoStore.query("select si_name,si_id from ci_systemobjects where si_name in ('fred','ephraim','sri','isabel')");
		
		for(int x = 0;x < infoObjects.size(); x++)
		{
			IUser user = (IUser)infoObjects.get(x);
			String userName = user.getTitle();
			System.out.println("Got user: " + userName);
			
			if(userName.equalsIgnoreCase("Fred"))
				user.setDescription("I am Fred");
			else if(userName.equalsIgnoreCase("Ephraim"))
				user.getGroups().add(12);
			else if(userName.equalsIgnoreCase("Isabel"))
				user.setTitle("Guest");
			else if(userName.equalsIgnoreCase("Sri"))
				user.setDescription("I am Sri");
		}
			
		infoStore.commit(infoObjects);

		System.out.println("Done!");
		session.logoff();
	}
}
