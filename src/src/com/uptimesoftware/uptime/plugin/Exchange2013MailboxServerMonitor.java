package com.uptimesoftware.uptime.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.commons.lang.SystemUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.PluginWrapper;

import com.uptimesoftware.uptime.plugin.api.Extension;
import com.uptimesoftware.uptime.plugin.api.Plugin;
import com.uptimesoftware.uptime.plugin.api.PluginMonitor;
import com.uptimesoftware.uptime.plugin.monitor.MonitorState;
import com.uptimesoftware.uptime.plugin.monitor.Parameters;

/**
 * Exchange2013 Mailbox Server Monitor.
 * 
 * @author uptime software
 */
public class Exchange2013MailboxServerMonitor extends Plugin {

	/**
	 * Constructor - a plugin wrapper.
	 * 
	 * @param wrapper
	 */
	public Exchange2013MailboxServerMonitor(PluginWrapper wrapper) {
		super(wrapper);
	}

	/**
	 * A nested static class which has to extend PluginMonitor.
	 * 
	 * Functions that require implementation : 1) The monitor function will
	 * implement the main functionality and should set the monitor's state and
	 * result message prior to completion. 2) The setParameters function will
	 * accept a Parameters object containing the values filled into the
	 * monitor's configuration page in Up.time.
	 */
	@Extension
	public static class UptimeExchange2013MailboxServerMonitor extends PluginMonitor {
		// Logger object.
		private static final Logger LOGGER = LoggerFactory.getLogger(UptimeExchange2013MailboxServerMonitor.class);

		// Constants.
		private static final String HOSTNAME = "hostname";
		private static final String DOMAIN_NAME = "domainName";
		private static final String PORT = "port";
		private static final String USERNAME = "userName";
		private static final String PASSWORD = "password";
		private static final String VERSION = "ver";
		private static final String OS_TYPE_WINDOWS = "Windows";
		private static final String SCRIPT_COMMAND_NAME = "ex2013mailbox";
		private static final String JSON_ATTRIBUTE_RESULT = "result";
		private static final int ERROR_CODE = -1;
		private static final String AGENT_ERR = "ERR";

		// LinkedList to contain output fields' names.
		private final LinkedList<String> outputList = new LinkedList<String>();

		// See definition in .xml file for plugin. Each plugin has different
		// number of input/output parameters.
		// [Input]
		String hostname;
		String domainName;
		int port;
		String userName;
		// On Windows, the password is for remote Powershell script. On Linux, the password is for Up.time Agent.
		String password;

		/**
		 * The setParameters function will accept a Parameters object containing
		 * the values filled into the monitor's configuration page in Up.time.
		 * 
		 * @param params
		 *            Parameters object which contains inputs.
		 */
		@Override
		public void setParameters(Parameters params) {
			LOGGER.debug("Step 1 : Setting parameters.");
			// [Input]
			hostname = params.getString(HOSTNAME);
			domainName = params.getString(DOMAIN_NAME);
			port = params.getInt(PORT);
			userName = params.getString(USERNAME);
			password = params.getString(PASSWORD);

			// [Outputs] in ArrayList
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
		}

