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
import java.util.Map;

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
		private static final Logger logger = LoggerFactory
				.getLogger(UptimeExchange2013MailboxServerMonitor.class);

		// Inputs from Up.time.
		HashMap<String, Object> inputs = new HashMap<String, Object>();

		// Input constants.
		static final String HOSTNAME = "hostname";
		static final String DOMAIN_NAME = "domainName";
		static final String PORT = "port";
		static final String AGENT_PASSWORD = "agentPassword";
		static final String USERNAME = "userName";
		static final String PASSWORD = "password";

		// Non-input constants.
		static final String AGENT_CMD_VER = "ver";
		static final String OS_TYPE_WINDOWS = "Windows";
		static final String JSON_ATTRIBUTE_RESULT = "result";
		static final int ERROR_CODE = -1;
		static final String AGENT_RESPONSE_ERR = "ERR";

		// Agent command to run a Powershell script.
		static final String AGENT_CUSTOM_SCRIPT_CMD = "ex2013mailbox";

		// LinkedList to contain output fields' names.
		final LinkedList<String> outputList = new LinkedList<String>();

		// Powershell script to Invoke-Command (remote Powershell)
		private final String remotePowershellScript = "remoteEx2013MailboxServer.ps1";
		// Name of a plugin string to be used in file path of the above Powershell script.
		private final String thePluginName = "exchange2013-mailbox-server-monitor";

		/**
		 * The setParameters function will accept a Parameters object containing
		 * the values filled into the monitor's configuration page in Up.time.
		 * 
		 * @param params
		 *            Parameters object which contains inputs.
		 */
		@Override
		public void setParameters(Parameters params) {
			logger.debug("Step 1 : Get inputs from Up.time and store them in HashMap.");
			// See definition in .xml file for plugin. Each plugin has different number of
			// input/output parameters.
			inputs.put(HOSTNAME, params.getString(HOSTNAME));
			inputs.put(PORT, params.getInteger(PORT));
			inputs.put(AGENT_PASSWORD, params.getString(AGENT_PASSWORD));
			inputs.put(DOMAIN_NAME, params.getString(DOMAIN_NAME));
			inputs.put(USERNAME, params.getString(USERNAME));
			inputs.put(PASSWORD, params.getString(PASSWORD));

			// [Outputs] in ArrayList
			outputList.add("MSExchange Assistants - Per Database - Events In Queue");
			outputList.add("MSExchange Delivery SmtpAvailability - % Availability");
			outputList
					.add("MSExchange Delivery SmtpAvailability - % Failures Due To Back Pressure");
			outputList.add("MSExchange Delivery SmtpAvailability - % Failures Due To TLS Errors");
			outputList
					.add("MSExchange Delivery SmtpAvailability - Failures Due to Maximum Local Loop Count");
			outputList.add("MSExchange RpcClientAccess - Connection Count");
			outputList.add("MSExchange RpcClientAccess - Active User Count");
			outputList.add("MSExchange RpcClientAccess - User Count");
			outputList.add("MSExchange RpcClientAccess - RPC Dispatch Task Queue Length");
			outputList.add("MSExchange RpcClientAccess - XTC Dispatch Task Queue Length");
			outputList
					.add("MSExchange RpcClientAccess - RPCHttpConnectionRegistration Dispatch Task Queue Length");
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
			String jsonResult = "";
			if (SystemUtils.IS_OS_LINUX) {
				logger.debug("Check if Agent Port, Agent Password are entered. If no, error.");
				if (!checkLinuxMonitoringStationInputs(inputs)) {
					setStateAndMessage(MonitorState.UNKNOWN,
							"Linux: Agent port and Agent password are required fields. "
									+ "Please confirm both fields are defined.");
					return;
				}

				logger.debug("Check if Up.time Agent is running on a remote Windows host.");
				if (!checkIfAgentIsRunningOnWindows(inputs, AGENT_CMD_VER)) {
					return;
				}

				logger.debug("Send rexec command to Up.time Agent running on a remote Windows host.");
				jsonResult = runAgentCustomScript(inputs, AGENT_CUSTOM_SCRIPT_CMD);

				logger.debug("Error handling : Check if Up.time Agent sent back ERR message or result is empty string.");
				if (jsonResult.equals(AGENT_RESPONSE_ERR) || jsonResult.isEmpty()) {
					setStateAndMessage(MonitorState.UNKNOWN,
							"Linux: Failed to communicate to the Agent on port. "
									+ "Please confirm Agent port and Agent password are correct.");
					return;
				}

				logger.debug("Error handling : Check if Up.time Agent sent back JSON result.");
				if (jsonResult == null | jsonResult.isEmpty()) {
					setStateAndMessage(MonitorState.UNKNOWN,
							"Could not get JSON result from Up.time Agent");
					return;
				}

			} else if (SystemUtils.IS_OS_WINDOWS) {
				logger.debug("Check if Password, Username are entered. If no, error.");
				if (!checkWindowsMonitoringStationInputs(inputs)) {
					setStateAndMessage(MonitorState.UNKNOWN,
							"Windows: Domain, Username and Password are required fields. "
									+ "Please confirm all fields are defined.");

					// Just testing. Delete it later.
					ArrayList<String> args = new ArrayList<String>();
					args.add("Powershell.exe");
					// Prevent 32-bit / 64-bit Powershell security issue.
					args.add("-ExecutionPolicy");
					args.add("Unrestricted");
					args.add("-Command");
					// Put the Powershell scripts in some location and change this path args
					// accordingly.
					args.add("& "
							+ getRemotePowerShellScriptPath(remotePowershellScript, thePluginName)
							+ " -remoteHost " + (String) inputs.get(HOSTNAME) + " -username "
							+ (String) inputs.get(DOMAIN_NAME) + (String) inputs.get(USERNAME)
							+ " -password " + (String) inputs.get(PASSWORD));
					for (String arg : args) {
						addVariable("DEBUG_pscmd_arg", arg);
					}
					addVariable("DEBUG_hostname", (String) inputs.get(HOSTNAME));
					addVariable("DEBUG_agent_port", (Integer) inputs.get(PORT));
					addVariable("DEBUG_agent_password", (String) inputs.get(AGENT_PASSWORD));
					addVariable("DEBUG_username", (String) inputs.get(USERNAME));
					addVariable("DEBUG_password", (String) inputs.get(PASSWORD));

					return;
				}

				logger.debug("Execute Powershell script on a Windows monitoring station "
						+ "against a remote Windows host.");
				jsonResult = executePowershellScriptAgainstRemoteHost(inputs);
				if (jsonResult == null | jsonResult.isEmpty()) {
					setStateAndMessage(MonitorState.UNKNOWN,
							"Windows: Failed to authenticate using domain, username and password. "
									+ "Please confirm values are correct.");

					// Just testing. Delete it later.
					ArrayList<String> args = new ArrayList<String>();
					args.add("Powershell.exe");
					// Prevent 32-bit / 64-bit Powershell security issue.
					args.add("-ExecutionPolicy");
					args.add("Unrestricted");
					args.add("-Command");
					// Put the Powershell scripts in some location and change this path args
					// accordingly.
					args.add("& "
							+ getRemotePowerShellScriptPath(remotePowershellScript, thePluginName)
							+ " -remoteHost " + (String) inputs.get(HOSTNAME) + " -username "
							+ (String) inputs.get(DOMAIN_NAME) + (String) inputs.get(USERNAME)
							+ " -password " + (String) inputs.get(PASSWORD));
					for (String arg : args) {
						addVariable("DEBUG_pscmd_arg", arg);
					}
					addVariable("DEBUG_hostname", (String) inputs.get(HOSTNAME));
					addVariable("DEBUG_agent_port", (Integer) inputs.get(PORT));
					addVariable("DEBUG_agent_password", (String) inputs.get(AGENT_PASSWORD));
					addVariable("DEBUG_username", (String) inputs.get(USERNAME));
					addVariable("DEBUG_password", (String) inputs.get(PASSWORD));

					return;
				}
			}

			logger.debug("Convert result String in JSON format to JsonNode object.");
			JsonNode jsonNode = convertStringToJsonNode(jsonResult);

			logger.debug("Error handling : Check if JsonNode object is null or not.");
			if (jsonNode == null) {
				setStateAndMessage(MonitorState.UNKNOWN,
						"Could not convert result String to JsonNode object. "
								+ "Check the result String is in JSON format.");

				// Just testing. Delete it later.
				ArrayList<String> args = new ArrayList<String>();
				args.add("Powershell.exe");
				// Prevent 32-bit / 64-bit Powershell security issue.
				args.add("-ExecutionPolicy");
				args.add("Unrestricted");
				args.add("-Command");
				// Put the Powershell scripts in some location and change this path args
				// accordingly.
				args.add("& "
						+ getRemotePowerShellScriptPath(remotePowershellScript, thePluginName)
						+ " -remoteHost " + (String) inputs.get(HOSTNAME) + " -username "
						+ (String) inputs.get(DOMAIN_NAME) + (String) inputs.get(USERNAME)
						+ " -password " + (String) inputs.get(PASSWORD));
				for (String arg : args) {
					addVariable("DEBUG_pscmd_arg", arg);
				}
				addVariable("DEBUG_hostname", (String) inputs.get(HOSTNAME));
				addVariable("DEBUG_agent_port", (Integer) inputs.get(PORT));
				addVariable("DEBUG_agent_password", (String) inputs.get(AGENT_PASSWORD));
				addVariable("DEBUG_username", (String) inputs.get(USERNAME));
				addVariable("DEBUG_password", (String) inputs.get(PASSWORD));

				return;
			}

			logger.debug("Parse output values from JsonNode object.");
			int outputValue = 0;
			String outputParam = "";
			while (outputList.size() != 0) {
				outputParam = outputList.pop();
				outputValue = getIntValueFromJsonNode(outputParam, jsonNode);
				if (outputValue == ERROR_CODE) {
					setStateAndMessage(MonitorState.UNKNOWN, "Unable to get int value of "
							+ outputParam);

					// Just testing. Delete it later.
					ArrayList<String> args = new ArrayList<String>();
					args.add("Powershell.exe");
					// Prevent 32-bit / 64-bit Powershell security issue.
					args.add("-ExecutionPolicy");
					args.add("Unrestricted");
					args.add("-Command");
					// Put the Powershell scripts in some location and change this path args
					// accordingly.
					args.add("& "
							+ getRemotePowerShellScriptPath(remotePowershellScript, thePluginName)
							+ " -remoteHost " + (String) inputs.get(HOSTNAME) + " -username "
							+ (String) inputs.get(DOMAIN_NAME) + (String) inputs.get(USERNAME)
							+ " -password " + (String) inputs.get(PASSWORD));
					for (String arg : args) {
						addVariable("DEBUG_pscmd_arg", arg);
					}
					addVariable("DEBUG_hostname", (String) inputs.get(HOSTNAME));
					addVariable("DEBUG_agent_port", (Integer) inputs.get(PORT));
					addVariable("DEBUG_agent_password", (String) inputs.get(AGENT_PASSWORD));
					addVariable("DEBUG_username", (String) inputs.get(USERNAME));
					addVariable("DEBUG_password", (String) inputs.get(PASSWORD));

					return;
				} else {
					addVariable(outputParam, outputValue);
				}
			}

			// Just testing. Delete it later.
			ArrayList<String> args = new ArrayList<String>();
			args.add("Powershell.exe");
			// Prevent 32-bit / 64-bit Powershell security issue.
			args.add("-ExecutionPolicy");
			args.add("Unrestricted");
			args.add("-Command");
			// Put the Powershell scripts in some location and change this path args accordingly.
			args.add("& " + getRemotePowerShellScriptPath(remotePowershellScript, thePluginName)
					+ " -remoteHost " + (String) inputs.get(HOSTNAME) + " -username "
					+ (String) inputs.get(DOMAIN_NAME) + (String) inputs.get(USERNAME)
					+ " -password " + (String) inputs.get(PASSWORD));
			for (String arg : args) {
				addVariable("DEBUG_pscmd_arg", arg);
			}
			addVariable("DEBUG_hostname", (String) inputs.get(HOSTNAME));
			addVariable("DEBUG_agent_port", (Integer) inputs.get(PORT));
			addVariable("DEBUG_agent_password", (String) inputs.get(AGENT_PASSWORD));
			addVariable("DEBUG_username", (String) inputs.get(USERNAME));
			addVariable("DEBUG_password", (String) inputs.get(PASSWORD));

			setStateAndMessage(MonitorState.OK, "Monitor successfully ran.");
		}

		/**
		 * Get int value of specified field.
		 * 
		 * @param fieldName
		 *            Name of field that has int value.
		 * @param nodeObject
		 *            JsonNode object containing all JSON nodes.
		 * @return int value of specified field.
		 */
		int getIntValueFromJsonNode(String fieldName, JsonNode nodeObject) {
			// Get nested JSON.
			JsonNode nestedJsonNode = nodeObject.get(JSON_ATTRIBUTE_RESULT);
			if (nestedJsonNode == null) {
				logger.error("Could not find {} attribute.", JSON_ATTRIBUTE_RESULT);
				return ERROR_CODE;
			}
			nestedJsonNode = nestedJsonNode.get(fieldName);
			if (nestedJsonNode == null) {
				logger.error("Could not find {} attribute within {} attribute", fieldName,
						JSON_ATTRIBUTE_RESULT);
				return ERROR_CODE;
			}
			return Integer.parseInt(nestedJsonNode.getTextValue());
		}

		/**
		 * Convert result String in JSON format to JsonNode object.
		 * 
		 * @param jsonFormatResult
		 *            Result String in JSON format.
		 * @return JsonNode object containing all JSON nodes.
		 */
		JsonNode convertStringToJsonNode(String jsonFormatResult) {
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getJsonFactory();
			JsonParser JSONParser = null;
			JsonNode nodeObject = null;
			try {
				JSONParser = factory.createJsonParser(jsonFormatResult);
				nodeObject = mapper.readTree(JSONParser);
			} catch (IOException e) {
				logger.error("Error while reading JSON tree", e);
			}
			if (nodeObject == null) {
				logger.error("Converting did not complete successfully.");
			}
			return nodeObject;
		}

		/**
		 * Check if Up.time Agent is running on Windows.
		 * 
		 * @param inputs
		 *            HashMap that contains input params.
		 * @param verb
		 *            Up.time Agent verb.
		 * @return True if Up.time Agent is running on Windows, false otherwise.
		 */
		boolean checkIfAgentIsRunningOnWindows(HashMap<String, Object> inputs, String verb) {
			logger.debug("Send a command to Up.time Agent and get a name of OS.");
			String osType = sendCmdToAgent(inputs, verb);
			if (osType == null || osType.isEmpty()) {
				setStateAndMessage(MonitorState.UNKNOWN, "Could not get OS info from Up.time Agent");
				return false;
			} else if (osType.equals(AGENT_RESPONSE_ERR)) {
				setStateAndMessage(MonitorState.UNKNOWN,
						"Agent sent back ERR. Check the ver command.");
				return false;
			} else if (!osType.contains(OS_TYPE_WINDOWS)) {
				setStateAndMessage(MonitorState.UNKNOWN,
						"Exchange 2013 Mailbox Server plugin requires Up.time Agent to be on remote Windows host.");
				return false;
			}
			return true;
		}

		/**
		 * Run a custom script on remote Windows host.
		 * 
		 * @param inputs
		 *            HashMap that contains input params.
		 * @param cmd
		 *            Command to run a custom script on Windows
		 * @return Response from a socket in String.
		 */
		String runAgentCustomScript(HashMap<String, Object> inputs, String cmd) {
			return sendCmdToAgent(inputs, "rexec " + (String) inputs.get(AGENT_PASSWORD) + " "
					+ cmd);
		}

		/**
		 * Open a socket and write to & read from the open socket.
		 * 
		 * @param inputs
		 *            HashMap that contains input params.
		 * @param cmd
		 *            Command to write to a open socket
		 * @return Response from a socket in String.
		 */
		private String sendCmdToAgent(HashMap<String, Object> inputs, String cmd) {
			StringBuilder result = new StringBuilder();
			// Try-with-resource statement will close socket and resources after completing a task.
			try (Socket socket = new Socket((String) inputs.get(HOSTNAME),
					(Integer) inputs.get(PORT));
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream()));
					BufferedReader in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));) {
				// Check if cmd is empty or not.
				if (cmd.isEmpty() || cmd == null) {
					logger.error("{} is empty/null", cmd);
				} else {
					// Write the cmd on the connected socket.
					out.write(cmd);
					// Flush OutputStream after writing because there is no guarantee that the
					// serialized representation will get sent to the other end.
					out.flush();
				}
				// Read line(s) from the connected socket.
				String line = "";
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
			} catch (UnknownHostException e) {
				logger.error("Unable to open a socket.", e);
			} catch (IOException e) {
				logger.error("Unable to get I/O of socket.", e);
			}
			return result.toString();
		}

		/**
		 * Create a new process and execute a Powershell script against remote Windows host.
		 * 
		 * @param inputs
		 *            HashMap that contains input params.
		 * @return Response from the process in String.
		 */
		String executePowershellScriptAgainstRemoteHost(HashMap<String, Object> inputs) {
			StringBuilder result = new StringBuilder();
			Process process = null;
			String remotePowershellScriptPath = getRemotePowerShellScriptPath(
					remotePowershellScript, thePluginName);

			ArrayList<String> args = new ArrayList<String>();
			args.add("Powershell.exe");
			// Prevent 32-bit / 64-bit Powershell security issue.
			args.add("-ExecutionPolicy");
			args.add("Unrestricted");
			args.add("-Command");
			// Put the Powershell scripts in some location and change this path args accordingly.
			args.add("& " + remotePowershellScriptPath + " -remoteHost "
					+ (String) inputs.get(HOSTNAME) + " -username "
					+ (String) inputs.get(DOMAIN_NAME) + (String) inputs.get(USERNAME)
					+ " -password " + (String) inputs.get(PASSWORD));
			try {
				ProcessBuilder pb = new ProcessBuilder(args);
				process = pb.start();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line);
				}
				process.waitFor();
				process.destroy();
			} catch (IOException | InterruptedException e) {
				logger.error("Unable to get result from the process.", e);
			}
			return result.toString();
		}

		/**
		 * Get file path to a given Powershell script (within uptime folder).
		 * 
		 * @param psScriptFileName
		 *            Name of a given Powershell script.
		 * @return File path to a given Powershell Script.
		 */
		String getRemotePowerShellScriptPath(String psScriptFileName, String thePluginName) {
			String uptimeFolder = "";
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				uptimeFolder = env.get(envName);
				// Environment variable named MIBDIRS contains path to uptime folder.
				if (uptimeFolder.contains("uptime")) {
					// get rid of 'mib\' part.
					uptimeFolder = uptimeFolder.substring(0, uptimeFolder.length() - 4);
					break;
				}
			}
			uptimeFolder += "plugins/java/" + thePluginName + "/scripts/";

			// Because Microsoft is so special, we now need to escape space character with back-tick
			return uptimeFolder.contains(" ") ? uptimeFolder.replaceAll(" ", "` ")
					+ psScriptFileName : uptimeFolder + psScriptFileName;
		}

		/**
		 * Check if Agent Password and Agent Port are entered on Linux monitoring station.
		 * 
		 * @param inputs
		 *            HashMap that contains input params.
		 * @return True if Agent Password and Agent Port are entered on Linux monitoring station.
		 */
		boolean checkLinuxMonitoringStationInputs(HashMap<String, Object> inputs) {
			return inputs.get(AGENT_PASSWORD) != null
					&& !((String) inputs.get(AGENT_PASSWORD)).isEmpty() && inputs.get(PORT) != null;
		}

		/**
		 * Check if Username, Password are entered on Windows monitoring station.
		 * 
		 * @param inputs
		 *            HashMap that contains input params.
		 * @return True if Username, Password are entered on Windows monitoring station.
		 */
		boolean checkWindowsMonitoringStationInputs(HashMap<String, Object> inputs) {
			return inputs.get(PASSWORD) != null && !((String) inputs.get(PASSWORD)).isEmpty()
					&& inputs.get(USERNAME) != null && !((String) inputs.get(USERNAME)).isEmpty();
		}
	}
}