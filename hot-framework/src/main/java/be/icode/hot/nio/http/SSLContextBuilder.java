package be.icode.hot.nio.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;

import be.icode.hot.utils.SSLUtils;

public class SSLContextBuilder {

	private static final String JKS = "jks";
	private static final String JKSPASSWORD = "jksPassword";
	private static final String JKS_CERTIFICATE_PASSWORD = "jksCertificatePassword";
	private static final String P12 = "p12";
	private static final String KEY = "key";
	private static final String PASSPHRASE = "passphrase";
	private static final String CERT = "cert";
	private static final String CA = "ca";
	private static final String REJECTUNAUTHORIZED = "rejectUnauthorized";
	private static final String SECUREPROTOCOL = "secureProtocol";
	
	protected SSLContext buildSSLContext(Map<String, Object> options) throws SSLContextInitializationException  {
		
		Map<String, Object> sslOptions = loadSSLOptions(options);
		KeyManager[] keyManagers = null;
		TrustManager[] trustManagers = null;
		
		try {
			String secureProtocol = sslOptions.get(SECUREPROTOCOL).toString();
			SSLContext sslContext = SSLContext.getInstance(secureProtocol);
			
			if (sslOptions.get(JKS) != null) {
				keyManagers = handleClientKeystoreURLProvided(sslOptions).getKeyManagers();
			} else if (sslOptions.get(P12) != null) {
				keyManagers = handleClientPFXURLProvided(sslOptions).getKeyManagers();
			} else if (sslOptions.get(KEY) != null) {
				keyManagers = handleClientKeyCertURLProvided(sslOptions).getKeyManagers();
			}
			trustManagers = handleTrustManagers(sslOptions);
			sslContext.init(keyManagers, trustManagers, null);
			return sslContext;
		} catch (KeyManagementException | NoSuchAlgorithmException | SSLContextInitializationException 
				| CertificateException | IOException | URISyntaxException e) {
			throw new SSLContextInitializationException(e);
		}
	}
	
	private Map<String, Object> loadSSLOptions (Map<String, Object> options) {
		
		Map<String, Object> parsedOptions = new HashMap<String, Object>();
		
		if (options.get(CA) != null) {
			parsedOptions.put(CA, options.get(CA));
		}
		if (options.get(CERT) != null) {
			parsedOptions.put(CERT, options.get(CERT));
		}
		if (options.get(JKS) != null) {
			parsedOptions.put(JKS, options.get(JKS));
		}
		if (options.get(JKS_CERTIFICATE_PASSWORD) != null) {
			parsedOptions.put(JKS_CERTIFICATE_PASSWORD, options.get(JKS_CERTIFICATE_PASSWORD));
		}
		if (options.get(JKSPASSWORD) != null) {
			parsedOptions.put(JKSPASSWORD, options.get(JKSPASSWORD));
		}
		if (options.get(KEY) != null) {
			parsedOptions.put(KEY, options.get(KEY));
		}
		if (options.get(PASSPHRASE) != null) {
			parsedOptions.put(PASSPHRASE, options.get(PASSPHRASE));
		}
		if (options.get(P12) != null) {
			parsedOptions.put(P12, options.get(P12));
		}
		if (options.get(REJECTUNAUTHORIZED) != null) {
			if (options.get(REJECTUNAUTHORIZED) instanceof String) {
				parsedOptions.put(REJECTUNAUTHORIZED, Boolean.parseBoolean((String) options.get(REJECTUNAUTHORIZED)));
			} else if (options.get(REJECTUNAUTHORIZED) instanceof Boolean) {
				parsedOptions.put(REJECTUNAUTHORIZED, options.get(REJECTUNAUTHORIZED));
			} else if (options.get(REJECTUNAUTHORIZED) instanceof Integer) {
				Integer integer = (Integer) options.get(REJECTUNAUTHORIZED);
				if (integer > 0) {
					parsedOptions.put(REJECTUNAUTHORIZED, true);
				} else {
					parsedOptions.put(REJECTUNAUTHORIZED, false);
				}
			}
		} else {
			parsedOptions.put(REJECTUNAUTHORIZED, true);
		} if (options.get(SECUREPROTOCOL) != null) {
			parsedOptions.put(SECUREPROTOCOL, options.get(SECUREPROTOCOL));
		} else {
			parsedOptions.put(SECUREPROTOCOL, "TLSv1.2");
		}
		return parsedOptions;
	}
	
	private TrustManager[] handleTrustManagers (Map<String, Object> options) throws CertificateException, IOException, URISyntaxException {
		boolean rejectUnauthorized = (boolean) options.get(REJECTUNAUTHORIZED);
		if (options.get(CA) != null) {
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			return new TrustManager[] {
				new TrustManager(
						(X509Certificate) certificateFactory.generateCertificate(
								getInputStream(new URI(options.get(CA).toString()))),
						rejectUnauthorized)
			};
		} else if (!rejectUnauthorized) {
			return new TrustManager[] {
				new TrustManager(null,rejectUnauthorized)
			};
		}
		return null;
	}
	
