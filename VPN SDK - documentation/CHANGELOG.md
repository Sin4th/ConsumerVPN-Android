# CHANGELOG

## v1.6.2
* Adds new OpenVPN certificate file. The previous certificate will expire on March 2023.

## v1.6.1
* Adds custom DNS configuration for all protocols; when set, the VPN will use the provided server
  to resolve all the incoming requests.

The default value is `null`, which will tell to the VPN to
use the DNS server provided by the VPN server.

If using a DNS in the local network, please ensure to enable `local lan` in
order to the VPN to reach it.

*Warning*: Is encouraged to avoid using a custom DNS as this could compromise the privacy of the
user. A common use case for this property is tech savvy users who own a DNS server (with AD blockers
or adult content blockers) and knowledge the risk of using it.

Using the DNS server provided by the VPN server ensures total privacy.


Usage:

```kotlin

    // External dns
    val customDns = "1.1.1.3" // Cloudfare no malware no adult content
    
    VpnConnectionConfiguration
        .Builder(username, password)
        .connectionProtocol(VpnConnectionProtocolOptions.WireGuard)
        ...
        .dns(IpAddress(customDns))
        .build()


    // Local network dns
    val customDns = "192.168.1.23" // A local server with PiHole or AdGuard

    VpnConnectionConfiguration
        .Builder(username, password)
        .connectionProtocol(VpnConnectionProtocolOptions.WireGuard)
        ... 
        .isLocalLanAllowed(true)  // Enable this to allow the vpn to reach the server
        .dns(IpAddress(customDns))
        .build()
    
```

## v1.6.0
* Adds Support to target SDK target 31

## v.1.5.13
* Add SdkConfig.version attribute.
* Updated documentation.

## v1.5.12
* Disables IPv6 support for IKEv2

## v1.5.11
* Fixed typo on VpnConfiguration's argument `reconnectOn`

## v1.5.10

### Features
* Added `apiHostMirrorConfiguration(...)` to the SDK configuration. With this configuration
if the main API host fails to respond the request, the SDK will retry it with the given list of host mirrors.
In addition to that, there is an option to stop the mirror call flow when a certain HTTP code is
returned by the endpoint (httpBreakingCodes). e.g. An a 401 code (Unauthorized) is a client failure
which should be ignored by the mirror call flow.

A `MirrorsConfiguration` is needed to call this method:

```kotlin
MirrorsConfiguration(
    mirrorUrlList.  //A list of URL objects with the host mirrors 
    httpBreakingCodes  //A list of integers with Http codes, this is optional an defaults to [401,409]
) 
```

* Added `openvpn_config_extras.txt` file. This file can be overridden to add extra configurations to OpenVpn.

## v1.5.9

### Features
* Added new Failure `NetworkErrorFailure` thrown whenever there's a network error during the 
`limits` Endpoint

* Added the possibility to change the api key after the library initialization

```kotlin
fun setApiKey(apiKey: String): Completable
```

* Added the possibility to change the authentication suffix after the
  library initialization

```kotlin
fun setAuthSuffix(authSuffix: String): Completable
```

## v1.5.8

### Features
* Added `attemptToDisconnect()` variation of `disconnect()`, will return
a `Completable` instead fo a `ICallback`, which will complete on a 
successful disconnection otherwise will emit a failure ([CouldNotDisconnectException]).
This method does not operate by default on a particular scheduler.

## v1.5.7

### Features

* Added `attemptToConnectToNearest(...)`,
`attemptToConnectToNearestRestrictedByCountry(...)`,
 `attemptToConnect(...)` variations of current `connect...` methods;
    These variations will return a `Completable` instead of a `ICallback`,
    which will complete on a successful connection
    otherwise will emit a failure ([CouldNotEstablishConnectionException]).
    These methods do not operate by default on a particular scheduler.


## v1.5.6

### Features

