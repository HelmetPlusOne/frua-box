
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
#define ALOG(level, tag, format, ...) do { \
    /* strip directory prefix from filename */ \
    size_t flen = strlen(__FILE__) + 1; \
    char* filename = alloca(flen); \
    strncpy(filename, __FILE__, flen); \
    while(1) { \
        char* slash = strchr(filename, '/'); \
        if(slash) { \
            int preflen = slash - filename; \
            strncpy(filename, slash + 1, strlen(filename) - preflen); \
        } else break; \
    }; \
    /* compute sizes */ \
    size_t funcoff = 1; \
    size_t funclen = strlen(__func__); \
    size_t fileoff = funcoff + funclen + 1; \
    size_t filelen = strlen(filename); \
    size_t lineoff = fileoff + filelen + 1; \
    size_t linelen = ALOG_NUM_OF_DIGITS(__LINE__); \
    size_t formatoff = linelen + lineoff + 2; \
    size_t full_format_len = strlen(format); \
    size_t formatlen = full_format_len <= ALOG_MAX_FORMAT_STR_LENGTH ? full_format_len : ALOG_MAX_FORMAT_STR_LENGTH; \
    size_t len = 2 + lineoff + linelen + 1 + formatlen + 1; \
    /* concatenate */ \
    char* msg = alloca(len); \
    msg[0] = '['; \
    strncpy(msg + funcoff, __func__, funclen); \
    msg[funcoff + funclen] = ' '; \
    strncpy(msg + fileoff, filename, filelen); \
    msg[fileoff + filelen] = '#'; \
    sprintf(msg + lineoff, "%u", __LINE__); \
    msg[linelen + lineoff] = ']'; \
    msg[linelen + lineoff + 1] = ' '; \
    strncpy(msg + formatoff, (format), formatlen); \
    msg[len - 1] = '\0'; \
    /* write to log */ \
    __android_log_print((level), (tag), msg, ##__VA_ARGS__); \
} while(0)

/* shortcats for log levels */
#define ALOGV(tag, format, ...) ALOG(ANDROID_LOG_VERBOSE, tag, format, ##__VA_ARGS__)
#define ALOGD(tag, format, ...) ALOG(ANDROID_LOG_DEBUG, tag, format, ##__VA_ARGS__)
#define ALOGI(tag, format, ...) ALOG(ANDROID_LOG_INFO, tag, format, ##__VA_ARGS__)
#define ALOGW(tag, format, ...) ALOG(ANDROID_LOG_WARN, tag, format, ##__VA_ARGS__)
#define ALOGE(tag, format, ...) ALOG(ANDROID_LOG_ERROR, tag, format, ##__VA_ARGS__)
#define ALOGF(tag, format, ...) ALOG(ANDROID_LOG_FATAL, tag, format, ##__VA_ARGS__)

#endif /* ANDROID_LOGGING_H */
