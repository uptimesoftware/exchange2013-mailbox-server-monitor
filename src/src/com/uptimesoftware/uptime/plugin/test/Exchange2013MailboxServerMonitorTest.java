package com.uptimesoftware.uptime.plugin.test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang.SystemUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uptimesoftware.uptime.plugin.Exchange2013MailboxServerMonitor.UptimeExchange2013MailboxServerMonitor;

public class Exchange2013MailboxServerMonitorTest {

	// Configure these private variables with your inputs. Default is 'null' not empty string "".
	private static String hostname;
	private static String domainName = ".\\";
	private static String userName = "administrator";
	// When you don't need password make it null, not empty string "".
	private static int port = 9998;
	// On Windows, the password is for remote Powershell script. On Linux, the password is for Up.time Agent.
	private static String password;

	// constants
	private static final String HOSTNAME = "hostname";
	private static final String DOMAIN_NAME = "domainName";
	private static final String USERNAME = "userName";
	private static final String PORT = "port";
	private static final String PASSWORD = "password";
	private static final String VERSION = "ver";
	private static final String SCRIPT_COMMAND_NAME = "ex2013mailbox";

	// store params into this HashMap before testing begins.
	private static HashMap<String, Object> params = new HashMap<String, Object>();
	private final int ERROR_CODE = -1;

	private final LinkedList<String> outputList = new LinkedList<String>();

	@BeforeClass
	public static void setUpOnce() {
		params.put(HOSTNAME, hostname);
		params.put(DOMAIN_NAME, domainName);
		params.put(USERNAME, userName);
		params.put(PORT, port);
		params.put(PASSWORD, password);
	}

	@Test
	public void checkIfAgentIsRunningOnWindowsTest() {
		assertTrue(invokeCheckIfAgentIsRunningOnWindows(params, VERSION));
	}

	@Test
	public void checkRunAgentCustomScriptTest() {
		outputList.add("MSExchange Assistants - Per Database - Events In Queue");
		outputList.add("MSExchange Delivery SmtpAvailability - % Availability");
		outputList.add("MSExchange Delivery SmtpAvailability - % Failures Due To Back Pressure");
		outputList.add("MSExchange Delivery SmtpAvailability - % Failures Due To TLS Errors");
		outputList.add("MSExchange Delivery SmtpAvailability - Failures Due to Maximum Local Loop Count");
		outputList.add("MSExchange RpcClientAccess - Connection Count");
		outputList.add("MSExchange RpcClientAccess - Active User Count");
		outputList.add("MSExchange RpcClientAccess - User Count");
		outputList.add("MSExchange RpcClientAccess - RPC Dispatch Task Queue Length");
		outputList.add("MSExchange RpcClientAccess - XTC Dispatch Task Queue Length");
		outputList.add("MSExchange RpcClientAccess - RPCHttpConnectionRegistration Dispatch Task Queue Length");
		outputList.add("MSExchangeTransport Queues - Active Mailbox Delivery Queue Length");
		outputList.add("MSExchangeTransport Queues - Submission Queue Length");
		outputList.add("MSExchangeTransport Queues - Active Non-Smtp Delivery Queue Length");
		outputList.add("MSExchangeTransport Queues - Retry Mailbox Delivery Queue Length");
		outputList.add("MSExchangeTransport Queues - Unreachable Queue Length");
		outputList.add("MSExchangeTransport Queues - Poison Queue Length");
		outputList.add("MSExchangeTransport Queues - Messages Queued for Delivery Per Second");
		outputList.add("MSExchangeTransport Queues - Retry Non-SMTP Delivery Queue Length");

		String result = "";
		if (SystemUtils.IS_OS_LINUX) {
			result = invokeRunAgentCustomScript(params, SCRIPT_COMMAND_NAME);
		} else if (SystemUtils.IS_OS_WINDOWS) {
			result = invokeExecutePowershellScriptAgainstRemoteHost(params);
		}

		assertFalse(result.equals(""));

		JsonNode jsonNode = invokeConvertStringToJsonNode(result);
		assertNotNull(jsonNode);

		int outputValue = 0;
		String outputParam = "";
		while (outputList.size() != 0) {
			outputParam = outputList.pop();
			outputValue = invokeGetIntValueFromJsonNode(outputParam, jsonNode);
			if (outputValue == ERROR_CODE) {
				fail("failed to get int value of specific filed.");
			} else {
				System.out.println(outputParam + " : " + outputValue);
			}
		}
	}

