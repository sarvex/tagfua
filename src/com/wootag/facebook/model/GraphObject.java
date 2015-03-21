/**
 * Copyright 2010-present Facebook. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.TagFu.facebook.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.wTagFufacebook.FacebookGraphObjectException;
import com.woTagFuacebook.internal.Utility;
import com.wooTagFucebook.internal.Validate;

/**
 * GraphObject is the primary interface used by the Facebook SDK for Android to represent objects in the Facebook Social
 * Graph and the Facebook Open Graph (OG). It is the base interface for all typed access to graph objects in the SDK. No
 * concrete classes implement GraphObject or its derived interfaces. Rather, they are implemented as proxies (see the
 * {@link com.wootTagFuebook.model.GraphObject.Factory Factory} class) that provide strongly-typed property getters and
 * setters to access the underlying data. Since the primary use case for graph objects is sending and receiving them
 * over the wire to/from Facebook services, they are represented as JSONObjects. No validation is done that a graph
 * object is actually of a specific type -- any graph object can be treated as any GraphObject-derived interface, and
 * the presence or absence of specific properties determines its suitability for use as that particular type of object. <br/>
 */
public interface GraphObject {

    /**
     * Returns a Java Collections map of names and properties. Modifying the returned map modifies the inner JSON
     * representation.
     *
     * @return a Java Collections map representing the GraphObject state
     */
    Map<String, Object> asMap();

    /**
     * Returns a new proxy that treats this graph object as a different GraphObject-derived type.
     *
     * @param graphObjectClass the type of GraphObject to return
     * @return a new instance of the GraphObject-derived-type that references the same underlying data
     */
    <T extends GraphObject> T cast(Class<T> graphObjectClass);

    /**
     * Gets the underlying JSONObject representation of this graph object.
     *
     * @return the underlying JSONObject representation of this graph object
     */
    JSONObject getInnerJSONObject();

    /**
     * Gets a property of the GraphObject
     *
     * @param propertyName the name of the property to get
     * @return the value of the named property
     */
    Object getProperty(String propertyName);

    /**
     * Gets a property of the GraphObject, cast to a particular GraphObject-derived interface. This gives some of the
     * benefits of having a property getter defined to return a GraphObject-derived type without requiring explicit
     * definition of an interface to define the getter.
     *
     * @param propertyName the name of the property to get
     * @param graphObjectClass the GraphObject-derived interface to cast the property to
     * @return
     */
    <T extends GraphObject> T getPropertyAs(String propertyName, Class<T> graphObjectClass);

    /**
     * Gets a property of the GraphObject, cast to a a list of instances of a particular GraphObject-derived interface.
     * This gives some of the benefits of having a property getter defined to return a GraphObject-derived type without
     * requiring explicit definition of an interface to define the getter.
     *
     * @param propertyName the name of the property to get
     * @param graphObjectClass the GraphObject-derived interface to cast the property to a list of
     * @return
     */
    <T extends GraphObject> GraphObjectList<T> getPropertyAsList(String propertyName, Class<T> graphObjectClass);

    /**
     * Removes a property of the GraphObject
     *
     * @param propertyName the name of the property to remove
     */
    void removeProperty(String propertyName);

    /**
     * Sets a property of the GraphObject
     *
     * @param propertyName the name of the property to set
     * @param propertyValue the value of the named property to set
     */
    void setProperty(String propertyName, Object propertyValue);

    /**
     * Creates proxies that implement GraphObject, GraphObjectList, and their derived types. These proxies allow access
     * to underlying collections and name/value property bags via strongly-typed property getters and setters.
     * <p/>
     * This supports get/set properties that use primitive types, JSON types, Date, other GraphObject types, Iterable,
     * Collection, List, and GraphObjectList.
     */
    final class Factory {

        private static final String UNCHECKED = "unchecked";
        private static final Set<Class<?>> verifiedGraphObjectClasses = new HashSet<Class<?>>();
        private static final SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd", Locale.US), };