		/**
		 * The monitor function will implement the main functionality and should
		 * set the monitor's state and result message prior to completion.
		 */
		@Override
		public void monitor() {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(HOSTNAME, hostname);
			params.put(DOMAIN_NAME, domainName);
			params.put(PORT, port);
			params.put(USERNAME, userName);
			params.put(PASSWORD, password);

			String jsonResult = "";
			if (SystemUtils.IS_OS_LINUX) {
				LOGGER.debug("Check if Up.time Agent is running on a remote Windows host.");
				if (!checkIfAgentIsRunningOnWindows(params, VERSION)) {
					return;
				}

				LOGGER.debug("Send rexec command to Up.time Agent running on a remote Windows host.");
				jsonResult = runAgentCustomScript(params, SCRIPT_COMMAND_NAME);

				LOGGER.debug("Error handling : Check if Up.time Agent sent back ERR message");
				if (jsonResult.equals(AGENT_ERR)) {
					setStateAndMessage(MonitorState.UNKNOWN, "Agent sent back ERR. Check the rexec command.");
					return;
				}

				LOGGER.debug("Error handling : Check if Up.time Agent sent back JSON result.");
				if (jsonResult == null | jsonResult.equals("")) {
					setStateAndMessage(MonitorState.UNKNOWN, "Could not get JSON result from Up.time Agent");
					return;
				}

			} else if (SystemUtils.IS_OS_WINDOWS) {
				LOGGER.debug("Execute Powershell script on a Windows monitoring station against a remote Windows host.");
				jsonResult = executePowershellScriptAgainstRemoteHost(params);
				if (jsonResult == null | jsonResult.equals("")) {
					setStateAndMessage(MonitorState.UNKNOWN, "Could not get JSON result from remote Powershell script.");
					return;
				}
			}

			LOGGER.debug("Convert result String in JSON format to JsonNode object.");
			JsonNode jsonNode = convertStringToJsonNode(jsonResult);

			LOGGER.debug("Error handling : Check if JsonNode object is null or not.");
			if (jsonNode == null) {
				setStateAndMessage(MonitorState.UNKNOWN, "Could not convert result String to JsonNode object");
				return;
			}

			LOGGER.debug("Parse output values from JsonNode object.");
			int outputValue = 0;
			String outputParam = "";
			while (outputList.size() != 0) {
				outputParam = outputList.pop();
				outputValue = getIntValueFromJsonNode(outputParam, jsonNode);
				if (outputValue == ERROR_CODE) {
					setStateAndMessage(MonitorState.UNKNOWN, "Unable to get int value of " + outputParam);
					return;
				} else {
					addVariable(outputParam, outputValue);
				}
			}

			LOGGER.debug("Everything ran okay. Set monitor state to OK");
			setStateAndMessage(MonitorState.OK, "Monitor successfully ran.");
		}

		/**
		 * Private helper function to get int value of specified field.
		 * 
		 * @param fieldName
		 *            Name of field that has int value.
		 * @param nodeObject
		 *            JsonNode object containing all JSON nodes.
		 * @return int value of specified field.
		 */
		private int getIntValueFromJsonNode(String fieldName, JsonNode nodeObject) {
			// Get nested JSON.
			JsonNode nestedJsonNode = nodeObject.get(JSON_ATTRIBUTE_RESULT);
			if (nestedJsonNode == null) {
				LOGGER.error("Could not find {} attribute.", JSON_ATTRIBUTE_RESULT);
				return ERROR_CODE;
			}
			nestedJsonNode = nestedJsonNode.get(fieldName);
			if (nestedJsonNode == null) {
				LOGGER.error("Could not find {} attribute within {} attribute", fieldName, JSON_ATTRIBUTE_RESULT);
				return ERROR_CODE;
			}
			return Integer.parseInt(nestedJsonNode.getTextValue());
		}

		/**
		 * Private helper function to convert result String in JSON format to JsonNode object. (3rd party library -
		 * Jackson)
		 * 
		 * @param jsonFormatResult
		 *            Result String in JSON format.
		 * @return JsonNode object containing all JSON nodes.
		 */
		private JsonNode convertStringToJsonNode(String jsonFormatResult) {
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getJsonFactory();
			JsonParser JSONParser = null;
			JsonNode nodeObject = null;
			try {
				JSONParser = factory.createJsonParser(jsonFormatResult);
				nodeObject = mapper.readTree(JSONParser);
			} catch (IOException e) {
				LOGGER.error("Error while reading JSON tree", e);
			}
			if (nodeObject == null) {
				LOGGER.error("Converting did not complete successfully.");
			}
			return nodeObject;
		}

