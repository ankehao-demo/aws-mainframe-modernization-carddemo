#!/bin/bash
#
# local_compile.sh - Compile CardDemo COBOL batch programs using GnuCOBOL
#
# Prerequisites: gnucobol (apt-get install gnucobol)
#
# Usage: Run from the repository root directory
#   ./scripts/local_compile.sh
#

set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CPY_DIR="$REPO_ROOT/app/cpy"
CBL_DIR="$REPO_ROOT/app/cbl"
BUILD_DIR="$REPO_ROOT/build"

mkdir -p "$BUILD_DIR"

PASS=0
FAIL=0

echo "=== Compiling CardDemo batch executables ==="
for f in CBACT01C CBACT02C CBACT03C CBCUS01C CBTRN01C CBTRN02C CBTRN03C COBSWAIT; do
    printf "  %-12s" "$f"
    if cobc -x -I "$CPY_DIR" --std=ibm-strict -o "$BUILD_DIR/$f" "$CBL_DIR/$f.cbl" 2>&1; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAILED"
        FAIL=$((FAIL + 1))
    fi
done

echo "=== Compiling CardDemo batch modules (subroutines) ==="
for f in CBACT04C CSUTLDTC; do
    printf "  %-12s" "$f"
    if cobc -m -I "$CPY_DIR" --std=ibm-strict -o "$BUILD_DIR/$f.so" "$CBL_DIR/$f.cbl" 2>&1; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAILED"
        FAIL=$((FAIL + 1))
    fi
done

for f in CBSTM03A CBSTM03B; do
    printf "  %-12s" "$f"
    if cobc -m -I "$CPY_DIR" --std=ibm-strict -o "$BUILD_DIR/$f.so" "$CBL_DIR/$f.CBL" 2>&1; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAILED"
        FAIL=$((FAIL + 1))
    fi
done

echo ""
echo "=== Build complete: $PASS passed, $FAIL failed ==="
echo "Output directory: $BUILD_DIR"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
