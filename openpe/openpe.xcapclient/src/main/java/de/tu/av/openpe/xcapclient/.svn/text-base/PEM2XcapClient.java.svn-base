package de.tu.av.openpe.xcapclient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.xcap.XCAPClientXDMS;

public class PEM2XcapClient extends XCAPClientXDMS {

	private final static Logger log = LoggerFactory
			.getLogger(PEM2XcapClient.class);
	protected String mimeType;

	public PEM2XcapClient(String host, int port, String root) {
		super(host, port, root);
	}

	@Override
	protected HttpClient createHttpClient() {
		log.info("creating XCAP client");

		// set the default MIME type
		this.mimeType = "application/xml";

		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setParameter("http.useragent", "XPOSER Xtest User Agent ["
				+ clientParams.getParameter("http.useragent") + "]");
		clientParams.setVersion(HttpVersion.HTTP_1_1);
		clientParams.setContentCharset("UTF-8");

		return new HttpClient(clientParams,
				new MultiThreadedHttpConnectionManager());
	}
}
