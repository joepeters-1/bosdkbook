package bosdkbook;

import java.util.Iterator;
import java.util.Locale;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IEffectivePrincipal;
import com.crystaldecisions.sdk.occa.infostore.IEffectiveRight;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.IRightSource;
import com.crystaldecisions.sdk.occa.infostore.ISecurityInfo2;

public class DisplayRights 
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IInfoObjects ios = infoStore.query("select si_id from ci_infoobjects where si_name = 'service'");

		for(Object o : ios)
		{
			IInfoObject io = (IInfoObject) o;
			ISecurityInfo2 sec = io.getSecurityInfo2();

			Iterator<IEffectivePrincipal> iPrinc =
				sec.getEffectivePrincipals().iterator();

			while(iPrinc.hasNext())
			{
				IEffectivePrincipal princ = iPrinc.next();

				System.out.println("Principal: " + princ.getName());

				Iterator<IEffectiveRight> itRight =
					princ.getRights().iterator();

				while(itRight.hasNext())
				{
					IEffectiveRight right = itRight.next();

					for(Object o2 : right.getSources())
					{
						IRightSource source = (IRightSource)o2;
						if(source.isInherited() && !source.isFromRole())
						{
							String sourceKind = right.getRightPluginKind();
							if(sourceKind.equals(""))
								sourceKind="General";

							String out = "Source kind: " 
								+ right.getRightPluginKind() + "\n"
								+ "Applies to: " 
								+ right.getApplicableKind() + "\n"
								+ "Scope: " + right.getScope() + "\n"
								+ "Description: "
								+ right.getDescription(Locale.getDefault())
								+ "\n"
								+ "Inherited from object: "
								+ getNameFromID(infoStore,source.getObjectID())
								+ "\n"
								+ "Inherited from principal: "
								+ getNameFromID(infoStore,
									source.getPrincipalID()) + "\n"
								+ "Assignment: "
								+ (right.isGranted() ? "Granted\n" : "Denied\n"); 

							System.out.println(out);
						}
					}
				}
			}
		}
	}

	private static String getNameFromID(IInfoStore infoStore,Integer id)
	throws SDKException
	{
		IInfoObjects ios = infoStore.query("select si_name from ci_systemobjects,ci_infoobjects,ci_appobjects where si_id = " + id);
		if(ios.size() == 0)
			return "";
		return ((IInfoObject)ios.get(0)).getTitle();
	}
}
