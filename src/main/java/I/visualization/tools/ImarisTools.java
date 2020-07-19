package I.visualization.tools;

import com.bitplane.xt.BPImarisLib;

import I.plugin.LogIn;

public class ImarisTools {
	public static ImarisServer.IServerPrx GetServer(BPImarisLib vImarisLib){
		ImarisServer.IServerPrx vServer = vImarisLib.GetServer();
		return vServer;
	}
	
	public static int GetObjectId(BPImarisLib vImarisLib) {
		try {
			ImarisServer.IServerPrx vServer = GetServer(vImarisLib);
			int vNumberOfObjects = vServer.GetNumberOfObjects();
			 for (int vIndex = 0; vIndex < vNumberOfObjects; vIndex++) {
				    int vObjectId = vServer.GetObjectID(vIndex);
				    return vObjectId; // work with the ID (return first one)
				  }
		} catch (Exception e) {
			LogIn.error("please reload image to imaris");
		}
		return -1; // invalid id
		
	}
	
}
