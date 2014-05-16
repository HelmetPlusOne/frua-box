
#ifndef CIRCULAR_BUFFER_H
#define CIRCULAR_BUFFER_H

/* 
 * Simple (non-optimized) implementation of fixed-sized FIFO circular buffer of 
 * primitives or arbitrary structures.
 * Elements are passed to/from buffer by value.
 * Keeps one slot open to distinguish between 'empty' and 'full' states.
 * Reads from empty buffer returns predefined 'empty' value.
 * Writes into full buffer caused overwrites of oldest values.
 * Read index is updated atomically (on read and on overwrite) using GCC extensions.
 * GCC statement-expression extension is used for 'get' operation.
 * Usage example:
 *    
 *    #define BUF_SIZE 4
 *
 *    // declare element type and define empty value
 *    typedef struct {
 *        char* val;
 *    } event_t;
 *    event_t NO_EVENT = {.val = "NO_EVENT"};
 *
 *    // declare buffer type 
 *    CBUF_DECLARE(my_buf_t, event_t, BUF_SIZE);
 *
 *    int fun() {
 *        // define buffer
 *        my_buf_t buf = CBUF_INIT(BUF_SIZE, NO_EVENT);
 *
 *        // put elements
 *        event_t ev = {.val = "MY_EVENT"};
 *        CBUF_PUT(buf, ev);
 *
 *        // get elements
 *        event_t ev1 = CBUF_GET(buf, event_t);
 *    }
 *
 */

/*
 * Defines type for buffer structure.
 *
 *  - cbuf_t buffer structure type name
 *  - val_t buffer element type name
 *  - cbuf_size buffer size not including empty slot
 */
#define CBUF_TYPEDEF(cbuf_t, val_t, cbuf_size) \
    typedef struct { \
        val_t body[cbuf_size + 1]; \
        val_t empty_value; \
        int ind_put; \
        int ind_get; \
        int size; \
    } cbuf_t 

/*
 * Initializes circular buffer structure
 *
 *  - cb buffer itself
 *  - cbuf_size buffer size not including empty slot
 *  - empty_val value to return on reads from empty buffer
 */
#define CBUF_INIT(cb, cbuf_size, empty_val) do { \
    cb.size = cbuf_size + 1; \
    cb.empty_value = empty_val; \
} while (0)

/*
 * Inserts element into buffer, overwrites the oldest element if buffer is full
 * 
 *  - cb buffer itself
 *  - val value to insert
 */
#define CBUF_PUT(cb, val) do { \
    int wnext = (cb.ind_put + 1) % cb.size; \
    int rcur = cb.ind_get; \
    if(wnext == rcur) { \
        int rnext = (rcur + 1) % cb.size; \
        __sync_val_compare_and_swap(&(cb.ind_get), rcur, rnext); \
    } \
    cb.body[cb.ind_put] = val; \
    cb.ind_put = wnext; \
} while (0)

/*
 * Selects and removes element from buffer.
 * Uses GCC statement-expressions.
 *
 *  - cb buffer itself
 *  - val_t element type
 */
#define CBUF_GET(cb, val_t) ({ \
    val_t res; \
    if(cb.ind_put == cb.ind_get) { \
        res = cb.empty_value; \
    } else { \
        res = cb.body[cb.ind_get]; \
        int rval = cb.ind_get; \
        int nval = (rval + 1) % cb.size; \
        __sync_val_compare_and_swap(&(cb.ind_get), rval, nval); \
    } \
    res; \
}) 

/*
 * Checks whether buffer is empty.
 *
 *  - cb buffer itself
 */
#define CBUF_IS_EMPTY(cb) (cb.ind_put == cb.ind_get)

#endif /* CIRCULAR_BUFFER_H */
