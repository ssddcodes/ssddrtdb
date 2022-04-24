import dev.ssdd.rtdb.*;
import dev.ssdd.rtdb.json.JSONObject;
import org.jetbrains.annotations.Nullable;

public class Main {
    public static void main(String[] args) {

        /**
         * let's say we have json like:-
         */
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

        /**
         * TO Init just call static init() method and pass DB url.
         * child() method is used to pass location of something in json, and path can either be used as:-
         * zotDB.child("foo/abc") or zotDB.child("foo").child("abc")
         */
        ZotDB zotDB = ZotDB.instance("ws://localhost:19195/ssdd").child("abc/xyz/xyz1");

        /**
         * used to listen to single value.
         * the output should be "ayo"
         */
        zotDB.addSingleValueEventListener(new SingleValueEventListener() {
            @Override
            public void onDataChange(@Nullable Object data) {
                System.out.println(data);
            }
        });

        /**
         * to update Value in the DB you can use setValue() method
         * now the value should be updated from "ayo" to "hie" in DB as well as in your app.
         */
        zotDB.child("abc/xyz/xyz1").setValue("hie");

        /**
         * adds numbers 0-4 with a unique identifier.
         */
        for(int x=0; x<5; x++){
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

        /**
         * this is used to listen to miltiple values, it returns list of JSON
         * the Snapshot class is same as JSONObject class, u can treate it as an JSONObject.
         * Snapshot has a method getParentKey(), which can be used to get parent:-
         * say from the above JSON, the snapshot value is "0" right now, so getParentKey() will return "-N0Oq1dNq3IBUkqVVCYd"
         */

        zotDB.child("abc/xyz/xyz1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshots snapshots) {
                if(snapshots != null){
                    for (Snapshot s : snapshots.getValue()) {
                        System.out.println(s);
                    }
                }
            }
        });

        /**
         * Removes value at the child reference
         */
        zotDB.child("abc/xyz/xyz1").removeValue();

        for (int i = 0; i < 5; i++) {
            String pushKey = zotDB.generatePushkey();
            zotDB.child("abc/xyz/xyz1").child(pushKey).setValue(new JSONObject().put("key", pushKey).put("val", i));
        }

        /**
         * You can also sterilize a Model class and get back list of model class.
         */

        zotDB.child("abc/xyz/xyz1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshots snapshots) {
                assert snapshots != null;
                for (Model s : snapshots.getValue(Model.class)) {
                    System.out.println("Key: "+ s.getKey() + " Value: "+ s.getVal());
                }
            }
        });

    }
}