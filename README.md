# SSDD Realtime Database (ZotDB)

ZotDB provides you realtime Database service on your own VPS/pc.

|   - | -                                           |
|----:|:--------------------------------------------|
|  📩 | [Download](#Implement)                      |
|  📋 | [AvailableParameters](#AvailableParameters) |
|  💻 | [Implementation](#Implementation.)          |
|  🧾 | [Changelog](#Changelog)                     |
|  🏆 | [Credits](#Credits)                         |
|  ⚖️ | [License](#License)                         |

## Implement

### *Gradle*

```groovy
 implementation 'dev.ssdd:rtdb:2.6'
```
### *Maven*

```xml
<dependency>
  <groupId>dev.ssdd</groupId>
  <artifactId>rtdb</artifactId>
  <version>2.6</version>
</dependency>
```

## Implement the Server.

Please checkout [server branch](https://github.com/ssddcodes/ssddrtdb/tree/server)

## Info.

## AvailableParameters

| Methods/params                |                                    uses                                    |
|-------------------------------|:--------------------------------------------------------------------------:|
| setValue()                    |                    used to push an object to database.                     |
| addValueEventListener()       |               used to query and listen to *multiple* values                |
| addSingleValueEventListener() |             used to query and listen to *single* value object              |
| child()                       |                 it's used to refer a path in the database                  |
| push()                        | used to generate a unique keyID for the database and add to path referance |
| getPushKey()                  |                        returns string of unique id                         |

## Implementation.

* To initiate:-

```java
  ZotDB zotDB = ZotDB.init("ws://<serverip>:<port>/<dbid>");
//dbid is the root name of your json, for more details please see server logs
```

* To navigate to some path:-

```java
//there are server ways to navigate through your db like:- 
ZotDB zotDB = ZotDB.init("ws://<serverip>/dbid").child("abc/xyz/xyz1");

// or
zotDB.child("abc/xyz/xyz1");
// or 
zotDB.child("abc").child("xyz").child("xyz1");

//note that the above path reference will be reset everytime
// you called method to listen values or set/update values.
```

* To Listen to Single Value:-

```java
zotDB.child("abc/xyz/xyz1").addSingleValueEventListener(new SingleValueEventListener() {
            @Override
            public void onDataChange(@Nullable Object data) {
                System.out.println(data);
            }
        });
```

* To add/update value to the database:-

```java
//let's say we have JSON:- 
//{
//
//     "ssdd": {
//         "abc": {
//             "xyz": {
//                 "xyz1": "ayo"
//             }
//         }
//     }
//
// }

//so setValue will set (or update value if exists) some value to "hie".
  zotDB.child("abc/xyz/xyz1").setValue("hie");
//in this case the json will become:- 
//{
//
//     "ssdd": {
//         "abc": {
//             "xyz": {
//                 "xyz1": "hie"
//             }
//         }
//     }
//
// }

//for more info please chekc test/java folder

```

* To listen to multiple values

```java
//please check out the folder test/java
```

For more info please checkout [Example](https://github.com/ssddcodes/ssddrtdb/tree/server/test/java/Main.java)

# Demo

![Demo](https://github.com/ssddcodes/ssddrtdb/blob/flutterlib/demo.gif)

## Changelog
* v2.6(dependency) :- major update and stable release 
* v2.6(server):- major update with feature to add multiple servers in the same server file.
* V-1.0.4-BETA:- Public beta, extended support for all java projects. 
* V-1.0.3-BETA:- Internal Tests.
* V-1.0.2-BETA:- Internal Tests.
* V-1.0.1-BETA:- Major bug fixes. 
* V-1.0.1:- Major bug fixes.
## Credits
> [Sandip](https://github.com/ssddcodes):- Creator, Developer of SSDDRTDB.
> 
> [Yamin](https://github.com/yamin8000):- Publishing and management.

## License

> RTDB is licensed under the [GNU General Public License v3.0](./LICENSE.md)  
> Permissions of this strong copyleft license are conditioned on making  
> available complete source code of licensed works and modifications,  
> which include larger works using a licensed work, under the same  
> license. Copyright and license notices must be preserved. Contributors  
> provide an express grant of patent rights.
