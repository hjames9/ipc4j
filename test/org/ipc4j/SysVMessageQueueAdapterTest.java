package org.ipc4j;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ipc4j.sysv.messagequeue.SysVMessageQueueAdapter;
import org.ipc4j.sysv.messagequeue.SysVMessageQueueException;
import org.junit.Assert;
import org.junit.Test;

public class SysVMessageQueueAdapterTest {

	@Test
	public void testKey() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		assertEquals(adapter.getKey(), 0xDEAD);
		
		adapter.setKey(0xBEEF);
		assertEquals(adapter.getKey(), 0xBEEF);
	}

	@Test
	public void testExists() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		
		try {
			if(!adapter.exists()) {
					adapter.create(0644);
			}

			assertTrue(adapter.exists());
			adapter.destroy();
			assertFalse(adapter.exists());
		}
		catch (SysVMessageQueueException e) {
				fail(e.getMessage());
		}
	}

	@Test
	public void testGetId() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);

		try {
			if(!adapter.exists()) {
				adapter.create(0644);
			}
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreate() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEADBEEF);
				
		try {
			if(adapter.exists()) {
				adapter.destroy();
			}
			adapter.create(0644);
			assertTrue(adapter.exists());
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		}
		
		try {
			adapter.create(0644);
			fail("Exception should be thrown");
		} catch (SysVMessageQueueException e) {
		}
		
		try {
			adapter.destroy();
			assertFalse(adapter.exists());
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testDestroy() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEADBEEF);

		if(adapter.exists()) {
			try {
				adapter.destroy();
			} catch (SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
		
		try {
			adapter.create(0644);
			assertTrue(adapter.exists());
			adapter.destroy();
			assertFalse(adapter.exists());
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSend() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEADBEEF);
		if(adapter.exists()) {
			try {
				adapter.destroy();
			} catch (SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
		
		try {
			adapter.create(0644);
			assertTrue(adapter.exists());
			
			byte [] data = new byte[] {(byte)0xCA, (byte)0xFE};
			adapter.send(1, data, false);
			
			byte [] data2 = adapter.receive(1, false);
			assertArrayEquals(new byte[] {(byte)0xCA, (byte)0xFE}, data2);
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} finally {
			try {
				adapter.destroy();
			} catch (SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testReceive() {
		final SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEADBEEF);
		
		try {
			if(!adapter.exists()) {
				adapter.create(0644);
			}
			
			ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
			executor.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						adapter.send(1, new byte[]{}, false);
						//adapter.interrupt();
					} catch (Exception e) {
						fail(e.getMessage());
					}
				}
			}, 1, TimeUnit.SECONDS);

			//Blocking
			adapter.receive(1, true);

			//Non-blocking
			byte [] empty = adapter.receive(1, false);
			assertArrayEquals(empty, new byte[]{});
		} catch(SysVMessageQueueException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} finally {
			try {
				adapter.destroy();
			} catch(SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testPermission() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		
		if(!adapter.exists()) {
			try {
				adapter.create(0644);
				Assert.assertEquals(0644, adapter.getPermission());
			} catch (SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
		
		try {
			adapter.setPermission(0655);
			assertEquals(0655, adapter.getPermission());
		} catch (SysVMessageQueueException e) {
		} finally {
			try {
				adapter.destroy();
			} catch(SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}

	@Test
	public void testSendReceiveTimes() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		try {
			if(!adapter.exists()) {
				adapter.create(0644);
			}
			
			byte [] data = new byte[] {(byte)0xCA, (byte)0xFE};
			adapter.send(1, data, false);
			
			byte [] data2 = adapter.receive(1, false);
			assertArrayEquals(new byte[] {(byte)0xCA, (byte)0xFE}, data2);
			
			Date sendTime = adapter.getLastSendTime();
			Date receiveTime = adapter.getLastReceiveTime();
			Date changeTime = adapter.getLastChangeTime();
			
			assertNotNull(sendTime);
			assertNotNull(receiveTime);
			assertNotNull(changeTime);
		} catch(SysVMessageQueueException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} finally {
			try {
				adapter.destroy();
			} catch(SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testSendReceivePids() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		try {
			if(!adapter.exists()) {
				adapter.create(0644);
			}
			
			byte [] data = new byte[] {(byte)0xCA, (byte)0xFE};
			adapter.send(1, data, false);
			
			byte [] data2 = adapter.receive(1, false);
			assertArrayEquals(new byte[] {(byte)0xCA, (byte)0xFE}, data2);
			
			int senderPid = adapter.getLastSendProcessId();
			int receiverPid = adapter.getLastReceiveProcessId();
			
			assertEquals(senderPid, receiverPid);
		} catch(SysVMessageQueueException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} finally {
			try {
				adapter.destroy();
			} catch(SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testNumOfMessagesInQueue() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		try {
			if(adapter.exists()) {
				adapter.destroy();
			}
			
			adapter.create(0644);
			
			assertEquals(0, adapter.getNumberOfMessagesInQueue());
			
			byte [] data = new byte[] {(byte)0xCA, (byte)0xFE};
			adapter.send(1, data, false);
			adapter.send(1, data, false);
			
			assertEquals(2, adapter.getNumberOfMessagesInQueue());
			
			byte [] data2 = adapter.receive(1, false);
			assertArrayEquals(new byte[] {(byte)0xCA, (byte)0xFE}, data2);

			assertEquals(1, adapter.getNumberOfMessagesInQueue());
		} catch(SysVMessageQueueException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} finally {
			try {
				adapter.destroy();
			} catch(SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testMap() {
		Map<SysVMessageQueueAdapter, Integer> adapters = new HashMap<SysVMessageQueueAdapter, Integer>();
		
		SysVMessageQueueAdapter adapter1 = new SysVMessageQueueAdapter(0xDEAD);
		SysVMessageQueueAdapter adapter2 = new SysVMessageQueueAdapter(0xDEAD);
		
		try {
			if(!adapter1.exists()) {
				adapter1.create(0644);
			}
			
			if(!adapter2.exists()) {
				adapter2.create(0644);
			}
			
			assertEquals(adapter1, adapter2);
			assertEquals(0, adapters.size());

			adapters.put(adapter1, 1);
			assertEquals(1, adapters.size());
			adapters.put(adapter2, 1);
			assertEquals(1, adapters.size());
			adapter2.setKey(0xBEEF);
			if(!adapter2.exists()) {
				adapter2.create(0644);
			}
			adapters.put(adapter2, 1);
			assertEquals(2, adapters.size());
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		} finally {
			try {
				if(adapter1.exists()) {
					adapter1.destroy();
				}
				
				if(adapter2.exists()) {
					adapter2.destroy();
				}
			} catch (SysVMessageQueueException e) {
				fail(e.getMessage());
			} 
		}
	}
	
	@Test
	public void testMap2() {
		Map<SysVMessageQueueAdapter, Integer> adapters = new HashMap<SysVMessageQueueAdapter, Integer>();
		
		SysVMessageQueueAdapter adapter1 = new SysVMessageQueueAdapter(0xDEAD);
		SysVMessageQueueAdapter adapter2 = new SysVMessageQueueAdapter(0xDEAD);
		
		try {
			if(!adapter1.exists()) {
				adapter1.create(0644);
			}
			
			if(!adapter2.exists()) {
				adapter2.create(0644);
			}
			
			adapters.put(adapter1, 1);
			adapters.put(adapter2, 1);
			assertEquals(1, adapters.size());
			
			adapters.remove(adapter1);
			assertEquals(0, adapters.size());
			
			adapters.put(adapter1, 1);
			adapters.put(adapter2, 1);
			assertEquals(1, adapters.size());
			
			adapter1.destroy();
			adapters.remove(adapter1);
			assertEquals(0, adapters.size());
		} catch (SysVMessageQueueException e) {
			fail(e.getMessage());
		} finally {
			try {
				if(adapter1.exists()) {
					adapter1.destroy();
				}
				
				if(adapter2.exists()) {
					adapter2.destroy();
				}
			} catch (SysVMessageQueueException e) {
				fail(e.getMessage());
			} 
		}
		
	}
	
	@Test
	public void testClear() {
		SysVMessageQueueAdapter adapter = new SysVMessageQueueAdapter(0xDEAD);
		try {
			if(adapter.exists()) {
				adapter.destroy();
			}
			
			adapter.create(0644);
			
			assertEquals(0, adapter.getNumberOfMessagesInQueue());
			
			byte [] data = new byte[] {(byte)0xCA, (byte)0xFE};
			adapter.send(1, data, false);
			adapter.send(1, data, false);
			
			assertEquals(2, adapter.getNumberOfMessagesInQueue());
			adapter.clear();
			assertEquals(0, adapter.getNumberOfMessagesInQueue());			
		} catch(SysVMessageQueueException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		} finally {
			try {
				adapter.destroy();
			} catch(SysVMessageQueueException e) {
				fail(e.getMessage());
			}
		}
	}
}
