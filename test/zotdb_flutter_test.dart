import 'package:zotdb_flutter/zotdb_flutter.dart';

void main() {
  ///let's say we have a JSON like:-
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

  ///TO Init just call static init() method and pass DB url.
  ///child() method is used to pass location of something in json, and path can either be used as:-
  ///zotDB.child("foo/abc") or zotDB.child("foo").child("abc")

  ZotDB zotDB = ZotDB.init("ws://localhost:19195/ssdd").child("abc/xyz/xyz1");

  /// used to listen to single value.
  /// the output should be "ayo"
  zotDB.addSingleValueEventListener((snap) {
    print(snap);
  });

  /// to update Value in the DB you can use setValue() method
  /// now the value should be updated from "ayo" to "hie" in DB as well as in your app.
  zotDB.child("abc/xyz/xyz1").setValue("hie");

  /// adds numbers 0-4 with a unique identifier.
  for (int x = 0; x < 5; x++) {
    zotDB.child("abc/xyz/xyz1").push().setValue(x);
    // alternatively you can use getPushKey() method which returns unique id (String) like:-
    //zotDB.child("abc/xyz/xyz1").child(getPushKey()).setValue(x);
  }
  // now json looks like {
  //
  //     "ssdd": {
  //         "abc": {
  //             "xyz": {
  //                 "xyz1": {
  //                     "-N0Oq1dNq3IBUkqVVCYd": "0",
  //                     "-N0Oq1dNq3IBUkqVVCYe": "1",
  //                     "-N0Oq1dNq3IBUkqVVCYf": "2",
  //                     "-N0Oq1dNq3IBUkqVVCYg": "3",
  //                     "-N0Oq1dNq3IBUkqVVCYh": "4"
  //                 }
  //             }
  //         }
  //     }
  //
  // }

  /// this is used to listen to miltiple values, it returns list of JSON
  zotDB.child("abc/xyz/xyz1").addValueEventListener((snap) {
    print('Val eve $snap');
  });
}
