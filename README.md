# ssddrtdb
This is SSDD's Realtime database. Which provides you realtime Database service on your **own** VPS/pc.

___Implimentation___

*Gradle*

```
 implementation 'io.github.ssddcodes:rtdb:1.0.1'
```

*Maven*

```
<dependency>
  <groupId>io.github.ssddcodes</groupId>
  <artifactId>rtdb</artifactId>
  <version>1.0.1</version>
  <type>aar</type>
</dependency>
```

1. Implement the **Server**.
____Version 1.*___
V1 is beta channel for the SSDD RTDB.
> Server Implementation would be generated soon.

____Version 1.* +___
Any version except 1.* Would be stable release.
> Server Implementation would be generated soon.

2. Connect to the server.

> Connection method might be changed after stable release (i.e. 1.*+).

3. Info.

```
private SSDD ssdd = new SSDD("yourServerIp/location", "wss"); //TODO server see method 1 to setup server.
```
Available parameters.

| **Methods/params** | **uses** |
| ------------------ | :------: |
| setValue()   | used to push object to database. |
| addValueEventListener() |  used to query and listen to *multiple* values |
| addSingleValueEventListener() | used to query and listen to *single* value object |
| child() | it's used to refer a path in the database |
| children | it's list or children which was created by child(), used to clear the list if needed |
| push() | used to generate a unique keyID for the database. |

4. Implementation.

** Would be uploaded soon.**

