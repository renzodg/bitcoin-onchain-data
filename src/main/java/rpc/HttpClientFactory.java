package rpc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import rpc.config.RPCConnectionConfig;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpClientFactory {

    public static HttpClient create(RPCConnectionConfig config) {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getCredentialsProvider().setCredentials(new AuthScope(config.getRpcHost(), config.getRpcPort()),
                new UsernamePasswordCredentials(config.getRpcUser(), config.getRpcPassword()));

        return httpclient;
    }
}
