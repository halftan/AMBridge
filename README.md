# AMBridge

A bridge between Apple Music and Zeekr's automotive OS. (not even a prototype)

## Current state

Third party developers are not able to get a binder to **CoreService** (like hvac, mediacenter, etc).

You need to give this app permission to fetch notifications in order for playback info to work (artist, title, album, artwork)

### What's implemented

- Playback control
- Media info display (need permission to get notifications)


### Caveats

1. Use `Log.e` because Zeekr OS only shows `error` level in production vehicles.
2. ecarx openapi has several caller verification:
    1. Package name got SHA1-ed and compared with the appKey in your metadata, appSign in `license.txt`
    2. You need to have an encrypted `license.txt` in your assets
    3. The public key used to decrypt `license.txt` can be found in CoreService.apk (extract it from your vehicle)
    4. Your package name and app uid also got verified. (`getPackageName()` must in `getPackagesForUid`)
    5. The decrypted `license.txt` looks like this:
  <details>
  <summary>Expand to see JSON</summary>

```json
{
  "appId": "<redacted>",
  "appSign": [
    {
      "supplier": "beta",
      "appsign": "<redacted>"
    },
    {
      "supplier": "release",
      "appsign": "<redacted>"
    }
  ],
  "apiSign": "<redacted>",
  "apiList": [
    {
      "master": "basicability",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "bt",
      "slave": [
        {
          "name": "bt",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        },
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "camera",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "device",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        },
        {
          "name": "device",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "mediacenter",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        },
        {
          "name": "mediacenter",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "multimedia",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "navi",
      "slave": [
        {
          "name": "navi",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "policy",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        },
        {
          "name": "policy",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "user",
      "slave": [
        {
          "name": "user",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "vehicle",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        },
        {
          "name": "status",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "vr_all",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            },
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    },
    {
      "master": "视频流服务",
      "slave": [
        {
          "name": "default",
          "list": [
            {
              "api": "*",
              "expired": 0
            }
          ]
        }
      ]
    }
  ]
}
```

</details>
