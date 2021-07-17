package com.asukahime.logmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

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
        assertEquals("ファイルを指定してください。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_不正な引数() {
        final String validFileName = "test/resources/valid_monitoring.log";
        final String dummyArg1 = "a";

        // 不正な数の引数
        final String[] args = new String[2];
        args[0] = validFileName;
        args[1] = dummyArg1;

        Main.main(args);
        assertEquals("引数の数が不正です。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_ファイル読み込み失敗() {
        // 存在しないファイル
        final String notExistFileName = "test/resources/not_exist_monitoring.log";

        final String[] args = new String[1];
        args[0] = notExistFileName;

        Main.main(args);
        assertEquals("ファイルの読み込みに失敗しました。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_1要素目_pattern1() {
        // 1要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_1.log";

        final String[] args = new String[1];
        args[0] = invalidFormatFileName;

        Main.main(args);
        assertEquals("指定のフォーマットではない行が存在します。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_1要素目_pattern2() {
        // 1要素目のフォーマット不正ありファイル
        final String invalidFormatFileName = "test/resources/invalid_monitoring_1_1.log";

        final String[] args = new String[1];
        args[0] = invalidFormatFileName;

        Main.main(args);
        assertEquals("指定のフォーマットではない行が存在します。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_2要素目_pattern1() {
        // 2要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_2.log";

        final String[] args = new String[1];
        args[0] = invalidFormatFileName;

        Main.main(args);
        assertEquals("指定のフォーマットではない行が存在します。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_2要素目_pattern2() {
        // 2要素目のフォーマット不正ありファイル
        final String invalidFormatFileName = "test/resources/invalid_monitoring_2_1.log";

        final String[] args = new String[1];
        args[0] = invalidFormatFileName;

        Main.main(args);
        assertEquals("指定のフォーマットではない行が存在します。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_3要素目_pattern1() {
        // 3要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_3.log";

        final String[] args = new String[1];
        args[0] = invalidFormatFileName;

        Main.main(args);
        assertEquals("指定のフォーマットではない行が存在します。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_フォーマット不正_3要素目_pattern2() {
        // 3要素目が存在しない行がある
        final String invalidFormatFileName = "test/resources/invalid_monitoring_3_1.log";

        final String[] args = new String[1];
        args[0] = invalidFormatFileName;

        Main.main(args);
        assertEquals("指定のフォーマットではない行が存在します。\r\n", out.toString());
    }

    @Test
    public void test_doProcess_正常() {
        final String validFileName = "test/resources/valid_monitoring.log";

        final String[] args = new String[1];
        args[0] = validFileName;

        Main.main(args);
        assertEquals("IP : 1.1.1.2/16, SECONDS_TO_RETURN : 10\r\nIP : 1.1.1.1/24, SECONDS_TO_RETURN : 20\r\n", out.toString());
    }
}