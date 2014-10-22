On Error Resume Next
' Sameple JSON format output. 
' 
' {
'   "hostname": "%COMPUTERNAME%",
'   "plugin": "Windows Exchange 2013 Agent side rexec plugin",
'   "version": "7.3.0",
'   "currentTime": "%DATE:~-4%-%DATE:~3,2%-%DATE:~0,2%T%TIME:~0,8%",
'   "status": "200",
'   "result": {
'      "SMTPBytesSentPerSecond": "128", 
'      "SMTPBytesReceivedPerSecond": "256",
'      "SMTPMessagesSentPerSecond": "512",
'      "SMTPInboundMessagesReceivedPerSecond": "640",
'      "SMTPAverageBytesPerInboundMessage": "768",
'      "SMTPInboundConnections": "896",
'      "SMTPOutboundConnections": "1024",
'      "CurrentWebmailUsers": "5",
'      "WebmailUserLogonsPerSecond": "10",
'      "RPCAveragedLatency": "15",
'      "RPCOperationsPerSecond": "20",
'      "RPCRequests": "25",
'      .......
'      .......
'   }
' }

'On up.time Agent console :
'Command Name : ex2013mailbox
'Path to script : cmd /c "cscript "C:\uptime_scripts\exchange2013-mailbox-server.vbs" //Nologo //T:0"

Const wbemFlagReturnImmediately = &h10
Const wbemFlagForwardOnly = &h20

'There should be a way to combine formJSON, formNestedJSON, formEndLineJSON functions into one. Fix it later.
'take fieldName and fieldValue parameters and form JSON.
Private Function formJSON(fieldName, fieldValue)
  Dim lineInJSONFormat
  lineInJSONFormat = chr(34) & fieldName & chr(34) & ": " & chr(34) & fieldValue & chr(34) & ","
  'set function's return value
  formJSON = lineInJSONFormat
END Function

'take fieldName and curly bracket parameters and form nested JSON.
Private Function formNestedJSON(fieldName)
  Dim lineInJSONFormat
  lineInJSONFormat = chr(34) & fieldName & chr(34) & ": " & "{"
  'set function's return value
  formNestedJSON = lineInJSONFormat
END Function

'take fieldName and curly bracket parameters and form nested JSON.
Private Function formEndLineJSON(fieldName, fieldValue)
  Dim lineInJSONFormat
  lineInJSONFormat = chr(34) & fieldName & chr(34) & ": " & chr(34) & fieldValue & chr(34)
  'set function's return value
  formEndLineJSON = lineInJSONFormat
END Function

'Get name of host
Dim wshNetwork
Set wshNetwork = CreateObject("WScript.Network")
strComputerName = wshNetwork.ComputerName

Set objWMIService = GetObject("winmgmts:\\" & strComputerName & "\root\CIMV2")

