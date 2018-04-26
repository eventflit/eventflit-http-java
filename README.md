# Eventflit Java HTTP library

In order to use this library, you need to have an account on <http://eventflit.com>. After registering, you will need the application credentials for your app.

## Installation

The eventflit-http-java library is available in Maven Central:

```
<dependency>
  <groupId>com.eventflit</groupId>
  <artifactId>eventflit-http-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

## JavaDoc

Javadocs for the latest version are published at <http://eventflit.github.io/eventflit-http-java/>. Javadoc artifacts are also in Maven and should be available for automatic download and attaching by your IDE.

## Configuration

The minimum configuration required to use the `Eventflit` object are the three constructor arguments which identify your Eventflit app. You can find them by going to "API Keys" on your app at <https://panel.eventflit.com>.

```java
Eventflit eventflit = new Eventflit(appId, apiKey, apiSecret);
```

### From URL

The basic parameters may also be set from a URL, as provided (for example) as an environment variable when running on Heroku with the Eventflit addon:

```java
Eventflit eventflit = new Eventflit("http://<key>:<secret>@service.eventflit.com:80/apps/<appId>");
```

This form sets the `key`, `secret`, `appId`, `host` and `secure` (based on the protocol in the URL) parameters all at once.

### Additional options

There are additional options wich can be set on the `Eventflit` object once constructed:

#### Cluster or host

If you are using an alternative Eventflit cluster, you can provide its name to the `setCluster` method.

If you wish to set a non-standard endpoint, perhaps for testing, you may use `setHost`.

```java
eventflit.setCluster("eu");
// OR, equivalent but more flexible:
eventflit.setHost("api-eu.eventflit.com");
```

#### Timeouts

The default timeout is 4 seconds. A timeout in milliseconds to be applied to socket opens and reads can be applied, for example 10 seconds:

```java
eventflit.setRequestTimeout(10000);
```

#### SSL

HTTPS can be used as transport by calling `setEncrypted(true)`. Note that your credentials are not exposed on an unencrypted connection, however the contents of your messages are. Use this option if your messages themselves are sensitive.

#### Advanced HTTP configuration

The library uses Apache HTTP Client (4 series) internally to make HTTP requests. In order to expose some of the rich and fine configuration available in this component, it is partially exposed. The HttpClient uses the Builder pattern to specify configuration. The Eventflit library exposes a method to fetch an `HttpClientBuilder` with sensible defaults, and a method to set the client instance in use to one created by a particular builder. By using these two methods, you can further configure the client, overriding defaults or adding new settings.

For example:

##### HTTP Proxy

To set a proxy:

```java
HttpClientBuilder builder = Eventflit.defaultHttpClientBuilder();
builder.setProxy(new HttpHost("proxy.example.com"));
eventflit.configureHttpClient(builder);
```

## Usage

### General info on responses

Requests return a member of the `Result` class. Results have a `getStatus` method returning a member of `Status` which classifies their outcome, for example `Status.SUCCESS` and `Status.AUTHENTICATION_ERROR`. Error `Result`s yield a description from `getMessage`.

### Publishing events

To send an event to one or more channels use the `trigger` method.

The data parameter is serialised using the GSON library (<https://code.google.com/p/google-gson/>). POJO classes or `java.util.Map`s are suitable.

#### Single channel

```java
eventflit.trigger("channel-one", "test_event", Collections.singletonMap("message", "hello world"));
```

#### Multiple channels

```java
List<String> channels = new ArrayList<>();
channels.add("channel-one");
channels.add("channel-two");

