package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;

public class ListUsers {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects users = infoStore.query("select si_name from ci_systemobjects where si_kind = 'user'");
		
		if(users.size() == 0)
		{
			System.out.println("No users found!");
			session.logoff();
			return;
		}
		for(int x = 0;x < users.size(); x++)
		{
			IInfoObject user = (IInfoObject) users.get(x);
			System.out.println(user.getTitle());
		}
		System.out.println("Done!");
		session.logoff();
	}
}
