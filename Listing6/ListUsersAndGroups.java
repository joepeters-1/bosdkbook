package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.plugin.desktop.usergroup.IUserGroup;

public class ListUsersAndGroups {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects infoObjects = infoStore.query("select si_name, si_userfullname, si_group_members, si_kind from ci_systemobjects where si_kind in ('user','usergroup')");
		
		if(infoObjects.size() == 0)
		{
			System.out.println("No users or user groups found!");
			session.logoff();
			return;
		}
		for(int x = 0;x < infoObjects.size(); x++)
		{
			IInfoObject infoObject = (IInfoObject)infoObjects.get(x);
			if(infoObject.getKind().equals(IUser.KIND))
			{
				IUser user = (IUser)infoObject;
				System.out.println("User: " + user.getTitle() + ", Full Name: " + user. getFullName());
			}
			else
			{
				IUserGroup userGroup = (IUserGroup)infoObject;
				System.out.println("Group: " + userGroup.getTitle() + ", member count: " + userGroup.getUsers().size());
			}
		}
		System.out.println("Done!");
		session.logoff();
	}
}
