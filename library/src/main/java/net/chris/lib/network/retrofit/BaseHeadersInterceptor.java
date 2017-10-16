package net.chris.lib.network.retrofit;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BaseHeadersInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(extraHeaders(chain.request()));
    }

    protected Request extraHeaders(@NonNull final Request request) {
        return request;
    }

}
