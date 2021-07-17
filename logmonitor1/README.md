# LogMonitor
ログファイルを解析し、故障状態のサーバアドレスとそのサーバの故障期間を標準出力します。

# 必要環境
Java 11

# 利用方法

```bash
git clone https://github.com/asukahime2/logmonitor.git
cd logmonitor1/out/artifacts/logmonitor_jar
java -jar logmonitor.jar ログファイルパス
```

# 注意事項
ログファイルのフォーマットは
yyyyMMddHHmmss,ネットワークプレフィックス長付きのIPv4アドレス,pingの応答時間(ミリ秒)
※ pingの応答時間は、pingがタイムアウトした場合"-"

```
20210717103010,10.0.0.0/16,-
20210717103010,10.0.0.0/16,50
```

# テスト結果
![image](https://user-images.githubusercontent.com/87558811/126040608-faa7a6b1-e4a7-45f1-8e8f-90859d2fa546.png)
