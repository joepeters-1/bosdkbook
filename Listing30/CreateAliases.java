package bosdkbook;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.plugin.desktop.user.IUserAlias;

public class CreateAliases
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "osboxes", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService("", "InfoStore");

		IInfoObjects users = infoStore.query("select si_name,si_aliases from ci_systemobjects where si_kind = 'user'");

		for(Object o : users)
		{
			IUser user = (IUser)o;
			boolean hasEnterpriseAlias = false;

			for(IUserAlias alias : user.getAliases())
			{
				if(alias.getType() == IUserAlias.ENTERPRISE)
					hasEnterpriseAlias = true;
			}

			if(!hasEnterpriseAlias)
			{
				System.out.println("Creating new enterprise alias for user: " + user.getTitle());
				IUserAlias alias = user.getAliases().addNew("secEnterprise:" + user.getTitle(), false);
				user.save();
			}
		}
	}
}
