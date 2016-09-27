package com.hurence.logisland.processor;

import com.hurence.logisland.component.ComponentContext;
import com.hurence.logisland.component.PropertyDescriptor;
import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.RecordType;
import com.hurence.logisland.utils.time.DateUtil;
import com.hurence.logisland.validator.StandardPropertyValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SplitText extends AbstractProcessor {

    static final long serialVersionUID = 1413578915552852739L;

    private static Logger logger = LoggerFactory.getLogger(SplitText.class);


    public static final PropertyDescriptor VALUE_REGEX = new PropertyDescriptor.Builder()
            .name("value.regex")
            .description("the regex to match for the message value")
            .required(true)
            .addValidator(StandardPropertyValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor VALUE_FIELDS = new PropertyDescriptor.Builder()
            .name("value.fields")
            .description("a comma separated list of fields corresponding to matching groups for the message value")
            .required(true)
            .addValidator(StandardPropertyValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor KEY_REGEX = new PropertyDescriptor.Builder()
            .name("key.regex")
            .description("the regex to match for the message key")
            .required(true)
            .addValidator(StandardPropertyValidators.NON_EMPTY_VALIDATOR)
            .defaultValue(".*")
            .build();

    public static final PropertyDescriptor KEY_FIELDS = new PropertyDescriptor.Builder()
            .name("key.fields")
            .description("a comma separated list of fields corresponding to matching groups for the message key")
            .required(true)
            .addValidator(StandardPropertyValidators.NON_EMPTY_VALIDATOR)
            .defaultValue("")
            .build();

    public static final PropertyDescriptor EVENT_TYPE = new PropertyDescriptor.Builder()
            .name("event.type")
            .description("default type of event")
            .required(true)
            .addValidator(StandardPropertyValidators.NON_EMPTY_VALIDATOR)
            .defaultValue("event")
            .build();

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(VALUE_REGEX);
        descriptors.add(VALUE_FIELDS);
        descriptors.add(KEY_REGEX);
        descriptors.add(KEY_FIELDS);
        descriptors.add(EVENT_TYPE);

        return Collections.unmodifiableList(descriptors);
    }

    @Override
    public Collection<Record> process(ComponentContext context, Collection<Record> records)  {

        final String[] keyFields = context.getProperty(KEY_FIELDS).asString().split(",");
        final String keyRegexString = context.getProperty(KEY_REGEX).asString();
        final Pattern keyRegex = Pattern.compile(keyRegexString);
        final String[] valueFields = context.getProperty(VALUE_FIELDS).asString().split(",");
        final String valueRegexString = context.getProperty(VALUE_REGEX).asString();
        final String eventType = context.getProperty(EVENT_TYPE).asString();
        final Pattern valueRegex = Pattern.compile(valueRegexString);

        List<Record> outputRecords = new ArrayList<>();

        /**
         * try to match the regexp to create an event
         */
        records.forEach(record -> {
            try {
                final String key = record.getField(RecordType.RECORD_KEY.toString()).asString();
                final String value = record.getField(RecordType.RECORD_VALUE.toString()).asString();

                Record outputRecord = new Record(eventType);

                // match the key
                if (key != null) {
                    try {
                        Matcher keyMatcher = keyRegex.matcher(key);
                        if (keyMatcher.matches()) {
                            for (int i = 0; i < keyMatcher.groupCount() + 1 && i < keyFields.length; i++) {
                                String content = keyMatcher.group(i);
                                if (content != null) {
                                    outputRecord.setField(keyFields[i], FieldType.STRING, keyMatcher.group(i + 1).replaceAll("\"", ""));

                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.info("error while matching key {} with regex {}", key, keyRegexString);
                    }
                }


                // match the value
                if (value != null) {
                    try {
                        Matcher valueMatcher = valueRegex.matcher(value);
                        if (valueMatcher.lookingAt()) {
                            for (int i = 0; i < valueMatcher.groupCount() + 1 && i < valueFields.length; i++) {
                                String content = valueMatcher.group(i);
                                if (content != null) {
                                    outputRecord.setField(valueFields[i], FieldType.STRING, valueMatcher.group(i).replaceAll("\"", ""));
                                }
                            }


                            // TODO removeField this ugly stuff with EL
                            if (outputRecord.getField("date") != null && outputRecord.getField("time") != null) {
                                String eventTimeString = outputRecord.getField("date").getRawValue().toString() +
                                        " " +
                                        outputRecord.getField(RecordType.RECORD_TIME.toString()).asString();

                                try {
                                    Date eventDate = DateUtil.parse(eventTimeString);

                                    if (eventDate != null) {
                                        outputRecord.setField(RecordType.RECORD_TIME.toString(), FieldType.LONG, eventDate.getTime());
                                    }
                                } catch (Exception e) {
                                    logger.warn("unable to parse date {}", eventTimeString);
                                }

                            }


                            // TODO removeField this ugly stuff with EL
                            if (outputRecord.getField(RecordType.RECORD_TIME.toString()) != null) {

                                try {
                                    long eventTime = Long.parseLong(outputRecord.getField(RecordType.RECORD_TIME.toString()).getRawValue().toString());
                                } catch (Exception ex) {

                                    Date eventDate = DateUtil.parse(outputRecord.getField(RecordType.RECORD_TIME.toString()).getRawValue().toString());
                                    if (eventDate != null) {
                                        outputRecord.setField(RecordType.RECORD_TIME.toString(), FieldType.LONG, eventDate.getTime());
                                    }
                                }
                            }


                            outputRecords.add(outputRecord);
                        }
                    } catch (Exception e) {
                        logger.warn("issue while matching regex {} on string {} exception {}", valueRegexString, value, e.getMessage());
                    }
                }
            } catch (Exception e) {
                // nothing to do here

                logger.warn("issue while matching getting K/V on record {}, exception {}", record, e.getMessage());
            }
        });


        return outputRecords;
    }

}
