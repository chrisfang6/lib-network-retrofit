package net.chris.lib.network.retrofit;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class BaseNetworkModuleHelper {

    protected final HttpUrl baseUrl;
    protected final boolean debug;

    public BaseNetworkModuleHelper(@NonNull final HttpUrl baseUrl,
                                   @NonNull final boolean debug) {
        this.baseUrl = baseUrl;
        this.debug = debug;
    }

    @NonNull
    public Retrofit.Builder getRetrofitBuilder() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(this.baseUrl)
                .client(getOkHttpClient());
        List<Converter.Factory> converters = getConverterFactory();
        for (Converter.Factory converter : converters) {
            builder.addConverterFactory(converter);
        }
        List<CallAdapter.Factory> adapters = getCallAdapterFactory();
        for (CallAdapter.Factory adapter : adapters) {
            builder.addCallAdapterFactory(adapter);
        }
        return builder;
    }

    /**
     * @return Default: JacksonConverterFactory
     */
    @NonNull
    protected List<Converter.Factory> getConverterFactory() {
        List<Converter.Factory> converters = new ArrayList<>();
        converters.add(JacksonConverterFactory.create(getObjectMapper()));
        return converters;
    }

    @NonNull
    public ObjectMapper getObjectMapper() {
        final ObjectMapper providedMapper = new ObjectMapper();
        providedMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        providedMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
        providedMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        providedMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        providedMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return providedMapper;
    }

    /**
     * @return Default: RxJava2CallAdapterFactory
     */
    @NonNull
    protected List<CallAdapter.Factory> getCallAdapterFactory() {
        List<CallAdapter.Factory> adapters = new ArrayList<>();
        adapters.add(RxJava2CallAdapterFactory.create());
        return adapters;
    }

    @NonNull
    private OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    @NonNull
    protected OkHttpClient.Builder getOkHttpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(getWriteTimeout(), TimeUnit.SECONDS);
        addInterceptor(builder);
        addNetworkInterceptor(builder);
        return builder;
    }

    @NonNull
    protected Interceptor getHeadersInterceptor() {
        return new BaseHeadersInterceptor();
    }

    @NonNull
    protected BaseHeadersInterceptor getNetworkHeadersInterceptor() {
        return null;
    }

    @NonNull
    protected Interceptor getLogInterceptor() {
        return null;
    }

    @NonNull
    protected Interceptor getNetworkLogInterceptor() {
        return new HttpLoggingInterceptor().setLevel(debug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.HEADERS);
    }

    @NonNull
    protected List<Interceptor> getExtraInterceptors() {
        return null;
    }

    @NonNull
    protected List<Interceptor> getExtraNetworkLogInterceptors() {
        return null;
    }

    protected int getConnectTimeout() {
        return 120;
    }

    protected int getReadTimeout() {
        return 120;
    }

    protected int getWriteTimeout() {
        return 120;
    }

    private void addInterceptor(@NonNull final OkHttpClient.Builder builder) {
        final List<Interceptor> interceptors = new ArrayList<>();
        Interceptor headersInterceptor = getHeadersInterceptor();
        if (headersInterceptor != null) {
            interceptors.add(headersInterceptor);
        }
        Interceptor logInterceptor = getLogInterceptor();
        if (logInterceptor != null) {
            interceptors.add(logInterceptor);
        }
        List<Interceptor> extra = getExtraInterceptors();
        if (extra != null && extra.size() > 0) {
            interceptors.addAll(extra);
        }
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
    }

    private void addNetworkInterceptor(@NonNull final OkHttpClient.Builder builder) {
        final List<Interceptor> interceptors = new ArrayList<>();
        Interceptor headersInterceptor = getNetworkHeadersInterceptor();
        if (headersInterceptor != null) {
            interceptors.add(headersInterceptor);
        }
        Interceptor logInterceptor = getNetworkLogInterceptor();
        if (logInterceptor != null) {
            interceptors.add(logInterceptor);
        }
        List<Interceptor> extra = getExtraNetworkLogInterceptors();
        if (extra != null && extra.size() > 0) {
            interceptors.addAll(extra);
        }
        for (Interceptor interceptor : interceptors) {
            builder.addNetworkInterceptor(interceptor);
        }
    }
}
