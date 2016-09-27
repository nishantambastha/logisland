/*
 * Copyright 2016 Hurence
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hurence.logisland.processor;

import com.hurence.logisland.component.PropertyDescriptor;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.validator.StandardPropertyValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MockProcessorChain extends AbstractProcessorChain {


    public static final PropertyDescriptor MOCK_CHAIN = new PropertyDescriptor.Builder()
            .name("mock.chain")
            .description("a fake")
            .required(true)
            .addValidator(StandardPropertyValidators.NON_EMPTY_VALIDATOR)
            .defaultValue("yoyo")
            .build();


    private static Logger logger = LoggerFactory.getLogger(MockProcessorChain.class);

    private static String EVENT_TYPE_NAME = "mock";


    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(MOCK_CHAIN);

        return Collections.unmodifiableList(descriptors);
    }

}
