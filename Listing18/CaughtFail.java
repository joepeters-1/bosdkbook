package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBaseEx;
import com.crystaldecisions.sdk.properties.IProperty;

public class CaughtFail implements IProgramBaseEx {

	@Override
	public void run(IEnterpriseSession paramIEnterpriseSession,
			IInfoStore paramIInfoStore, IInfoObject paramIInfoObject,
			String objectID, String[] paramArrayOfString)
			throws SDKException {

		paramIInfoObject.getProcessingInfo().properties().add(
				"SI_PROGRAM_CAN_FAIL_JOB", Boolean.TRUE, IProperty.DIRTY);
		paramIInfoObject.save();
	
		try
		{
			IInfoObjects ios = paramIInfoStore.query("xxx");
		}
		catch (SDKException e)
		{
			// Print the code to signal the failure
			System.out.println("PROCPROGRAM:PROGRAM_ERROR");
			System.out.println("62009");
			
			// Print the error message
			System.out.println(e.getMessage());
			
			// exit the program
			System.exit(1);
		}
	}
}
