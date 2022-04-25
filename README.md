<!-- 

ZotDB, is constantly improving opensource project which tries to provide all functionalities same as firebase RTDB.
the ZotDB server can be hosted anywhere on any device. 

It's best tool for people having a store, can use this and
don't have to pay for any bandwidth.
-->

ZotDB, is constantly improving opensource project which tries to provide all functionalities same as firebase RTDB.
the ZotDB server can be hosted anywhere on any device.

It's best tool for people having a store, can use this and
don't have to pay for any bandwidth.

## Features
***
One can host the server on any device, and can create as many servers as they want just from a single file.
![](https://github.com/ssddcodes/ssddrtdb/blob/flutterlib/demo.gif)
## Getting started
***
The Main class is ZotDB, to initiate the class please use init() method.

## Usage
***
 
Example available at `/test` folder. 

To initiate:- 

```dart
  ZotDB zotDB = ZotDB.init("ws://<serverip>:<port>/<dbid>");
//dbid is the root name of your json, for more details please see server logs
```

To navigate to some path:- 

```dart
//there are server ways to navigate through your db like:- 
ZotDB zotDB = ZotDB.init("ws://<serverip>/dbid").child("abc/xyz/xyz1");

// or
zotDB.child("abc/xyz/xyz1");
// or 
zotDB.child("abc").child("xyz").child("xyz1");

//note that the above path reference will be reset everytime
// you called method to listen values or set/update values.
```

To Listen to Single Value:- 

```dart
// snap returns an object, which you can cast to Array, String, 
// etc according to your need.
  zotDB.addSingleValueEventListener((snap) {
    print(snap);
  });
```

To update some value:- 
```dart
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
```
To remove Value:-
```dart
zotDB.child("abc/xyz/xyz1").removeValue();
//this will remove value at 'abc/xyz/xyz1'
```

To get unique key:- 
```dart
String uid = zotDB.getPushKey();
//please check test folder for more details
```

To listen to multiple values please checkout example at `/test`

## Additional information
***
This is licenced under GNU (GENERAL PUBLIC LICENSE) 3.0

> RTDB is licensed under the [GNU General Public License v3.0](./LICENSE.md)  
> Permissions of this strong copyleft license are conditioned on making  
> available complete source code of licensed works and modifications,  
> which include larger works using a licensed work, under the same  
> license. Copyright and license notices must be preserved. Contributors  
> provide an express grant of patent rights.
