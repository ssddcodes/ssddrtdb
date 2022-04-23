package dev.ssdd.rtdb;

import dev.ssdd.rtdb.json.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

public class Snapshot {

    static final Pattern NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
    private final LinkedHashMap<String, Object> map;
    public static final Object NULL = new Snapshot.Null();

    private String parentkey = "";

    public Snapshot() {
        this.map = new LinkedHashMap<>();
    }

    public Snapshot(Snapshot jo, String... names) {
        this(names.length);

        for (String name : names) {
            try {
                this.putOnce(name, jo.opt(name));
            } catch (Exception ignored) {
            }
        }
    }

    public Snapshot(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '{') {
            throw x.syntaxError("A Snapshot text must begin with '{'");
        } else {
            while (true) {
                char c = x.nextClean();
                switch (c) {
                    case '\u0000':
                        throw x.syntaxError("A Snapshot text must end with '}'");
                    case '}':
                        return;
                    default:
                        x.back();
                        String key = x.nextValue().toString();
                        c = x.nextClean();
                        if (c != ':') {
                            throw x.syntaxError("Expected a ':' after a key");
                        }

                        if (key != null) {
                            if (this.opt(key) != null) {
                                throw x.syntaxError("Duplicate key \"" + key + "\"");
                            }

                            Object value = x.nextValue();
                            if (value != null) {
                                this.put(key, value);
                            }
                        }

                        switch (x.nextClean()) {
                            case ',':
                            case ';':
                                if (x.nextClean() == '}') {
                                    return;
                                }

                                x.back();
                                break;
                            case '}':
                                return;
                            default:
                                throw x.syntaxError("Expected a ',' or '}'");
                        }
                }
            }
        }
    }

    public Snapshot(LinkedHashMap<?, ?> m) {
        if (m == null) {
            this.map = new LinkedHashMap<>();
        } else {
            this.map = new LinkedHashMap<>(m.size());

            for (Map.Entry<?, ?> entry : m.entrySet()) {
                if (entry.getKey() == null) {
                    throw new NullPointerException("Null key.");
                }

                Object value = entry.getValue();
                if (value != null) {
                    this.map.put(String.valueOf(entry.getKey()), wrap(value));
                }
            }
        }

    }

    public Snapshot(Object bean) {
        this();
        this.populateMap(bean);
    }

    private Snapshot(Object bean, Set<Object> objectsRecord) {
        this();
        this.populateMap(bean, objectsRecord);
    }

    public Snapshot(Object object, String... names) {
        this(names.length);
        Class<?> c = object.getClass();

        for (String name : names) {
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignored) {
            }
        }

    }

    public Snapshot(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public Snapshot(String baseName, Locale locale) throws JSONException {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
        Enumeration<String> keys = bundle.getKeys();

        while (true) {
            String key;
            do {
                if (!keys.hasMoreElements()) {
                    return;
                }

                key = keys.nextElement();
            } while (key == null);

            String[] path = key.split("\\.");
            int last = path.length - 1;
            Snapshot target = this;

            for (int i = 0; i < last; ++i) {
                String segment = path[i];
                Snapshot nextTarget = target.optSnapshot(segment);
                if (nextTarget == null) {
                    nextTarget = new Snapshot();
                    target.put(segment, (Object) nextTarget);
                }

                target = nextTarget;
            }

            target.put(path[last], (Object) bundle.getString(key));
        }
    }

    public Snapshot(int initialCapacity) {
        this.map = new LinkedHashMap<>(initialCapacity);
    }

    public Snapshot accumulate(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof JSONArray ? (new JSONArray()).put(value) : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray) object).put(value);
        } else {
            this.put(key, (Object) (new JSONArray()).put(object).put(value));
        }

        return this;
    }

    public Snapshot append(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, (Object) (new JSONArray()).put(value));
        } else {
            if (!(object instanceof JSONArray)) {
                throw wrongValueFormatException(key, "JSONArray", (Object) null, (Throwable) null);
            }

            this.put(key, (Object) ((JSONArray) object).put(value));
        }

        return this;
    }

    public static String doubleToString(double d) {
        if (!Double.isInfinite(d) && !Double.isNaN(d)) {
            String string = Double.toString(d);
            if (string.indexOf(46) > 0 && string.indexOf(101) < 0 && string.indexOf(69) < 0) {
                while (string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }

                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }

            return string;
        } else {
            return "null";
        }
    }

    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        } else {
            Object object = this.opt(key);
            if (object == null) {
                throw new JSONException("Snapshot[" + quote(key) + "] not found.");
            } else {
                return object;
            }
        }
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
        E val = this.optEnum(clazz, key);
        if (val == null) {
            throw wrongValueFormatException(key, "enum of type " + quote(clazz.getSimpleName()), (Throwable) null);
        } else {
            return val;
        }
    }

    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (!object.equals(Boolean.FALSE) && (!(object instanceof String) || !((String) object).equalsIgnoreCase("false"))) {
            if (!object.equals(Boolean.TRUE) && (!(object instanceof String) || !((String) object).equalsIgnoreCase("true"))) {
                throw wrongValueFormatException(key, "Boolean", (Throwable) null);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public BigInteger getBigInteger(String key) throws JSONException {
        Object object = this.get(key);
        BigInteger ret = objectToBigInteger(object, (BigInteger) null);
        if (ret != null) {
            return ret;
        } else {
            throw wrongValueFormatException(key, "BigInteger", object, (Throwable) null);
        }
    }

    public BigDecimal getBigDecimal(String key) throws JSONException {
        Object object = this.get(key);
        BigDecimal ret = objectToBigDecimal(object, (BigDecimal) null);
        if (ret != null) {
            return ret;
        } else {
            throw wrongValueFormatException(key, "BigDecimal", object, (Throwable) null);
        }
    }

    public double getDouble(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else {
            try {
                return Double.parseDouble(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "double", var4);
            }
        }
    }

    public float getFloat(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else {
            try {
                return Float.parseFloat(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "float", var4);
            }
        }
    }

    public Number getNumber(String key) throws JSONException {
        Object object = this.get(key);

        try {
            return object instanceof Number ? (Number) object : stringToNumber(object.toString());
        } catch (Exception var4) {
            throw wrongValueFormatException(key, "number", var4);
        }
    }

    public int getInt(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).intValue();
        } else {
            try {
                return Integer.parseInt(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "int", var4);
            }
        }
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw wrongValueFormatException(key, "JSONArray", (Throwable) null);
        }
    }

    public Snapshot getSnapshot(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Snapshot) {
            return (Snapshot) object;
        } else {
            throw wrongValueFormatException(key, "Snapshot", (Throwable) null);
        }
    }

    public long getLong(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).longValue();
        } else {
            try {
                return Long.parseLong(object.toString());
            } catch (Exception var4) {
                throw wrongValueFormatException(key, "long", var4);
            }
        }
    }

    public static String[] getNames(Snapshot jo) {
        return jo.isEmpty() ? null : (String[]) jo.keySet().toArray(new String[jo.length()]);
    }

    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        } else {
            Class<?> klass = object.getClass();
            Field[] fields = klass.getFields();
            int length = fields.length;
            if (length == 0) {
                return null;
            } else {
                String[] names = new String[length];

                for (int i = 0; i < length; ++i) {
                    names[i] = fields[i].getName();
                }

                return names;
            }
        }
    }

    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String) object;
        } else {
            throw wrongValueFormatException(key, "string", (Throwable) null);
        }
    }

    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    public Snapshot increment(String key) throws JSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, (Integer) value + 1);
        } else if (value instanceof Long) {
            this.put(key, (Long) value + 1L);
        } else if (value instanceof BigInteger) {
            this.put(key, (Object) ((BigInteger) value).add(BigInteger.ONE));
        } else if (value instanceof Float) {
            this.put(key, (Float) value + 1.0F);
        } else if (value instanceof Double) {
            this.put(key, (Double) value + 1.0D);
        } else {
            if (!(value instanceof BigDecimal)) {
                throw new JSONException("Unable to increment [" + quote(key) + "].");
            }

            this.put(key, (Object) ((BigDecimal) value).add(BigDecimal.ONE));
        }

        return this;
    }

    public boolean isNull(String key) {
        return NULL.equals(this.opt(key));
    }

    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    protected Set<Map.Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    public int length() {
        return this.map.size();
    }

    public void clear() {
        this.map.clear();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public JSONArray names() {
        return this.map.isEmpty() ? null : new JSONArray(this.map.keySet());
    }

    public static String numberToString(Number number) throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        } else {
            testValidity(number);
            String string = number.toString();
            if (string.indexOf(46) > 0 && string.indexOf(101) < 0 && string.indexOf(69) < 0) {
                while (string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }

                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }

            return string;
        }
    }

    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        return this.optEnum(clazz, key, null);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        try {
            Object val = this.opt(key);
            if (NULL.equals(val)) {
                return defaultValue;
            } else if (clazz.isAssignableFrom(val.getClass())) {
                E myE = (E) val;
                return myE;
            } else {
                return Enum.valueOf(clazz, val.toString());
            }
        } catch (IllegalArgumentException | NullPointerException var6) {
            return defaultValue;
        }
    }

    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof Boolean) {
            return (Boolean) val;
        } else {
            try {
                return this.getBoolean(key);
            } catch (Exception var5) {
                return defaultValue;
            }
        }
    }

    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        Object val = this.opt(key);
        return objectToBigDecimal(val, defaultValue);
    }

    protected static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue) {
        return objectToBigDecimal(val, defaultValue, true);
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue, boolean exact) {
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        } else if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger) val);
        } else if (!(val instanceof Double) && !(val instanceof Float)) {
            if (!(val instanceof Long) && !(val instanceof Integer) && !(val instanceof Short) && !(val instanceof Byte)) {
                try {
                    return new BigDecimal(val.toString());
                } catch (Exception var4) {
                    return defaultValue;
                }
            } else {
                return new BigDecimal(((Number) val).longValue());
            }
        } else if (!numberIsFinite((Number) val)) {
            return defaultValue;
        } else {
            return exact ? BigDecimal.valueOf(((Number) val).doubleValue()) : new BigDecimal(val.toString());
        }
    }

    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        Object val = this.opt(key);
        return objectToBigInteger(val, defaultValue);
    }

    protected static BigInteger objectToBigInteger(Object val, BigInteger defaultValue) {
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof BigInteger) {
            return (BigInteger) val;
        } else if (val instanceof BigDecimal) {
            return ((BigDecimal) val).toBigInteger();
        } else if (!(val instanceof Double) && !(val instanceof Float)) {
            if (!(val instanceof Long) && !(val instanceof Integer) && !(val instanceof Short) && !(val instanceof Byte)) {
                try {
                    String valStr = val.toString();
                    return isDecimalNotation(valStr) ? (new BigDecimal(valStr)).toBigInteger() : new BigInteger(valStr);
                } catch (Exception var3) {
                    return defaultValue;
                }
            } else {
                return BigInteger.valueOf(((Number) val).longValue());
            }
        } else {
            return !numberIsFinite((Number) val) ? defaultValue : (new BigDecimal(((Number) val).doubleValue())).toBigInteger();
        }
    }

    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    public double optDouble(String key, double defaultValue) {
        Number val = this.optNumber(key);
        if (val == null) {
            return defaultValue;
        } else {
            double doubleValue = val.doubleValue();
            return doubleValue;
        }
    }

    /*public float optFloat(String key) {
        return this.optFloat(key, 0.0F / 0.0);
    }

    public float optFloat(String key, float defaultValue) {
        Number val = this.optNumber(key);
        if (val == null) {
            return defaultValue;
        } else {
            return val.floatValue();
        }
    }*/

    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        Number val = this.optNumber(key, (Number) null);
        return val == null ? defaultValue : val.intValue();
    }

    public JSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    public Snapshot optSnapshot(String key) {
        return this.optSnapshot(key, (Snapshot) null);
    }

    public Snapshot optSnapshot(String key, Snapshot defaultValue) {
        Object object = this.opt(key);
        return object instanceof Snapshot ? (Snapshot) object : defaultValue;
    }

    public long optLong(String key) {
        return this.optLong(key, 0L);
    }

    public long optLong(String key, long defaultValue) {
        Number val = this.optNumber(key, (Number) null);
        return val == null ? defaultValue : val.longValue();
    }

    public Number optNumber(String key) {
        return this.optNumber(key, (Number) null);
    }

    public Number optNumber(String key, Number defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        } else if (val instanceof Number) {
            return (Number) val;
        } else {
            try {
                return stringToNumber(val.toString());
            } catch (Exception var5) {
                return defaultValue;
            }
        }
    }

    public String optString(String key) {
        return this.optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }

    private void populateMap(Object bean) {
        this.populateMap(bean, Collections.newSetFromMap(new IdentityHashMap()));
    }

    private void populateMap(Object bean, Set<Object> objectsRecord) {
        Class<?> klass = bean.getClass();
        boolean includeSuperClass = klass.getClassLoader() != null;
        Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
        int var7 = methods.length;

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getParameterTypes().length == 0 && !method.isBridge() && method.getReturnType() != Void.TYPE && isValidMethodName(method.getName())) {
                String key = getKeyNameFromMethod(method);
                if (key != null && !key.isEmpty()) {
                    try {
                        Object result = method.invoke(bean);
                        if (result != null) {
                            if (objectsRecord.contains(result)) {
                                throw recursivelyDefinedObjectException(key);
                            }

                            objectsRecord.add(result);
                            this.map.put(key, wrap(result, objectsRecord));
                            objectsRecord.remove(result);
                            if (result instanceof Closeable) {
                                try {
                                    ((Closeable) result).close();
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                    }
                }
            }
        }

    }

    private static boolean isValidMethodName(String name) {
        return !"getClass".equals(name) && !"getDeclaringClass".equals(name);
    }

    private static String getKeyNameFromMethod(Method method) {
        int ignoreDepth = getAnnotationDepth(method, JSONPropertyIgnore.class);
        if (ignoreDepth > 0) {
            int forcedNameDepth = getAnnotationDepth(method, JSONPropertyName.class);
            if (forcedNameDepth < 0 || ignoreDepth <= forcedNameDepth) {
                return null;
            }
        }

        JSONPropertyName annotation = (JSONPropertyName) getAnnotation(method, JSONPropertyName.class);
        if (annotation != null && annotation.value() != null && !annotation.value().isEmpty()) {
            return annotation.value();
        } else {
            String name = method.getName();
            String key;
            if (name.startsWith("get") && name.length() > 3) {
                key = name.substring(3);
            } else {
                if (!name.startsWith("is") || name.length() <= 2) {
                    return null;
                }

                key = name.substring(2);
            }

            if (!Character.isLowerCase(key.charAt(0))) {
                if (key.length() == 1) {
                    key = key.toLowerCase(Locale.ROOT);
                } else if (!Character.isUpperCase(key.charAt(1))) {
                    key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
                }

                return key;
            } else {
                return null;
            }
        }
    }

    private static <A extends Annotation> A getAnnotation(Method m, Class<A> annotationClass) {
        if (m != null && annotationClass != null) {
            if (m.isAnnotationPresent(annotationClass)) {
                return m.getAnnotation(annotationClass);
            } else {
                Class<?> c = m.getDeclaringClass();
                if (c.getSuperclass() == null) {
                    return null;
                } else {
                    Class[] var3 = c.getInterfaces();
                    int var4 = var3.length;

                    for (Class i : var3) {
                        try {
                            Method im = i.getMethod(m.getName(), m.getParameterTypes());
                            return getAnnotation(im, annotationClass);
                        } catch (SecurityException | NoSuchMethodException ignored) {
                        }
                    }

                    try {
                        return getAnnotation(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
                    } catch (SecurityException | NoSuchMethodException var8) {
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
    }

    private static int getAnnotationDepth(Method m, Class<? extends Annotation> annotationClass) {
        if (m != null && annotationClass != null) {
            if (m.isAnnotationPresent(annotationClass)) {
                return 1;
            } else {
                Class<?> c = m.getDeclaringClass();
                if (c.getSuperclass() == null) {
                    return -1;
                } else {
                    Class[] var3 = c.getInterfaces();
                    int var4 = var3.length;

                    for (Class i : var3) {
                        try {
                            Method im = i.getMethod(m.getName(), m.getParameterTypes());
                            int d = getAnnotationDepth(im, annotationClass);
                            if (d > 0) {
                                return d + 1;
                            }
                        } catch (SecurityException | NoSuchMethodException ignored) {
                        }
                    }

                    try {
                        int d = getAnnotationDepth(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
                        return d > 0 ? d + 1 : -1;
                    } catch (SecurityException | NoSuchMethodException var9) {
                        return -1;
                    }
                }
            }
        } else {
            return -1;
        }
    }

    public Snapshot put(String key, boolean value) throws JSONException {
        return this.put(key, (Object) (value ? Boolean.TRUE : Boolean.FALSE));
    }

    public Snapshot put(String key, Collection<?> value) throws JSONException {
        return this.put(key, (Object) (new JSONArray(value)));
    }

    public Snapshot put(String key, double value) throws JSONException {
        return this.put(key, (Object) value);
    }

    public Snapshot put(String key, float value) throws JSONException {
        return this.put(key, (Object) value);
    }

    public Snapshot put(String key, int value) throws JSONException {
        return this.put(key, (Object) value);
    }

    public Snapshot put(String key, long value) throws JSONException {
        return this.put(key, (Object) value);
    }

    public Snapshot put(String key, Map<?, ?> value) throws JSONException {
        return this.put(key, (Object) (new Snapshot(value)));
    }

    public Snapshot put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        } else {
            if (value != null) {
                testValidity(value);
                this.map.put(key, value);
            } else {
                this.remove(key);
            }

            return this;
        }
    }

    public Snapshot putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            } else {
                return this.put(key, value);
            }
        } else {
            return this;
        }
    }

    public Snapshot putOpt(String key, Object value) throws JSONException {
        return key != null && value != null ? this.put(key, value) : this;
    }

    public Object query(String jsonPointer) {
        return this.query(new JSONPointer(jsonPointer));
    }

    public Object query(JSONPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }

    public Object optQuery(String jsonPointer) {
        return this.optQuery(new JSONPointer(jsonPointer));
    }

    public Object optQuery(JSONPointer jsonPointer) {
        try {
            return jsonPointer.queryFrom(this);
        } catch (JSONPointerException var3) {
            return null;
        }
    }

    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            String var10000;
            try {
                var10000 = quote(string, sw).toString();
            } catch (IOException var5) {
                return "";
            }

            return var10000;
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string != null && !string.isEmpty()) {
            char c = 0;
            int len = string.length();
            w.write(34);

            for (int i = 0; i < len; ++i) {
                char b = c;
                c = string.charAt(i);
                switch (c) {
                    case '\b':
                        w.write("\\b");
                        continue;
                    case '\t':
                        w.write("\\t");
                        continue;
                    case '\n':
                        w.write("\\n");
                        continue;
                    case '\f':
                        w.write("\\f");
                        continue;
                    case '\r':
                        w.write("\\r");
                        continue;
                    case '"':
                    case '\\':
                        w.write(92);
                        w.write(c);
                        continue;
                    case '/':
                        if (b == '<') {
                            w.write(92);
                        }

                        w.write(c);
                        continue;
                }
                if (c >= ' ' && (c < 128 || c >= 160) && (c < 8192 || c >= 8448)) {
                    w.write(c);
                } else {
                    w.write("\\u");
                    String hhhh = Integer.toHexString(c);
                    w.write("0000", 0, 4 - hhhh.length());
                    w.write(hhhh);
                }
            }

            w.write(34);
            return w;
        } else {
            w.write("\"\"");
            return w;
        }
    }

    public Object remove(String key) {
        return this.map.remove(key);
    }

    public boolean similar(Object other) {
        try {
            if (!(other instanceof Snapshot)) {
                return false;
            } else if (!this.keySet().equals(((Snapshot) other).keySet())) {
                return false;
            } else {
                Iterator<Map.Entry<String, Object>> var2 = this.entrySet().iterator();

                Object valueThis;
                Object valueOther;
                do {
                    while (true) {
                        do {
                            if (!var2.hasNext()) {
                                return true;
                            }

                            Map.Entry<String, ?> entry = var2.next();
                            String name = entry.getKey();
                            valueThis = entry.getValue();
                            valueOther = ((Snapshot) other).get(name);
                        } while (valueThis == valueOther);

                        if (valueThis == null) {
                            return false;
                        }

                        if (valueThis instanceof Snapshot) {
                            break;
                        }

                        if (valueThis instanceof JSONArray) {
                            if (!((JSONArray) valueThis).similar(valueOther)) {
                                return false;
                            }
                        } else if (valueThis instanceof Number && valueOther instanceof Number) {
                            if (!isNumberSimilar((Number) valueThis, (Number) valueOther)) {
                                return false;
                            }
                        } else if (!valueThis.equals(valueOther)) {
                            return false;
                        }
                    }
                } while (((Snapshot) valueThis).similar(valueOther));

                return false;
            }
        } catch (Throwable var7) {
            return false;
        }
    }

    protected static boolean isNumberSimilar(Number l, Number r) {
        if (numberIsFinite(l) && numberIsFinite(r)) {
            if (l.getClass().equals(r.getClass()) && l instanceof Comparable) {
                int compareTo = ((Comparable) l).compareTo(r);
                return compareTo == 0;
            } else {
                BigDecimal lBigDecimal = objectToBigDecimal(l, (BigDecimal) null, false);
                BigDecimal rBigDecimal = objectToBigDecimal(r, (BigDecimal) null, false);
                if (lBigDecimal != null && rBigDecimal != null) {
                    return lBigDecimal.compareTo(rBigDecimal) == 0;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static boolean numberIsFinite(Number n) {
        if (!(n instanceof Double) || !((Double) n).isInfinite() && !((Double) n).isNaN()) {
            return !(n instanceof Float) || !((Float) n).isInfinite() && !((Float) n).isNaN();
        } else {
            return false;
        }
    }

    protected static boolean isDecimalNotation(String val) {
        return val.indexOf(46) > -1 || val.indexOf(101) > -1 || val.indexOf(69) > -1 || "-0".equals(val);
    }

    protected static Number stringToNumber(String val) throws NumberFormatException {
        char initial = val.charAt(0);
        if ((initial < '0' || initial > '9') && initial != '-') {
            throw new NumberFormatException("val [" + val + "] is not a valid number.");
        } else if (isDecimalNotation(val)) {
            try {
                BigDecimal bd = new BigDecimal(val);
                return (Number) (initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0 ? -0.0D : bd);
            } catch (NumberFormatException var5) {
                try {
                    Double d = Double.valueOf(val);
                    if (!d.isNaN() && !d.isInfinite()) {
                        return d;
                    } else {
                        throw new NumberFormatException("val [" + val + "] is not a valid number.");
                    }
                } catch (NumberFormatException var4) {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
            }
        } else {
            char at1;
            if (initial == '0' && val.length() > 1) {
                at1 = val.charAt(1);
                if (at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if (at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
            }

            BigInteger bi = new BigInteger(val);
            if (bi.bitLength() <= 31) {
                return bi.intValue();
            } else {
                return (Number) (bi.bitLength() <= 63 ? bi.longValue() : bi);
            }
        }
    }

    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        } else if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        } else if ("null".equalsIgnoreCase(string)) {
            return NULL;
        } else {
            char initial = string.charAt(0);
            if (initial >= '0' && initial <= '9' || initial == '-') {
                try {
                    return stringToNumber(string);
                } catch (Exception ignored) {
                }
            }

            return string;
        }
    }

    public static void testValidity(Object o) throws JSONException {
        if (o instanceof Number && !numberIsFinite((Number) o)) {
            throw new JSONException("JSON does not allow non-finite numbers.");
        }
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names != null && !names.isEmpty()) {
            JSONArray ja = new JSONArray();

            for (int i = 0; i < names.length(); ++i) {
                ja.put(this.opt(names.getString(i)));
            }

            return ja;
        } else {
            return null;
        }
    }

    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception var2) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
        }
    }

    public static String valueToString(Object value) throws JSONException {
        return JSONWriter.valueToString(value);
    }

    public static Object wrap(Object object) {
        return wrap(object, null);
    }

    private static Object wrap(Object object, Set<Object> objectsRecord) {
        try {
            if (NULL.equals(object)) {
                return NULL;
            } else if (!(object instanceof Snapshot) && !(object instanceof JSONArray) && !NULL.equals(object) && !(object instanceof JSONString) && !(object instanceof Byte) && !(object instanceof Character) && !(object instanceof Short) && !(object instanceof Integer) && !(object instanceof Long) && !(object instanceof Boolean) && !(object instanceof Float) && !(object instanceof Double) && !(object instanceof String) && !(object instanceof BigInteger) && !(object instanceof BigDecimal) && !(object instanceof Enum)) {
                if (object instanceof Collection) {
                    Collection<?> coll = (Collection) object;
                    return new JSONArray(coll);
                } else if (object.getClass().isArray()) {
                    return new JSONArray(object);
                } else if (object instanceof Map) {
                    Map<?, ?> map = (Map) object;
                    return new Snapshot(map);
                } else {
                    Package objectPackage = object.getClass().getPackage();
                    String objectPackageName = objectPackage != null ? objectPackage.getName() : "";
                    if (!objectPackageName.startsWith("java.") && !objectPackageName.startsWith("javax.") && object.getClass().getClassLoader() != null) {
                        return objectsRecord != null ? new Snapshot(object, objectsRecord) : new Snapshot(object);
                    } else {
                        return object.toString();
                    }
                }
            } else {
                return object;
            }
        } catch (JSONException var4) {
            throw var4;
        } catch (Exception var5) {
            return null;
        }
    }

    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    protected static Writer writeValue(Writer writer, Object value, int indentFactor, int indent) throws JSONException, IOException {
        if (value != null && !value.equals(null)) {
            String numberAsString;
            if (value instanceof JSONString) {
                try {
                    numberAsString = ((JSONString) value).toJSONString();
                } catch (Exception var6) {
                    throw new JSONException(var6);
                }

                writer.write(numberAsString != null ? numberAsString.toString() : quote(value.toString()));
            } else if (value instanceof Number) {
                numberAsString = numberToString((Number) value);
                if (NUMBER_PATTERN.matcher(numberAsString).matches()) {
                    writer.write(numberAsString);
                } else {
                    quote(numberAsString, writer);
                }
            } else if (value instanceof Boolean) {
                writer.write(value.toString());
            } else if (value instanceof Enum) {
                writer.write(quote(((Enum) value).name()));
            } else if (value instanceof Snapshot) {
                ((Snapshot) value).write(writer, indentFactor, indent);
            } else if (value instanceof JSONArray) {
                ((JSONArray) value).write(writer, indentFactor, indent);
            } else if (value instanceof Map) {
                Map<?, ?> map = (Map) value;
                (new Snapshot(map)).write(writer, indentFactor, indent);
            } else if (value instanceof Collection) {
                Collection<?> coll = (Collection) value;
                (new JSONArray(coll)).write(writer, indentFactor, indent);
            } else if (value.getClass().isArray()) {
                (new JSONArray(value)).write(writer, indentFactor, indent);
            } else {
                quote(value.toString(), writer);
            }
        } else {
            writer.write("null");
        }

        return writer;
    }

    protected static void indent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; ++i) {
            writer.write(32);
        }

    }

    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        try {
            boolean needsComma = false;
            int length = this.length();
            writer.write(123);
            if (length == 1) {
                Map.Entry<String, ?> entry = (Map.Entry<String, Object>) this.entrySet().iterator().next();
                String key = (String) entry.getKey();
                writer.write(quote(key));
                writer.write(58);
                if (indentFactor > 0) {
                    writer.write(32);
                }

                try {
                    writeValue(writer, entry.getValue(), indentFactor, indent);
                } catch (Exception var12) {
                    throw new JSONException("Unable to write Snapshot value for key: " + key, var12);
                }
            } else if (length != 0) {
                int newIndent = indent + indentFactor;

                for (Iterator<Map.Entry<String, Object>> var15 = this.entrySet().iterator(); var15.hasNext(); needsComma = true) {
                    Map.Entry<String, ?> entry = var15.next();
                    if (needsComma) {
                        writer.write(44);
                    }

                    if (indentFactor > 0) {
                        writer.write(10);
                    }

                    indent(writer, newIndent);
                    String key = (String) entry.getKey();
                    writer.write(quote(key));
                    writer.write(58);
                    if (indentFactor > 0) {
                        writer.write(32);
                    }

                    try {
                        writeValue(writer, entry.getValue(), indentFactor, newIndent);
                    } catch (Exception var11) {
                        throw new JSONException("Unable to write Snapshot value for key: " + key, var11);
                    }
                }

                if (indentFactor > 0) {
                    writer.write(10);
                }

                indent(writer, indent);
            }

            writer.write(125);
            return writer;
        } catch (IOException var13) {
            throw new JSONException(var13);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> results = new LinkedHashMap<>();

        Map.Entry entry;
        Object value;
        for (Iterator var2 = this.entrySet().iterator(); var2.hasNext(); results.put(String.valueOf(entry.getKey()), value)) {
            entry = (Map.Entry) var2.next();
            if (entry.getValue() != null && !NULL.equals(entry.getValue())) {
                if (entry.getValue() instanceof Snapshot) {
                    value = ((Snapshot) entry.getValue()).toMap();
                } else if (entry.getValue() instanceof JSONArray) {
                    value = ((JSONArray) entry.getValue()).toList();
                } else {
                    value = entry.getValue();
                }
            } else {
                value = null;
            }
        }

        return results;
    }

    private static JSONException wrongValueFormatException(String key, String valueType, Throwable cause) {
        return new JSONException("Snapshot[" + quote(key) + "] is not a " + valueType + ".", cause);
    }

    private static JSONException wrongValueFormatException(String key, String valueType, Object value, Throwable cause) {
        return new JSONException("Snapshot[" + quote(key) + "] is not a " + valueType + " (" + value + ").", cause);
    }

    private static JSONException recursivelyDefinedObjectException(String key) {
        return new JSONException("JavaBean object contains recursively defined member variable of key " + quote(key));
    }

    private static final class Null {
        private Null() {
        }

        protected final Object clone() {
            return this;
        }

        public boolean equals(Object object) {
            return object == null || object == this;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "null";
        }
    }

    public Snapshot setParentkey(String key){
        this.parentkey = key;
        return this;
    }

    public String getParentKey(){
        return this.parentkey;
    }

}
