package bosdkbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBaseEx;
import com.crystaldecisions.sdk.plugin.desktop.user.IUser;
import com.crystaldecisions.sdk.properties.IProperty;

public class SyncUsers implements IProgramBaseEx  
{
	private IInfoStore infoStore;
	private final static Logger LOGGER = Logger.getLogger(SyncUsers.class);
	private final static String syncedUserGroupName="Synced Users";
	private final static String initialPassword="P2$$w0rD";
	private final static String createdUserLogFileName = "output/createdusers.txt";
	private final static String deletedUserLogFileName = "output/deletedusers.txt";
	private final static String inputFileName = "input/users.txt";

	@Override
	public void run(IEnterpriseSession paramIEnterpriseSession,
			IInfoStore paramIInfoStore, IInfoObject paramIInfoObject,
			String objectID, String[] paramArrayOfString)
			throws SDKException 
	{
		this.infoStore = paramIInfoStore;

		if(paramIInfoObject!=null)
		{
			paramIInfoObject.getProcessingInfo().properties().add(
					"SI_PROGRAM_CAN_FAIL_JOB", Boolean.TRUE,
					IProperty.DIRTY);
			paramIInfoObject.save();
		}
		
		LOGGER.info("Starting " + this.getClass().getSimpleName() + " version 1.0");
		try
		{
			doSync();
		}
		catch (Exception e)
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
	
	private void doSync() throws Exception
	{
		// inputUsers will contain the list of user IDs from the input file.
		// The map key is the user ID in lower case; the value is the original ID in mixed case.
		Map<String,String> inputUsers = new HashMap<String,String>();

		// Read the input file into the inputUsers map
		BufferedReader is = null;

		try
		{
			is = new BufferedReader(new FileReader(inputFileName));
			
			String inputUserLine;
			while ((inputUserLine = is.readLine()) != null) 
			{
				if(!inputUserLine.trim().equals(""))
					inputUsers.put(inputUserLine.toLowerCase(),inputUserLine);
			}
		}
		finally
		{
			if(is != null)
				is.close();
		}
		
		if(inputUsers.size()==0)
		{
			LOGGER.info("No records found in input file.  Exiting.");
			return;
		}
		
		LOGGER.info("Read " + inputUsers.size() + " records from input file.");
		
		// Get the "Synced Users" user group as an InfoObject, store in syncUserGroup
		IInfoObjects syncUserGroups = infoStore.query("select si_id from ci_systemobjects where si_kind = 'usergroup' and si_name = '" + syncedUserGroupName + "'");
		if(syncUserGroups.size()==0)
		{
			throw new Exception("Unable to locate the Synced Users group!");
		}
		
		IInfoObject syncUserGroup = (IInfoObject)syncUserGroups.get(0);

		// Any new user IDs that we create in the CMS will be placed in the newUsers IInfoObjects collection
		IInfoObjects newUsers = infoStore.newInfoObjectCollection();
		
		PrintWriter createdUserLogWriter = null;
		PrintWriter deletedUserLogWriter = null;
		
		try
		{
			createdUserLogWriter = 
              new PrintWriter(
                  new BufferedWriter(
                      new FileWriter(createdUserLogFileName,true)));
			deletedUserLogWriter = 
              new PrintWriter(
                  new BufferedWriter(
                      new FileWriter(deletedUserLogFileName,true)));
	
			for(String inputUser : inputUsers.values())
			{
				// Check if there's an existing user in the CMS for this ID from the input file
				IInfoObjects cmsUsers = infoStore.query("select si_id,si_name,si_usergroups from ci_systemobjects where si_kind = 'user' and si_name = '" + inputUser + "'");
				if(cmsUsers.size() == 0) // User ID does not exist in CMS
				{
					LOGGER.info("Creating new user: " + inputUser);
					createdUserLogWriter.println(inputUser);
					IUser newUser = (IUser) newUsers.add(IUser.KIND);
					newUser.getGroups().add(syncUserGroup.getID());
					newUser.setTitle(inputUser);
					newUser.setNewPassword(initialPassword);
					newUser.save();
				}
				else
				{ // User ID exists in CMS; check if it's in the Synced Users group
					IUser cmsUser = (IUser)cmsUsers.get(0);
					if(!cmsUser.getGroups().contains(syncUserGroup.getID()))
					{ // ID is not in Synced Users group, add it
						LOGGER.info("Adding existing user: " + inputUser + " to Synced Users group.");
						cmsUser.getGroups().add(syncUserGroup.getID());
						cmsUser.save();
					}
					// else ID exists in CMS and is in Synced Users group - no action
				}
			}
	
			// Get a list of all user IDs in Synced User group, so we can check if they are in input file
			IInfoObjects existingUsers = infoStore.query(
				" select si_id,si_names,si_usergroups " +
				" from ci_systemobjects " +
				" where children(\"si_name='usergroup-user'\"," +
				"                \"si_name='" + syncedUserGroupName + "'\")");
			
			LOGGER.info("Read " + existingUsers.size() + " existing users from CMS.");
			
			// Go through each CMS user; check if in input file
			for(Object o : existingUsers)
			{
				IUser existingUser = (IUser)o;
				if(!inputUsers.keySet()
						.contains(existingUser.getTitle().toLowerCase()))
				{ // CMS user not in input file.  If this user is only in two groups (Synced Users and Everyone), then delete the user ID 
					deletedUserLogWriter.println(existingUser.getTitle());
					if(existingUser.getGroups().size() == 2)
					{
						LOGGER.info("User: " +existingUser.getTitle() + " not found in input file.  Deleting from CMS.");
						existingUser.deleteNow();
					}
					else // User is in additional groups. Don't delete, but just remove user from Synced Users
					{
						LOGGER.info("User: " + existingUser.getTitle() + " not found in input file.  Removing from Synced Users group.");
						existingUser.getGroups().remove(syncUserGroup.getID());
						existingUser.save();
					}
				}
			}
		} 
		finally
		{
			if(createdUserLogWriter!=null)
				createdUserLogWriter.close();
			if(deletedUserLogWriter!=null)
				deletedUserLogWriter.close();
		}
		LOGGER.info("Done!");
	}
}
