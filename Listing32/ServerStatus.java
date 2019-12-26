package bosdkbook;

import java.util.Locale;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.IStatusInfo;

public class ServerStatus
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService("", "InfoStore");

		IInfoObjects ios = infoStore.query("select * from ci_systemobjects where si_kind = 'server' order by si_name");

		for(Object o : ios)
		{
			IServer server = (IServer)o;

			System.out.print(server.getTitle() + ": " );

			IStatusInfo statusInfo = server.getStatusInfo();
			switch(statusInfo.getStatus())
			{
				case IStatusInfo.Status.FAILED:
					System.out.println("Failed: " + statusInfo.getMessage(Locale.getDefault()));
					break;
				case IStatusInfo.Status.INITIALIZING:
					System.out.println("Initializing");
					break;
				case IStatusInfo.Status.RUNNING:
					System.out.println("Running");
					break;
				case IStatusInfo.Status.RUNNING_WITH_ERRORS:
					System.out.println("Running with Errors");
					break;
				case IStatusInfo.Status.RUNNING_WITH_WARNINGS:
					System.out.println("Running with Warnings");
					break;
				case IStatusInfo.Status.STARTING:
					System.out.println("Starting");
					break;
				case IStatusInfo.Status.STOPPED:
					System.out.println("Stopped");
					break;
				case IStatusInfo.Status.STOPPING:
					System.out.println("Stopping");
					break;
			}
		}
	}
}
