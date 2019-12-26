package bosdkbook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.CeScheduleType;
import com.crystaldecisions.sdk.occa.infostore.IDestination;
import com.crystaldecisions.sdk.occa.infostore.IDestinationPlugin;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.ISchedulingInfo;
import com.crystaldecisions.sdk.plugin.destination.diskunmanaged
	.IDiskUnmanaged;
import com.crystaldecisions.sdk.plugin.destination.diskunmanaged
	.IDiskUnmanagedOptions;
import com.crystaldecisions.sdk.plugin.destination.ftp.IFTP;
import com.crystaldecisions.sdk.plugin.destination.ftp.IFTPOptions;
import com.crystaldecisions.sdk.plugin.destination.managed.IManaged;
import com.crystaldecisions.sdk.plugin.destination.managed.IManagedOptions;
import com.crystaldecisions.sdk.plugin.destination.sftp.ISFTP;
import com.crystaldecisions.sdk.plugin.destination.smtp.ISMTP;
import com.crystaldecisions.sdk.plugin.destination.smtp.ISMTPOptions;
import com.crystaldecisions.sdk.plugin.destination.streamwork.IStreamWork;

public class RetrieveSched {

	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		// Retrieve a list of all calendar templates in the infostore
		IInfoObjects ioCalendars = infoStore.query("select * from ci_systemobjects where si_kind = 'calendar'");
		Map<Integer,String> calendarMap= new HashMap<Integer,String>();
		
		// Load the ioCalendars map with calendar templates, keyed by ID
		for(Object o : ioCalendars)
			calendarMap.put(((IInfoObject)o).getID(),
							((IInfoObject)o).getTitle());
		
		// Retrieve a list of all events in the infostore
		IInfoObjects ioEvents = infoStore.query("select * from ci_systemobjects where si_kind = 'event'");
		Map<Integer,String> eventMap= new HashMap<Integer,String>();
		
		// Load the ioEvents map with events, keyed by ID
		for(Object o : ioEvents)
			eventMap.put(((IInfoObject)o).getID(),
						((IInfoObject)o).getTitle());
		
		// Retrieve a list of all plugins in the infostore (we filter for only the Destination plugins below)
		IInfoObjects ioDesintationPlugins = infoStore.query("select * from ci_systemobjects where si_plugin_object = 1");
		Map<String,IDestinationPlugin> destinationPluginMap = new HashMap<String,IDestinationPlugin>();

		// Load the ioDesitnationPlugins map with IDestinationPlugin objects, keyed by plugin name
		for(Object o : ioDesintationPlugins)
			if(o instanceof IDestinationPlugin)
				destinationPluginMap.put(((IInfoObject)o).getTitle(),
							(IDestinationPlugin)o);
		
		// Retrieve a list of all users and user groups in the infostore
		IInfoObjects ioUsersGroups = infoStore.query("select si_id,si_name from ci_systemobjects where si_kind in ('user','usergroup')");
		Map<Integer,String> usersGroupsMap = new HashMap<Integer,String>();

		// Load the ioUsersGroups map, keyed by ID
		for(Object o : ioUsersGroups)
			usersGroupsMap.put(((IInfoObject)o).getID(),
						((IInfoObject)o).getTitle());

		// Retrieve a list of all instances in the infostore
		IInfoObjects infoObjects = infoStore.query("select * from ci_infoobjects where si_instance = 1");
		