        // No objects of this type should exist.
        private Factory() {

        }

        /**
         * Creates a GraphObject proxy that initially contains no data.
         *
         * @return a GraphObject with no data
         * @throws com.wootaTagFubook.FacebookException If the passed in Class is not a valid GraphObject interface
         */
        public static GraphObject create() {

            return create(GraphObject.class);
        }

        /**
         * Creates a GraphObject-derived proxy that initially contains no data.
         *
         * @param graphObjectClass the GraphObject-derived type to return
         * @return a graphObjectClass with no data
         * @throws com.wootagTagFuook.FacebookException If the passed in Class is not a valid GraphObject interface
         */
        public static <T extends GraphObject> T create(final Class<T> graphObjectClass) {

            return createGraphObjectProxy(graphObjectClass, new JSONObject());
        }

        /**
         * Creates a GraphObject proxy that provides typed access to the data in an underlying JSONObject.
         *
         * @param json the JSONObject containing the data to be exposed
         * @return a GraphObject that represents the underlying data
         * @throws com.wootag.TagFuok.FacebookException If the passed in Class is not a valid GraphObject interface
         */
        public static GraphObject create(final JSONObject json) {

            return create(json, GraphObject.class);
        }

        /**
         * Creates a GraphObject-derived proxy that provides typed access to the data in an underlying JSONObject.
         *
         * @param json the JSONObject containing the data to be exposed
         * @param graphObjectClass the GraphObject-derived type to return
         * @return a graphObjectClass that represents the underlying data
         * @throws com.wootag.fTagFuk.FacebookException If the passed in Class is not a valid GraphObject interface
         */
        public static <T extends GraphObject> T create(final JSONObject json, final Class<T> graphObjectClass) {

            return createGraphObjectProxy(graphObjectClass, json);
        }

        /**
         * Creates a GraphObjectList-derived proxy that initially contains no data.
         *
         * @param graphObjectClass the GraphObject-derived type to return
         * @return a GraphObjectList with no data
         * @throws com.wootag.faTagFu.FacebookException If the passed in Class is not a valid GraphObject interface
         */
        public static <T> GraphObjectList<T> createList(final Class<T> graphObjectClass) {

            return createList(new JSONArray(), graphObjectClass);
        }

        /**
         * Creates a GraphObjectList-derived proxy that provides typed access to the data in an underlying JSONArray.
         *
         * @param array the JSONArray containing the data to be exposed
         * @param graphObjectClass the GraphObject-derived type to return
         * @return a graphObjectClass that represents the underlying data
         * @throws com.wootag.facebook.FacebookException If the passed in Class is not a valid GraphObject interface
         */
        public static <T> GraphObjectList<T> createList(final JSONArray array, final Class<T> graphObjectClass) {

            return new GraphObjectListImpl<T>(array, graphObjectClass);
        }

        /**
         * Determines if two GraphObjects represent the same underlying graph object, based on their IDs.
         *
         * @param a a graph object
         * @param graphObject another graph object
         * @return true if both graph objects have an ID and it is the same ID, false otherwise
         */
        public static boolean hasSameId(final GraphObject a, final GraphObject graphObject) {

            if ((a == null) || (graphObject == null) || !a.asMap().containsKey("id")
                    || !graphObject.asMap().containsKey("id")) {
                return false;
            }
            if (a.equals(graphObject)) {
                return true;
            }
            final Object idA = a.getProperty("id");
            final Object idB = graphObject.getProperty("id");
            if ((idA == null) || (idB == null) || !(idA instanceof String) || !(idB instanceof String)) {
                return false;
            }
            return idA.equals(idB);
        }

        private static synchronized <T extends GraphObject> boolean hasClassBeenVerified(final Class<T> graphObjectClass) {

            return verifiedGraphObjectClasses.contains(graphObjectClass);
        }

