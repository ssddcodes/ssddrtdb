# SSDD Realtime Database
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

1. Implement the Server.

___Version 1.*___

V1 is beta channel for the SSDD RTDB.
> Server Implementation would be uploaded soon.

___Version 2.* +___

Any version except 1.* Would be stable release.

> Server Implementation would be uploaded soon.

2. Info.

Available parameters.

| **Methods/params** | **uses** |
| ------------------ | :------: |
| setValue()   | used to push an object to database. |
| addValueEventListener() |  used to query and listen to *multiple* values |
| addSingleValueEventListener() | used to query and listen to *single* value object |
| child() | it's used to refer a path in the database |
| children | it's list of children which was created by child(), used to clear the list if needed |
| push() | used to generate a unique keyID for the database. |

3. Implementation.

*initiate connection:-

```
SSDD ssdd = new SSDD("yourServerIp/location", "wss"); //TODO to setup server see method 1.
```

*to nevigate to specific location:-
```
ssdd.child("path/to/success");
```

*to add/set value to the database:-

```
ssdd.setValue("one step closer to success."); 
// you can use push() method to generate unique id.
```
**OR**
```
//you can even set children location just before setting values.

ssdd.child("path/to/success").setValue("one step closer to success.");
```
**OR**
```
ssdd.child("path").child("to").child("success").setValue("one step closer to success.");
```

*to listen to a single value:- 
```
ssdd.addSingleValueEventListener(new SingleValueEventListener() {
                @Override
                public void onDataChange(@Nullable Object data) {
                //TODO don't forget to runOnUiThread while setting text to textviews or assigning adapter to recycler view or making toast.
                    runOnUiThread(() -> {
                        if(data != null){
                        textView.setText(data.toString());
                        }
                    });
                }

                @Override
                public void onError(@Nullable Exception e) {

                }
            });
   ```
*to listen to multiple values
```
List<Model> models = new ArrayList<>();

            ssdd.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@Nullable List<DataSnapshot> dataSnapshots) {
                    if(dataSnapshots != null){
                        for (DataSnapshot d: dataSnapshots) {
                            models.add(d.getValue(Model.class));
                        }
                    }

                    runOnUiThread(()->{
                        adapter = new Adapter(models);
                        recyclerView.setAdapter(adapter);
                    });

                }

                @Override
                public void onError(@Nullable JSONException e) {

                }
            });
```
 

**More information will be uploaded soon.**

