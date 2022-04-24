library zotdb_flutter;

import 'dart:convert';
import 'package:json_path/json_path.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:zotdb_flutter/path_gen.dart';

typedef ValueChange<T> = void Function(T snap);

class ZotDB {
/*  ValueChange<dynamic>? _singleVal;
  ValueChange<dynamic>? _Val;*/

  Map eventInsts = {};

  List<String> _children = [];

  WebSocketChannel _channel;
  String _vfier = "", _childrenHolder = "";
  final String _ssddKeepAlive = "keep-alive",
      _id = "id",
      _single = "single",
      _nsv = "nsv",
      _sv = "sv",
      _message = "message",
      _path = "path",
      _dbid = "dbid",
      _dbidx,
      _reqid = "reqid";

  ZotDB(this._channel, String url, this._dbidx) {
    start();
  }

  static ZotDB init(String url) {
    var _channel = WebSocketChannel.connect(Uri.parse(url));
    List<String> strings = url.split("/");
    String dbid = strings[strings.length - 1];
    return ZotDB(_channel, url, dbid);
  }

  void start() {
    _channel.stream.listen((msg) {
      String msgx = msg.toString();
      if (msgx != _vfier) {
        _vfier = msgx;

        if (msgx != _ssddKeepAlive) {
          var object = jsonDecode(msgx);
          if (object[_id] == _nsv) {
            List<String> z = [];
            final arr = JsonPath(r'$.*');
            arr.read(object[_message])
                .map((match) => '${match.pointer}:\t${match.value}')
                .forEach((element) {
              z.add(jsonEncode(element));
            });
            eventInsts[object[_reqid]]!(z);
          } else if (object[_id] == _single) {
            eventInsts[object[_reqid]]!(object[_message]);
          }
        }
      }
    });
  }

  ///used to listen single value, however you can query for multiple values and filter manually, it returns list of JSON

  void addSingleValueEventListener(ValueChange<dynamic> single) {
    String genReqId =
        PathGen.generatePushChildName(DateTime.now().millisecondsSinceEpoch);
    eventInsts[genReqId] = single;
    Map tmp = {};
    tmp[_id] = _single;
    tmp[_path] = _childrenHolder;
    _childrenHolder = "";
    _children = [];
    tmp[_dbid] = _dbidx;
    tmp[_reqid] = genReqId;
    _channel.sink.add(jsonEncode(tmp));
  }

  ///used to listen multiple values, it returns list of JSON Objects

  void addValueEventListener(ValueChange<dynamic> valueChange) {
    String genReqId =
        PathGen.generatePushChildName(DateTime.now().millisecondsSinceEpoch);
    eventInsts[genReqId] = valueChange;
    Map tmp = {};
    tmp[_id] = _nsv;
    tmp[_path] = _childrenHolder;
    _childrenHolder = "";
    _children = [];
    tmp[_reqid] = genReqId;
    tmp[_dbid] = _dbidx;
    _channel.sink.add(jsonEncode(tmp));
  }

  ///child() method is used to pass location of something in json, and path can either be used as:-
  ///zotDB.child("foo/abc") or zotDB.child("foo").child("abc")
  ZotDB child(String path) {
    List<String>? tmpSplitContainer;
    if (path.contains("/")) {
      tmpSplitContainer = path.split("/");
      _children.addAll(tmpSplitContainer);
      StringBuffer buffer = StringBuffer();
      for (var x in _children) {
        buffer.write(x);
        buffer.write("/");
      }
      _childrenHolder = buffer.toString();
    } else {
      if (_childrenHolder.endsWith("/")) {
        _childrenHolder = _childrenHolder + path;
      } else {
        _childrenHolder = _childrenHolder + "/" + path;
      }
    }
    return this;
  }

  ///to update Value in the DB you can use setValue() method

  void setValue(dynamic value) {
    Map tmp = {};
    tmp[_id] = _sv;
    tmp[_path] = _childrenHolder;
    _childrenHolder = "";
    _children = [];
    tmp[_dbid] = _dbidx;
    tmp[_message] = value.toString();
    _channel.sink.add(jsonEncode(tmp));
  }

  ///this adds unique identifier for between the path

  ZotDB push(){
    String path = PathGen.generatePushChildName(DateTime.now().millisecondsSinceEpoch);
    if (_childrenHolder.endsWith("/")) {
      _childrenHolder = _childrenHolder + path;
    } else {
      _childrenHolder = _childrenHolder + "/" + path;
    }
    return this;
  }

  String getPushKey(){
    return PathGen.generatePushChildName(DateTime.now().millisecondsSinceEpoch);
  }

}