		for(Object o : infoObjects)
		{
			IInfoObject io = (IInfoObject)o;
			ISchedulingInfo sched = io.getSchedulingInfo();
			
			System.out.println("Instance name: " + io.getTitle());
			System.out.println("Owner: " + io.getOwner());
			
			displayRecurrence(calendarMap, sched);
			
			displayStatus(io, sched);

			displayDestination(destinationPluginMap, usersGroupsMap, sched);
			
			System.out.println("Events to wait for:");
			for(Object oe : sched.getDependencies())
			{
				System.out.println("    " + eventMap.get(oe));
			}
			
			System.out.println("Events to fire on completion:");
			for(Object oe : sched.getDependants())
			{
				System.out.println("    " + eventMap.get(oe));
			}
			
			System.out.println("------------------------\n");
		}
	}

	// DIsplay the status of the instance.  If failed, include the error message
	private static void displayStatus(IInfoObject io, ISchedulingInfo sched) 
	throws SDKException 
	{
		switch(sched.getStatus())
		{
			case ISchedulingInfo.ScheduleStatus.COMPLETE:
				System.out.println("Status: Success");
				break;
			case ISchedulingInfo.ScheduleStatus.PAUSED:
				System.out.println("Status: Paused");
				break;
			case ISchedulingInfo.ScheduleStatus.PENDING:
				System.out.println("Status: Pending");
				break;
			case ISchedulingInfo.ScheduleStatus.RUNNING:
				System.out.println("Status: Running");
				break;
			case ISchedulingInfo.ScheduleStatus.EXPIRED:
				System.out.println("Status: Expired");
				break;
			case ISchedulingInfo.ScheduleStatus.Warning:
				System.out.println("Status: Warning");
				break;
			case ISchedulingInfo.ScheduleStatus.FAILURE:
				System.out.println("Status: Failure\n    " + sched.getErrorMessage());
				break;
			default:
				System.out.println("Unknown status value: " + sched.getStatus());
		}
	}

	// Display the recurrence settings for the instance 
	private static void displayRecurrence(Map<Integer, String> calendarMap,
		ISchedulingInfo sched) 
	{
		if(sched.isRightNow())
			System.out.println("Recurrence: Run Now");
		else
			switch (sched.getType())
			{
				case CeScheduleType.ONCE:
					System.out.println("Recurrence: Once");
					break;
				case CeScheduleType.DAILY:
					System.out.println("Recurrence: Every " + sched.getIntervalDays() + " days");
					break;
				case CeScheduleType.MONTHLY:
					System.out.println("Recurrence: Every " + sched.getIntervalMonths() + " months");
					break;
				case CeScheduleType.HOURLY:
					System.out.println("Recurrence: Every " + sched.getIntervalHours() + " hours and " + sched.getIntervalMinutes() + " minutes");
					break;
				case CeScheduleType.FIRST_MONDAY:
					System.out.println("Recurrence: First Monday of each month");
					break;
				case CeScheduleType.LAST_DAY:
					System.out.println("Recurrence: Last day of each month");
					break;
				case CeScheduleType.NTH_DAY:
					System.out.println("Recurrence: Day " + sched.getIntervalNthDay() + " of each month");
					break;
				case CeScheduleType.CALENDAR_TEMPLATE:
					System.out.println("Recurrence: Calendar template: " + calendarMap.get(sched.getCalendarTemplate()));
					break;
				case CeScheduleType.CALENDAR:
					System.out.println("Recurrence: Custom calendar");
					break;
				default:
					System.out.println("Recurrence: Unknown value");
			}
	}

	// Display the destination settings of the instance
	private static void displayDestination(Map<String, IDestinationPlugin>
			destinationPluginMap,
			Map<Integer, String> usersGroupsMap, ISchedulingInfo sched) 
	throws SDKException 
	{
		System.out.print("Destination: ");
		if(sched.getDestinations().isEmpty())
			System.out.println("Default Enterprise Location");
		else
		{
			IDestination dest = (IDestination) sched.getDestinations().get(0);
			String pluginName = dest.getName();
			
			IDestinationPlugin plugin = destinationPluginMap.get(pluginName);

			dest.copyToPlugin(plugin);

			if(pluginName.equals(IDiskUnmanaged.PROGID))
			{
				System.out.println("File system");
				if(dest.isSystemDefaultOptionsUsed())
				{
					System.out.println("   (job server defaults)");
				}
				else
				{
					IDiskUnmanagedOptions diskUnmanagedOptions = 
						(IDiskUnmanagedOptions)plugin.getScheduleOptions(); 
					String path = (String)
						diskUnmanagedOptions
						.getDestinationFiles()
						.get(0);
					String userName = diskUnmanagedOptions.getUserName();
					if(userName == null)
						userName = "(job server default)";
					String password = diskUnmanagedOptions.isPasswordSet()
							? "xxxx"
							: "(job server default";
					System.out.println("    Path: " + path);
					System.out.println("    User: " + userName);
					System.out.println("    Password: " + password);
				}
			}
			else if(pluginName.equals(IFTP.PROGID ))
			{
				System.out.println("FTP");
				if(dest.isSystemDefaultOptionsUsed())
				{
					System.out.println("   (job server defaults)");
				}
				else
				{					
					IFTPOptions ftpOptions = 
						(IFTPOptions) plugin.getScheduleOptions();
					String host = ftpOptions.getServerName();
					String path = (String)
						ftpOptions.getDestinationFiles().get(0);
					String userName = ftpOptions.getUserName();
					if(userName == null)
						userName = "(job server default)";
					String password = ftpOptions.isPasswordSet()
							? "xxxx"
							: "(job server default";
					System.out.println("    Host: " + host);
					System.out.println("    Path: " + path);
					System.out.println("    User: " + userName);
					System.out.println("    Password: " + password);
				}
			}
			else if(pluginName.equals(IManaged.PROGID ))
			{
				System.out.println("BI Inbox");
				if(dest.isSystemDefaultOptionsUsed())
				{
					System.out.println("   (job server defaults)");
				}
				else
				{					
					IManagedOptions managedOptions = 
						(IManagedOptions) plugin.getScheduleOptions();
					for(Iterator<Integer> i =
						managedOptions.getDestinations().iterator();
						i.hasNext();)
					{
						System.out.println("   " +
							usersGroupsMap.get(i.next()));
					}
				}
			}
			else if(pluginName.equals(ISMTP.PROGID ))
			{
				System.out.println("Email");
				if(dest.isSystemDefaultOptionsUsed())
				{
					System.out.println("   (job server defaults)");
				}
				else
				{					
					ISMTPOptions smtpOptions =
							(ISMTPOptions) plugin.getScheduleOptions();
					String toAddresses = formatAddresses(
							smtpOptions.isToAddressesSet(),
							smtpOptions.getToAddresses());
					String ccAddresses = formatAddresses(
							smtpOptions.isCCAddressesSet(),
							smtpOptions.getCCAddresses());
					String bccAddresses = formatAddresses(
							smtpOptions.isBCCAddressesSet(),
							smtpOptions.getBCCAddresses());
					System.out.println("Server: " +
							smtpOptions.getServerName());
					System.out.println("Sender: " +
							smtpOptions.getSenderAddress());
					System.out.println("To: " + toAddresses);
					System.out.println("Cc: " + ccAddresses);
					System.out.println("Bcc: " + bccAddresses);
					System.out.println("Subject: " + smtpOptions.getSubject());
					System.out.println("Message: " + smtpOptions.getMessage());
				}
			}
			else if(pluginName.equals(ISFTP.PROGID ))
				System.out.println("SFTP");
			else if(pluginName.equals(IStreamWork.PROGID ))
				System.out.println("SAP Streamwork");
			else
				System.out.println("Unknown destination type");
		}
	}


	// Format a list of email addresses into a semicolon-delimited String
	private static String formatAddresses(boolean isSet, 
		List<String> addresses) 
	{
		if(!isSet)
			return "(job server default)";
		StringBuilder sb = new StringBuilder();
		for(String address : addresses)
		{
			if(sb.length()!=0)
				sb.append(";");
			sb.append(address);
		}
		return sb.toString();
	}
}	
