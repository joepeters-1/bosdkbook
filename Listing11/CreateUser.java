package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;

public class CreateUser {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObject financeGroup = (IInfoObject) infoStore.query("select si_id from ci_systemobjects where si_kind = 'usergroup' and si_name = 'finance'").get(0);
		
		IInfoObjects infoObjects = infoStore.newInfoObjectCollection();
		
		IUser newUser = (IUser)infoObjects.add(IUser.KIND);
		
		newUser.setTitle("Francine");
		newUser.setFullName("Francine Fullworth");
		newUser.setEmailAddress("francine@company.com");
		newUser.setNewPassword("pAssWord");
		
		newUser.getGroups().add(financeGroup.getID());
		
		newUser.save();
		
		System.out.println("Done!");
		session.logoff();
	}
}
