# Sameple JSON format output. 
# 
# {
#   "hostname": "%COMPUTERNAME%",
#   "plugin": "Windows Exchange 2013 Agent side rexec plugin",
#   "version": "7.3.0",
#   "currentTime": "%DATE:~-4%-%DATE:~3,2%-%DATE:~0,2%T%TIME:~0,8%",
#   "status": "200",
#   "result": {
#      "SMTPBytesSentPerSecond": "128", 
#      "SMTPBytesReceivedPerSecond": "256",
#      "SMTPMessagesSentPerSecond": "512",
#      "SMTPInboundMessagesReceivedPerSecond": "640",
#      "SMTPAverageBytesPerInboundMessage": "768",
#      "SMTPInboundConnections": "896",
#      "SMTPOutboundConnections": "1024",
#      "CurrentWebmailUsers": "5",
#      "WebmailUserLogonsPerSecond": "10",
#      "RPCAveragedLatency": "15",
#      "RPCOperationsPerSecond": "20",
#      "RPCRequests": "25",
#      .......
#      .......
#   }
# }

#Each counter path has the following format:
#               "[\\<ComputerName>]\<CounterSet>(<Instance>)\<CounterName>"
Function Get-ExchangeCounter ($computerNameInput, $counterSet, $instanceName, $counterName) {
    $cookedValue =  (Get-Counter "\\$computerNameInput\$counterSet($instanceName)\$counterName").CounterSamples.CookedValue
    Write-Host """$counterSet - $counterName"" : ""$cookedValue"","
}

Function Get-ExchangeCounterNoInstanceName ($computerNameInput, $counterSet, $counterName) {
    $cookedValue =  (Get-Counter "\\$computerNameInput\$counterSet\$counterName").CounterSamples.CookedValue
    Write-Host """$counterSet - $counterName"" : ""$cookedValue"","
}

Function Get-ExchangeCounterEndLine ($computerNameInput, $counterSet, $instanceName, $counterName) {
    $cookedValue =  (Get-Counter "\\$computerNameInput\$counterSet($instanceName)\$counterName").CounterSamples.CookedValue
    Write-Host """$counterSet - $counterName"" : ""$cookedValue"""
}

#### ComputerName ####
$computerName = get-content env:computername
$curDate = Get-Date

# Useful cmdlets to get counters
# (Get-Counter -ListSet "MSExchange Calendar *").paths : This will show all available CounterName for "MSExchange Calendar *"
# Get-Counter "\MSExchange Database ==> Instances(*)\I/O Log Writes/sec" : This will show avaialbe InstanceName for "\MSExchange Database ==> Instances(*)\I/O Log Writes/sec"

#### CounterSet constants ####
#$MSExchangeISClientType = "MSExchangeIS Client Type"
#$MSExchangeDatabase = "MSExchange Database"
$MSExchangeAssistants_PerDatabase = "MSExchange Assistants - Per Database"
#$MSExchangeDatabaseInstances = "MSExchange Database ==> Instances"
#$MSExchangeTransportSMTPReceive = "MSExchangeTransport SMTPReceive"
#$MSExchangeTransportSmtpSend = "MSExchangeTransport SmtpSend"
#$MSExchangeResourceBooking = "MSExchange Resource Booking"
#$MSExchangeCalendarAttendant = "MSExchange Calendar Attendant"
#$MSExchangeStoreInterface = "MSExchange Store Interface"
#$MSExchangeSubmission = "MSExchange Submission"
#$MSExchangeReplication = "MSExchange Replication"
$MSExchangeDeliverySmtpAvailability = "MSExchange Delivery SmtpAvailability"
$MSExchangeRpcClientAccess = "MSExchange RpcClientAccess"
$MSExchangeTransportQueues = "MSExchangeTransport Queues"


#### InstanceName constants ####
$_total = "_total"
$informationStore = "information store"
$msExchangeMailboxAssistantsTotal = "msexchangemailboxassistants-total"
$informationStoreTotal = "information store/_total"
$edgetransportTotal = "edgetransport/_total"
$defaultMailboxDelivery = "default mailbox delivery"
$totalHigh = "total - high"

