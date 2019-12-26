package bosdkbook;

import java.util.Set;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;

public class ListFredsGroups {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects infoObjects = infoStore.query("select si_usergroups from ci_systemobjects where si_name = 'fred' and si_kind = 'user'");
		
		IUser fred = (IUser)infoObjects.get(0);
		
		Set<Integer> groupIDs = fred.getGroups();
		for(Integer groupID : groupIDs)
			System.out.println(groupID);

		System.out.println("Done!");
		session.logoff();
	}
}
