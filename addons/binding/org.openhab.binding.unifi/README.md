# UniFi Binding

This binding integrates with [Ubiquiti UniFi Networks](https://www.ubnt.com/products/#unifi) allowing for presence detection of network clients.


## Supported Things

* UniFi Controller 
* UniFi Wireless Client (any wireless client connected to a UniFi wireless network)


## Discovery

Discovery is not currently supported.


## Binding Configuration
 
The binding has no configuration options, all configuration is done at the Bridge and Thing levels.

 
## Bridge Configuration

You need at least one UniFi Controller (Bridge) for this binding to work. It requires a network accessible instance of the [Ubiquiti Networks Controller Software](https://www.ubnt.com/download/unifi).    

The following table describes the Bridge configuration parameters:

| Parameter                | Description                                    | Config   | Default |
| ------------------------ | ---------------------------------------------- |--------- | ------- |
| host                     | Hostname of IP address of the UniFi Controller | Required | -       |
| port                     | Port of the UniFi Controller                   | Required | -       |
| username                 | The username to access the UniFi Controller    | Required | -       |
| password                 | The password to access the UniFi Controller    | Required | -       |
| refresh                  | Refresh interval in seconds                    | Optional | 10      |
| considerHome<sup>1</sup> | Consider home interval in seconds              | Optional | 180     |

<sup>1</sup> The `considerHome` parameter allows you to control how quickly the binding marks a client as away. For example, using the default of `180` (seconds), the binding will report a client away as soon as `lastSeen` + `180` (seconds) < `now`

## Thing Configuration

You must define a UniFi Controller (Bridge) before defining UniFi Wireless Clients (Things) for this binding to work.

The following table describes the Thing configuration parameters:

| Parameter               | Description                                             | Config   | Default | Options    |
| ----------------------- | ------------------------------------------------------- |--------- | ------- |----------- |
| mac                     | The MAC address of the Wireless Client                  | Required | -       | -          |
| site<sup>1</sup>        | The site name where the Wireless Client should be found | Optional | -       | -          |
| contactType<sup>2</sup> | The contact type for the online channel                 | Optional | `NO`    | `NO`, `NC` |

<sup>1</sup> The `site` configuration parameter is optional. If you leave it blank, the Wireless Client will appear `ONLINE` if found in *any* site defined on the UniFi Controller. 

You may use the `site` parameter as a filter if you only want the Wireless Client to appear home if it's found in the UniFi Site defined in the `site` parameter.

Additionally, you may use friendly site names as they appear in the controller web UI.

<sup>2</sup> The `contactType` parameter allows you to control the normal (default) state of the `online` channel. The normal state is when the wireless client is absent.

Normally Open (NO) means the `Contact` is `OPEN` when the client is absent; it is `CLOSED` when the client is present.

Normally Closed (NC) means the `Contact` is `CLOSED` when the client is absent; it is `OPEN` when the client is present. 

## Channels

The Wireless Client information that is retrieved is available as these channels:

| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| online<sup>1</sup> | Contact | Online status of the client |
| ap | String | Access point (AP) the client is connected to |
| essid | String | Network name (ESSID) the client is connected to |
| rssi | Number | Received signal strength indicator (RSSI) of the client |
| site | String | Site name (from the controller web UI) the client is associated with |
| uptime | Number | Uptime of the wireless client (in seconds) |
| lastSeen | DateTime | Date and Time the wireless client was last seen |

<sup>1</sup> The `online` channel's normal (default) `Contact` state can be configured via the `contactType` parameter

*Note: All channels are read-only*

## Full Example

things/unifi.things

```
Bridge unifi:controller:home "UniFi Controller" [ host="unifi", port=8443, username="$username", password="$password", refresh=10, considerHome=180 ] {
	Thing client matthewsPhone "Matthew's iPhone" [ mac="$mac", contactType="NO" ]
}
```

Replace `$user`, `$password` and `$mac` accordingly. `contactType` should be `NO` (normally open) or `NC` (normally closed)

items/unifi.items

```
Contact  MatthewsPhone           "Matthew's iPhone [MAP(unifi.map):%s]"             { channel="unifi:client:home:matthewsPhone:online" }
String   MatthewsPhoneSite       "Matthew's iPhone: Site [%s]"                      { channel="unifi:client:home:matthewsPhone:site" }
String   MatthewsPhoneAP         "Matthew's iPhone: AP [%s]"                        { channel="unifi:client:home:matthewsPhone:ap" }
String   MatthewsPhoneESSID      "Matthew's iPhone: ESSID [%s]"                     { channel="unifi:client:home:matthewsPhone:essid" }
Number   MatthewsPhoneRSSI       "Matthew's iPhone: RSSI [%d]"                      { channel="unifi:client:home:matthewsPhone:rssi" }
Number   MatthewsPhoneUptime     "Matthew's iPhone: Uptime [%d]"                    { channel="unifi:client:home:matthewsPhone:uptime" }
DateTime MatthewsPhoneLastSeen   "Matthew's iPhone: Last Seen [%1$tH:%1$tM:%1$tS]"  { channel="unifi:client:home:matthewsPhone:lastSeen" } 
```

transform/unifi.map (using a `NO` contact, swap if you're using `NC` contacts)

```
OPEN=Away
CLOSED=Home
```

sitemaps/unifi.sitemap

```
sitemap unifi label="UniFi Binding"
{
	Frame {
		Text item=MatthewsPhone
		Text item=MatthewsPhoneSite
		Text item=MatthewsPhoneAP
		Text item=MatthewsPhoneESSID
		Text item=MatthewsPhoneRSSI
		Text item=MatthewsPhoneUptime
		Text item=MatthewsPhoneLastSeen
	}
}
```
