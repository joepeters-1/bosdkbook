package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBaseEx;

public class SampleProgramObject implements IProgramBaseEx {

	@Override
	public void run(IEnterpriseSession paramIEnterpriseSession,
			IInfoStore paramIInfoStore, IInfoObject paramIInfoObject,
			String objectID, String[] paramArrayOfString)
			throws SDKException {

		IInfoObjects users = paramIInfoStore.query("select si_name from ci_systemobjects where si_kind = 'user'");
		
		if(users.size() == 0)
		{
			System.out.println("No users found!");
			return;
		}
		for(int x = 0;x < users.size(); x++)
		{
			IInfoObject user = (IInfoObject) users.get(x);
			System.out.println(user.getTitle());
		}
		System.out.println("Done!");
	}
}
