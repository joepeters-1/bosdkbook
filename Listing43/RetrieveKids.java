package bosdkbook;

import com.businessobjects.sdk.plugin.desktop.webi.IWebi;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;

public class RetrieveKids {

	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IInfoObjects infoObjects = infoStore.query("select * from ci_infoobjects where si_id = 5927");
		
		IWebi webi = (IWebi)infoObjects.get(0);

		IInfoObjects kids = webi.getInstances();

		for(Object o : kids)
		{
			IInfoObject kid = (IInfoObject)o;
			System.out.println("Child instance " + kid.getTitle() + " completed at " + kid.getUpdateTimeStamp());
		}
	}
}
