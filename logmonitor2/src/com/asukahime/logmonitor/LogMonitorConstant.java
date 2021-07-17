package com.asukahime.logmonitor;

public class LogMonitorConstant {

    public static final String LINE_REGEXP = "^[0-9]{14},[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}/[0-9]{1,3},([0-9]+|-)$";
    public static final String LINE_DELIMITER = ",";
    public static final String TIMEOUT_LETTER = "-";
    public static final String DATE_PATTERN = "yyyyMMddHHmmss";
    public static final int INDEX_CONFIRM_DATE = 0;
    public static final int INDEX_RETURN_TIME = 2;
    public static final int VALID_NUMBER_OF_ARGS = 2;

    public static final String MESSAGE_FILE_CAN_NOT_READ = "ファイルの読み込みに失敗しました。";
    public static final String MESSAGE_FILE_NOT_SPECIFIED = "ファイルを指定してください。";
    public static final String MESSAGE_INVALID_ARGS = "引数の数が不正です。";
    public static final String MESSAGE_INVALID_ARG_1 = "第二引数は数値を入力してください。";
    public static final String MESSAGE_INVALID_FORMAT = "指定のフォーマットではない行が存在します。";
}
