package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;

public class PurgeRecycleBin 
{
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IInfoObjects infoObjects = infoStore.query("select si_name, si_cuid from ci_infoobjects where children(\"si_name='folder hierarchy'\",\"si_name = 'recycle bin' and si_parentid = 23\")");
		
		for(Object o : infoObjects)
		{
			IInfoObject infoObject = (IInfoObject)o;
			System.out.println("Deleting: " + infoObject.getTitle());
			infoObject.deleteNow();
		}
	}
}
