package be.solidx.hot.utils;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
