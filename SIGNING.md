# Debug 签名（覆盖安装）

Debug APK **必须**用仓库里的这份钥匙，不要用机器上的默认 `~/.android/debug.keystore`。云端 VM 一换，默认钥匙就会变，覆盖安装会变成 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`。

| 项 | 值 |
|---|---|
| 文件 | `app/debug.keystore`（已进 git，勿重新生成、勿替换） |
| alias | `androiddebugkey` |
| store 密码 | `android` |
| key 密码 | `android` |
| 包名 | `com.shijiankuai.app.debug` |

`app/build.gradle.kts` 的 `signingConfigs.debug` 已钉死上述路径和密码。`./gradlew assembleDebug` 只走这份 keystore。

证书 SHA-256（与当前可安装包一致）：

```
bf5405b2bfd918cb133cba0a4d309de7fd35ffd63372e2a822471355cf962066
```

核对：

```bash
apksigner verify --print-certs app/build/outputs/apk/debug/app-debug.apk
```

`keytool -printcert -jarfile` 对这份 v2-only APK 会报 Not a signed jar，以 `apksigner` 为准。
