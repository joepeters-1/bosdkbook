package bosdkbook;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.plugin.desktop.user.IUserAlias;

public class DisableInactiveUsers
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService("", "InfoStore");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		String yearAgo = sdf.format(cal.getTime());

		IInfoObjects inactiveUsers = infoStore.query("select si_name,si_aliases,si_lastlogontime from ci_systemobjects where si_kind = 'user' and (si_lastlogontime is null or si_lastlogontime <= '" + yearAgo + "') and si_name not in ('administrator','guest','smadmin','qaawsservletprincipal')");

		for(Object o : inactiveUsers)
		{
			IUser inactiveUser = (IUser)o;
			System.out.println("Disabling user: " + inactiveUser.getTitle());
			for(IUserAlias alias : inactiveUser.getAliases())
				alias.setDisabled(true);
		}
		infoStore.commit(inactiveUsers);
	}
}
