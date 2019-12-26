package bosdkbook;

import java.util.Iterator;
import java.util.Locale;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IExplicitPrincipal;
import com.crystaldecisions.sdk.occa.infostore.IExplicitRight;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.ISecurityInfo2;

public class DisplayRights 
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IInfoObjects ios = infoStore.query("select si_id from ci_infoobjects where si_parentid = 23 and si_name = 'reports'");

		for(Object o : ios)
		{
			IInfoObject io = (IInfoObject) o;
			ISecurityInfo2 sec = io.getSecurityInfo2();

			Iterator<IExplicitPrincipal> iPrinc =
				sec.getExplicitPrincipals().iterator();

			while(iPrinc.hasNext())
			{
				IExplicitPrincipal princ = iPrinc.next();

				System.out.println("Principal: " + princ.getName());

				System.out.println(
					"Source Kind\tApplies\tGranted\tScope\tDescription");

				Iterator<IExplicitRight> itRight = princ.getRights().iterator();
				while(itRight.hasNext())
				{
					IExplicitRight right = itRight.next();

							String sourceKind = right.getRightPluginKind();
							if(sourceKind.equals(""))
								sourceKind="General";

					String out = right.getRightPluginKind() + "\t" 
						+ right.getApplicableKind() + "\t"
						+ (right.isGranted() ? "Granted\t" : "Denied\t") 
						+ right.getScope() + "\t"	
						+ right.getDescription(Locale.getDefault());

					System.out.println(out);
				}
			}
		}
	}
}
