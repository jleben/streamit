/*
 * Copyright 2006 by the Massachusetts Institute of Technology.
 *
 * Permission to use, copy, modify, and distribute this
 * software and its documentation for any purpose and without
 * fee is hereby granted, provided that the above copyright
 * notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting
 * documentation, and that the name of M.I.T. not be used in
 * advertising or publicity pertaining to distribution of the
 * software without specific, written prior permission.
 * M.I.T. makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is"
 * without express or implied warranty.
 */
#ifndef __FILEWRITER_H
#define __FILEWRITER_H
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <assert.h>

#define BUF_SIZE 4000

class FileWriter_state {
public:
  FileWriter_state() { file_handle = -1; buf_index = 0; }
  int file_handle;
  int file_offset, file_length;
  char file_buf[BUF_SIZE];
  int buf_index;
};

/* Routines that are independent of <T>, make them not be in class */

extern void *FileWriter_open(char *pathname);

extern void FileWriter_close(void *fs_ptr);

extern int FileWriter_flush(void *fs_ptr);

extern int FileWriter_getpos(void *fs_ptr);

extern void FileWriter_setpos(void *fs_ptr, int pos);

template<class T>
 inline void FileWriter_write(void *fs_ptr, T data) {
    FileWriter_state *fs = (FileWriter_state*)fs_ptr;

    // Flush if adding data to the buffer would overflow the buffer
    if (fs->buf_index + sizeof(T) > BUF_SIZE) FileWriter_flush(fs_ptr);

    char * cdata = (char*) &data;
    for (int i = 0; i < sizeof(T); ++i)
    {
        fs->file_buf[fs->buf_index + i] = cdata[i];
    }

    fs->buf_index += sizeof(T);
}

template<>
 inline void FileWriter_write(void *fs_ptr, unsigned char data) {
    FileWriter_state *fs = (FileWriter_state*)fs_ptr;

    // Flush if adding data to the buffer would overflow the buffer
    if (fs->buf_index >= BUF_SIZE) FileWriter_flush(fs_ptr);

    fs->file_buf[fs->buf_index] = data;
    ++(fs->buf_index);
}

//template<>
inline void FileWriter_write(void *fs_ptr, const void* data, int len) {
    FileWriter_state *fs = (FileWriter_state*)fs_ptr;

    FileWriter_flush(fs_ptr);
    
    write(fs->file_handle, data, len);	
}
#endif
