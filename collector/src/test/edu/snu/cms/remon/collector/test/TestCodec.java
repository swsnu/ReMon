package edu.snu.cms.remon.collector.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.snu.cms.remon.collector.Metric;
import edu.snu.cms.remon.collector.Codec;
import edu.snu.cms.remon.collector.Collector;

/*
 * Unit test code for test Codec class
 */
public class TestCodec {
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEncodeDecode() { // test for Codec.encode() and Codec.decode()
		byte[] barray;
		List<Metric> values;
		
		values = new ArrayList<>();
		for(int i = 0; i < 20; i++) {
			values.add(new Metric("Test"+i, 0.5+1));
		}
		barray = new Codec().encode(values);
		List<Metric> decodedvalues = new Codec().decode(barray);
		assertEquals(decodedvalues, values);
	}

}
