package be.solidx.hot.utils;

public interface HttpDataDeserializer {

	Object processRequestData(byte[] data, String contentType);

}