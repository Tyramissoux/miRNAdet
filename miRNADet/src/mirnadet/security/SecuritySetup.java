package mirnadet.security;

import java.security.Permission;

public class SecuritySetup {
	
	//https://www.mulesoft.com/tcat/tomcat-securityv
	//https://blog.frankel.ch/java-security-manager/#gsc.tab=0
	
	public SecuritySetup(){
		
		
		
	}
	
	public void disableSystemExit() {
		SecurityManager securityManager = new StopExitSecurityManager();
		System.setSecurityManager(securityManager);
	}

	public void enableSystemExit() {
		SecurityManager mgr = System.getSecurityManager();
		if ((mgr != null && mgr instanceof StopExitSecurityManager)) {
			StopExitSecurityManager smgr = (StopExitSecurityManager) mgr;
			System.setSecurityManager(smgr.getPreviousMgr());
		} else
			System.setSecurityManager(null);
	}

	public class StopExitSecurityManager extends SecurityManager {
		private SecurityManager _prevMgr = System.getSecurityManager();

		// Wenn auskommentiert: access denied("java.lang.RuntimePermission"
		// "setIO")
		public void checkPermission(Permission perm) {

		}

		public void checkExit(int status) {
			super.checkExit(status);
			throw new IllegalStateException();
		}

		public SecurityManager getPreviousMgr() {
			return _prevMgr;
		}
	}
}
