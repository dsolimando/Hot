package be.icode.hot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;

public class SSLUtils {
	
	private static final String PEM_KEY_BEGIN_DELIMITER = "-----BEGIN PRIVATE KEY-----";
	private static final String PEM_KEY_END_DELIMITER = "-----END PRIVATE KEY-----";

	public static PrivateKey toPrivateKey(InputStream pemInputStream) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		String data = IOUtils.toString(pemInputStream);
	    String[] tokens = data.split(PEM_KEY_BEGIN_DELIMITER);
	    tokens = tokens[1].trim().split(PEM_KEY_END_DELIMITER);
	    return generatePrivateKeyFromDER(DatatypeConverter.parseBase64Binary(tokens[0]));
	}
	
	protected static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
	    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
	    KeyFactory factory = KeyFactory.getInstance("RSA");
	    return (RSAPrivateKey)factory.generatePrivate(spec);        
	}
}