'Outputing JSON begins
WScript.Echo "{"
WScript.Echo formJSON("Computer", strComputerName)
WScript.Echo formJSON("plugin", "Windows Exchange 2013 Agent side rexec plugin")
WScript.Echo formJSON("version", "7.3.0")
WScript.Echo formJSON("currentTime", Now)
WScript.Echo formJSON("status", "200")
'Nested JSON begins
WScript.Echo formNestedJSON("result")

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeISClientType_MSExchangeISClientType WHERE Name = ""_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
'  WScript.Echo formJSON("MSExchangeISClientType - RPC Average Latency", objItem.RPCAverageLatency)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_ESE_MSExchangeDatabase WHERE Name= ""information store""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
''  WScript.Echo formJSON("MSExchangeDatabase - Database Page Fault Stalls Per Second", objItem.DatabasePageFaultStallsPersec)
''  WScript.Echo formJSON("MSExchangeDatabase - Database Cache Percent Hit", objItem.DatabaseCachePercentHit)
''  WScript.Echo formJSON("MSExchangeDatabase - Log Record Stalls Per Second", objItem.LogRecordStallsPersec)
''  WScript.Echo formJSON("MSExchangeDatabase - Log Threads Waiting", objItem.LogThreadsWaiting)
''  WScript.Echo formJSON("MSExchangeDatabase - IO Database Reads Attached Average Latency", objItem.IODatabaseReadsAttachedAverageLatency)
''  WScript.Echo formJSON("MSExchangeDatabase - IO Database Writes Attached Average Latency", objItem.IODatabaseWritesAttachedAverageLatency)
''  WScript.Echo formJSON("MSExchangeDatabase - IO Log Writes Average Latency", objItem.IOLogWritesAverageLatency)
''  WScript.Echo formJSON("MSExchangeDatabase - IO Database Reads Recovery Average Latency", objItem.IODatabaseReadsRecoveryAverageLatency)
''  WScript.Echo formJSON("MSExchangeDatabase - IO Database Writes Recovery Average Latency", objItem.IODatabaseWritesRecoveryAverageLatency)
''  WScript.Echo formJSON("MSExchangeDatabase - IO Log Reads Average Latency", objItem.IOLogReadsAverageLatency)
''  WScript.Echo formJSON("MSExchangeDatabase - Version Buckets Allocated", objItem.VersionBucketsAllocated)
''  WScript.Echo formJSON("MSExchangeDatabase - Database Cache Size MB", objItem.DatabaseCacheSizeMB)
''  WScript.Echo formJSON("MSExchangeDatabase - Log Bytes Write Per Second", objItem.LogBytesWritePersec)  
'Next

Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeAssistantsPerDatabase_MSExchangeAssistantsPerDatabase WHERE Name = ""msexchangemailboxassistants-total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
For Each objItem In colItems
  WScript.Echo formJSON("MSExchange Assistants - Per Database - Events In Queue", objItem.Eventsinqueue)
  'WScript.Echo formJSON("MSExchangeAssistantsPerDatabase - Average Event Processing Time In Second", objItem.AverageEventProcessingTimeInseconds)
  'WScript.Echo formJSON("MSExchangeAssistantsPerDatabase - Mailboxes Processed Per Second", objItem.MailboxesprocessedPersec) 
  'WScript.Echo formJSON("MSExchangeAssistantsPerDatabase - Events Polled Per Second", objItem.EventsPolledPersec)
Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_ESE_MSExchangeDatabaseInstances WHERE Name= ""information store/_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesInformationStore - Log Generation Check Point Depth", objItem.LogGenerationCheckpointDepth) 
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesInformationStore - IO Database Instances Reads Average Latency", objItem.IODatabaseReadsAverageLatency)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesInformationStore - IO Database Instances Writes Average Latency", objItem.IODatabaseWritesAverageLatency) 
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_ESE_MSExchangeDatabaseInstances WHERE Name= ""edgetransport/_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems 
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - IO Log Writes Per Second", objItem.IOLogWritesPersec)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - IO Log Reads Per Second", objItem.IOLogReadsPersec)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - Log Generation Check Point Depth", objItem.LogGenerationCheckpointDepth)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - Version Buckets Allocated", objItem.VersionBucketsAllocated)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - IO Database Reads Per Second", objItem.IODatabaseReadsPersec)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - IO Database Writes Per Second", objItem.IODatabaseWritesPersec)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - Log Record Stalls Per Second", objItem.LogRecordStallsPersec)
  'WScript.Echo formJSON("MSExchangeDatabaseInstancesEdgetransport - Log Threads Waiting", objItem.LogThreadsWaiting)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeTransportSMTPReceive_MSExchangeTransportSMTPReceive WHERE Name = ""_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeTransportSMTPReceive - Average Bytes Per Message", objItem.AveragebytesPermessage)
  'WScript.Echo formJSON("MSExchangeTransportSMTPReceive - Messages Received Per Second", objItem.MessagesReceivedPersec)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeTransportSmtpSend_MSExchangeTransportSmtpSend WHERE Name = ""_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeTransportSmtpSend - Messages Sent Per Second", objItem.MessagesSentPersec)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeResourceBooking_MSExchangeResourceBooking", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeResourceBooking - Average Resource Booking Processing Time", objItem.AverageResourceBookingProcessingTime)
  'WScript.Echo formJSON("MSExchangeResourceBooking - Requests Failed", objItem.RequestsFailed)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeCalendarAttendant_MSExchangeCalendarAttendant", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeCalendarAttendant - Average Calendar Attendant Processing Time", objItem.AverageCalendarAttendantProcessingTime)
  'WScript.Echo formJSON("MSExchangeCalendarAttendant - Requests Failed", objItem.RequestsFailed)  
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeStoreInterface_MSExchangeStoreInterface WHERE Name = ""_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeStoreInterface - ROP Requests Outstanding", objItem.ROPRequestsoutstanding)
  'WScript.Echo formJSON("MSExchangeStoreInterface - RPC Requests Outstanding", objItem.RPCRequestsoutstanding)
  'WScript.Echo formJSON("MSExchangeStoreInterface - RPC Requests Sent Per Second", objItem.RPCRequestssentPersec)
  'WScript.Echo formJSON("MSExchangeStoreInterface - RPC Slow Requests Latency Average Microsecond", objItem.RPCSlowrequestslatencyaveragemsec)
  'WScript.Echo formJSON("MSExchangeStoreInterface - RPC Requests Failed Percent", objItem.RPCRequestsfailedPercent)
  'WScript.Echo formJSON("MSExchangeStoreInterface - RPC Slow Requests Percent", objItem.RPCSlowrequestsPercent)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeSubmission_MSExchangeSubmission", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeSubmission - Failed Submissions Per Second", objItem.FailedSubmissionsPerSecond)
  'WScript.Echo formJSON("MSExchangeSubmission - Temporary Submission Failures Per Second", objItem.TemporarySubmissionFailuresPersec)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeReplication_MSExchangeReplication WHERE Name = ""_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'For Each objItem In colItems
  'WScript.Echo formJSON("MSExchangeReplication - Copy Queue Length", objItem.CopyQueueLength)
  'WScript.Echo formJSON("MSExchangeReplication - Replay Queue Length", objItem.ReplayQueueLength)
