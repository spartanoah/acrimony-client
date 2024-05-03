/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@JacksonStdImpl
public class CollectionDeserializer
extends ContainerDeserializerBase<Collection<Object>>
implements ContextualDeserializer {
    private static final long serialVersionUID = -1L;
    protected final JsonDeserializer<Object> _valueDeserializer;
    protected final TypeDeserializer _valueTypeDeserializer;
    protected final ValueInstantiator _valueInstantiator;
    protected final JsonDeserializer<Object> _delegateDeserializer;

    public CollectionDeserializer(JavaType collectionType, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser, ValueInstantiator valueInstantiator) {
        this(collectionType, valueDeser, valueTypeDeser, valueInstantiator, null, null, null);
    }

    protected CollectionDeserializer(JavaType collectionType, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser, ValueInstantiator valueInstantiator, JsonDeserializer<Object> delegateDeser, NullValueProvider nuller, Boolean unwrapSingle) {
        super(collectionType, nuller, unwrapSingle);
        this._valueDeserializer = valueDeser;
        this._valueTypeDeserializer = valueTypeDeser;
        this._valueInstantiator = valueInstantiator;
        this._delegateDeserializer = delegateDeser;
    }

    protected CollectionDeserializer(CollectionDeserializer src) {
        super(src);
        this._valueDeserializer = src._valueDeserializer;
        this._valueTypeDeserializer = src._valueTypeDeserializer;
        this._valueInstantiator = src._valueInstantiator;
        this._delegateDeserializer = src._delegateDeserializer;
    }

    protected CollectionDeserializer withResolved(JsonDeserializer<?> dd, JsonDeserializer<?> vd, TypeDeserializer vtd, NullValueProvider nuller, Boolean unwrapSingle) {
        return new CollectionDeserializer(this._containerType, vd, vtd, this._valueInstantiator, dd, nuller, unwrapSingle);
    }

    @Override
    public boolean isCachable() {
        return this._valueDeserializer == null && this._valueTypeDeserializer == null && this._delegateDeserializer == null;
    }

    public CollectionDeserializer createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<Object> delegateDeser = null;
        if (this._valueInstantiator != null) {
            JavaType delegateType;
            if (this._valueInstantiator.canCreateUsingDelegate()) {
                delegateType = this._valueInstantiator.getDelegateType(ctxt.getConfig());
                if (delegateType == null) {
                    ctxt.reportBadDefinition(this._containerType, String.format("Invalid delegate-creator definition for %s: value instantiator (%s) returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'", this._containerType, this._valueInstantiator.getClass().getName()));
                }
                delegateDeser = this.findDeserializer(ctxt, delegateType, property);
            } else if (this._valueInstantiator.canCreateUsingArrayDelegate()) {
                delegateType = this._valueInstantiator.getArrayDelegateType(ctxt.getConfig());
                if (delegateType == null) {
                    ctxt.reportBadDefinition(this._containerType, String.format("Invalid delegate-creator definition for %s: value instantiator (%s) returned true for 'canCreateUsingArrayDelegate()', but null for 'getArrayDelegateType()'", this._containerType, this._valueInstantiator.getClass().getName()));
                }
                delegateDeser = this.findDeserializer(ctxt, delegateType, property);
            }
        }
        Boolean unwrapSingle = this.findFormatFeature(ctxt, property, Collection.class, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JsonDeserializer<Object> valueDeser = this._valueDeserializer;
        valueDeser = this.findConvertingContentDeserializer(ctxt, property, valueDeser);
        JavaType vt = this._containerType.getContentType();
        valueDeser = valueDeser == null ? ctxt.findContextualValueDeserializer(vt, property) : ctxt.handleSecondaryContextualization(valueDeser, property, vt);
        TypeDeserializer valueTypeDeser = this._valueTypeDeserializer;
        if (valueTypeDeser != null) {
            valueTypeDeser = valueTypeDeser.forProperty(property);
        }
        NullValueProvider nuller = this.findContentNullProvider(ctxt, property, valueDeser);
        if (unwrapSingle != this._unwrapSingle || nuller != this._nullProvider || delegateDeser != this._delegateDeserializer || valueDeser != this._valueDeserializer || valueTypeDeser != this._valueTypeDeserializer) {
            return this.withResolved(delegateDeser, valueDeser, valueTypeDeser, nuller, unwrapSingle);
        }
        return this;
    }

    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return this._valueDeserializer;
    }

    @Override
    public ValueInstantiator getValueInstantiator() {
        return this._valueInstantiator;
    }

    @Override
    public Collection<Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String str;
        if (this._delegateDeserializer != null) {
            return (Collection)this._valueInstantiator.createUsingDelegate(ctxt, this._delegateDeserializer.deserialize(p, ctxt));
        }
        if (p.hasToken(JsonToken.VALUE_STRING) && (str = p.getText()).length() == 0) {
            return (Collection)this._valueInstantiator.createFromString(ctxt, str);
        }
        return this.deserialize(p, ctxt, this.createDefaultInstance(ctxt));
    }

    protected Collection<Object> createDefaultInstance(DeserializationContext ctxt) throws IOException {
        return (Collection)this._valueInstantiator.createUsingDefault(ctxt);
    }

    @Override
    public Collection<Object> deserialize(JsonParser p, DeserializationContext ctxt, Collection<Object> result) throws IOException {
        JsonToken t;
        if (!p.isExpectedStartArrayToken()) {
            return this.handleNonArray(p, ctxt, result);
        }
        p.setCurrentValue(result);
        JsonDeserializer<Object> valueDes = this._valueDeserializer;
        if (valueDes.getObjectIdReader() != null) {
            return this._deserializeWithObjectId(p, ctxt, result);
        }
        TypeDeserializer typeDeser = this._valueTypeDeserializer;
        while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
            try {
                Object value;
                if (t == JsonToken.VALUE_NULL) {
                    if (this._skipNullValues) continue;
                    value = this._nullProvider.getNullValue(ctxt);
                } else {
                    value = typeDeser == null ? valueDes.deserialize(p, ctxt) : valueDes.deserializeWithType(p, ctxt, typeDeser);
                }
                result.add(value);
            } catch (Exception e) {
                boolean wrap;
                boolean bl = wrap = ctxt == null || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
                if (!wrap) {
                    ClassUtil.throwIfRTE(e);
                }
                throw JsonMappingException.wrapWithPath((Throwable)e, result, result.size());
            }
        }
        return result;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return typeDeserializer.deserializeTypedFromArray(p, ctxt);
    }

    protected final Collection<Object> handleNonArray(JsonParser p, DeserializationContext ctxt, Collection<Object> result) throws IOException {
        Object value;
        boolean canWrap;
        boolean bl = canWrap = this._unwrapSingle == Boolean.TRUE || this._unwrapSingle == null && ctxt.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        if (!canWrap) {
            return (Collection)ctxt.handleUnexpectedToken(this._containerType, p);
        }
        JsonDeserializer<Object> valueDes = this._valueDeserializer;
        TypeDeserializer typeDeser = this._valueTypeDeserializer;
        try {
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                if (this._skipNullValues) {
                    return result;
                }
                value = this._nullProvider.getNullValue(ctxt);
            } else {
                value = typeDeser == null ? valueDes.deserialize(p, ctxt) : valueDes.deserializeWithType(p, ctxt, typeDeser);
            }
        } catch (Exception e) {
            boolean wrap;
            boolean bl2 = wrap = ctxt == null || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
            if (!wrap) {
                ClassUtil.throwIfRTE(e);
            }
            throw JsonMappingException.wrapWithPath((Throwable)e, Object.class, result.size());
        }
        result.add(value);
        return result;
    }

    protected Collection<Object> _deserializeWithObjectId(JsonParser p, DeserializationContext ctxt, Collection<Object> result) throws IOException {
        JsonToken t;
        if (!p.isExpectedStartArrayToken()) {
            return this.handleNonArray(p, ctxt, result);
        }
        p.setCurrentValue(result);
        JsonDeserializer<Object> valueDes = this._valueDeserializer;
        TypeDeserializer typeDeser = this._valueTypeDeserializer;
        CollectionReferringAccumulator referringAccumulator = new CollectionReferringAccumulator(this._containerType.getContentType().getRawClass(), result);
        while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
            try {
                Object value;
                if (t == JsonToken.VALUE_NULL) {
                    if (this._skipNullValues) continue;
                    value = this._nullProvider.getNullValue(ctxt);
                } else {
                    value = typeDeser == null ? valueDes.deserialize(p, ctxt) : valueDes.deserializeWithType(p, ctxt, typeDeser);
                }
                referringAccumulator.add(value);
            } catch (UnresolvedForwardReference reference) {
                ReadableObjectId.Referring ref = referringAccumulator.handleUnresolvedReference(reference);
                reference.getRoid().appendReferring(ref);
            } catch (Exception e) {
                boolean wrap;
                boolean bl = wrap = ctxt == null || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
                if (!wrap) {
                    ClassUtil.throwIfRTE(e);
                }
                throw JsonMappingException.wrapWithPath((Throwable)e, result, result.size());
            }
        }
        return result;
    }

    private static final class CollectionReferring
    extends ReadableObjectId.Referring {
        private final CollectionReferringAccumulator _parent;
        public final List<Object> next = new ArrayList<Object>();

        CollectionReferring(CollectionReferringAccumulator parent, UnresolvedForwardReference reference, Class<?> contentType) {
            super(reference, contentType);
            this._parent = parent;
        }

        @Override
        public void handleResolvedForwardReference(Object id, Object value) throws IOException {
            this._parent.resolveForwardReference(id, value);
        }
    }

    public static class CollectionReferringAccumulator {
        private final Class<?> _elementType;
        private final Collection<Object> _result;
        private List<CollectionReferring> _accumulator = new ArrayList<CollectionReferring>();

        public CollectionReferringAccumulator(Class<?> elementType, Collection<Object> result) {
            this._elementType = elementType;
            this._result = result;
        }

        public void add(Object value) {
            if (this._accumulator.isEmpty()) {
                this._result.add(value);
            } else {
                CollectionReferring ref = this._accumulator.get(this._accumulator.size() - 1);
                ref.next.add(value);
            }
        }

        public ReadableObjectId.Referring handleUnresolvedReference(UnresolvedForwardReference reference) {
            CollectionReferring id = new CollectionReferring(this, reference, this._elementType);
            this._accumulator.add(id);
            return id;
        }

        public void resolveForwardReference(Object id, Object value) throws IOException {
            Iterator<CollectionReferring> iterator = this._accumulator.iterator();
            Collection<Object> previous = this._result;
            while (iterator.hasNext()) {
                CollectionReferring ref = iterator.next();
                if (ref.hasId(id)) {
                    iterator.remove();
                    previous.add(value);
                    previous.addAll(ref.next);
                    return;
                }
                previous = ref.next;
            }
            throw new IllegalArgumentException("Trying to resolve a forward reference with id [" + id + "] that wasn't previously seen as unresolved.");
        }
    }
}

