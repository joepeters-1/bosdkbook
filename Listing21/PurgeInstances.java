package bosdkbook;

import org.apache.log4j.Logger;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBaseEx;

public class PurgeInstances implements IProgramBaseEx  
{
	@Override
	public void run(IEnterpriseSession paramIEnterpriseSession,
			IInfoStore paramIInfoStore, IInfoObject paramIInfoObject,
			String objectID, String[] paramArrayOfString)
			throws SDKException 
	{
		String ancestor = paramArrayOfString[0];

		System.out.println("Starting purge of instances from parent: " + ancestor	);

		IInfoObjects ios = paramIInfoStore.query("select si_id,si_name from ci_infoobjects where si_schedule_status in (1,3) and si_ancestor = " + ancestor);

		for(Object o : ios)
		{
			IInfoObject io = (IInfoObject)o;
			io.deleteNow();
			IInfoObject io = (IInfoObject)o;
		}
	}
}
