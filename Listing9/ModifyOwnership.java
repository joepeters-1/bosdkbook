package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.CePropertyID;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.properties.IProperties;
import com.crystaldecisions.sdk.properties.IProperty;

public class ModifyOwnership {
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		IInfoObjects infoObjects = infoStore.query("select si_name,si_owner,si_ownerid from ci_infoobjects where si_kind = 'webi' and si_owner != 'fred' and si_ancestor = 4213");
		
		for(Object o : infoObjects)
		{
			IInfoObject infoObject = (IInfoObject)o;
			System.out.println("Setting ownership for document: " 
                                + infoObject.getTitle());
			IProperties props = infoObject.properties();
			IProperty prop = props.getProperty(CePropertyID.SI_OWNERID);
			prop.setValue(4104);

			infoObject.save();
		}
		
		System.out.println("Done!");
		session.logoff();
	}
}
