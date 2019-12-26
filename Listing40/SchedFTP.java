package bosdkbook;

import com.businessobjects.sdk.plugin.desktop.webi.IWebi;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IDestination;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.ISchedulingInfo;
import com.crystaldecisions.sdk.plugin.destination.ftp.IFTP;
import com.crystaldecisions.sdk.plugin.destination.ftp.IFTPOptions;

public class SchedFTP {

	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IFTP ftpPlugin = (IFTP) infoStore.query("select * from ci_systemobjects where si_plugin_object = 1 and si_kind = '" + IFTP.KIND + "'").get(0);
		IFTPOptions ftpOptions = (IFTPOptions) ftpPlugin.getScheduleOptions();

		IInfoObjects infoObjects = infoStore.query("select * from ci_infoobjects where si_name = 'Schedule Test' and si_kind = 'webi' and si_instance = 0");
		
		if(infoObjects.size() > 0)
		{
			IWebi webi = (IWebi) infoObjects.get(0);
			ISchedulingInfo sched = webi.getSchedulingInfo();
			
			ftpOptions.setServerName("ftp.company.com");
			ftpOptions.setPort(21);
			ftpOptions.setUserName("ftp_user");
			ftpOptions.setPassword("SuperSecret");
			ftpOptions.getDestinationFiles().add("/var/upload/report.wid");
			
			IDestination dest = sched.getDestinations().add(IFTP.PROGID);
			dest.setFromPlugin(ftpPlugin);
			webi.schedule();
		}
		else
			System.out.println("Could not find document!");
		
		System.out.println("Done!");
	}
}
