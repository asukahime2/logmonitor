package com.asukahime.logmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static com.asukahime.logmonitor.LogMonitorConstant.*;

public class MainTest {

    private ByteArrayOutputStream out;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @After
    public void tearDown() throws Exception {
        out.close();
    }

    @Test
    public void test_doProcess_ファイル未指定() {
        // ファイル指定なし
        final String[] args = new String[0];

        Main.main(args);
        assertEquals(MESSAGE_INVALID_ARGS + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_不正な引数() {
        final String validFileName = "test/resources/valid_monitoring.log";
        final String dummyArg1 = "a";
        final String dummyArg2 = "b";

        // 不正な数の引数
        final String[] args = new String[3];
        args[0] = validFileName;
        args[1] = dummyArg1;
        args[2] = dummyArg2;

        Main.main(args);
        assertEquals(MESSAGE_INVALID_ARGS + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_ファイル読み込み失敗() {
        // 存在しないファイル
        final String notExistFileName = "test/resources/not_exist_monitoring.log";

        final String[] args = new String[2];
        args[0] = notExistFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_FILE_CAN_NOT_READ + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_1要素目_pattern1() {
        // 1要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_1.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_FORMAT + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_1要素目_pattern2() {
        // 1要素目のフォーマット不正ありファイル
        final String invalidFormatFileName = "test/resources/invalid_monitoring_1_1.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_FORMAT + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_2要素目_pattern1() {
        // 2要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_2.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_FORMAT + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_2要素目_pattern2() {
        // 2要素目のフォーマット不正ありファイル
        final String invalidFormatFileName = "test/resources/invalid_monitoring_2_1.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_FORMAT + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_3要素目_pattern1() {
        // 3要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_3.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_FORMAT + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_3要素目_pattern2() {
        // 3要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_3_1.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_FORMAT + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_第二引数_数値以外() {
        // 3要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_3_1.log";

        final String[] args = new String[2];
        args[0] = invalidFormatFileName;
        args[1] = "a";

        Main.main(args);
        assertEquals(MESSAGE_INVALID_ARG_1 + "\r\n", out.toString());
    }

    @Test
    public void test_doProcess_正常_故障期間1つのみ() {
        final String validFileName = "test/resources/valid_monitoring.log";

        final String[] args = new String[2];
        args[0] = validFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals("IP : 1.1.1.2/16, SECONDS_TO_RETURN : 10\r\nIP : 1.1.1.1/24, SECONDS_TO_RETURN : 20\r\n", out.toString());
    }

    @Test
    public void test_doProcess_正常_故障期間複数() {
        final String validFileName = "test/resources/valid_monitoring_1.log";

        final String[] args = new String[2];
        args[0] = validFileName;
        args[1] = "1";

        Main.main(args);
        assertEquals("IP : 1.1.1.1/16, SECONDS_TO_RETURN : 10\r\n"
                        + "IP : 1.1.1.1/16, SECONDS_TO_RETURN : 10\r\n"
                        + "IP : 1.1.1.1/24, SECONDS_TO_RETURN : 20\r\n"
                , out.toString());
    }

    @Test
    public void test_doProcess_正常_故障期間複数_出力条件2回以上() {
        final String validFileName = "test/resources/valid_monitoring_1.log";

        final String[] args = new String[2];
        args[0] = validFileName;
        args[1] = "2";

        Main.main(args);
        assertEquals("IP : 1.1.1.1/24, SECONDS_TO_RETURN : 20\r\n"
                , out.toString());
    }

    @Test
    public void test_doProcess_正常_サブネット故障期間() {
        final String validFileName = "test/resources/valid_monitoring_2.log";

        final String[] args = new String[2];
        args[0] = validFileName;
        args[1] = "2";

        Main.main(args);
        assertEquals("IP : 1.1.1.1/16, SECONDS_TO_RETURN : 30\r\n"
            + "IP : 1.1.1.2/16, SECONDS_TO_RETURN : 20\r\n"
            + "SUBNET_IP : 1.1.***.***, FAULT_PERIOD : 20210717101021-20210717101031\r\n"
            , out.toString());
    }

    @Test
    public void test_doProcess_正常_サブネット故障期間_複数() {
        final String validFileName = "test/resources/valid_monitoring_3.log";

        final String[] args = new String[2];
        args[0] = validFileName;
        args[1] = "2";

        Main.main(args);
        assertEquals("IP : 1.1.1.1/16, SECONDS_TO_RETURN : 30\r\n"
                        + "IP : 1.1.1.2/16, SECONDS_TO_RETURN : 30\r\n"
                        + "IP : 1.1.1.1/24, SECONDS_TO_RETURN : 20\r\n"
                        + "SUBNET_IP : 1.1.***.***, FAULT_PERIOD : 20210717101011-20210717101031\r\n"
                        + "SUBNET_IP : 1.1.1.***, FAULT_PERIOD : 20210717101033-20210717101043\r\n"
                , out.toString());
    }
}