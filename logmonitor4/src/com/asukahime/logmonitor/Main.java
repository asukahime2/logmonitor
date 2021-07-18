package com.asukahime.logmonitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
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

    private static final Function<String[], String> extractSubnet = (arg1) -> {
        final String fullIP = arg1[INDEX_SERVER_IP];
        final String[] ipArray = fullIP.substring(0, fullIP.indexOf("/")).split("\\.");
        final int prefixLength = Integer.parseInt(fullIP.substring(fullIP.indexOf("/") + 1));

        String subnet;
        switch(prefixLength) {
            case 8: subnet = ipArray[0] + ".***.***.***";break;
            case 16: subnet = ipArray[0] + "." + ipArray[1] + ".***.***";break;
            case 24: subnet = ipArray[0] + "." + ipArray[1] + "." + ipArray[2] + ".***";break;
            default : subnet = "";
        }

        return subnet;
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
                    .forEach(pair -> pair.getRight().forEach(seconds ->
                        System.out.println("IP : "
                                + pair.getLeft()
                                + ", SECONDS_TO_RETURN : "
                                + seconds)
                    ));
            createTimeoutSubnetAndReturnTimePairList(lineList, Integer.parseInt(args[1]))
                    .forEach(pair -> pair.getRight().forEach(periods ->
                        System.out.println("SUBNET_IP : "
                                + pair.getLeft()
                                + ", FAULT_PERIOD : "
                                + periods)
                    ));
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
                .collect(Collectors.groupingBy(line -> line[1]))
                .entrySet()
                .stream()
                .map(entry -> new Pair<>(entry.getKey(), calcBetweenTimeout(entry.getValue(), notAcceptableCount)))
                .filter(pair -> !pair.getRight().isEmpty())
                .collect(Collectors.toList());
    }

    private List<Pair<String, List<String>>> createTimeoutSubnetAndReturnTimePairList(final List<String> lineList, final int notAcceptableCount) {

        return lineList
            .stream()
            .map(line -> line.split(LINE_DELIMITER))
            .collect(Collectors.groupingBy(extractSubnet))
            .entrySet()
            .stream()
            .map(entry -> new Pair<>(entry.getKey(), calcBetweenTimeoutBySubnet(entry.getValue(), notAcceptableCount)))
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

    private List<String> calcBetweenTimeoutBySubnet(final List<String[]> lineList, final int notAcceptableCount) {

        final List<String> timeoutPeriods = new ArrayList<>();

        final Comparator<String[]> com1 = Comparator.comparing(line -> line[INDEX_CONFIRM_DATE]);
        final Comparator<String[]> com2 = Comparator.comparing(line -> line[INDEX_SERVER_IP]);
        final List<String[]> sortedList = lineList
                .stream()
                .sorted(com1.thenComparing(com2))
                .collect(Collectors.toList());

        final List<String> ipList = lineList
                .stream()
                .map(line -> line[INDEX_SERVER_IP])
                .collect(Collectors.toList());

        final Map<String, Integer> ipTimeoutStatusMap = new HashMap<>();
        ipList.forEach(ip -> ipTimeoutStatusMap.put(ip, 0));

        // 最新タイムアウト時刻
        String latestTimeoutStartDate = null;
        // 故障フラグ
        boolean isFault = false;
        for (int i = 0; i < sortedList.size(); i++) {
            final String[] tmp = sortedList.get(i);
            // タイムアウトの場合、最新タイムアウト時刻を記録
            // 各IPごとのタイムアウト回数をインクリメント
            if (TIMEOUT_LETTER.equals(tmp[INDEX_RETURN_TIME])) {
                final int timeoutCount = ipTimeoutStatusMap.get(tmp[INDEX_SERVER_IP]);
                if (timeoutCount == 0) {
                    latestTimeoutStartDate = tmp[INDEX_CONFIRM_DATE];
                }

                ipTimeoutStatusMap.put(tmp[INDEX_SERVER_IP], timeoutCount + 1);

                // タイムアウトしていない場合、各IPごとのタイムアウト回数を0にリセット
                // 故障フラグがtrue の場合は最新タイムアウト時刻から現在行の時刻までの期間を記録し、故障フラグをfalseにする
            } else {
                ipTimeoutStatusMap.put(tmp[INDEX_SERVER_IP], 0);
                if (isFault) {
                    timeoutPeriods.add(latestTimeoutStartDate + "-" + sortedList.get(i - 1)[INDEX_CONFIRM_DATE]);
                    latestTimeoutStartDate = null;
                }

                isFault = false;
                continue;
            }

            isFault = ipTimeoutStatusMap.entrySet()
                    .stream()
                    .allMatch(entry -> entry.getValue() >= notAcceptableCount);
        }

        return timeoutPeriods;
    }
}
