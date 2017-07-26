package cs.com.sqlservice;

import com.locus.jedi.log.ErrorLogger;
import com.locus.jedi.service.sql.*;
import com.locus.jedi.biz.action.*;
import com.locus.jedi.biz.*;
import com.locus.jedi.waf.CommonDTO;

/**
 * @author PJW
 */
public class SQLServiceBizAction extends BizActionSupport
{
	public Object execute(CommonDTO common,Object arg) 
		throws BizAppException{
		JediTransaction tran = JediTransactionManager.getJediTransaction();
		Object result=null;
		try{
			
			int dsSelectedId = DataSourceSelection.getInstance().getDataSourceSelectedId();
			ErrorLogger.debug("@@@@@@@@@@@ dsSelectedId : " + dsSelectedId);
			
			if(arg instanceof SQLParam){
				SQLParam sqlparam = (SQLParam)arg;
				if( dsSelectedId > 0 ) {
					SQLServiceXmlDAO dao = SQLServiceXmlDAO.getInstance();
					SQLServiceSpec spec = dao.getSQLServiceSpec(sqlparam.getSqlName());
					ResourceSpec res = dao.getResourceSpec(spec.getDatasource());
					String nDatasource = res.getDatasource();
					
					sqlparam.setDatasource(nDatasource + "-backup" + dsSelectedId);
				}
				result =  SQLServiceManager.getInstance().execute(sqlparam,tran);
			}else if(arg instanceof SQLParam[]){
				SQLParam[] sqlparams = (SQLParam[])arg;
				if( dsSelectedId > 0 ) {
					for (int i = 0; i < sqlparams.length; i++) {
						SQLServiceXmlDAO dao = SQLServiceXmlDAO.getInstance();
						SQLServiceSpec spec = dao.getSQLServiceSpec(sqlparams[i].getSqlName());
						ResourceSpec res = dao.getResourceSpec(spec.getDatasource());
						String nDatasource = res.getDatasource();
						
						sqlparams[i].setDatasource(nDatasource + "-backup" + dsSelectedId);
					}
				}
				result =  SQLServiceManager.getInstance().execute(sqlparams,tran);
			}else{
				tran.rollback();
				throw new BizAppException("sqlservicewebaction","SQLServiceBizAction : arg type must be SQLParam or SQLParam[]");
			}
		}catch(SQLServiceException e){
			tran.rollback();
			throw new BizAppException(e.getCode(),e.getMessage(),e);
		}
		tran.commit();
		return result;
	}
};