        private static synchronized <T extends GraphObject> void recordClassHasBeenVerified(
                final Class<T> graphObjectClass) {

            verifiedGraphObjectClasses.add(graphObjectClass);
        }

        private static <T extends GraphObject> void verifyCanProxyClass(final Class<T> graphObjectClass) {

            if (hasClassBeenVerified(graphObjectClass)) {
                return;
            }

            if (!graphObjectClass.isInterface()) {
                throw new FacebookGraphObjectException("Factory can only wrap interfaces, not class: "
                        + graphObjectClass.getName());
            }

            final Method[] methods = graphObjectClass.getMethods();
            for (final Method method : methods) {
                final String methodName = method.getName();
                final int parameterCount = method.getParameterTypes().length;
                final Class<?> returnType = method.getReturnType();
                final boolean hasPropertyNameOverride = method.isAnnotationPresent(PropertyName.class);

                if (method.getDeclaringClass().isAssignableFrom(GraphObject.class)) {
                    // Don't worry about any methods from GraphObject or one of its base classes.
                    continue;
                } else if ((parameterCount == 1) && (returnType == Void.TYPE)) {
                    if (hasPropertyNameOverride) {
                        // If a property override is present, it MUST be valid. We don't fallback
                        // to using the method name
                        if (!Utility.isNullOrEmpty(method.getAnnotation(PropertyName.class).value())) {
                            continue;
                        }
                    } else if (methodName.startsWith("set") && (methodName.length() > 3)) {
                        // Looks like a valid setter
                        continue;
                    }
                } else if ((parameterCount == 0) && (returnType != Void.TYPE)) {
                    if (hasPropertyNameOverride) {
                        // If a property override is present, it MUST be valid. We don't fallback
                        // to using the method name
                        if (!Utility.isNullOrEmpty(method.getAnnotation(PropertyName.class).value())) {
                            continue;
                        }
                    } else if (methodName.startsWith("get") && (methodName.length() > 3)) {
                        // Looks like a valid getter
                        continue;
                    }
                }

                throw new FacebookGraphObjectException("Factory can't proxy method: " + method.toString());
            }

            recordClassHasBeenVerified(graphObjectClass);
        }

