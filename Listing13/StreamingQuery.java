package bosdkbook;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.IStreamingQuery;

public class StreamingQuery 
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IStreamingQuery streamingQuery = infoStore.getStreamingQuery("select * from ci_infoobjects,ci_appobjects,ci_systemobjects");

		while(streamingQuery.hasNext())
		{
			IInfoObjects ios = streamingQuery.next();
			System.out.println(ios.size());
		}
		streamingQuery.close();
	}
}
