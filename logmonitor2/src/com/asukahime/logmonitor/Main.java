package com.asukahime.logmonitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.asukahime.logmonitor.LogMonitorConstant.*;


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

            createTimeoutServerIPAndReturnTimePairList(lineList, Integer.parseInt(args[1]))
                    .forEach(pair -> pair.getRight().forEach(seconds -> {
                        System.out.println("IP : "
                                + pair.getLeft()
                                + ", SECONDS_TO_RETURN : "
                                + seconds);
                    }));
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

        if (IntStream.range(0, args[1].length()).anyMatch(i -> !Character.isDigit(args[1].charAt(i)))){
            throw new LogMonitoringException(MESSAGE_INVALID_ARG_1);
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
     * @return left:サーバーIP,right:復帰までの秒数
     */
    private List<Pair<String, List<Long>>> createTimeoutServerIPAndReturnTimePairList(final List<String> lineList, final int notAcceptableCount) {

        return lineList
                .stream()
                .map(line -> line.split(LINE_DELIMITER))
                .collect(Collectors.groupingBy(line -> line[1], Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new Pair<>(entry.getKey(), calcBetweenTimeout(entry.getValue(), notAcceptableCount)))
                .filter(pair -> !pair.getRight().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * サーバーがタイムアウトしている場合、復帰するまでの秒数を返却します。
     *
     * @param lineList 各行
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
}
