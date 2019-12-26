package bosdkbook;

import org.apache.log4j.Logger;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.program.IProgramBaseEx;

public class PurgeRecycleBinPO implements IProgramBaseEx  
{
	private static Logger LOGGER = Logger.getLogger(PurgeRecycleBinPO.class);
	private IEnterpriseSession session;
	private IInfoStore infoStore;
	
	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");
		
		new PurgeRecycleBinPO().run(session, infoStore, null, null, args);
	}

	@Override
	public void run(IEnterpriseSession paramIEnterpriseSession,
			IInfoStore paramIInfoStore, IInfoObject paramIInfoObject,
			String objectID, String[] paramArrayOfString)
			throws SDKException 
	{
		this.session = paramIEnterpriseSession;
		this.infoStore = paramIInfoStore;

		LOGGER.info("Starting.");
		try
		{
			doPurge();
			LOGGER.info("Done!");
		}
		catch (SDKException e)
		{
			LOGGER.fatal("Unexpected error, aborting.",e);
		}
	}

	private void doPurge() throws SDKException
	{
		IInfoObjects infoObjects = infoStore.query("select si_name, si_cuid from ci_infoobjects where children(\"si_name='folder hierarchy'\",\"si_name = 'recycle bin' and si_parentid = 23\")");
		
		LOGGER.info("Received " + infoObjects.size() + " objects to delete.");
		for(Object o : infoObjects)
		{
			IInfoObject infoObject = (IInfoObject)o;
			LOGGER.debug("Deleting: " + infoObject.getTitle());
			try
			{
				infoObject.deleteNow();
			}
			catch (SDKException e)
			{
				LOGGER.error("Error deleting object: " + e.getMessage());
			}
		}
	}
}
