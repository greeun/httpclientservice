package com.withwiz.service.network.http.client.httpclientservice;

import java.io.InputStream;
import java.util.Map;

import com.withwiz.beach.network.http.client.DefaultHttpProcessor;
import com.withwiz.beach.network.http.message.*;
import com.withwiz.jellyfish.message.MessageException;
import com.withwiz.jellyfish.service.DefaultService;
import com.withwiz.jellyfish.service.IGenericService;
import com.withwiz.jellyfish.service.ServiceException;
import com.withwiz.plankton.io.StringInputStream;

/**
 * HTTP client service.<BR/>
 * Created by uni4love on 2014. 11. 23..
 */
public class HttpClientService extends
		DefaultService<HttpClientServiceRequestDeliveryMessage, HttpClientServiceResponseDeliveryMessage>
{
	/**
	 * service parameter key: HTTP_METHOD
	 */
	public static final String KEY_HTTP_METHOD = "HTTP_METHOD";

	/**
	 * service parameter key: SERVICE_URL
	 */
	public static final String KEY_SERVICE_URL = "SERVICE_URL";

	/**
	 * service parameter key: SEND_DATA
	 */
	public static final String KEY_SEND_DATA = "SEND_DATA";

	/**
	 * service parameter key: IS_PROXY_RESPONSE
	 */
	public static final String KEY_IS_PROXY_RESPONSE = "IS_PROXY_RESPONSE";

	/**
	 * service parameter key: CONNECTION_TIMEOUT
	 */
	public static final String KEY_CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";

	/**
	 * service parameter key: SOCKET_TIMEOUT
	 */
	public static final String KEY_SOCKET_TIMEOUT = "SOCKET_TIMEOUT";

	/**
	 * service parameter key: NETWORK_BUFFER_SIZE
	 */
	public static final String KEY_NETWORK_BUFFER_SIZE = "NETWORK_BUFFER_SIZE";

	/**
	 * service parameter key: IS_SSL_TRUST
	 */
	public static final String KEY_IS_SSL_TRUST = "IS_SSL_TRUST";

	/**
	 * service parameter key: HEADER_PARAMETERS
	 */
	public static final String KEY_HEADER_PARAMETERS = "HEADER_PARAMETERS";

	/**
	 * service parameter key: KEY_BODY_INPUT_STREAM
	 */
	public static final String KEY_BODY_INPUT_STREAM = "KEY_BODY_INPUT_STREAM";

	/**
	 * service parameter key: KEY_HTTP_RESPONSE
	 */
	public static final String KEY_HTTP_RESPONSE = "KEY_HTTP_RESPONSE";

	/**
	 * service parameter value: method get
	 */
	public static final int VALUE_HTTP_METHOD_GET = HttpMessage.METHOD_GET;

	/**
	 * service parameter value: method post
	 */
	public static final int VALUE_HTTP_METHOD_POST = HttpMessage.METHOD_POST;

	/**
	 * service parameter value: method put
	 */
	public static final int VALUE_HTTP_METHOD_PUT = HttpMessage.METHOD_PUT;

	/**
	 * service parameter value: method delete
	 */
	public static final int VALUE_HTTP_METHOD_DELETE = HttpMessage.METHOD_DELETE;

	/**
	 * service name
	 */
	private static final String SERVICE_NAME = "HTTP client service";

	/**
	 * constructor
	 */
	public HttpClientService()
	{
		this.name = SERVICE_NAME;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void onService(
			HttpClientServiceRequestDeliveryMessage requestDeliveryMessage,
			HttpClientServiceResponseDeliveryMessage responseDeliveryMessage)
					throws ServiceException
	{
		// url
		String url = (String) requestDeliveryMessage.getValue(KEY_SERVICE_URL);

		// method
		int httpMethod = DefaultHttpMessage.METHOD_GET;
		if (requestDeliveryMessage.containsKey(KEY_HTTP_METHOD))
		{
			httpMethod = (Integer) requestDeliveryMessage
					.getValue(KEY_HTTP_METHOD);
		}

		// headers
		Map headerParams = (Map) requestDeliveryMessage
				.getValue(KEY_HEADER_PARAMETERS);

		// body inputstream
		InputStream bodyInputStream = (InputStream) requestDeliveryMessage
				.getValue(KEY_BODY_INPUT_STREAM);

		// HTTP request message
		IHttpRequestMessage httpRequestMessage = null;
		try
		{
			httpRequestMessage = createHttpRequestMessage(url, httpMethod,
					headerParams, bodyInputStream);
			// connection timeout
			if (requestDeliveryMessage.containsKey(KEY_CONNECTION_TIMEOUT))
			{
				int connectTimeout = (Integer) requestDeliveryMessage
						.getValue(KEY_CONNECTION_TIMEOUT);
				httpRequestMessage.setConnectionTimeout(connectTimeout);
			}

			// socket timeout
			if (requestDeliveryMessage.containsKey(KEY_SOCKET_TIMEOUT))
			{
				int socketTimeout = (Integer) requestDeliveryMessage
						.getValue(KEY_SOCKET_TIMEOUT);
				httpRequestMessage.setSocketTimeout(socketTimeout);
			}

			// network buffer size
			if (requestDeliveryMessage.containsKey(KEY_NETWORK_BUFFER_SIZE))
			{
				int networkBufferSize = (Integer) requestDeliveryMessage
						.getValue(KEY_NETWORK_BUFFER_SIZE);
				httpRequestMessage.setNetworkBufferSize(networkBufferSize);
			}

			// trust ssl
			if (requestDeliveryMessage.containsKey(KEY_IS_SSL_TRUST))
			{
				boolean isSslTrust = (Boolean) requestDeliveryMessage
						.getValue(KEY_IS_SSL_TRUST);
				httpRequestMessage.setTrustSsl(isSslTrust);
			}
		}
		catch (MessageException e)
		{
			throw new ServiceException(e);
		}

		// http processor
		DefaultHttpProcessor http = new DefaultHttpProcessor();

		// request
		IHttpResponseMessage httpResponseMessage = http
				.request(httpRequestMessage);

		responseDeliveryMessage.addValue(KEY_HTTP_RESPONSE,
				httpResponseMessage);
	}

	/**
	 * HttpRequestMessage를 생성하여 리턴한다.
	 *
	 * @param url
	 *            service url
	 * @param httpMethod
	 *            HTTP method
	 * @param headerParameters
	 *            header parameters
	 * @param bodyInputStream
	 *            InputStream for send data
	 * @return HttpRequestMessage
	 * @throws MessageException
	 */
	protected IHttpRequestMessage createHttpRequestMessage(String url,
			int httpMethod, Map<String, String> headerParameters,
			InputStream bodyInputStream) throws MessageException
	{
		DefaultHttpRequestMessage req = null;
		try
		{
			req = new DefaultHttpRequestMessage(url);
			req.setMethod(httpMethod);

			if (headerParameters != null)
			{
				req.addHeaderParameters(headerParameters);
			}
			req.setBodyInputStream(bodyInputStream);
		}
		catch (Exception e)
		{
			throw new MessageException(e);
		}
		return req;
	}

	/**
	 * test main
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		String serviceUrl = "http://166.104.112.43:20080/sda/ctx/CM-1-1-100/monday,0850";

		// request delivery message
		HttpClientServiceRequestDeliveryMessage req = new HttpClientServiceRequestDeliveryMessage();
		// add key-value list.
		req.addValue(HttpClientService.KEY_SERVICE_URL, serviceUrl);
		req.addValue(HttpClientService.KEY_CONNECTION_TIMEOUT, 9000);
		req.addValue(HttpClientService.KEY_HTTP_METHOD,
				HttpClientService.VALUE_HTTP_METHOD_POST);

		// body data
		StringInputStream inputStream = new StringInputStream(new String("###BODY###"));
		req.addValue(HttpClientService.KEY_BODY_INPUT_STREAM, inputStream);

		// response delivery message
		HttpClientServiceResponseDeliveryMessage res = new HttpClientServiceResponseDeliveryMessage();

		// HttpClientService
		IGenericService service = new HttpClientService();
		try
		{
			// execute a service
			service.onService(req, res);
			// get a service response
			IHttpResponseMessage httpResponseMessage = res
					.getValue(HttpClientService.KEY_HTTP_RESPONSE);
			// print a response
			System.out.println(res);
		}
		catch (ServiceException e)
		{
			e.printStackTrace();
		}
	}
}
