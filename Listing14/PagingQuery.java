package bosdkbook;

import java.util.Iterator;

import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.uri.IPageResult;
import com.crystaldecisions.sdk.uri.IStatelessPageInfo;
import com.crystaldecisions.sdk.uri.PagingQueryOptions;

public class PagingQuery 
{
	public static void main(String[] args) throws Exception
	{
		ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
		IEnterpriseSession session = sessionManager.logon ("administrator", "xxxx", "192.168.56.102", "secEnterprise");

		IInfoStore infoStore = (IInfoStore) session.getService ("","InfoStore");

		PagingQueryOptions pagingOptions = new PagingQueryOptions();

		String urlQuery = "path://InfoObjects/**";
		IPageResult pageResult = infoStore.getPagingQuery(urlQuery, pagingOptions);

		Iterator<String> itPageResult = pageResult.iterator();
		while(itPageResult.hasNext())
		{
			String pageQuery = itPageResult.next();
			System.out.println("Rendered URI query: " + pageQuery + "\n");

			IStatelessPageInfo page = infoStore.getStatelessPageInfo(pageQuery, pagingOptions);
			System.out.println("Generated SQL query: " + page.getPageSQL() + "\n");
			IInfoObjects ios = infoStore.query(page.getPageSQL());

			System.out.println("Results:");
			for(int x=0;x<Math.min(5, ios.size());x++)
				System.out.println(((IInfoObject)ios.get(x)).getTitle());

			System.out.println("Total: " + ios.size() + "\n");
		}
	}
}
