package edu.snu.cms.remon.collector.test;

import static org.junit.Assert.*;

import org.apache.reef.task.TaskMessage;
import org.apache.reef.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Collector;

/*
 * Unit test code for test Collector.heartbeatHandler class
 */
public class TestheartbeatHandler {
	Collector.heartbeatHandler hbHandler;
	@Before
	public void setUp() throws Exception {
		hbHandler = new Collector.heartbeatHandler();
		for (int i = 0; i < 30; i++) {
			Collector.putData("TestTag" + i, 0.5 +i);
		}
	}

	@Test
	public void testGetMessage() { // test for Collector.heartbeatHandler.getMessage()
		Optional<TaskMessage> op = hbHandler.getMessage();
		assertArrayEquals(op.get().get(), new Codec().encode(Collector.values));
	}

}
