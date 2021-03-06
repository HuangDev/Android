/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ignition.support.http;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

/**
 * abstract:
 * 
 * 
 */
public abstract class IgnitedHttpRequestBase implements IgnitedHttpRequest,
		ResponseHandler<IgnitedHttpResponse> {
	// change: from 5 to 3
	private static final int MAX_RETRIES = 2;

	protected static final String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

	protected List<Integer> expectedStatusCodes = new ArrayList<Integer>();

	protected IgnitedHttpClient ignitedHttp;

	protected AbstractHttpClient httpClient;

	protected HttpUriRequest request;

	protected int maxRetries = MAX_RETRIES;

	private int oldSocketTimeout, oldConnTimeout;
	private boolean timeoutChanged;

	private int executionCount;

	IgnitedHttpRequestBase(IgnitedHttpClient http) {
		this.ignitedHttp = http;
		this.httpClient = http.getHttpClient();
	}

	/**
	 * @return such HttpGet, HttpPost ...
	 */
	public HttpUriRequest unwrap() {
		return request;
	}

	public String getRequestUrl() {
		return request.getURI().toString();
	}

	public IgnitedHttpRequestBase expecting(Integer... statusCodes) {
		expectedStatusCodes = Arrays.asList(statusCodes);
		return this;
	}

	public IgnitedHttpRequestBase retries(int retries) {
		if (retries < 0) {
			this.maxRetries = 0;
		} else if (retries > MAX_RETRIES) {
			this.maxRetries = MAX_RETRIES;
		} else {
			this.maxRetries = retries;
		}
		return this;
	}

	public IgnitedHttpRequest withTimeout(int timeout) {
		oldSocketTimeout = httpClient.getParams().getIntParameter(
				CoreConnectionPNames.SO_TIMEOUT,
				IgnitedHttpClient.DEFAULT_SOCKET_TIMEOUT);
		oldConnTimeout = httpClient.getParams().getIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT,
				IgnitedHttpClient.DEFAULT_WAIT_FOR_CONNECTION_TIMEOUT);

		ignitedHttp.setSocketTimeout(timeout);
		ignitedHttp.setConnectionTimeout(timeout);

		timeoutChanged = true;
		return this;
	}

	public IgnitedHttpResponse send() throws ConnectException {

		IgnitedHttpRequestRetryHandler retryHandler = new IgnitedHttpRequestRetryHandler(
				maxRetries);

		// tell HttpClient to user our own retry handler
		httpClient.setHttpRequestRetryHandler(retryHandler);

		HttpContext context = new BasicHttpContext();

		// Grab a coffee now and lean back, I'm not good at explaining stuff.
		// This code realizes
		// a second retry layer on top of HttpClient. Rationale:
		// HttpClient.execute sometimes craps
		// out even *before* the HttpRequestRetryHandler set above is called,
		// e.g. on a
		// "Network unreachable" SocketException, which can happen when failing
		// over from Wi-Fi to
		// 3G or vice versa. Hence, we catch these exceptions, feed it through
		// the same retry
		// decision method *again*, and align the execution count along the way.
		boolean retry = true;
		IOException cause = null;
		while (retry) {
			try {
				return httpClient.execute(request, this, context);
			} catch (IOException e) {
				cause = e;
				retry = retryRequest(retryHandler, cause, context);
			} catch (NullPointerException e) {
				// there's a bug in HttpClient 4.0.x that on some occasions
				// causes
				// DefaultRequestExecutor to throw an NPE, see
				// http://code.google.com/p/android/issues/detail?id=5255
				cause = new IOException("NPE in HttpClient" + e.getMessage());
				retry = retryRequest(retryHandler, cause, context);
			} finally {
				// if timeout was changed with this request using withTimeout(),
				// reset it
				if (timeoutChanged) {
					ignitedHttp.setConnectionTimeout(oldConnTimeout);
					ignitedHttp.setSocketTimeout(oldSocketTimeout);
				}
			}
		}

		// no retries left, crap out with exception
		ConnectException ex = new ConnectException();
		ex.initCause(cause);
		throw ex;
	}

	private boolean retryRequest(IgnitedHttpRequestRetryHandler retryHandler,
			IOException cause, HttpContext context) {
		Log.e(IgnitedHttpClient.LOG_TAG,
				"Intercepting exception that wasn't handled by HttpClient");
		executionCount = Math.max(executionCount,
				retryHandler.getTimesRetried());
		return retryHandler.retryRequest(cause, ++executionCount, context);
	}

	public IgnitedHttpResponse handleResponse(HttpResponse response)
			throws IOException {
		int status = response.getStatusLine().getStatusCode();
		if (expectedStatusCodes != null && !expectedStatusCodes.isEmpty()
				&& !expectedStatusCodes.contains(status)) {
			throw new HttpResponseException(status, "Unexpected status code: "
					+ status);
		}
		return new IgnitedHttpResponseImpl(response);
	}
}
