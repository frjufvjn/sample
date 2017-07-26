package cs.com.rm.player;

import java.io.File;

import com.locus.jedi.log.ErrorLogger;
import com.locus.jedi.util.EnvironmentUtil;
import com.locus.jedi.waf.action.WebActionException;
import com.locus.jedi.waf.controller.JediRequest;
import com.locus.jedi.waf.controller.JediResponse;

import jedix.xwing.action.XwingWebAction;

/**
 * @author PJW
 *
 */
public class PlayerFileService extends XwingWebAction {

	private static final long serialVersionUID = 4513643239880185110L;
	private static String targetPath;
	
	public PlayerFileService() {
		targetPath = EnvironmentUtil.getProperty("rm_player_target_path");
	}
	
	@Override
	public void perform(JediRequest request, JediResponse response)
			throws WebActionException 
	{
		// super.perform(request, response);
		try {
			String resultCode = "0";
			String encFilePath = (String) request.param.getValue("p", "");
			
			String[] splitWord = encFilePath.split("\\\\");
			int extractLength = splitWord.length -1;
			String _fileName = splitWord[extractLength];
			
			String fileName = _fileName.replace(".Vox", ".wav");
			String decFilePath = targetPath + fileName;
			
			ErrorLogger.debug("####################################################");
			ErrorLogger.debug("### encFilePath : " + encFilePath);
			ErrorLogger.debug("### decFilePath : " + decFilePath);
			ErrorLogger.debug("####################################################");
			
			File encFile = new File(encFilePath);
			File decFile = new File(decFilePath);
			
			CryptoUtils.getInstance().decrypt("", encFile, decFile);
			
			response.setResultCode(resultCode);
			response.param.addValue("res", fileName);
			
		} catch (CryptoException ce) {
			ErrorLogger.error(ce);
			response.setError(true);
			response.setResultCode("Z1");
		} catch (Exception e) {
			ErrorLogger.error(e);
			response.setError(true);
			response.setResultCode("Z2");
		}
	}

}
