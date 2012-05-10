package org.ipc4j;

import java.io.File;
import java.lang.management.ManagementFactory;

public class ProcessUtils {
	private static String UNIX_PROCESS_DIR = "/proc";
	
	public static int getProcessId(int fallback) {
	    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
	    final int index = jvmName.indexOf('@');

	    if (index < 1) {
	        // part before '@' empty (index = 0) / '@' not found (index = -1)
	        return fallback;
	    }

	    return Integer.parseInt(jvmName.substring(0, index));
	}
	
	public static boolean isUNIXProcessAlive(int pid) {
		File proc = new File(String.format("%s//%d", UNIX_PROCESS_DIR , pid));
		return proc.exists();
	}
}
