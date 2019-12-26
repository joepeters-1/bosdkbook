package bosdkbook;

import java.util.Scanner;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.ExpectedRunState;
import com.crystaldecisions.sdk.plugin.desktop.server.IStatusInfo;

public class ServerManager
{
	Scanner s = new Scanner(System.in);

	public static void main(String[] args) throws Exception
	{
		new ServerManager();
	}

	private ServerManager() 
	throws SDKException, NumberFormatException, InterruptedException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService("", "InfoStore");

		boolean loop = true;
		while(loop)
		{
			IInfoObjects ios = infoStore.query("select * from ci_systemobjects where si_kind = 'server' order by si_name");

			for(int i=0;i<ios.size();i++)
			{
				IServer server = (IServer) ios.get(i);
				String runState = getStatusDesc(server.getStatusInfo().getStatus());
				String enabledDesc = getDisabledDesc(server.getCurrentDisabledState());
				System.out.printf("%2d. %14s, %8s - %s%n",i+1,runState,enabledDesc,server.getTitle());
			}
			System.out.print("Select server to manage or 0 to exit: ");

			String input = s.nextLine();

			if(input.trim().equals("0"))
				loop = false;
			else
				doServer((IServer) ios.get(Integer.parseInt(input)-1), infoStore);
		}
	}

	private void doServer(IServer server,IInfoStore infoStore) 
	throws SDKException, InterruptedException
	{
		switch(server.getStatusInfo().getStatus())
		{
			case IStatusInfo.Status.FAILED:
			case IStatusInfo.Status.STOPPED:
				System.out.println("S. Start");
				break;
			default:
				System.out.println("P. Stop");
				System.out.println("R. Restart");
				System.out.println("F. Force Stop");
				if(server.getCurrentDisabledState())
					System.out.println("E. Enable");
				else
					System.out.println("D. Disable");
		}
		System.out.println("0. Return");
		System.out.print("Select action: ");

		String input = s.nextLine();

		boolean expectedDisabled = server.getCurrentDisabledState();

		switch(input.trim().toUpperCase())
		{
			case "S":
				server.setExpectedRunState(ExpectedRunState.RUNNING);
				System.out.println("Attempting to start server.");
				break;
			case "P":
				server.setExpectedRunState(ExpectedRunState.STOPPED);
				System.out.println("Attempting to stop server.");
				break;
			case "F":
				server.setExpectedRunState(ExpectedRunState.STOPNOW);
				System.out.println("Attempting to force stop server.");
				break;
			case "R":
				server.setExpectedRunState(ExpectedRunState.RESTART);
				System.out.println("Attempting to restart server.");
				break;
			case "E":
				server.setDisabled(false);
				expectedDisabled = false;
				System.out.println("Attempting to enable server.");
				break;
			case "D":
				server.setDisabled(true);
				expectedDisabled = true;
				System.out.println("Attempting to disable server.");
				break;
			default:
				return; 
		}
		
		System.out.println("Setting server " + server.getTitle()
			+ " to " + server.getExpectedRunState()
			+ " / " + getDisabledDesc(expectedDisabled));
		server.save();
		
		boolean waiting = true;
		while(waiting)
		{
			Thread.sleep(3000);
			server = (IServer) infoStore.query("select * from ci_systemobjects where si_id = " + server.getID()).get(0);
			int status = server.getStatusInfo().getStatus();
			System.out.println("\nCurrent state: " + getStatusDesc(status) + " / " + getDisabledDesc(server.getCurrentDisabledState()));
			System.out.println("Expected:  " + server.getExpectedRunState() + " / " + getDisabledDesc(server.isDisabled()));
			if(status == IStatusInfo.Status.FAILED)
			{
				System.out.println("Server is failed.");
				waiting = false;
			}
			if(server.getExpectedRunState() == ExpectedRunState.RUNNING
				&& (status == IStatusInfo.Status.RUNNING_WITH_ERRORS || status == IStatusInfo.Status.RUNNING_WITH_WARNINGS))
				waiting = false;
			
				if(getStatusDesc(status)
.equals(server.getExpectedRunState().toString())
					&& server.getCurrentDisabledState() == server.isDisabled())
			{
				waiting = false;
				System.out.println("STATUS CHANGE COMPLETE\n");
			}
			else
				System.out.println("Server is not in expected state; waiting to check again.");
		}
	}
	
	private static String getDisabledDesc(boolean isDisabled)
	{
		return isDisabled ? "disabled" : "enabled";
	}

	private static String getStatusDesc(int status)
	{
		switch(status)
		{
			case IStatusInfo.Status.FAILED:
				return "Failed";
			case IStatusInfo.Status.INITIALIZING:
				return "Initializing";
			case IStatusInfo.Status.RUNNING:
				return "Running";
			case IStatusInfo.Status.RUNNING_WITH_ERRORS:
				return "Running WIth Errors";
			case IStatusInfo.Status.RUNNING_WITH_WARNINGS:
				return "Running WIth Warnings";
			case IStatusInfo.Status.STARTING:
				return "Starting";
			case IStatusInfo.Status.STOPPED:
				return "Stopped";
			case IStatusInfo.Status.STOPPING:
				return "Stopping";
		}
		return "Invalid";
	}
}
