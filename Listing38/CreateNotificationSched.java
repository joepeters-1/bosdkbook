package bosdkbook;

import com.businessobjects.sdk.plugin.desktop.webi.IWebi;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IDestination;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.INotifications;
import com.crystaldecisions.sdk.occa.infostore.ISchedulingInfo;
import com.crystaldecisions.sdk.plugin.destination.smtp.ISMTP;
import com.crystaldecisions.sdk.plugin.destination.smtp.ISMTPOptions;

public class CreateNotificationSched {

	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		ISMTP smtpPlugin = (ISMTP) infoStore.query("select * from ci_systemobjects where si_plugin_object = 1 and si_kind = '" + ISMTP.KIND + "'").get(0);
		ISMTPOptions smtpOptions = (ISMTPOptions) smtpPlugin.getScheduleOptions();

		IInfoObjects infoObjects = infoStore.query("select * from ci_infoobjects where si_name = 'Schedule Test' and si_kind = 'webi' and si_instance = 0");
		
		if(infoObjects.size() > 0)
		{
			IWebi webi = (IWebi) infoObjects.get(0);
			ISchedulingInfo sched = webi.getSchedulingInfo();

			// Retrieve the Notifications collection
			INotifications notif = sched.getNotifications();

			// Set the email options for a success notification
			smtpOptions.getToAddresses()
				.add("%SI_USERFULLNAME% <%SI_EMAIL_ADDRESS%>");
			smtpOptions.setSenderAddress("bo_admin@company.com");
			smtpOptions.setSubject("%SI_NAME% report completed successfully");
			smtpOptions.setMessage("Hello, %SI_USERFULLNAME%.\r\nThe %SI_NAME% report completed at %SI_STARTTIME%.");

			// Create the success notification
			IDestination successDest =
						notif.getDestinationsOnSuccess()
						.add(ISMTP.PROGID);

			// Apply the SMTP options to the success notification
			successDest.setFromPlugin(smtpPlugin);

			// We re-use smtpOptions for the failure notification --
			// we will just change the subject line.
			smtpOptions.setSubject("%SI_NAME% report failed");

			// Create the failure notification
			IDestination failDest =
					notif.getDestinationsOnFailure()
					.add(ISMTP.PROGID);

			// Apply the SMTP options to the failure notification
			failDest.setFromPlugin(smtpPlugin);

			// Schedule the document
			webi.schedule();
		}
		else
			System.out.println("Could not find document!");
		System.out.println("Done!");
	}
}