echo "{ ""computer"" : ""$computerName"","
Write-Host """plugin"" : ""Windows Exchange 2013 Agent side rexec plugin"","
Write-Host """version"" : ""7.3.0"","
Write-Host """currentTime"" : ""$curDate"","
Write-Host """status"" : ""200"","
Write-Host """result"" : {"

#### CounterName constants ####
# MSExchangeISClientType :
#$items = @("RPC Average Latency")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeISClientType $_total $item
#}

# ESE_MSExchangeDatabase :
#$items = @("Database Page Faults/sec",
#    "Database Cache % Hit",
#    "Log Record Stalls/sec",
#    "Log Threads Waiting",
#    "I/O Database Reads (Attached) Average Latency",
#    "I/O Database Writes (Attached) Average Latency",
#    "I/O Log Writes Average Latency",
#    "I/O Database Reads (Recovery) Average Latency",
#    "I/O Database Writes (Recovery) Average Latency",
#    "I/O Log Reads Average Latency",
#    "Version Buckets Allocated",
#    "Database Cache Size (MB)",
#    "Log Bytes Write/sec")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeDatabase $informationStore $item
#}

# MSExchangeAssistantsPerDatabase :
$items = @("Events In Queue")
foreach ($item in $items) {
    Get-ExchangeCounter $computerName $MSExchangeAssistants_PerDatabase $msExchangeMailboxAssistantsTotal $item
}

# MSExchange Database ==> Instances (InstanceName = "information store/_total")
#$items = @("Log Generation Checkpoint Depth",
#    "I/O Database Reads Average Latency",
#    "I/O Database Writes Average Latency")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeDatabaseInstances $informationStoreTotal $item
#}

# MSExchange Database ==> Instances (InstanceName = "edgetransport/_total")
#$items = @("I/O Log Writes/sec",
#    "I/O Log Reads/sec",
#    "Log Generation Checkpoint Depth",
#    "Version buckets allocated",
#    "I/O Database Reads/sec",
#    "I/O Database Writes/sec",
#    "Log Record Stalls/sec",
#    "Log Threads Waiting")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeDatabaseInstances $edgetransportTotal $item
#}

# MSExchangeTransportSMTPReceive
#$items = @("Average bytes/message",
#    "Messages Received/sec")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeTransportSMTPReceive $_total $item
#}

# MSExchangeTransport SmtpSend
#$items = @("Messages Sent/sec")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeTransportSmtpSend $_total $item
#}

# MSExchange Resource Booking
#$items = @("Average Resource Booking Processing Time",
#    "Requests Failed")
#foreach ($item in $items) {
#    Get-ExchangeCounterNoInstanceName $computerName $MSExchangeResourceBooking $item
#}

# MSExchange Calendar Attendant
#$items = @("Average Calendar Attendant Processing Time",
#    "Requests Failed")
#foreach ($item in $items) {
#    Get-ExchangeCounterNoInstanceName $computerName $MSExchangeCalendarAttendant $item
#}

# MSExchange Store Interface
#$items = @("ROP Requests outstanding",
#    "RPC Requests outstanding",
#    "RPC Requests sent/sec",
#    "RPC Slow requests latency average (msec)",
#    "RPC Requests failed (%)",
#    "RPC Slow requests (%)")   
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeStoreInterface $_total $item
#}

# MSExchange Submission
#$items = @("Failed Submissions Per Second",
#    "Temporary Submission Failures/sec")
#foreach ($item in $items) {
#    Get-ExchangeCounterNoInstanceName $computerName $MSExchangeSubmission $item
#}

# MSExchange Replication
#$items = @("CopyQueueLength",
#    "ReplayQueueLength")
#foreach ($item in $items) {
#    Get-ExchangeCounter $computerName $MSExchangeReplication $_total $item
#}

# MSExchange Delivery SmtpAvailability
$items = @("% Availability",
    "% Failures Due To Back Pressure",
    "% Failures Due To TLS Errors",
    "Failures Due to Maximum Local Loop Count")
foreach ($item in $items) {
    Get-ExchangeCounter $computerName $MSExchangeDeliverySmtpAvailability $defaultMailboxDelivery $item
}

# MSExchange RpcClientAccess
$items = @("Connection Count",
    "Active User Count",
    "User Count",
    "RPC Dispatch Task Queue Length",
    "XTC Dispatch Task Queue Length",
    "RPCHttpConnectionRegistration Dispatch Task Queue Length")
foreach ($item in $items) {
    Get-ExchangeCounterNoInstanceName $computerName $MSExchangeRpcClientAccess $item
}

# MSExchangeTransport Queues
$items = @("Active Mailbox Delivery Queue Length",
    "Submission Queue Length",
    "Active Non-Smtp Delivery Queue Length",
    "Retry Mailbox Delivery Queue Length",
    "Unreachable Queue Length",
    "Poison Queue Length",
    "Messages Queued for Delivery Per Second",    
    "Retry Non-SMTP Delivery Queue Length")
foreach ($item in $items) {
    if($items[-1] -ne $item) {
        Get-ExchangeCounter $computerName $MSExchangeTransportQueues $_total $item
    } else {
        Get-ExchangeCounterEndLine $computerName $MSExchangeTransportQueues $_total $item
    }    
}
Write-Host "}}"