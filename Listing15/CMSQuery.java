package bosdkbook;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.ICMSQuery;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;

public class CMSQuery 
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		ICMSQuery cmsQuery = infoStore.createCMSQuery("select si_id,si_name  from ci_infoobjects where si_parentid = 23 order by si_name");

		cmsQuery.setPageSize(6);
		cmsQuery.setCurrentPageNumber(0);

		while(cmsQuery.hasNextPage())
		{
			cmsQuery.nextPage();
			IInfoObjects ios = infoStore.query(cmsQuery);
			System.out.println("\nBatch size: " + ios.size());
			for(Object o : ios)
			{
				IInfoObject io = (IInfoObject)o;
				System.out.println(io.getTitle());
			}
		} 
	}
}