		/**
		 * Check if Up.time Agent is running on Windows.
		 * 
		 * @param params
		 *            HashMap that contains input params.
		 * @param verb
		 *            Up.time Agent verb.
		 * @return True if Up.time Agent is running on Windows, false otherwise.
		 */
		private boolean checkIfAgentIsRunningOnWindows(HashMap<String, Object> params, String verb) {
			LOGGER.debug("Send a command to Up.time Agent and get a name of OS.");
			String osType = sendCmdToAgent(params, verb);
			if (osType == null || osType.equals("")) {
				setStateAndMessage(MonitorState.UNKNOWN, "Could not get OS info from Up.time Agent");
				return false;
			} else if (osType.equals(AGENT_ERR)) {
				setStateAndMessage(MonitorState.UNKNOWN, "Agent sent back ERR. Check the ver command.");
				return false;
			} else if (!osType.contains(OS_TYPE_WINDOWS)) {
				setStateAndMessage(MonitorState.UNKNOWN, "Exchange 2013 Mailbox Server plugin cannot run on Linux.");
				return false;
			}
			return true;
		}

		/**
		 * Private helper function to run a custom script on remote Windows host.
		 * 
		 * @param params
		 *            HashMap that contains input params.
		 * @param cmd
		 *            Command to run a custom script on Windows
		 * @return Response from a socket in String.
		 */
		private String runAgentCustomScript(HashMap<String, Object> params, String cmd) {
			return sendCmdToAgent(params, "rexec " + (String) params.get(PASSWORD) + " " + cmd);
		}

		/**
		 * Private helper function to open a socket and write to & read from the open socket.
		 * 
		 * @param params
		 *            HashMap that contains input params.
		 * @param cmd
		 *            Command to write to a open socket
		 * @return Response from a socket in String.
		 */
		private String sendCmdToAgent(HashMap<String, Object> params, String cmd) {
			StringBuilder result = new StringBuilder();
			// Try-with-resource statement will close socket and resources after completing a task.
			try (Socket socket = new Socket((String) params.get(HOSTNAME), (int) params.get(PORT));
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
				// Check if cmd is empty or not.
				if (cmd.equals("") || cmd == null) {
					LOGGER.error("{} is empty/null", cmd);
				} else {
					// Write the cmd on the connected socket.
					out.write(cmd);
					// Flush OutputStream after writing because there is no guarantee that the serialized representation
					// will get sent to the other end.
					out.flush();
				}
				// Read line(s) from the connected socket.
				String line = "";
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
			} catch (UnknownHostException e) {
				LOGGER.error("Unable to open a socket.", e);
			} catch (IOException e) {
				LOGGER.error("Unable to get I/O of socket.", e);
			}
			return result.toString();
		}

		/**
		 * Create a new process and execute a Powershell script against remote Windows host.
		 * 
		 * @param params
		 *            HashMap that contains input params.
		 * @return Response from the process in String.
		 */
		private String executePowershellScriptAgainstRemoteHost(HashMap<String, Object> params) {
			StringBuilder result = new StringBuilder();
			Process process = null;
			ArrayList<String> args = new ArrayList<String>();
			args.add("Powershell.exe");
			args.add("-Command");
			// Put the Powershell scripts in some location and change this path args accordingly.
			args.add("& C:\\uptime_scripts\\remoteEx2013MailboxServer.ps1 -remoteHost " + (String) params.get(HOSTNAME)
					+ " -username " + (String) params.get(DOMAIN_NAME) + (String) params.get(USERNAME) + " -password "
					+ (String) params.get(PASSWORD));
			try {
				ProcessBuilder pb = new ProcessBuilder(args);
				process = pb.start();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line);
				}
				process.waitFor();
				process.destroy();
			} catch (IOException | InterruptedException e) {
				LOGGER.error("Unable to get result from the process.", e);
			}
			return result.toString();
		}
	}
}