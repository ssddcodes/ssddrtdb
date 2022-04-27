package dev.ssdd.sms;

import dev.ssdd.rtdb.WebSocket;
import dev.ssdd.ws.ZotWS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static dev.ssdd.sms.Insts.insts;

public class SmsMain {
    public SmsMain main(String dbid, int port) {
        File htmfil, dbfile, dist,distx;

        htmfil = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + dbid + File.separator + "index.html");
        dbfile = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + dbid + File.separator + "db.json");
        dist = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + dbid + File.separator + "dist" + File.separator + "jquery.json-editor.min.js");
        distx = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + dbid + File.separator  + "style.css");
//        credfile = new File(System.getProperty("user.home") + File.separator + ".ssddrtdb" + File.separator + dbid + "creds.json");

        String updurl = "ws://localhost:"+ port +"/";


        String distjs = "!function () {\n" +
                "    var e = '/* Syntax highlighting for JSON objects */ .json-editor-blackbord {   background: #1c2833;   color: #fff;   font-size: 13px;   font-family: Menlo,Monaco,Consolas,\"Courier New\",monospace; } @media screen and (min-width: 1600px) {   .json-editor-blackbord {     font-size: 14px;   } }  ul.json-dict, ol.json-array {   list-style-type: none;   margin: 0 0 0 1px;   border-left: 1px dotted #525252;   padding-left: 2em; } .json-string {   /*color: #0B7500;*/   /*color: #BCCB86;*/   color: #0ad161; } .json-literal {   /*color: #1A01CC;*/   /*font-weight: bold;*/   color: #ff8c00; } .json-url {   color: #1e90ff; } .json-property {   color: #4fdee5;   line-height: 160%;   font-weight: 500; }  /* Toggle button */ a.json-toggle {   position: relative;   color: inherit;   text-decoration: none;   cursor: pointer; } a.json-toggle:focus {   outline: none; } a.json-toggle:before {   color: #aaa;   content: \"\\\\25BC\"; /* down arrow */   position: absolute;   display: inline-block;   width: 1em;   left: -1em; } a.json-toggle.collapsed:before {   transform: rotate(-90deg); /* Use rotated down arrow, prevents right arrow appearing smaller than down arrow in some browsers */   -ms-transform: rotate(-90deg);   -webkit-transform: rotate(-90deg); }   /* Collapsable placeholder links */ a.json-placeholder {   color: #aaa;   padding: 0 1em;   text-decoration: none;   cursor: pointer; } a.json-placeholder:hover {   text-decoration: underline; }',\n" +
                "        o = function (e) {\n" +
                "            var o = document.getElementsByTagName(\"head\")[0], t = document.createElement(\"style\");\n" +
                "            if (o.appendChild(t), t.styleSheet) t.styleSheet.disabled || (t.styleSheet.cssText = e); else try {\n" +
                "                t.innerHTML = e\n" +
                "            } catch (n) {\n" +
                "                t.innerText = e\n" +
                "            }\n" +
                "        };\n" +
                "    o(e)\n" +
                "}(), function (e) {\n" +
                "    function o(e) {\n" +
                "        return e instanceof Object && Object.keys(e).length > 0\n" +
                "    }\n" +
                "\n" +
                "    function t(e) {\n" +
                "        var o = /^(ftp|http|https):\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?/;\n" +
                "        return o.test(e)\n" +
                "    }\n" +
                "\n" +
                "    function n(e, r) {\n" +
                "        var s = \"\";\n" +
                "        if (\"string\" == typeof e) e = e.replace(/&/g, \"&amp;\").replace(/</g, \"&lt;\").replace(/>/g, \"&gt;\"), s += t(e) ? '<a href=\"' + e + '\" class=\"json-string json-url\">\"' + e + '\"</a>' : '<span class=\"json-string\">\"' + e + '\"</span>'; else if (\"number\" == typeof e) s += '<span class=\"json-literal json-literal-number\">' + e + \"</span>\"; else if (\"boolean\" == typeof e) s += '<span class=\"json-literal json-literal-boolean\">' + e + \"</span>\"; else if (null === e) s += '<span class=\"json-literal json-literal-null\">null</span>'; else if (e instanceof Array) if (e.length > 0) {\n" +
                "            s += '[<ol class=\"json-array\">';\n" +
                "            for (var l = 0; l < e.length; ++l) s += \"<li>\", o(e[l]) && (s += '<a href class=\"json-toggle\"></a>'), s += n(e[l], r), l < e.length - 1 && (s += \",\"), s += \"</li>\";\n" +
                "            s += \"</ol>]\"\n" +
                "        } else s += \"[]\"; else if (\"object\" == typeof e) {\n" +
                "            var a = Object.keys(e).length;\n" +
                "            if (a > 0) {\n" +
                "                s += '{<ul class=\"json-dict\">';\n" +
                "                for (var i in e) if (e.hasOwnProperty(i)) {\n" +
                "                    s += \"<li>\";\n" +
                "                    var c = r.withQuotes ? '<span class=\"json-string json-property\">\"' + i + '\"</span>' : '<span class=\"json-property\">' + i + \"</span>\";\n" +
                "                    s += o(e[i]) ? '<a href class=\"json-toggle\"></a>' + c : c, s += \": \" + n(e[i], r), --a > 0 && (s += \",\"), s += \"</li>\"\n" +
                "                }\n" +
                "                s += \"</ul>}\"\n" +
                "            } else s += \"{}\"\n" +
                "        }\n" +
                "        return s\n" +
                "    }\n" +
                "\n" +
                "    e.fn.jsonViewer = function (t, r) {\n" +
                "        return r = r || {}, this.each(function () {\n" +
                "            var s = n(t, r);\n" +
                "            o(t) && (s = '<a href class=\"json-toggle\"></a>' + s), e(this).html(s), e(this).off(\"click\"), e(this).on(\"click\", \"a.json-toggle\", function () {\n" +
                "                var o = e(this).toggleClass(\"collapsed\").siblings(\"ul.json-dict, ol.json-array\");\n" +
                "                if (o.toggle(), o.is(\":visible\")) o.siblings(\".json-placeholder\").remove(); else {\n" +
                "                    var t = o.children(\"li\").length, n = t + (t > 1 ? \" items\" : \" item\");\n" +
                "                    o.after('<a href class=\"json-placeholder\">' + n + \"</a>\")\n" +
                "                }\n" +
                "                return !1\n" +
                "            }), e(this).on(\"click\", \"a.json-placeholder\", function () {\n" +
                "                return e(this).siblings(\"a.json-toggle\").click(), !1\n" +
                "            }), 1 == r.collapsed && e(this).find(\"a.json-toggle\").click()\n" +
                "        })\n" +
                "    }\n" +
                "}(jQuery), function (e) {\n" +
                "    function o(e) {\n" +
                "        var o = {'\"': '\\\\\"', \"\\\\\": \"\\\\\\\\\", \"\\b\": \"\\\\b\", \"\\f\": \"\\\\f\", \"\\n\": \"\\\\n\", \"\\r\": \"\\\\r\", \"\t\": \"\\\\t\"};\n" +
                "        return e.replace(/[\"\\\\\\b\\f\\n\\r\\t]/g, function (e) {\n" +
                "            return o[e]\n" +
                "        })\n" +
                "    }\n" +
                "\n" +
                "    function t(e) {\n" +
                "        if (\"string\" == typeof e) return o(e);\n" +
                "        if (\"object\" == typeof e) for (var n in e) e[n] = t(e[n]); else if (Array.isArray(e)) for (var r = 0; r < e.length; r++) e[r] = t(e[r]);\n" +
                "        return e\n" +
                "    }\n" +
                "\n" +
                "    function n(o, t, n) {\n" +
                "        n = n || {}, n.editable !== !1 && (n.editable = !0), this.$container = e(o), this.options = n, this.load(t)\n" +
                "    }\n" +
                "\n" +
                "    n.prototype = {\n" +
                "        constructor: n, load: function (e) {\n" +
                "            this.$container.jsonViewer(t(e), {\n" +
                "                collapsed: this.options.defaultCollapsed,\n" +
                "                withQuotes: !0\n" +
                "            }).addClass(\"json-editor-blackbord\").attr(\"contenteditable\", !!this.options.editable)\n" +
                "        }, get: function () {\n" +
                "            try {\n" +
                "                return this.$container.find(\".collapsed\").click(), JSON.parse(this.$container.text())\n" +
                "            } catch (e) {\n" +
                "                throw new Error(e)\n" +
                "            }\n" +
                "        }\n" +
                "    }, window.JsonEditor = n\n" +
                "}(jQuery);";

        String htm = "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
                "    <title>Zot JSON Editor</title>\n" +
                "    <link TYPE=\"text/css\" href=\"style.css\" media=\"screen\" rel=\"stylesheet\">\n" +
                "</head>\n" +
                "<body bgcolor=\"#1c2833\" rightmargin=\"25px\">\n" +
                "<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\"\n" +
                "        integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\"\n" +
                "        crossorigin=\"anonymous\"></script>\n" +
                "<script type=\"text/javascript\" src=\"./dist/jquery.json-editor.min.js\"></script>\n" +
                "\n" +
                "<input type=\"password\" id=\"pw\" placeholder=\"Enter password\">\n" +
                "<input type=\"checkbox\" onclick=\"myFunction()\">Show Password\n" +
                "<button id=\"submit\">connect</button>\n <p>   can connect via ws://ip:ssddTmpDBPort/ssddTmpDBNotifID</p>" +
                "\n" +
                "<p id=\"json-input\"></p>\n" +
                "<button id=\"translate\">Update JSON</button>\n" +
                "<pre id=\"json-display\"></pre>\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "  var x = document.getElementById(\"pw\");\n" +
                "  let jsonDisplay = document.getElementById(\"json-display\");\n" +
                "\n" +
                "function myFunction() {\n" +
                "  if (x.type === \"password\") {\n" +
                "    x.type = \"text\";\n" +
                "  } else {\n" +
                "    x.type = \"password\";\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "    let msgHolder = \"\";\n" +
                "    const ws = new WebSocket(\"ws://localhost:19194/\");\n" +
                "\n" +
                "    $('#submit').on('click', function(){\n" +
                "    let tmppw = x.value;\n" +
                "    x.value = \"\";\n" +
                "        if (\"WebSocket\" in window) {\n" +
                "            let tmp = {\n" +
                "                        \"pw\": tmppw,\n" +
                "                        \"id\": \"ssddNotifier07!\",\n" +
                "                        \"path\": \"\",\n" +
                "                        \"dbid\": \"ssddTmpDBNotifID\"\n" +
                "                    }\n" +
                "        ws.send(JSON.stringify(tmp));\n" +
                "        ws.onmessage = function (evt) {\n" +
                "            if (msgHolder !== evt.data && evt.data !== \"AD.\") {\n" +
                "                msgHolder = evt.data;\n" +
                "\n" +
                "                try{\n" +
                "                    const y = JSON.parse(evt.data);\n" +
                "                let editor = new JsonEditor(jsonDisplay, y);\n" +
                "                editor.load(y);\n" +
                "\n" +
                "                $('#translate').on('click', function () {\n" +
                "                    try {\n" +
                "                        let tmp = {\n" +
                "                            \"pw\": tmppw,\n" +
                "                            \"id\": \"ssddUpdator07!\",\n" +
                "                            \"message\": editor.get(),\n" +
                "                            \"path\": \"\",\n" +
                "                            \"dbid\": \"ssddTmpDBNotifID\"\n" +
                "                        }\n" +
                "                        ws.send(JSON.stringify(tmp));\n" +
                "                    } catch(x){\n" +
                "                    alert(\"invalid format or JSON unavailable\");\n" +
                "                    console.log(x);\n" +
                "                    }\n" +
                "                });\n" +
                "                jsonDisplay.addEventListener(\"keyup\", enterpressalert);\n" +
                "\n" +
                "                function enterpressalert(e) {\n" +
                "                const code = (e.keyCode ? e.keyCode : e.which);\n" +
                "\n" +
                "                if (code === 13) {\n" +
                "                    try {\n" +
                "                        let tmp = {\n" +
                "                            \"pw\": tmppw,\n" +
                "                            \"id\": \"ssddUpdator07!\",\n" +
                "                            \"message\": editor.get(),\n" +
                "                            \"path\": \"\",\n" +
                "                            \"dbid\": \"ssddTmpDBNotifID\"\n" +
                "                        }\n" +
                "                        ws.send(JSON.stringify(tmp));\n" +
                "                        } catch(x){\n" +
                "                        alert(\"invalid format or JSON unavailable\");\n" +
                "                        console.log(x);\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "                }catch(e){\n" +
                "                    alert(\"incorrect json format.\");\n" +
                "                }\n" +
                "            }else if (evt.data == \"AD.\"){\n" +
                "                alert(\"Access denied! Please recheck your password.\");\n" +
                "            }\n" +
                "        };\n" +
                "        } else {\n" +
                "        // The browser doesn't support WebSocket\n" +
                "        alert(\"UH-OH your Browser is NOT supported :(\");\n" +
                "        }\n" +
                "    });\n" +
                "\n" +
                "    \n" +
                "</script>\n" +
                "</body>\n" +
                "</html>", htmfin = htm.replace("\"ws://localhost:19194/\"", "window.location.protocol.toString().replace(\"http\", \"ws\")+\"//\"+ window.location.hostname +\":\"" + "+window.location.port+" + "\"/" + dbid + "\"").replace("ssddTmpDBNotifID", dbid)
                .replace("ssddTmpDBPort", String.valueOf(port));

        String stylecss = "pre {margin-left: 20px;}\n" +
                "p {color: #CE9FFC}";

        SmsGenFile.fileCheck(dbfile, htmfil, htmfin, dbid);
        try {
            dist.getParentFile().mkdirs();
            dist.createNewFile();
            distx.getParentFile().mkdirs();
            dist.createNewFile();
            SmsGenFile.writeFile(dist, distjs);
            SmsGenFile.writeFile(distx, stylecss);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ZotWS zz = new ZotWS();
        insts.put(dbid, zz);
        zz.port(port);
        zz.externalStaticFileLocation(htmfil.getParent());
        zz.webSocket("/" + dbid, SmsWebSocket.class);
        zz.init();
        return this;
    }
}