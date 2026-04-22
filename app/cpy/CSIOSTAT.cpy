      *****************************************************************
      * Common I/O Status Display Routine                             *
      *                                                               *
      * Shared paragraph used by batch programs to format and         *
      * display the current file status code (IO-STATUS).             *
      * Requires the caller to define the following working-storage   *
      * fields:                                                       *
      *   IO-STATUS            (group: IO-STAT1 + IO-STAT2, each X)   *
      *   IO-STATUS-04         (group with IO-STATUS-0401 9 and       *
      *                         IO-STATUS-0403 999)                   *
      *   TWO-BYTES-BINARY     (PIC 9(4) BINARY)                      *
      *   TWO-BYTES-ALPHA      (REDEFINES TWO-BYTES-BINARY with       *
      *                         TWO-BYTES-LEFT  PIC X and             *
      *                         TWO-BYTES-RIGHT PIC X)                *
      *****************************************************************
       Z-DISPLAY-IO-STATUS.
           IF  IO-STATUS NOT NUMERIC
           OR  IO-STAT1 = '9'
               MOVE IO-STAT1 TO IO-STATUS-04(1:1)
               MOVE 0        TO TWO-BYTES-BINARY
               MOVE IO-STAT2 TO TWO-BYTES-RIGHT
               MOVE TWO-BYTES-BINARY TO IO-STATUS-0403
               DISPLAY 'FILE STATUS IS: NNNN' IO-STATUS-04
           ELSE
               MOVE '0000' TO IO-STATUS-04
               MOVE IO-STATUS TO IO-STATUS-04(3:2)
               DISPLAY 'FILE STATUS IS: NNNN' IO-STATUS-04
           END-IF
           EXIT.
