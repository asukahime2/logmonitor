# LogMonitor
ログファイルを解析し、故障状態のサーバアドレスとそのサーバの故障期間を標準出力します。

ただし、pingの応答時間が第二引数に指定した故障判定回数だけ連続した場合、故障とみなします。

また、直近の平均応答時間が特定ミリ秒を超えた場合は、サーバが過負荷状態になっているとみなし、過負荷になった期間を出力します。ファイルの末尾で過負荷状態が継続していた場合は出力しません。

# 必要環境
Java 11

# 利用方法

```bash
git clone https://github.com/asukahime2/logmonitor.git
cd logmonitor3/out/artifacts/logmonitor_jar
java -jar logmonitor.jar ログファイルパス 故障判定回数 応答時間を平均する割り数 直近の平均応答時間の閾値
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
![image](https://user-images.githubusercontent.com/87558811/126055752-853d61e5-23cd-44dd-82d3-4d954db8694c.png)
