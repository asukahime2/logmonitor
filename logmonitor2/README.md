# LogMonitor
ログファイルを解析し、故障状態のサーバアドレスとそのサーバの故障期間を標準出力します。

ただし、pingの応答時間が第二引数に指定した故障判定回数だけ連続した場合、故障とみなします。

# 必要環境
Java 11

# 利用方法

```bash
git clone https://github.com/asukahime2/logmonitor.git
cd logmonitor2/out/artifacts/logmonitor2_jar
java -jar logmonitor2.jar ログファイルパス 故障判定回数
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
![image](https://user-images.githubusercontent.com/87558811/126045256-c4496815-099c-4a0a-8f23-251fdf87542b.png)
