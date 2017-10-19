package net.chris.lib.network.retrofit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.GzipSource;
import okio.Okio;

/**
 * @see {@link okhttp3.internal.http.BridgeInterceptor} & {@code okhttp/samples/guide/src/main/java/okhttp3/recipes/RequestBodyCompression.java}
 */
public class GzipInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request finalRequest;
        Request originalRequest = chain.request();
        if (originalRequest.body() == null
                || originalRequest.header("Content-Encoding") != null) {
            finalRequest = originalRequest;
        } else {
            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
            finalRequest = compressedRequest;
        }
        Response response = chain.proceed(finalRequest);
        Response finalResponse;
        String contentEncoding = response.header("Content-Encoding");
        if (contentEncoding != null
                && contentEncoding.toLowerCase().contains("gzip")
                && HttpHeaders.hasBody(response)) {
            final ResponseBody body = new RealResponseBody(response.body().contentType().toString(),
                    response.body().contentLength(),
                    Okio.buffer(new GzipSource(response.body().source())));
            finalResponse = response.newBuilder()
                    .headers(response.headers().newBuilder()
                            .removeAll("Content-Encoding")
                            .removeAll("Content-Length")
                            .build())
                    .body(body)
                    .build();
        } else {
            finalResponse = response;
        }
        return finalResponse;
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }

}