        // If expectedType is a generic type, expectedTypeAsParameterizedType must be provided in order to determine
        // generic parameter types.
        static <U> U coerceValueToExpectedType(final Object value, final Class<U> expectedType,
                final ParameterizedType expectedTypeAsParameterizedType) {

            if (value == null) {
                if (boolean.class.equals(expectedType)) {
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) Boolean.FALSE;
                    return result;
                } else if (char.class.equals(expectedType)) {
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) (Character) '\0';
                    return result;
                } else if (expectedType.isPrimitive()) {
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) (Number) 0;
                    return result;
                } else {
                    return null;
                }
            }

            final Class<?> valueType = value.getClass();
            if (expectedType.isAssignableFrom(valueType)) {
                @SuppressWarnings(UNCHECKED)
                final U result = (U) value;
                return result;
            }

            if (expectedType.isPrimitive()) {
                // If the result is a primitive, let the runtime succeed or fail at unboxing it.
                @SuppressWarnings(UNCHECKED)
                final U result = (U) value;
                return result;
            }

            if (GraphObject.class.isAssignableFrom(expectedType)) {
                @SuppressWarnings(UNCHECKED)
                final Class<? extends GraphObject> graphObjectClass = (Class<? extends GraphObject>) expectedType;

                // We need a GraphObject, but we don't have one.
                if (JSONObject.class.isAssignableFrom(valueType)) {
                    // We can wrap a JSONObject as a GraphObject.
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) createGraphObjectProxy(graphObjectClass, (JSONObject) value);
                    return result;
                } else if (GraphObject.class.isAssignableFrom(valueType)) {
                    // We can cast a GraphObject-derived class to another GraphObject-derived class.
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) ((GraphObject) value).cast(graphObjectClass);
                    return result;
                } else {
                    throw new FacebookGraphObjectException("Can't create GraphObject from " + valueType.getName());
                }
            } else if (Iterable.class.equals(expectedType) || Collection.class.equals(expectedType)
                    || List.class.equals(expectedType) || GraphObjectList.class.equals(expectedType)) {
                if (expectedTypeAsParameterizedType == null) {
                    throw new FacebookGraphObjectException("can't infer generic type of: " + expectedType.toString());
                }

                final Type[] actualTypeArguments = expectedTypeAsParameterizedType.getActualTypeArguments();

                if ((actualTypeArguments == null) || (actualTypeArguments.length != 1)
                        || !(actualTypeArguments[0] instanceof Class<?>)) {
                    throw new FacebookGraphObjectException(
                            "Expect collection properties to be of a type with exactly one generic parameter.");
                }
                final Class<?> collectionGenericArgument = (Class<?>) actualTypeArguments[0];

                if (JSONArray.class.isAssignableFrom(valueType)) {
                    final JSONArray jsonArray = (JSONArray) value;
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) createList(jsonArray, collectionGenericArgument);
                    return result;
                }
                throw new FacebookGraphObjectException("Can't create Collection from " + valueType.getName());
            } else if (String.class.equals(expectedType)) {
                if (Double.class.isAssignableFrom(valueType) || Float.class.isAssignableFrom(valueType)) {
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) String.format(Locale.getDefault(), "%f", value);
                    return result;
                } else if (Number.class.isAssignableFrom(valueType)) {
                    @SuppressWarnings(UNCHECKED)
                    final U result = (U) String.format(Locale.getDefault(), "%d", value);
                    return result;
                }
            } else if (Date.class.equals(expectedType)) {
                if (String.class.isAssignableFrom(valueType)) {
                    for (final SimpleDateFormat format : dateFormats) {
                        try {
                            final Date date = format.parse((String) value);
                            if (date != null) {
                                @SuppressWarnings(UNCHECKED)
                                final U result = (U) date;
                                return result;
                            }
                        } catch (final ParseException e) {
                            // Keep going.
                        }
                    }
                }
            }
            throw new FacebookGraphObjectException("Can't convert type" + valueType.getName() + " to "
                    + expectedType.getName());
        }

        static String convertCamelCaseToLowercaseWithUnderscores(String string) {

            string = string.replaceAll("([a-z])([A-Z])", "$1_$2");
            return string.toLowerCase(Locale.US);
        }

        static <T extends GraphObject> T createGraphObjectProxy(final Class<T> graphObjectClass, final JSONObject state) {

            verifyCanProxyClass(graphObjectClass);

            final Class<?>[] interfaces = new Class<?>[] { graphObjectClass };
            final GraphObjectProxy graphObjectProxy = new GraphObjectProxy(state, graphObjectClass);

            @SuppressWarnings(UNCHECKED)
            final T graphObject = (T) Proxy.newProxyInstance(GraphObject.class.getClassLoader(), interfaces,
                    graphObjectProxy);

            return graphObject;
        }

        static Map<String, Object> createGraphObjectProxyForMap(final JSONObject state) {

            final Class<?>[] interfaces = new Class<?>[] { Map.class };
            final GraphObjectProxy graphObjectProxy = new GraphObjectProxy(state, Map.class);

            @SuppressWarnings(UNCHECKED)
            final Map<String, Object> graphObject = (Map<String, Object>) Proxy.newProxyInstance(
                    GraphObject.class.getClassLoader(), interfaces, graphObjectProxy);

            return graphObject;
        }

        static Object getUnderlyingJSONObject(final Object obj) {

            if (obj == null) {
                return null;
            }

            final Class<?> objClass = obj.getClass();
            if (GraphObject.class.isAssignableFrom(objClass)) {
                final GraphObject graphObject = (GraphObject) obj;
                return graphObject.getInnerJSONObject();
            } else if (GraphObjectList.class.isAssignableFrom(objClass)) {
                final GraphObjectList<?> graphObjectList = (GraphObjectList<?>) obj;
                return graphObjectList.getInnerJSONArray();
            } else if (Iterable.class.isAssignableFrom(objClass)) {
                final JSONArray jsonArray = new JSONArray();
                final Iterable<?> iterable = (Iterable<?>) obj;
                for (final Object o : iterable) {
                    if (GraphObject.class.isAssignableFrom(o.getClass())) {
                        jsonArray.put(((GraphObject) o).getInnerJSONObject());
                    } else {
                        jsonArray.put(o);
                    }
                }
                return jsonArray;
            }
            return obj;
        }

        private final static class GraphObjectListImpl<T> extends AbstractList<T> implements GraphObjectList<T> {

            private static final Logger LOG = LoggerManager.getLogger();

            private static final String GRAPH_OBJECT_LIST_ITEM_TYPE_S_STATE_S = "GraphObjectList{itemType=%s, state=%s}";
            private final JSONArray state;
            private final Class<?> itemType;

            public GraphObjectListImpl(final JSONArray state, final Class<?> itemType) {

                Validate.notNull(state, "state");
                Validate.notNull(itemType, "itemType");

                this.state = state;
                this.itemType = itemType;
            }

            @Override
            public void add(final int location, final T object) {

                // We only support adding at the end of the list, due to JSONArray restrictions.
                if (location < 0) {
                    throw new IndexOutOfBoundsException();
                } else if (location < size()) {
                    throw new UnsupportedOperationException("Only adding items at the end of the list is supported.");
                }

                try {
                    put(location, object);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
            }

            @Override
            public final <U extends GraphObject> GraphObjectList<U> castToListOf(final Class<U> graphObjectClass) {

                if (GraphObject.class.isAssignableFrom(this.itemType)) {
                    if (graphObjectClass.isAssignableFrom(this.itemType)) {
                        @SuppressWarnings(UNCHECKED)
                        final GraphObjectList<U> result = (GraphObjectList<U>) this;
                        return result;
                    }

                    return createList(this.state, graphObjectClass);
                }
                throw new FacebookGraphObjectException("Can't cast GraphObjectCollection of non-GraphObject type "
                        + this.itemType);
            }

            @Override
            public void clear() {

                throw new UnsupportedOperationException();
            }

            @Override
            public boolean equals(final Object obj) {

                if (obj == null) {
                    return false;
                } else if (this == obj) {
                    return true;
                } else if (getClass() != obj.getClass()) {
                    return false;
                }
                @SuppressWarnings(UNCHECKED)
                final GraphObjectListImpl<T> other = (GraphObjectListImpl<T>) obj;
                return this.state.equals(other.state);
            }

            @SuppressWarnings(UNCHECKED)
            @Override
            public T get(final int location) {

                checkIndex(location);

                final Object value = this.state.opt(location);

                // Class<?> expectedType = method.getReturnType();
                // Type genericType = method.getGenericReturnType();
                final T result = (T) coerceValueToExpectedType(value, this.itemType, null);

                return result;
            }

            @Override
            public final JSONArray getInnerJSONArray() {

                return this.state;
            }

            @Override
            public int hashCode() {

                return this.state.hashCode();
            }

            @Override
            public boolean remove(final Object o) {

                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(final Collection<?> collection) {

                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(final Collection<?> collection) {

                throw new UnsupportedOperationException();
            }

            @Override
            public T set(final int location, final T object) {

                checkIndex(location);

                final T result = get(location);
                try {
                    put(location, object);
                } catch (final JSONException exception) {
                    LOG.e(exception);
                }
                return result;
            }

            @Override
            public int size() {

                return this.state.length();
            }

            @Override
            public String toString() {

                return String.format(GRAPH_OBJECT_LIST_ITEM_TYPE_S_STATE_S, this.itemType.getSimpleName(), this.state);
            }

            private void checkIndex(final int index) {

                if ((index < 0) || (index >= this.state.length())) {
                    throw new IndexOutOfBoundsException();
                }
            }

            private void put(final int index, final T obj) throws JSONException {

                final Object underlyingObject = getUnderlyingJSONObject(obj);
                this.state.put(index, underlyingObject);
            }
        }

        private final static class GraphObjectProxy extends ProxyBase<JSONObject> {

            private static final String CLEAR_METHOD = "clear";
            private static final String CONTAINSKEY_METHOD = "containsKey";
            private static final String CONTAINSVALUE_METHOD = "containsValue";
            private static final String ENTRYSET_METHOD = "entrySet";
            private static final String GET_METHOD = "get";
            private static final String ISEMPTY_METHOD = "isEmpty";
            private static final String KEYSET_METHOD = "keySet";
            private static final String PUT_METHOD = "put";
            private static final String PUTALL_METHOD = "putAll";
            private static final String REMOVE_METHOD = "remove";
            private static final String SIZE_METHOD = "size";
            private static final String VALUES_METHOD = "values";
            private static final String CAST_METHOD = "cast";
            private static final String CASTTOMAP_METHOD = "asMap";
            private static final String GETPROPERTY_METHOD = "getProperty";
            private static final String GETPROPERTYAS_METHOD = "getPropertyAs";
            private static final String GETPROPERTYASLIST_METHOD = "getPropertyAsList";
            private static final String SETPROPERTY_METHOD = "setProperty";
            private static final String REMOVEPROPERTY_METHOD = "removeProperty";
            private static final String GETINNERJSONOBJECT_METHOD = "getInnerJSONObject";

            private final Class<?> graphObjectClass;

            public GraphObjectProxy(final JSONObject state, final Class<?> graphObjectClass) {

                super(state);
                this.graphObjectClass = graphObjectClass;
            }

            @Override
            public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

                final Class<?> declaringClass = method.getDeclaringClass();

                if (declaringClass == Object.class) {
                    return proxyObjectMethods(proxy, method, args);
                } else if (declaringClass == Map.class) {
                    return proxyMapMethods(method, args);
                } else if (declaringClass == GraphObject.class) {
                    return proxyGraphObjectMethods(proxy, method, args);
                } else if (GraphObject.class.isAssignableFrom(declaringClass)) {
                    return proxyGraphObjectGettersAndSetters(method, args);
                }

                return throwUnexpectedMethodSignature(method);
            }

            @Override
            public String toString() {

                return String.format("GraphObject{graphObjectClass=%s, state=%s}",
                        this.graphObjectClass.getSimpleName(), this.state);
            }

            private Object createGraphObjectsFromParameters(final CreateGraphObject createGraphObject, Object value) {

                if ((createGraphObject != null) && !Utility.isNullOrEmpty(createGraphObject.value())) {
                    final String propertyName = createGraphObject.value();
                    if (List.class.isAssignableFrom(value.getClass())) {
                        final GraphObjectList<GraphObject> graphObjects = GraphObject.Factory
                                .createList(GraphObject.class);
                        @SuppressWarnings(UNCHECKED)
                        final List<Object> values = (List<Object>) value;
                        for (final Object obj : values) {
                            final GraphObject graphObject = GraphObject.Factory.create();
                            graphObject.setProperty(propertyName, obj);
                            graphObjects.add(graphObject);
                        }

                        value = graphObjects;
                    } else {
                        final GraphObject graphObject = GraphObject.Factory.create();
                        graphObject.setProperty(propertyName, value);

                        value = graphObject;
                    }
                }

                return value;
            }

            private final Object proxyGraphObjectGettersAndSetters(final Method method, final Object[] args)
                    throws JSONException {

                final String methodName = method.getName();
                final int parameterCount = method.getParameterTypes().length;
                final PropertyName propertyNameOverride = method.getAnnotation(PropertyName.class);

                final String key = propertyNameOverride != null ? propertyNameOverride.value()
                        : convertCamelCaseToLowercaseWithUnderscores(methodName.substring(3));

                // If it's a get or a set on a GraphObject-derived class, we can handle it.
                if (parameterCount == 0) {
                    // Has to be a getter. ASSUMPTION: The GraphObject-derived class has been verified
                    Object value = this.state.opt(key);

                    final Class<?> expectedType = method.getReturnType();

                    final Type genericReturnType = method.getGenericReturnType();
                    ParameterizedType parameterizedReturnType = null;
                    if (genericReturnType instanceof ParameterizedType) {
                        parameterizedReturnType = (ParameterizedType) genericReturnType;
                    }

                    value = coerceValueToExpectedType(value, expectedType, parameterizedReturnType);

                    return value;
                } else if (parameterCount == 1) {
                    // Has to be a setter. ASSUMPTION: The GraphObject-derived class has been verified
                    final CreateGraphObject createGraphObjectAnnotation = method.getAnnotation(CreateGraphObject.class);
                    Object value = createGraphObjectsFromParameters(createGraphObjectAnnotation, args[0]);

                    // If this is a wrapped object, store the underlying JSONObject instead, in order to serialize
                    // correctly.
                    value = getUnderlyingJSONObject(value);
                    this.state.putOpt(key, value);
                    return null;
                }

                return throwUnexpectedMethodSignature(method);
            }

            private final Object proxyGraphObjectMethods(final Object proxy, final Method method, final Object[] args) {

                final String methodName = method.getName();
                if (methodName.equals(CAST_METHOD)) {
                    @SuppressWarnings(UNCHECKED)
                    final Class<? extends GraphObject> graphObjectClass = (Class<? extends GraphObject>) args[0];

                    if ((graphObjectClass != null) && graphObjectClass.isAssignableFrom(this.graphObjectClass)) {
                        return proxy;
                    }
                    return Factory.createGraphObjectProxy(graphObjectClass, this.state);
                } else if (methodName.equals(GETINNERJSONOBJECT_METHOD)) {
                    return ((GraphObjectProxy) Proxy.getInvocationHandler(proxy)).state;
                } else if (methodName.equals(CASTTOMAP_METHOD)) {
                    return Factory.createGraphObjectProxyForMap(this.state);
                } else if (methodName.equals(GETPROPERTY_METHOD)) {
                    return this.state.opt((String) args[0]);
                } else if (methodName.equals(GETPROPERTYAS_METHOD)) {
                    final Object value = this.state.opt((String) args[0]);
                    final Class<?> expectedType = (Class<?>) args[1];

                    return coerceValueToExpectedType(value, expectedType, null);
                } else if (methodName.equals(GETPROPERTYASLIST_METHOD)) {

                    final Object value = this.state.opt((String) args[0]);
                    final Class<?> expectedType = (Class<?>) args[1];

                    final ParameterizedType parameterizedType = new ParameterizedType() {

                        @Override
                        public Type[] getActualTypeArguments() {

                            return new Type[] { expectedType };
                        }

                        @Override
                        public Type getOwnerType() {

                            return null;
                        }

                        @Override
                        public Type getRawType() {

                            return GraphObjectList.class;
                        }
                    };
                    return coerceValueToExpectedType(value, GraphObjectList.class, parameterizedType);
                } else if (methodName.equals(SETPROPERTY_METHOD)) {
                    return setJSONProperty(args);
                } else if (methodName.equals(REMOVEPROPERTY_METHOD)) {
                    this.state.remove((String) args[0]);
                    return null;
                }

                return throwUnexpectedMethodSignature(method);
            }

            private final Object proxyMapMethods(final Method method, final Object[] args) {

                final String methodName = method.getName();
                if (methodName.equals(CLEAR_METHOD)) {
                    JsonUtil.jsonObjectClear(this.state);
                    return null;
                } else if (methodName.equals(CONTAINSKEY_METHOD)) {
                    return Boolean.valueOf(this.state.has((String) args[0]));
                } else if (methodName.equals(CONTAINSVALUE_METHOD)) {
                    return Boolean.valueOf(JsonUtil.jsonObjectContainsValue(this.state, args[0]));
                } else if (methodName.equals(ENTRYSET_METHOD)) {
                    return JsonUtil.jsonObjectEntrySet(this.state);
                } else if (methodName.equals(GET_METHOD)) {
                    return this.state.opt((String) args[0]);
                } else if (methodName.equals(ISEMPTY_METHOD)) {
                    return Boolean.valueOf(this.state.length() == 0);
                } else if (methodName.equals(KEYSET_METHOD)) {
                    return JsonUtil.jsonObjectKeySet(this.state);
                } else if (methodName.equals(PUT_METHOD)) {
                    return setJSONProperty(args);
                } else if (methodName.equals(PUTALL_METHOD)) {
                    Map<String, Object> map = null;
                    if (args[0] instanceof Map<?, ?>) {
                        @SuppressWarnings(UNCHECKED)
                        final Map<String, Object> castMap = (Map<String, Object>) args[0];
                        map = castMap;
                    } else if (args[0] instanceof GraphObject) {
                        map = ((GraphObject) args[0]).asMap();
                    } else {
                        return null;
                    }
                    JsonUtil.jsonObjectPutAll(this.state, map);
                    return null;
                } else if (methodName.equals(REMOVE_METHOD)) {
                    this.state.remove((String) args[0]);
                    return null;
                } else if (methodName.equals(SIZE_METHOD)) {
                    return Integer.valueOf(this.state.length());
                } else if (methodName.equals(VALUES_METHOD)) {
                    return JsonUtil.jsonObjectValues(this.state);
                }

                return throwUnexpectedMethodSignature(method);
            }

            private Object setJSONProperty(final Object[] args) {

                final String name = (String) args[0];
                final Object property = args[1];
                final Object value = getUnderlyingJSONObject(property);
                try {
                    this.state.putOpt(name, value);
                } catch (final JSONException e) {
                    throw new IllegalArgumentException(e);
                }
                return null;
            }
        }

        private abstract static class ProxyBase<STATE> implements InvocationHandler {

            // Pre-loaded Method objects for the methods in java.lang.Object
            private static final String EQUALS_METHOD = "equals";
            private static final String TOSTRING_METHOD = "toString";

            protected final STATE state;

            protected ProxyBase(final STATE state) {

                this.state = state;
            }

            protected final Object proxyObjectMethods(final Object proxy, final Method method, final Object[] args)
                    throws Throwable {

                final String methodName = method.getName();
                if (methodName.equals(EQUALS_METHOD)) {
                    final Object other = args[0];

                    if (other == null) {
                        return Boolean.FALSE;
                    }

                    final InvocationHandler handler = Proxy.getInvocationHandler(other);
                    if (!(handler instanceof GraphObjectProxy)) {
                        return Boolean.FALSE;
                    }
                    final GraphObjectProxy otherProxy = (GraphObjectProxy) handler;
                    return Boolean.valueOf(this.state.equals(otherProxy.state));
                } else if (methodName.equals(TOSTRING_METHOD)) {
                    return toString();
                }

                // For others, just defer to the implementation object.
                return method.invoke(this.state, args);
            }

            // Declared to return Object just to simplify implementation of proxy helpers.
            protected final Object throwUnexpectedMethodSignature(final Method method) {

                throw new FacebookGraphObjectException(getClass().getName() + " got an unexpected method signature: "
                        + method.toString());
            }

        }
    }
}