	/**
	 * Invoke private checkIfAgentIsRunningOnWindows method by using Java Reflection.
	 * 
	 * @param params
	 *            HashMap that contains input params.
	 * @param cmd
	 *            Command to send to Up.time Agent.
	 * @return True if executing runAgentWindowsEventLogsScript() is successful,
	 *         false otherwise.
	 */
	private String invokeRunAgentCustomScript(HashMap<String, Object> params, String cmd) {
		String result = "";
		try {
			Method method = UptimeExchange2013MailboxServerMonitor.class.getDeclaredMethod("runAgentCustomScript",
					HashMap.class, String.class);
			method.setAccessible(true);
			result = (String) method.invoke(UptimeExchange2013MailboxServerMonitor.class.newInstance(), new Object[] {
					params, cmd });
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Invoke private checkIfAgentIsRunningOnWindows method by using Java Reflection.
	 * 
	 * @param hostname
	 *            Name of host.
	 * @param port
	 *            Up.time Agent port number.
	 * @param verb
	 *            Verb to send to Up.time Agent.
	 * @return True if executing checkIfAgentIsRunningOnWindows() is successful,
	 *         false otherwise.
	 */
	private boolean invokeCheckIfAgentIsRunningOnWindows(HashMap<String, Object> params, String verb) {
		boolean gotResult = false;
		try {
			Method method = UptimeExchange2013MailboxServerMonitor.class.getDeclaredMethod(
					"checkIfAgentIsRunningOnWindows", HashMap.class, String.class);
			method.setAccessible(true);
			gotResult = (boolean) method.invoke(UptimeExchange2013MailboxServerMonitor.class.newInstance(),
					new Object[] { params, verb });
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return gotResult;
	}

	/**
	 * Invoke private executePowershellScriptAgainstRemoteHost method by using Java Reflection.
	 * 
	 * @param params
	 *            HashMap that contains input params.
	 * @return True if executing runAgentWindowsEventLogsScript() is successful,
	 *         false otherwise.
	 */
	private String invokeExecutePowershellScriptAgainstRemoteHost(HashMap<String, Object> params) {
		String result = "";
		try {
			Method method = UptimeExchange2013MailboxServerMonitor.class.getDeclaredMethod(
					"executePowershellScriptAgainstRemoteHost", HashMap.class);
			method.setAccessible(true);
			result = (String) method.invoke(UptimeExchange2013MailboxServerMonitor.class.newInstance(),
					new Object[] { params });
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Invoke private method convertStringToJsonNode by using Reflection.
	 * 
	 * @param jsonFormatResult
	 *            a String result in JSON format.
	 * @return JsonNode object.
	 */
	private JsonNode invokeConvertStringToJsonNode(String jsonFormatResult) {
		JsonNode jsonNode = null;
		try {
			Method method = UptimeExchange2013MailboxServerMonitor.class.getDeclaredMethod("convertStringToJsonNode",
					String.class);
			method.setAccessible(true);
			jsonNode = (JsonNode) method.invoke(UptimeExchange2013MailboxServerMonitor.class.newInstance(),
					new Object[] { jsonFormatResult });
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return jsonNode;
	}

	/**
	 * Invoke private method getIntValueFromJsonNode by using Reflection.
	 * 
	 * @param fieldName
	 *            fieldName of the JSON
	 * @param jsonNode
	 *            jsonNode object.
	 * @return int value of specific field.
	 */
	private int invokeGetIntValueFromJsonNode(String fieldName, JsonNode jsonNode) {
		int value = 0;
		try {
			Method method = UptimeExchange2013MailboxServerMonitor.class.getDeclaredMethod("getIntValueFromJsonNode",
					String.class, JsonNode.class);
			method.setAccessible(true);
			value = (int) method.invoke(UptimeExchange2013MailboxServerMonitor.class.newInstance(), new Object[] {
					fieldName, jsonNode });
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return value;
	}
}
