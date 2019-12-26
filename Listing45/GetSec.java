package bosdkbook;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.ISecurityInfo2;

public class GetSec {

	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IInfoObjects ios = infoStore.query("select si_id from ci_infoobjects where si_parentid = 23 and si_name = 'reports'");

		for(Object o : ios)
		{
			IInfoObject io = (IInfoObject) o;
			ISecurityInfo2 sec = io.getSecurityInfo2();
		}
	}
}
