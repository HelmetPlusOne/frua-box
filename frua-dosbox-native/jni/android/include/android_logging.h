
#ifndef ANDROID_LOGGING_H
#define ANDROID_LOGGING_H

#include <string.h>
#include <stdio.h>
#include <alloca.h>
#include <android/log.h>

/* Defines wrapper macros for android logging,
 * adding function name, function file name and file number to the log message,
 * example: LOGI("MyTag", "hello: %s", "android") will be printed as "[my_function my_source_file.c#42] hello android".
 * Uses "alloca" and "##" GCC extensions.
 */

/* max length of message format string */
#define ALOG_MAX_FORMAT_STR_LENGTH 1<<7

/* computes number of digits in decimal number */
#define ALOG_NUM_OF_DIGITS(num) ((num) < 10 ? 1 : \
                                ((num) < 100 ? 2 : \
                                ((num) < 1000 ? 3 : \
                                ((num) < 10000 ? 4 : \
                                ((num) < 100000 ? 5 : \
                                ((num) < 1000000 ? 6 : \
                                ((num) < 10000000 ? 7 : \
                                ((num) < 100000000 ? 8 : \
                                ((num) < 1000000000 ? 9 : 10)))))))))

/* will print into log: [_func_ __FILE(without dir prefix)__#__LINE] message */
#define ALOG(level, tag, format, ...)

/* shortcats for log levels */
#define ALOGV(tag, format, ...) ALOG(ANDROID_LOG_VERBOSE, tag, format, ##__VA_ARGS__)
#define ALOGD(tag, format, ...) ALOG(ANDROID_LOG_DEBUG, tag, format, ##__VA_ARGS__)
#define ALOGI(tag, format, ...) ALOG(ANDROID_LOG_INFO, tag, format, ##__VA_ARGS__)
#define ALOGW(tag, format, ...) ALOG(ANDROID_LOG_WARN, tag, format, ##__VA_ARGS__)
#define ALOGE(tag, format, ...) ALOG(ANDROID_LOG_ERROR, tag, format, ##__VA_ARGS__)
#define ALOGF(tag, format, ...) ALOG(ANDROID_LOG_FATAL, tag, format, ##__VA_ARGS__)

#endif /* ANDROID_LOGGING_H */
