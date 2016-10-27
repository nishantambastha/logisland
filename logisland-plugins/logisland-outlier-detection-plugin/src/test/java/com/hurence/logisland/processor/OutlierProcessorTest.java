/**
 * Copyright (C) 2016 Hurence (bailet.thomas@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hurence.logisland.processor;

import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;


public class OutlierProcessorTest {
    private static final Logger logger = LoggerFactory.getLogger(TimeSeriesCsvLoader.class);
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final String RESOURCES_DIRECTORY = "target/test-classes/benchmark_data/";
    private static final DateTimeFormatter inputDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");



    @Test
    @Ignore("too long")
    public void testDetection() throws IOException {
        final TestRunner testRunner = TestRunners.newTestRunner(new OutlierProcessor());
        testRunner.setProperty(OutlierProcessor.ROTATION_POLICY_TYPE, "by_amount");
        testRunner.setProperty(OutlierProcessor.ROTATION_POLICY_AMOUNT, "100");
        testRunner.setProperty(OutlierProcessor.ROTATION_POLICY_UNIT, "points");
        testRunner.setProperty(OutlierProcessor.CHUNKING_POLICY_TYPE, "by_amount");
        testRunner.setProperty(OutlierProcessor.CHUNKING_POLICY_AMOUNT, "10");
        testRunner.setProperty(OutlierProcessor.CHUNKING_POLICY_UNIT, "points");
        testRunner.setProperty(OutlierProcessor.GLOBAL_STATISTICS_MIN, "-100000");
        testRunner.setProperty(OutlierProcessor.MIN_AMOUNT_TO_PREDICT, "100");
        testRunner.setProperty(OutlierProcessor.ZSCORE_CUTOFFS_NORMAL, "3.5");
        testRunner.setProperty(OutlierProcessor.ZSCORE_CUTOFFS_MODERATE, "5");
        testRunner.assertValid();


        File f = new File(RESOURCES_DIRECTORY);

        for (File file : FileUtils.listFiles(f, new SuffixFileFilter(".csv"), TrueFileFilter.INSTANCE)) {
            BufferedReader reader = Files.newBufferedReader(file.toPath(), ENCODING);
            List<Record> records = TimeSeriesCsvLoader.load(reader, true, inputDateFormat);
            Assert.assertTrue(!records.isEmpty());


            testRunner.clearQueues();
            testRunner.enqueue(records.toArray(new Record[records.size()]));
            testRunner.run();
            testRunner.assertAllInputRecordsProcessed();

            System.out.println("records.size() = " + records.size());
            System.out.println("testRunner.getOutputRecords().size() = " + testRunner.getOutputRecords().size());
          //  testRunner.assertOutputRecordsCount(533);
        }

    }


}
