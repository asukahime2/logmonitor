# LogMonitor
ログファイルを解析し、故障状態のサーバアドレスとそのサーバの故障期間を標準出力します。

ただし、pingの応答時間が第二引数に指定した故障判定回数だけ連続した場合、故障とみなします。

また、同サブネットの全IPアドレスが故障とみなされた場合、サブネットの故障としてサブネットIPと故障期間を出力します。なお、故障期間は全IPのタイムアウトが初めて発生した時刻から、少なくとも1台が故障から復帰した時刻までとなります。

# 必要環境
Java 11

# 利用方法

```bash
git clone https://github.com/asukahime2/logmonitor.git
cd logmonitor4/out/artifacts/logmonitor4_jar
java -jar logmonitor.jar ログファイルパス 故障判定回数
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
![image](https://user-images.githubusercontent.com/87558811/126059288-2818f760-b832-449b-a174-27215ca702bf.png)