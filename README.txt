**FUNCTIONALITY CHECKLIST**
	BASIC FUNCTIONALITY
		- DONE: Text sending works - please send text strings instead of fully formatted XML (see below for bonus)
		- DONE: client, Atom server and content server processes start up and communicate
		- DONE: PUT operation works for one content server
		- DONE: GET operation works for many read clients
		- DONE: Atom server expired feeds works (12s)
		- DONE: Retry on errors (server not available etc) works
	FULL FUNCTIONALITY
		- DONE: Lamport clocks are implemented
		- DONE: All error codes are implemented
		- DONE: Content servers are replicated and fault tolerant 
	BONUS FUNCTIONALITY
		- INCOMPLETE: XML Parsing

**INFORMATION ON IMPLEMENTATION**
	-> Aggregation Server
		- Lamport Clock: A lamport clock is initialised on the AS in the beginning of the program. The servers clock is incremented following any significant event.
		- Restoring Content: Upon restart, the server will restore the content contained in local file "Atom.txt". The server will allow a one minute window for content servers to reconnect and claim connection to their respective entries. If the respective contents connection is not restored within 60 seconds, it will be removed and the aggregate feed will be updated accordingly.
		- Request Handler: The server is always open for requests. This section of code reads the request, validates it, and calls the respective requests function (either PUT or GET). In the case of an invalid request, a response code is sent to the client. Before the request is processed, the lamport clock time is incremented & the process is sent to the request scheduler.
		- Request Scheduler: Before the critical section of code is accessed, requests are stored in a priority queue according to their arrival time, maintained through use of lamport clocks. When the critical section is free, the process in the front of the queue (with the earliest lamport clock arrival time) is allowed access. Upon completion of the critical section, the process sets the class static variable to free and the next process is allowed in.
		- PUT Request: Input is checked for content validity - if title, link or id are missing/invalid, error codes are sent in response. A temporary vector of this requests entries is created and sent to the feed handler.
		- Feed Handler: Uploads this requests entries to the servers static vector of entries. Duplicate entries are ignored. Upon validation of correct format, a success response code is sent (200 or 201). The updated static entries vector is formatted and uploaded to local file "Atom.txt". A heartbeat check is then maintained for this content server.
		- Heartbeat: The heartbeat check on the AS side involves sleeping for 12 seconds, and then attempting to read the CS response (if it exists). If the CS had not sent a heartbeat response within the last 12 seconds, the respective process entry/s are removed from the feed.
		- GET Request: "Atom.txt" is read. If it is empty a "204 - No Content" response is sent. If there exists content, it is then sent to the client along with a "200 - OK" response code. 
	-> Content Server
		- The content server reads the input text file given in its parameters and attempts to send this content to the aggregate server through a PUT request. The content server will read the servers response, and if valid, will send heartbeat signals to the server every 12 seconds. The content servers implementation allows for replication and fault tolerance. If the AS is not available, the content server will repeatedly attempt to connect in 5 second intervals until manually stopped. If the content server is connected to the AS and the AS crashes/restarts, the content server will automatically loop back and repeatedly attempt to reconnect. This implementation, in combination with the AS implementation, will assure that the aggregate feed is persistent.
	-> Client Server
		- The client server will check its input parameters to validate a successful call. The client will attempt to reconnect to the AS, and if this connection is unavailable, it will repeatedly attempt to connect in 5 second intervals. Once a connection is sustained, a GET request is sent, and the content is sent and printed.


**INSTRUCTIONS FOR RUNNING**
	javac *.java
	java AggregationServer
	java ContentServer localhost:4567 fileName
	java GETClient localhost:4567
	-> input.txt & input1.txt are available examples of input atom feeds that are used for this system

***TESTING***
	- Automated testing for this distributed system is completed in bash
	- Instructions for running:
		-> chmod +x testCases.sh testComparison.sh testComparisonCS.sh
		-> ./testCases.sh
	- Test cases & their descriptions are listed in the testCases.sh file. The tests are sorted into categories (basic flow, response codes, heartbeat, persistence, replication etc.)
	- Tests are run and, depending on whether the content server output or the client server output is analysed, a corresponding bash function is called that performs a diff operation with the expected outputs.
	- Expected outputs are accessible in EO1-14.txt and are referenced before each test to allow for easy viewership if required.

**CHANGES FOLLOWING REVISION 1 FEEDBACK**
	- see Changes.txt
