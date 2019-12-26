package bosdkbook;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.common.IReportFormatOptions;
import com.crystaldecisions.sdk.plugin.desktop.common.IReportFormatOptions.CeReportFormat;
import com.crystaldecisions.sdk.plugin.desktop.report.IReport;

public class CrystalToExcel {

	public static void main(String[] args) throws SDKException
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");
		
		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		IInfoObjects infoObjects = infoStore.query("select * from ci_infoobjects where si_name = 'crystal schedule test' and si_kind = 'crystalreport' and si_instance = 0");
		
		if(infoObjects.size() > 0)
		{
			IReport report = (IReport) infoObjects.get(0);
			IReportFormatOptions formatOptions =
						report.getReportFormatOptions();
			formatOptions.setFormat(
						CeReportFormat.TEXT_CHARACTER_SEPARATED);

			report.schedule();
		}
		else
			System.out.println("Could not find document!");
		System.out.println("Done!");
	}
}
