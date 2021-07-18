package com.asukahime.logmonitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.asukahime.logmonitor.LogMonitorConstant.*;
import static com.asukahime.logmonitor.LogMonitorConstant.INDEX_CONFIRM_DATE;


public class Main {

    private static final Pattern pattern = Pattern.compile(LINE_REGEXP);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final Predicate<String> predicate = (arg) -> {
        Matcher matcher = pattern.matcher(arg);
        return matcher.matches();
    };

    public static void main(String[] args) {
        final Main instance = new Main();

        instance.doProcess(args);
    }

    /**
     * 主処理
     *
     * @param args コマンドライン引数
     */
    public void doProcess(String[] args) throws LogMonitoringException{

        try {
            checkArgs(args);
            final Path path = Paths.get(args[0]);

            final List<String> lineList = Files.lines(path).collect(Collectors.toList());
            checkFileFormat(lineList);

            createTimeoutServerIPAndReturnTimePairList(lineList, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]))
                    .forEach(pair -> {
                        final Pair<List<Long>, List<String>> tmpPair = pair.getRight();
                        tmpPair.getLeft().forEach(seconds ->
                            System.out.println("IP : "
                                    + pair.getLeft()
                                    + ", SECONDS_TO_RETURN : "
                                    + seconds));

                        tmpPair.getRight().forEach(period ->
                            System.out.println("IP : "
                                    + pair.getLeft()
                                    + ", OVERLOAD_PERIOD : "
                                    + period));
                    });
        } catch (IOException e) {
            System.out.println(MESSAGE_FILE_CAN_NOT_READ);
        } catch (LogMonitoringException e) {
            System.out.println(e.getMessage());
        }
    }

    private void checkArgs(final String[] args) {
        if (args.length != VALID_NUMBER_OF_ARGS) {
            throw new LogMonitoringException(MESSAGE_INVALID_ARGS);
        }

        for (int i = 1; i < 4; i++) {
            final int argIndex = i;
            if (IntStream.range(0, args[argIndex].length()).anyMatch(index -> !Character.isDigit(args[argIndex].charAt(index)))){
                throw new LogMonitoringException(MESSAGE_INVALID_ARG_1);
            }
        }
    }

    /**
     * ファイルの全行が指定のフォーマットであることを確認します。
     *
     * @param lineList ファイルの各行を要素とするリスト
     */
    private void checkFileFormat(final List<String> lineList) {

        if (!lineList.stream().allMatch(predicate)) {
            throw new LogMonitoringException(MESSAGE_INVALID_FORMAT);
        }
    }

    /**
     * notAcceptableCountの回数以上連続してタイムアウトしたサーバーIPと復帰までの秒数のPairのリストを返却します
     *
     * @param lineList ログファイルの各行
     * @param notAcceptableCount 故障とみなされるタイムアウト回数
     * @return left:サーバーIP,right:復帰までの秒数と過負荷期間のペア
     */
    private List<Pair<String, Pair<List<Long>, List<String>>>> createTimeoutServerIPAndReturnTimePairList(
            final List<String> lineList,
            final int notAcceptableCount,
            final int averageCount,
            final int notAcceptableMillis) {

        return lineList
                .stream()
                .map(line -> line.split(LINE_DELIMITER))
                .collect(Collectors.groupingBy(line -> line[1], Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry ->
                        new Pair<>(
                                entry.getKey(),
                                new Pair<>(
                                        calcBetweenTimeout(entry.getValue(), notAcceptableCount),
                                        calcOverloadPeriod(entry.getValue(), averageCount, notAcceptableMillis))))
                .collect(Collectors.toList());
    }

    /**
     * サーバーがタイムアウトしている場合、復帰するまでの秒数を返却します。
     *
     * @param lineList 各行
     * @param notAcceptableCount 故障とみなされるタイムアウト回数
     * @return 各サーバーがタイムアウトから復帰するまでの秒数をリストで返却。タイムアウトしていない場合は空のリスト。
     */
    private List<Long> calcBetweenTimeout(final List<String[]> lineList, final int notAcceptableCount) {

        final List<Long> timeoutSeconds = new ArrayList<>();

        LocalDateTime timeoutDateTime = null;
        boolean isTimeout = false;

        int timeoutCount = 0;
        for (final String[] line : lineList) {

            // 連続するタイムアウトの初回のみ時刻を記録
            // 連続するタイムアウト回数をインクリメント
            if (TIMEOUT_LETTER.equals(line[INDEX_RETURN_TIME])) {
                isTimeout = true;
                timeoutCount++;

                if (timeoutDateTime == null) {
                    timeoutDateTime = LocalDateTime.parse(line[INDEX_CONFIRM_DATE], DATE_FORMATTER);
                }
                continue;
            }

            if (!isTimeout) {
                continue;
            }

            // タイムアウトから復帰するまでの連続タイムアウト回数が非許容回数以上の場合、復帰までの秒数を記録
            if (timeoutCount >= notAcceptableCount) {
                timeoutSeconds.add(ChronoUnit.SECONDS.between(timeoutDateTime, LocalDateTime.parse(line[INDEX_CONFIRM_DATE], DATE_FORMATTER)));
            }

            // 記録用変数を初期化
            timeoutDateTime = null;
            isTimeout = false;
            timeoutCount = 0;
        }

        return timeoutSeconds;
    }

    /**
     * 過負荷状態になっていた期間のリストを返却します。
     *
     * @param lineList 各行
     * @param averageCount 指定平均回数
     * @param notAcceptableMillis 過負荷とみなされるミリ秒数
     * @return 過負荷になっていた期間を文字列化したリスト(yyyyMMddHHmmss-yyyyMMddHHmmss)
     */
    private List<String> calcOverloadPeriod(
            final List<String[]> lineList,
            final int averageCount,
            final int notAcceptableMillis) {

        final List<String> overloadPeriodList = new ArrayList<>();

        final Deque<Pair<Integer, Long>> latestResponseTimes = new ArrayDeque<>();
        boolean isOverload = false;
        String overloadStart = null;

        for (int i = 0; i < lineList.size(); i++) {
            final String[] line = lineList.get(i);

            if (TIMEOUT_LETTER.equals(line[INDEX_RETURN_TIME])) {
                continue;
            }

            latestResponseTimes.addLast(new Pair<>(i, Long.valueOf(line[INDEX_RETURN_TIME])));

            // 指定された回数に到達
            if (latestResponseTimes.size() >= averageCount) {
                final double averageTime = latestResponseTimes.stream()
                        .map(Pair::getRight)
                        .collect(Collectors.averagingLong(a -> a));

                // 平均が許容秒数を超えている場合
                if (averageTime > (double) notAcceptableMillis) {
                    if (!isOverload) {
                        // 最初の要素のインデックスを用いて日時を取得
                        overloadStart = lineList.get(latestResponseTimes.getFirst().getLeft())[INDEX_CONFIRM_DATE];
                    }

                    isOverload = true;
                } else {
                    // 過負荷状態が終了
                    if (isOverload) {
                        overloadPeriodList.add(overloadStart + "-" + lineList.get(i - 1)[INDEX_CONFIRM_DATE]);
                        overloadStart = null;
                    }

                    isOverload = false;
                }

                latestResponseTimes.removeFirst();
            }
        }

        return overloadPeriodList;
    }
}
