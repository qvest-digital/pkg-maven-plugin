package de.tarent.maven.plugins.pkg;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UtilsTest  extends TestCase{
	@Test
	public void testGetTargetConfigurationFromString(){
		
		List<TargetConfiguration> l = new ArrayList<TargetConfiguration>();
		TargetConfiguration t1 = new TargetConfiguration();
		TargetConfiguration t2 = new TargetConfiguration();
		TargetConfiguration t3 = new TargetConfiguration();
		TargetConfiguration t4 = new TargetConfiguration();
		l.add(t1);
		l.add(t2);
		l.add(t3);
		l.add(t4);
		
		t1.target = "unwantedConfig";
		t2.target = "unwantedConfig";
		t4.target = "unwantedConfig";	
		
		t3.target = "wantedConfig";
		t3.chosenDistro = "wantedDistro";
		
		Assert.assertEquals("wantedDistro",Utils.getTargetConfigurationFromString("wantedConfig", l).chosenDistro);
		
		
	}

}