	private KeyManagerFactory handleClientKeystoreURLProvided (Map<String, Object> options) throws SSLContextInitializationException {
		
		InputStream jksFileInputStream = null;
		try {
			KeyManagerFactory keyManagerFactory = null;
			
			jksFileInputStream = getInputStream(new URI(options.get(JKS).toString()));
			KeyStore keyStore = KeyStore.getInstance(JKS);
			keyStore.load(jksFileInputStream, options.get(JKSPASSWORD).toString().toCharArray());
			
			keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, options.get(JKS_CERTIFICATE_PASSWORD).toString().toCharArray());
			
			return keyManagerFactory;
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | URISyntaxException
				| IOException e) {
			throw new SSLContextInitializationException(e);
		} finally {
			if (jksFileInputStream != null) {
				try { jksFileInputStream.close(); } catch (IOException e) {}
			}
		}
	}
	
	private KeyManagerFactory handleClientPFXURLProvided (Map<String, Object> options) throws SSLContextInitializationException {
		
		InputStream jksFileInputStream = null;
		try {
			KeyManagerFactory keyManagerFactory = null;
			
			jksFileInputStream = getInputStream(new URI(options.get(P12).toString()));
			KeyStore keyStore = KeyStore.getInstance("pkcs12","SunJSSE");
			keyStore.load(jksFileInputStream, options.get(PASSPHRASE).toString().toCharArray());
			
			keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, options.get(PASSPHRASE).toString().toCharArray());
			return keyManagerFactory;
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException
				| CertificateException | URISyntaxException | IOException e) {
			throw new SSLContextInitializationException(e);
		} finally {
			if (jksFileInputStream != null) {
				try { jksFileInputStream.close(); } catch (IOException e) {}
			}
		}
	}
	
	private KeyManagerFactory handleClientKeyCertURLProvided (Map<String, Object> options) throws SSLContextInitializationException {
		
		InputStream keyInputStream = null;
		InputStream certInputStream = null;
		
		try {
			KeyManagerFactory keyManagerFactory = null;
			KeyStore keyStore = KeyStore.getInstance(JKS);
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			
			byte[] key = IOUtils.toByteArray(getInputStream(new URI(options.get(KEY).toString())));
			Certificate certCertificate = certificateFactory.generateCertificate(getInputStream(new URI(options.get(CERT).toString())));
			char[] password = options.get(PASSPHRASE).toString().toCharArray();
			
			keyStore.load(null, null);
			// No CA needed, just add pivate key and associated public key to a keystore
			keyStore.setKeyEntry(KEY, 
					SSLUtils.toPrivateKey(new ByteArrayInputStream(key)),
					password,
					new Certificate[]{ certCertificate });
			
			keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore,password);
			
			return keyManagerFactory;
		} catch (NullPointerException | UnrecoverableKeyException | KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | URISyntaxException | InvalidKeySpecException e ) {
			throw new SSLContextInitializationException(e);
		} finally {
			if (keyInputStream != null) {
				try { keyInputStream.close(); } catch (IOException e) {}
			}
			if (certInputStream != null) {
				try { certInputStream.close(); } catch (IOException e) {}
			}
		}
	}
	
	private InputStream getInputStream (URI uri) throws IOException {
		InputStream jksFileInputStream;
		if (uri.getScheme() != null && uri.equals("file")) {
			jksFileInputStream = new FileInputStream(new File(uri));
		} else {
			if (uri.isAbsolute()) {
				jksFileInputStream = getClass().getResourceAsStream(uri.getPath());
			} else {
				jksFileInputStream = getClass().getClassLoader().getResourceAsStream(uri.getPath());
			}
		}
		return jksFileInputStream;
	}
	
	private static class TrustManager implements X509TrustManager {
		
		X509Certificate x509Certificate;
		boolean rejectUnauthorized;

		public TrustManager(X509Certificate x509Certificate, boolean rejectUnauthorized) {
			this.x509Certificate = x509Certificate;
			this.rejectUnauthorized = rejectUnauthorized;
		}
		
		// Unneeded because no client auth
		@Override public void checkClientTrusted(X509Certificate[] arg0, String authType) throws CertificateException {}
		@Override public X509Certificate[] getAcceptedIssuers() {return null;}

		@Override
		public void checkServerTrusted(X509Certificate[] serverCertificates, String authType) throws CertificateException {
			if (!rejectUnauthorized) return;
			
			if (serverCertificates.length == 0) {
				throw new CertificateException("Server didn't send any certificate");
			}
			// verify each certificate against CA certificate to find a valid one
			boolean found = false;
			for (X509Certificate x509Certificate : serverCertificates) {
				try {
					x509Certificate.verify(this.x509Certificate.getPublicKey());
					found = true;
					break;
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
					throw new CertificateException(e);
				}
			}
			if (!found) {
				throw new CertificateException("None of the provided certificates can be validated");
			}
		}
	}
	
	protected static class SSLContextInitializationException extends Exception {

		private static final long serialVersionUID = -792537019282300095L;

		public SSLContextInitializationException(Throwable cause) {
			super(cause);
		}
	}
}