eventflit.trigger(channels, "test_event", Collections.singletonMap("message", "hello world"));
```

You can trigger an event to at most 10 channels at once. Passing more than 10 channels will cause an exception to be thrown.

#### Excluding event recipients

In order to avoid the client that triggered the event from also receiving it, the `trigger` function takes an optional `socketId` parameter. For more information see: <http://docs.eventflit.com/publisher_api_guide/publisher_excluding_recipients>.

```java
eventflit.trigger(channel, event, data, "1302.1081607");
```

### Authenticating private channels

To authorise your users to access private channels on Eventflit, you can use the `authenticate` method. This method returns the response body which should be returned to the user requesting authentication.

```java
String authBody = eventflit.authenticate(socketId, channel);
```

For more information see: <http://docs.eventflit.com/authenticating_users>

### Authenticating presence channels

Using presence channels is similar to private channels, but you can specify extra data to identify that particular user. This data is passed as a `PresenceUser` object. As with the message data in `trigger`, the `userInfo` is serialised using GSON.

```java
String userId = "unique_user_id";
Map<String, String> userInfo = new HashMap<>();
userInfo.add("name", "Phil Leggetter");
userInfo.add("twitterId", "@leggetter");

String authBody = eventflit.authenticate(socketId, channel, new PresenceUser(userId, userInfo));
```

For more information see: <http://docs.eventflit.com/authenticating_users>

### Application state

It's possible to query the state of the application using the `get` method.

The `path` parameter identifies the resource that the request should be made to and the `parameters` parameter should be a map of additional query string key and value pairs.

For example:

#### Get the list of channels in an application

```java
Result result = eventflit.get("/channels", params);
if (result.getStatus() == Status.SUCCESS) {
    String channelListJson = result.getMessage();
    // Parse and act upon list
}
```

Information on the optional `params` and the structure of the returned JSON is defined in the [HTTP API reference](http://docs.eventflit.com/rest_api#method-get-channels).

#### Get the state of a channel

```java
Result result = eventflit.get("/channels/[channel_name]");
```

Information on the optional `params` option property and the structure of the returned JSON is defined in the [HTTP API reference](http://docs.eventflit.com/rest_api#method-get-channel).

#### Get the list of users in a presence channel

```java
Result result = eventflit.get("/channels/[channel_name]/users");
```

The `channel_name` in the path must be a [presence channel](http://docs.eventflit.com/presence). The structure of the returned JSON is defined in the [HTTP API reference](http://docs.eventflit.com/rest_api#method-get-users).

### WebHooks

The library provides a simple helper to validate the authenticity of webhooks received from Eventflit.

Call `validateWebhookSignature` with the values from the `X-Eventflit-Key` and `X-Eventflit-Signature` headers from the webhook request, and the body as a String. You will receive a member of `Validity` indicating whether the webhook could be validated or not.

### Generating HTTP API signatures

If you wanted to send the HTTP API requests manually (e.g. using a different HTTP client), you can use the `signedUri` method to generate a java.net.URI for your request which contains the necessary signatures:

```java
URI requestUri = eventflit.signedUri("GET", "/apps/<appId>/channels", null); // no body on GET request
```

Note that the URI does not include the body parameter, however the signature contains a digest of it, so the body must be sent with the request as it was presented at signature time:

```java
URI requestUri = eventflit.signedUri("POST", "/apps/<appId>/events", body);

PostRequest request = new PostRequest(requestUri);
request.setBody(body);
request.execute();
```

Additional query params may be passed to be added to the URI:

```java
URI requestUri = eventflit.signedUri("GET", "/apps/<appId>/channels", null, Collections.singletonMap("filter_by_prefix", "myprefix-"));
```

Query parameters can't contain following keys, as they are used to sign the request:

- auth_key
- auth_timestamp
- auth_version
- auth_signature
- body_md5

### Multi-threaded usage

The library is threadsafe and intended for use from many threads simultaneously. By default, HTTP connections are persistent and a pool of open connections is maintained. This re-use reduces the overhead involved in repeated TCP connection establishments and teardowns.

IO calls are blocking, and if more threads make requests at one time than the configured maximum pool size, they will wait for a conneciton to become idle. By default there are at most 2 concurrent connections maintained. This should be enough to many use cases, but it can be configured:

```java
PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
connManager.setDefaultMaxPerRoute(maxConns);

eventflit.configureHttpClient(
    Eventflit.defaultHttpClientBuilder()
          .setConnectionManager(connManager)
);
```

## License

This code is free to use under the terms of the MIT license.
