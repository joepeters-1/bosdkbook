package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;

public class SetFinanceUsers {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects infoObjects = infoStore.query("select si_name,si_id from ci_systemobjects where children(\"si_name='usergroup-user'\", \"si_name='finance'\")");
		
		for(int x = 0;x < infoObjects.size(); x++)
		{
			IInfoObject infoObject = (IInfoObject)infoObjects.get(x);
			System.out.println("Setting description for user: " + infoObject.getTitle());
			infoObject.setDescription("Finance User");
			infoObject.save();
		}

		System.out.println("Done!");
		session.logoff();
	}
}