'Next

'Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeDeliverySmtpAvailability_MSExchangeDeliverySmtpAvailability = ""default mailbox delivery""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
'"default mailbox delivery" is not required in WQL, but is required in when getting the counter on Powershell script.
Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeDeliverySmtpAvailability_MSExchangeDeliverySmtpAvailability", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
For Each objItem in colItems
  WScript.Echo formJSON("MSExchange Delivery SmtpAvailability - % Availability", objItem.PercentAvailability)
  WScript.Echo formJSON("MSExchange Delivery SmtpAvailability - % Failures Due To Back Pressure", objItem.PercentFailuresDueToBackPressure)
  WScript.Echo formJSON("MSExchange Delivery SmtpAvailability - % Failures Due To TLS Errors", objItem.PercentFailuresDueToTLSErrors)
  WScript.Echo formJSON("MSExchange Delivery SmtpAvailability - Failures Due to Maximum Local Loop Count", objItem.FailuresDuetoMaximumLocalLoopCount)
Next

Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeRpcClientAccess_MSExchangeRpcClientAccess", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
For Each objItem in colItems
  WScript.Echo formJSON("MSExchange RpcClientAccess - Connection Count", objItem.ConnectionCount)
  WScript.Echo formJSON("MSExchange RpcClientAccess - Active User Count", objItem.ActiveUserCount)
  WScript.Echo formJSON("MSExchange RpcClientAccess - User Count", objItem.UserCount)
  WScript.Echo formJSON("MSExchange RpcClientAccess - RPC Dispatch Task Queue Length", objItem.RPCdispatchtaskqueuelength)
  WScript.Echo formJSON("MSExchange RpcClientAccess - XTC Dispatch Task Queue Length", objItem.XTCdispatchtaskqueuelength)
  WScript.Echo formJSON("MSExchange RpcClientAccess - RPCHttpConnectionRegistration Dispatch Task Queue Length", objItem.RpcHttpConnectionRegistrationdispatchtaskqueuelength)
Next

Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_PerfFormattedData_MSExchangeTransportQueues_MSExchangeTransportQueues WHERE Name = ""_total""", "WQL", wbemFlagReturnImmediately + wbemFlagForwardOnly)
For Each objItem In colItems
  WScript.Echo formJSON("MSExchangeTransport Queues - Active Mailbox Delivery Queue Length", objItem.ActiveMailboxDeliveryQueueLength)
  WScript.Echo formJSON("MSExchangeTransport Queues - Submission Queue Length", objItem.SubmissionQueueLength)
  WScript.Echo formJSON("MSExchangeTransport Queues - Active Non-Smtp Delivery Queue Length", objItem.ActiveNonSmtpDeliveryQueueLength)
  WScript.Echo formJSON("MSExchangeTransport Queues - Retry Mailbox Delivery Queue Length", objItem.RetryMailboxDeliveryQueueLength)
  WScript.Echo formJSON("MSExchangeTransport Queues - Unreachable Queue Length", objItem.UnreachableQueueLength)
  WScript.Echo formJSON("MSExchangeTransport Queues - Poison Queue Length", objItem.PoisonQueueLength)
  WScript.Echo formJSON("MSExchangeTransport Queues - Messages Queued for Delivery Per Second", objItem.MessagesQueuedforDeliveryPerSecond)
  'WScript.Echo formJSON("MSExchangeTransportQueues - Messages Completed Delivery Per Second", objItem.MessagesCompletedDeliveryPerSecond)
  'WScript.Echo formJSON("MSExchangeTransportQueues - Messages Submitted Per Second", objItem.MessagesSubmittedPerSecond)
  WScript.Echo formEndLineJSON("MSExchangeTransport Queues - Retry Non-SMTP Delivery Queue Length", objItem.RetryNonSmtpDeliveryQueueLength)
Next
WScript.Echo "}}"

'End of outputing JSON.