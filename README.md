# ZotDB Server

## Setup
***
### Pre requisites:- 
* Download JDK 8 or above 
* set path reference to the java file
* a secure device which no one else have access to

### Setup:-

* Download the latest Jar/binary file from [here](https://github.com/ssddcodes/ssddrtdb/tags)

**Jar setup**
* Download the file and to run it make sure that no other app is running on port `19194`.
* execute the command `java -jar zotdb_server.jar`
* it will ask you to create password for the database
* after entering the password it should ask for `root name` for the db, which is basically starting point for your json
```json
{
  "rootName": {}
}
```
* select the port for the database server, let's say port `8080`, so the db server would be created and will start serving at
`ws://<ip>:8080/rootName`
* to create, launch, kill database server you can visit `http://<ip>:19194/index.html`

### Security:-

* We recommend you to operate the server in some secure workspace.
* Always use WSS connections, for that you can setup nginx and proxy pass it.
* Setup some Client Authentication by HTTP for example make http request to nginx and somehow authenticate request then upgrade connection to ws.

## Changelog:- 
* With version 2.6.1, you can pass password to the jar file from a text file by:- 
`java -jar zotdb_server -pw /path/to/txt/file.txt`