* Added `getUserCurrentTier()` to get user current tier status. 
This method will return a RxJava2 Single that emits a [Tier] object. 
A [Tier] is a sealed class with three different 
children([SubscriptionTier], [BandwidthTier] and [EmptyTier])
    ```kotlin
    fun getUserCurrentTier(): Single<Tier>
    ```

* Added `.apiLimitsEndpoint(...)` to sdk configuration. This configuration is required in order to 
use `getUserCurrentTier()`. Because this is handled with Retrofit, there is no need to add `/` to 
this start start of the endpoint.
    ```kotlin
    SdkConfig.Builder(...)
    .
    .
    .apiLimitsEndpoint("limits") // No need to add `/` to these endpoint
    ```

## v1.5.5

### Features

* Added support for Androidx fragments thorough `prepareVpnService(fragment: androidx.fragment.app.Fragment)`

* The call `updateServerList` also does a token refresh if the current access token is invalid

## v1.5.4

### Breaking Changes

* [Dagger v2.23.2](](https://www.google.com)) is required for this version to work correctly

* A new VpnState was added called `Disconnected Error` 

* `refreshToken()` was deprecated use instead `refreshToken(username, password)`

### Bug Fixes

### Features

* Added `setCustomGeoInfo(vpnGeoInfo)` Adds manual geo information to
the sdk. Setting this values manually invalidates the need to call 
`fetchGeoInfo()` for the load balance to work. 
    ```kotlin
    fun setCustomGeoInfo(vpnGeoData: VpnGeoData): ICallback<Boolean>
    ```


* Added `refreshToken(username, password)` Tries to refresh token
when the session token is invalid, in case of an error it will automatically
refresh the session performing a login
     ```kotlin
    fun refreshToken(username: String, password: String): Callback<VpnLoginResponse>
    ```

* Added Locale into the sdk configuration. This allows to switch 
languages to support different countries names and searches using as 
a default value `Locale("en", "US")`

    ```kotlin
     SdkConfig.Builder(ACCOUNT_NAME, API_KEY, AUTH_SUFFIX)
        // .... Other Configurations
        .locale(Locale("es", "MX"))
        .build()
    ```

* Added `updateCountriesLanguage` this allows to switch, during 
runtime, the current sdk Locale and updates the name of the countries
in the SDK database. This helps you to use different languages to
search for countries using the device language

    ```kotlin
    fun updatePopsCountryLanguage(locale: Locale): Callback<Boolean>
    ```


* Added VpnState `Disconnected Error`. This will allow handling errors 
that produce disconnections easier. The most common error on this 
scenario is "AUTH_FAILURE"

    ```kotlin
    getVpnSdk().listenToConnectState()
        .subscribe({ vpnState ->
            when (vpnState.connectionState) {
                VpnState.DISCONNECTED_ERROR -> {}
                VpnState.DISCONNECTED -> {}
                VpnState.CONNECTED -> {}
                VpnState.CONNECTING -> {}
            }
            Unit
        }, { throwable ->
            // Handle throwable in case of an error
            Unit
        })
    ```

* Added `fetchAllServersByCountryAndCity` operations to fetch servers 
filtered by country and city name. This will avoid the need to fetch 
pops first and then fetch all servers by VPN Pop

    ``` kotlin
    fun fetchAllServersByCountryAndCity(countryCode: String, city: String)
    
    fun fetchAllServersByCountryAndCity(countryCode: String, city: String, sortServer: SortServer)
    ```

## v1.5.3 

### Breaking Changes

* [Dagger v2.21](](https://www.google.com)) is required for this version to work correctly

* A new SDKConfig Constructor was added to make the log tag optional

* Added a new logTag, this new tag is used to override the Log output with any desired suffix

    ```kotlin

    //Example using a builder
     SdkConfig = SdkConfig.Builder(
            BuildConfig.ACCOUNT_NAME,
            BuildConfig.API_KEY,
            BuildConfig.AUTH_SUFFIX)
            .client(BuildConfig.CLIENT)
            .apiHost(application.getString(R.string.endpoint_main_api))
            .ipGeoUrl(BuildConfig.IP_GEO)
            .apiLoginEndpoint(BuildConfig.LOGIN_API)
            .apiTokenRefreshEndpoint(BuildConfig.REFRESH_API)
            .apiProtocolListEndpoint(BuildConfig.PROTOCOL_LIST_API)
            .apiServerListEndpoint(BuildConfig.SERVER_LIST_API)
            .logTag(MY_LOG_TAG)
            .build()

    //Example using the default constructor
    SdkConfig(
              val accountName: String,
              override val apiKey: String,
              val authSuffix: String = accountName,
              override val client: String = BuildConfig.CLIENT,
              override val apiHost: String = BuildConfig.ENDPOINT,
              override val ipGeoUrl: String = BuildConfig.IP_GEO,
              override val apiLoginEndpoint: String = BuildConfig.LOGIN_API,
              override val apiTokenRefreshEndpoint: String = BuildConfig.TOKEN_REFRESH_API,
              override val apiProtocolListEndpoint: String = BuildConfig.PROTOCOL_LIST_API,
              override val apiServerListEndpoint: String = BuildConfig.SERVER_LIST_API,
              val logTag: String
    ```

* `fetchAvailableVpnPortOptions(vpnProtocolOptions, scrambleEnabled)` was deprecated
use instead `fetchAvailableVpnPortOptions(vpnProtocolOptions, vpnConnectionProtocolOptions, scrambleEnabled)`

* Permissions will no longer be requested automatically
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    ```

* Update servers will no longer update protocols only servers

* Connections to the VPN now require a new argument `vpnRevokedNotification`.
This notification will be shown whenever the system revokes the permission for the
VPN to keep running.It is only shown when the VPN is connected and the system revokes
necessary permissions
    ```kotlin
    ICallback<Boolean> connect(vpnNotification, vpnRevokedNotification, vpnConnectionConfiguration)
    ```

### Bug fixes

* Fix for the Vpn detailed state description not showing
while listening for the disconnected state

* OpenVpn ports will now fetch ports where the cipher is not null using the new function
`fetchAvailableVpnPortOptions(vpnProtocolOptions, vpnConnectionProtocolOptions, scrambleEnabled)`

* Fixed VPN notification dismissal after several network changes

* Fixed Pops sorting functionality for search queries, now it prioritize strings starting with the query
Added the following methods for query search sorting:

    ```kotlin
    //Fetch a list of all pops sorted by query in specific order, first by city
    fetchPopsFirstByCityQuery(query: String, sortPop: SortPop)

    //Fetch a list of all pops sorted by query in specific order, first by country
    fetchPopsFirstByCountryQuery(query: String, sortPop: SortPop)

    //Fetch a list of all pops by country query, sorted and filtered by city query in specific order
    fetchPopsByCountryCodeFilterByCityQuery(countryCode: String, query: String, sortPop: SortPop)

    Fetch a list of all pops sorted by country query in specific order
    fetchPopsByCountryQuery(query: String)

    Fetch a list of all pops filtered by country code, sorted by city query in specific order
    fetchPopsByCityQuery(countryCode: String, query: String)
    ```

### Feature

* Added a listener for connection state for centralized operations.
Allows to execute custom centralized operations
that react to the VPN connection states. For example, it can be used to notify Widgets,
Quick Tiles Settings or Services. Important: Only use centralized single operations
otherwise is better to use `listenToConnectState()`
    ```kotlin
    listenToCentralizedConnectionState(vpnStateConnectionCallback)
    ```

* Added fetch available ports with connection settings.
Replaces `fetchAvailableVpnPortOptions(vpnProtocolOptions, scrambleEnabled)`
    ``` kotlin
    fetchAvailableVpnPortOptions(vpnProtocolOptions, vpnConnectionProtocolOptions, scrambleEnabled)
    ```

* Deprecated `connect(VpnPop)` in replacement of only `connectToNearest(VpnPop)`.

* Deprecated `connect(countryCode)` in replacement of only `connectToNearest(notification)`. Balancing is
    performed according to saved location in configuration object

* Deprecated `connect(notification)` in replacement of only `connectToNearest(notification)`.

* Added `connectToNearest(VpnPop)` to perform a default server balance, closest to farthest, no country limit

* Added `connectToNearest(notification)` to perform a default server balance, closest to farthest, no country limit

* Added `connectToNearestRestrictedByCountry(VpnPop)` to perform server balance limited to a given country

* Added `connectToNearestRestrictedByCountry(countryCode)` to perform server balance limited to a given country

* Added `connectToNearestRestrictedByCountry(notification)` to perform server balance limited to a given country

* Added update protocol and servers call. Replaces `updateServerList`

    ```kotlin
    fun updateServerProtocolList()
    ```

* Added update protocol list

    ```kotlin
    fun updateProtocolList()
    ```

* Added blocking thread capabilities to `ICallback` class
    ```kotlin
    fun blockCurrentThread()
    ```

* The `VpnPop -> country` parameter will be stored in English
instead of using the device `Locale`. To update the internal database use:
    ```kotlin
    fun updateServerList()
    ```

* Added two new fetch calls to retrieve all servers filtered by
country code `fetchAllServersByCountryCode`.

    ```kotlin
    fun fetchAllServersByCountryCode(countryCode:String): ICallback<List<VpnServer>>
    
    fun fetchAllServersByCountryCode(countryCode:String): ICallback<List<VpnServer>>
    ```

* The IDE will now show available docs in autocomplete window. eg. (Android Studio)

## v1.5.2

### Feature

New argument to override the MTU value when the user is on a mobile connection.

For some IPv6 only networks is necessary to override the MTU value to 1280
 to avoid issues with fragmentation, a new argument for 
 VPNConnectionConfiguration was added  `.shouldOverrideMobileMtu(true);` to
 add that behavior

```java
VpnConnectionConfiguration.Builder(
    credentials.getUsername(),
    credentials.getPassword())
    .shouldOverrideMobileMtu(true);
```


## v1.5.1

### Feature

The VPN initialization callback is now supported in Fragment's `startActivityForResult` 

Introduction of Split tunneling per application and discovery accessibility of the LAN,
 see VpnConnectionConfiguration.Builder

Introduction of Builder pattern for the VPN configuration

```java
VpnConnectionConfiguration.Builder(
                    credentials.getUsername(),
                    credentials.getPassword())
            .connectionProtocol(VpnConnectionProtocolOptions.OPENVPN)
            .vpnProtocol(VpnProtocolOptions.PROTOCOL_UDP)
            .debugLevel(0)
            .scrambleOn(false)
            .port(VpnPortOptions.PORT_443)
            .build();
```
                
### Bug Fixes
* Fixed empty Observable for logout.subscribe(), the callback is executed after the logout is made. 

## v1.5.0

### Bug Fixes

* Supports empty values for the StrongSwan protocol in case is not supported by the API
* The token is properly refreshed using the access token 
* The methods to check if an access token is valid `isAccessTokenValid` informs of expiration 
three days prior expiration for renewal.
* The load balancing now always chose the closest city 
* The MTU is set to 1280 by default in mobile networks to avoid issues with IPv6
  
## v1.4

### Feature

Added in the long awaited Strongswan integration to the SDK. This will allow for connections
using the IKEv2/IPSec protocols implemented by Strongswan. This feature can be activated with 
the new option in the VpnConnectionConfiguration object. For now OpenVPN will remain the default
connection option due to better server support.

Here is the latest VpnConnectionConfiguration constructor with defaults: 

```kotlin
VpnConnectionConfiguration(
        username: String,
        password: String,
        scrambleOn: Boolean = false,
        reconnectOn: Boolean = true,
        port: VpnPortOptions = VpnPortOptions.PORT_443,
        protocol: VpnProtocolOptions = VpnProtocolOptions.PROTOCOL_UDP,
        connectionProtocol: VpnConnectionProtocolOptions = VpnConnectionProtocolOptions.OPENVPN,
        debugLevel: Int = 5
    )
```
    
To activate Strongswan simply change the connectionProtocol value to `VpnConnectionProtocolOptions.IKEV2`.
This will require the API to support this connection protocol so ensure Strongswan is setup before
attempting.

This version also upgrades the database version and runs a migration to prepare for the new attributes
in the api to support the IKEv2 `remoteId` and `gateway` attributes.

    

## v1.3

### Features

* Added in debug level configuration option to `VpnConnectionConfiguration` default is level 3 with an available
range of 0 to 11 with 0 having no logging.
* Added in request and response logging for debugging purposes
* Deprecated `getGeoInfo()` and `fetchIpGeoLocation()` in replacement of only `fetchGeoInfo()`. 
This will eliminate the confusion of what geo call to use and will handle geo state when 
connected and disconnected behind the scenes.
* Added in `fetchAvailableVpnPortOptions()` to get the available ports you can make for connections.
* Fetch calls have been upgraded with sort options. This will add in ORDER BY statements to your queries.
See the SortPop and SortServer objects for reference. They can be used like so:
    ```kotlin
    // NAME is table column, ASC is table row order
    fetchAllServers(SortServer(SortServerOption.NAME, SortOrder.ASC))
    ```
* Added in 3 new Auth helpers:
    ```kotlin
    // Is current stored access token still valid?
    fun isAccessTokenValid(): Boolean
    
    // Is access token valid and not expired?
    fun isUserLoggedIn(): Boolean
    
    // Refresh the token using the stored refresh token
    fun refreshToken()
    ```
    
### Bug Fixes

* Login request will now properly pass along error when the login attempt has failed
* Fixed an issue where configuration generation sometimes created duplicate options
* Improved state handling and suppressed connection events that could lead to a faulty disconnect event
* Better state pushes to avoid redundancy

## v1.2

### Features

* Added data usage record to the `listenToConnectState` method
* Added in new method to retrieve information about the connected server:
    ```kotlin
    fetchConnectionInfo() : ICallback<VpnConnectionInfo>
    ```
* Added in new connect overload method that allows you to connect without selecting
a server or pop. SDK will use your geolocation to determine best server and pop.
    ```kotlin
    fun connect(notification: VpnNotification,
                             configuration: VpnConnectionConfiguration): ICallback<Boolean>
    ```
* Added in a new connect overload method that allows you to connect by two letter country code
    ```kotlin
    fun connect(countryCode: String,
                             notification: VpnNotification,
                             configuration: VpnConnectionConfiguration): ICallback<Boolean>
    ```

## v1.1

### Features

* Added the following fetch calls
    ```kotlin
    fun fetchServerByName(name: String): ICallback<VpnServer>

    fun fetchPopByName(name: String): ICallback<VpnPop>

    fun fetchAllPopsByCountryCode(countryCode: String) : ICallback<List<VpnPop>>

    fun fetchPopByCountryCodeAndCity(countryCode: String, city: String): ICallback<VpnPop>
    ```
* Failed login attempts will now return an error message in the throwable
* Added Server capacity to VpnServer model
* DeviceInfo is now publicly accessible through `getDeviceInfo()`
* AuthInfo is now publicly accessible through `getAuthInfo()`
* AccountInfo is now publicly accessible through `getAccountInfo()`
* GeoInfo is now publicly accessible through `getGeoInfo()`

### Bug Fixes

* SdkConfig should only require `accountName` and `apiKey` when implementing in java
* Fixed bug where `fetchAllServers()` and `fetchAllServersByPop()` would only return 1 result
* Fixed the Callback subscribe method so the callbacks are optional in both java and kotlin
* The SDK `init()` method no longer requires calling the companion object when implementing in java

[Dagger]: http://www.reddit.com
