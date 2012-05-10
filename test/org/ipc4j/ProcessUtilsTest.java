package org.ipc4j;

import static org.junit.Assert.*;

import org.ipc4j.ProcessUtils;
import org.junit.Test;

public class ProcessUtilsTest {

	@Test
	public void testGetProcessId() {
		int pid = ProcessUtils.getProcessId(-1);
		assertTrue(-1 != pid);
	}

	@Test
	public void testIsUNIXProcessAlive() {
		int pid = ProcessUtils.getProcessId(-1);
		assertTrue(-1 != pid);
		
		assertTrue(ProcessUtils.isUNIXProcessAlive(pid));
		assertFalse(ProcessUtils.isUNIXProcessAlive(Integer.MAX_VALUE));
		
	}

}
