package com.tonapps.ur;

import com.tonapps.ur.registry.CryptoCoinInfo;
import com.tonapps.ur.registry.CryptoHDKey;
import com.tonapps.ur.registry.CryptoKeypath;
import com.tonapps.ur.registry.RegistryType;
import com.tonapps.ur.registry.TonSignature;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Ported from https://github.com/BlockchainCommons/URKit
 */
public class UR {
    public static final String UR_PREFIX = "ur";

    private final String type;
    private final byte[] data;

    public UR(RegistryType registryType, byte[] data) throws InvalidTypeException {
        this(registryType.toString(), data);
    }

    public UR(String type, byte[] data) throws InvalidTypeException {
        if(!isURType(type)) {
            throw new InvalidTypeException("Invalid UR type: " + type);
        }

        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public RegistryType getRegistryType() {
        return RegistryType.fromString(type);
    }

    public byte[] getCborBytes() {
        return data;
    }

    public Object decodeFromRegistry() throws InvalidCBORException {
        RegistryType registryType = getRegistryType();

        try {
            List<DataItem> dataItems = CborDecoder.decode(getCborBytes());
            DataItem item = dataItems.get(0);

            if (registryType == RegistryType.BYTES) {
                return ((ByteString)item).getBytes();
            } else if (registryType == RegistryType.CRYPTO_HDKEY) {
                return CryptoHDKey.fromCbor(item);
            } else if (registryType == RegistryType.CRYPTO_KEYPATH) {
                return CryptoKeypath.fromCbor(item);
            } else if (registryType == RegistryType.CRYPTO_COIN_INFO) {
                return CryptoCoinInfo.fromCbor(item);
            } else if (registryType == RegistryType.TON_SIGNATURE) {
                return TonSignature.fromCbor(item);
            }
        } catch(CborException e) {
            throw new InvalidCBORException(e.getMessage());
        }

        return null;
    }

    public byte[] toBytes() throws InvalidCBORException {
        try {
            List<DataItem> dataItems = CborDecoder.decode(getCborBytes());
            if(!(dataItems.get(0) instanceof ByteString)) {
                throw new IllegalArgumentException("First element of CBOR is not a byte string");
            }

            return ((ByteString)dataItems.get(0)).getBytes();
        } catch(CborException e) {
            throw new InvalidCBORException(e.getMessage());
        }
    }

    public static boolean isURType(String type) {
        for(char c : type.toCharArray()) {
            if('a' <= c && c <= 'z') {
                return true;
            }
            if('0' <= c && c <= '9') {
                return true;
            }
            if(c == '-') {
                return true;
            }
        }

        return false;
    }

    public static UR fromBytes(byte[] data) throws InvalidTypeException, InvalidCBORException {
        return fromBytes(RegistryType.BYTES.toString(), data);
    }

    public static UR fromBytes(String type, byte[] data) throws InvalidTypeException, InvalidCBORException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new CborEncoder(baos).encode(new CborBuilder()
                    .add(data)
                    .build());
            byte[] cbor = baos.toByteArray();

            return new UR(type, cbor);
        } catch(CborException e) {
            throw new InvalidCBORException(e.getMessage());
        }
    }

    public String toString() {
        return UREncoder.encode(this);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        UR ur = (UR) o;
        return type.equals(ur.type) &&
                Arrays.equals(data, ur.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    public static class URException extends Exception {
        public URException(String message) {
            super(message);
        }
    }

    public static class InvalidTypeException extends URException {
        public InvalidTypeException(String message) {
            super(message);
        }
    }

    public static class InvalidSchemeException extends URException {
        public InvalidSchemeException(String message) {
            super(message);
        }
    }

    public static class InvalidPathLengthException extends URException {
        public InvalidPathLengthException(String message) {
            super(message);
        }
    }

    public static class InvalidSequenceComponentException extends URException {
        public InvalidSequenceComponentException(String message) {
            super(message);
        }
    }

    public static class InvalidCBORException extends URException {
        public InvalidCBORException(String message) {
            super(message);
        }
    }
}