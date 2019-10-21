package ssltest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLcall {

	public static void main(String[] args) throws Exception {
		while(true) {
			new SSLcall().download("https://192.168.0.14:8443/examples", System.currentTimeMillis() + ".txt");
			Thread.sleep(5000);
		}

	}

	public void download(String sourceUrl, String targetFilename) throws Exception {
		FileOutputStream fos = null;
		InputStream is = null;
		HttpsURLConnection conn = null;
		try {
			SSLContext ctx = SSLContext.getInstance("TLS"); // SSL도 가능
			ctx.init(
					null, // new KeyManager[0], 
					new TrustManager[] {new DefaultTrustManager()}, 
					new SecureRandom() // null도 가능
					);
			SSLContext.setDefault(ctx);

			fos = new FileOutputStream("D:/dev/" + targetFilename);

			URL url = new URL(sourceUrl);
			conn = (HttpsURLConnection) url.openConnection();

			conn.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					System.out.println(arg0 + ", " + arg1);
					return true;

				}
			});

			System.out.println(conn.getResponseCode());

			is = conn.getInputStream();

			byte[] buffer = new byte[1024];
			int readBytes;
			while ((readBytes = is.read(buffer)) != -1) {
				fos.write(buffer, 0, readBytes);
			}
		} 
		/*
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}*/ 
		finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// 둘다 상관 없음.
			// return null;
			return new X509Certificate[0];
		}
	}
}
