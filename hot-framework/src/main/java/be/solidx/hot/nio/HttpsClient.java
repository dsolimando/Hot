package be.solidx.hot.nio;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.springframework.core.convert.support.DefaultConversionService;

import be.solidx.hot.utils.SSLUtils;


public abstract class HttpsClient<CLOSURE,MAP> extends HttpClient<CLOSURE,MAP> {
	
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
	
	public HttpsClient(ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(eventLoopPool, defaultConversionService);
	}

	public HttpsClient(ExecutorService bossExecutorService, ExecutorService eventLoopPool, DefaultConversionService defaultConversionService) {
		super(bossExecutorService, eventLoopPool, defaultConversionService);
	}
	
	protected abstract class HttpsRequest extends Request {

		public HttpsRequest(Map<String, Object> options, CLOSURE requestClosure) {
			super(options, requestClosure);
		}

		@Override
		protected ChannelPipelineFactory buildChannelPipelineFactory() {
				return new ChannelPipelineFactory() {
				
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					SSLEngine sslEngine = buildSSLContext(options).createSSLEngine();
				    sslEngine.setUseClientMode(true);
					
					ChannelPipeline pipeline = Channels.pipeline();
					pipeline.addLast("log", new LoggingHandler());
					pipeline.addLast("ssl", new SslHandler(sslEngine));
					pipeline.addLast("codec", new HttpClientCodec());
					pipeline.addLast("inflater", new HttpContentDecompressor());
					pipeline.addLast("handler", new HotSimpleChannelUpstreamHandler(response));
					return pipeline;
				}
			};
		}
	}
	
	protected SSLContext buildSSLContext(Map<String, Object> options) throws SSLContextInitializationException  {
		
		KeyManager[] keyManagers = null;
		TrustManager[] trustManagers = null;
		
		try {
			String secureProtocol = options.get(SECUREPROTOCOL).toString();
			SSLContext sslContext = SSLContext.getInstance(secureProtocol);
			
			if (options.get(JKS) != null) {
				keyManagers = handleClientKeystoreURLProvided(options).getKeyManagers();
			} else if (options.get(P12) != null) {
				keyManagers = handleClientPFXURLProvided(options).getKeyManagers();
			} else if (options.get(KEY) != null) {
				keyManagers = handleClientKeyCertURLProvided(options).getKeyManagers();
			}
			trustManagers = handleTrustManagers(options);
			sslContext.init(keyManagers, trustManagers, null);
			return sslContext;
		} catch (KeyManagementException | NoSuchAlgorithmException | SSLContextInitializationException 
				| CertificateException | IOException | URISyntaxException e) {
			throw new SSLContextInitializationException(e);
		}
	}
	
	@Override
	protected OptionsMapper buildOptionsMapper() {
		return new SSLOptionsMapper();
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
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
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
	
	public class SSLOptionsMapper extends OptionsMapper {
		Map<String, Object> toNettyOptions (Map<String, Object> options) {
			Map<String, Object> nettyOptions = super.toNettyOptions(options);
			
			if (options.get(CA) != null) {
				nettyOptions.put(CA, options.get(CA));
			}
			if (options.get(CERT) != null) {
				nettyOptions.put(CERT, options.get(CERT));
			}
			if (options.get(JKS) != null) {
				nettyOptions.put(JKS, options.get(JKS));
			}
			if (options.get(JKS_CERTIFICATE_PASSWORD) != null) {
				nettyOptions.put(JKS_CERTIFICATE_PASSWORD, options.get(JKS_CERTIFICATE_PASSWORD));
			}
			if (options.get(JKSPASSWORD) != null) {
				nettyOptions.put(JKSPASSWORD, options.get(JKSPASSWORD));
			}
			if (options.get(KEY) != null) {
				nettyOptions.put(KEY, options.get(KEY));
			}
			if (options.get(PASSPHRASE) != null) {
				nettyOptions.put(PASSPHRASE, options.get(PASSPHRASE));
			}
			if (options.get(P12) != null) {
				nettyOptions.put(P12, options.get(P12));
			}
			if (options.get(REJECTUNAUTHORIZED) != null) {
				nettyOptions.put(REJECTUNAUTHORIZED, HttpsClient.this.conversionService.convert(options.get(REJECTUNAUTHORIZED), Boolean.class));
			} else {
				nettyOptions.put(REJECTUNAUTHORIZED, true);
			} if (options.get(SECUREPROTOCOL) != null) {
				nettyOptions.put(SECUREPROTOCOL, options.get(SECUREPROTOCOL));
			} else {
				nettyOptions.put(SECUREPROTOCOL, "TLSv1.2");
			}
			return nettyOptions;
		}
	}
}
