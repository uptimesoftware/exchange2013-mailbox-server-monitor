package com.uptimesoftware.uptime.plugin;

import static org.junit.Assert.*;

import java.util.HashMap;
import org.apache.commons.lang.SystemUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uptimesoftware.uptime.plugin.Exchange2013MailboxServerMonitor.UptimeExchange2013MailboxServerMonitor;

public class Exchange2013MailboxServerMonitorTest {

	// store inputs into this HashMap before testing begins.
	private static HashMap<String, Object> inputs = new HashMap<String, Object>();

	// Test instance.
	private UptimeExchange2013MailboxServerMonitor testInstance = new UptimeExchange2013MailboxServerMonitor();

	@BeforeClass
	public static void setUpOnce() {
		inputs.put(UptimeExchange2013MailboxServerMonitor.HOSTNAME, null);
		inputs.put(UptimeExchange2013MailboxServerMonitor.PORT, 9998);
		inputs.put(UptimeExchange2013MailboxServerMonitor.AGENT_PASSWORD, "uptime");
		inputs.put(UptimeExchange2013MailboxServerMonitor.DOMAIN_NAME, null);
		inputs.put(UptimeExchange2013MailboxServerMonitor.USERNAME, null);
		inputs.put(UptimeExchange2013MailboxServerMonitor.PASSWORD, null);

	}

	@Test
	public void checkIfAgentIsRunningOnWindowsTest() {
		if (SystemUtils.IS_OS_LINUX) {
			assertTrue(testInstance.checkIfAgentIsRunningOnWindows(inputs,
					UptimeExchange2013MailboxServerMonitor.AGENT_RESPONSE_ERR));
		}
	}

	@Test
	public void checkRunAgentCustomScriptTest() {
		String result = "";
		if (SystemUtils.IS_OS_LINUX) {
			result = testInstance.runAgentCustomScript(inputs,
					UptimeExchange2013MailboxServerMonitor.AGENT_CUSTOM_SCRIPT_CMD);
		} else if (SystemUtils.IS_OS_WINDOWS) {
			result = testInstance.executePowershellScriptAgainstRemoteHost(inputs);
		}

		System.out.println(result);
		assertFalse(result.isEmpty());

		JsonNode jsonNode = testInstance.convertStringToJsonNode(result);
		assertNotNull(jsonNode);

		int outputValue = 0;
		String outputParam = "";
		while (testInstance.outputList.size() != 0) {
			outputParam = testInstance.outputList.pop();
			outputValue = testInstance.getIntValueFromJsonNode(outputParam, jsonNode);
			if (outputValue == UptimeExchange2013MailboxServerMonitor.ERROR_CODE) {
				fail("failed to get int value of specific filed.");
			} else {
				System.out.println(outputParam + " : " + outputValue);
			}
		}
	}
}
