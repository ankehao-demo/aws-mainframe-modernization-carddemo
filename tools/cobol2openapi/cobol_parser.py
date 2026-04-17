"""COBOL Copybook Parser.

Reads COBOL copybook (.cpy) files and produces structured representations
of their data layouts, including PIC clauses, level hierarchy, 88-level
conditions, REDEFINES, and OCCURS.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Union


@dataclass
class Condition88:
    """Represents an 88-level condition-name (validation rule)."""

    name: str
    values: list[Union[str, int, float]] = field(default_factory=list)
    through_ranges: list[tuple[Union[int, float], Union[int, float]]] = field(
        default_factory=list
    )


@dataclass
class CobolField:
    """Represents a single COBOL field from a copybook."""

    level: int
    name: str
    pic_clause: str | None = None
    pic_type: str | None = None
    length: int | None = None
    decimal_places: int = 0
    is_filler: bool = False
    is_signed: bool = False
    redefines: str | None = None
    value: str | None = None
    children: list[CobolField] = field(default_factory=list)
    conditions: list[Condition88] = field(default_factory=list)
    occurs: int | None = None


@dataclass
class CopybookRecord:
    """Top-level 01-level record parsed from a copybook file."""

    name: str
    source_file: str
    fields: list[CobolField] = field(default_factory=list)
    record_length: int | None = None


# ---------------------------------------------------------------------------
# PIC clause parsing
# ---------------------------------------------------------------------------

_PIC_PATTERN = re.compile(
    r"^(S)?"  # optional sign
    r"(.*)",  # rest of the PIC string
    re.IGNORECASE,
)

_REPEAT_PATTERN = re.compile(r"([X9AZ])\((\d+)\)", re.IGNORECASE)


def _expand_pic(pic_body: str) -> str:
    """Expand shorthand like X(10) -> XXXXXXXXXX, 9(3) -> 999."""

    def _repl(m: re.Match) -> str:
        return m.group(1).upper() * int(m.group(2))

    return _REPEAT_PATTERN.sub(_repl, pic_body).upper()


def parse_pic_clause(pic: str) -> tuple[str, int, int, bool]:
    """Parse a PIC clause string into (type, length, decimal_places, is_signed).

    Examples:
      'X(16)'            -> ('alphanumeric', 16, 0, False)
      '9(11)'            -> ('numeric', 11, 0, False)
      'S9(10)V99'        -> ('signed_decimal', 12, 2, True)
      'X(01)'            -> ('alphanumeric', 1, 0, False)
      'XXX'              -> ('alphanumeric', 3, 0, False)
      '9(04)'            -> ('numeric', 4, 0, False)
      '-ZZZ,ZZZ,ZZZ.ZZ'  -> ('alphanumeric', 15, 0, False)  # edit pattern
    """
    pic = pic.strip().upper()

    # Detect edit-pattern PICs (contain Z, comma, +, -, $, *, B, 0, /)
    # These are display-formatting patterns, not computational types
    if re.search(r"[Z,+\-$*B/]", pic) and not re.match(r"^S?[9X()\dVA]+$", pic):
        # Count the display length (each character = 1 position)
        length = len(pic)
        return "alphanumeric", length, 0, False

    is_signed = pic.startswith("S")
    body = pic[1:] if is_signed else pic

    # Split on implicit decimal point 'V'
    if "V" in body:
        integer_part, decimal_part = body.split("V", 1)
        int_expanded = _expand_pic(integer_part)
        dec_expanded = _expand_pic(decimal_part)
        decimal_places = len(dec_expanded)
        total_length = len(int_expanded) + decimal_places
        pic_type = "signed_decimal" if is_signed else "decimal"
        return pic_type, total_length, decimal_places, is_signed

    expanded = _expand_pic(body)
    total_length = len(expanded)

    if all(c == "9" for c in expanded):
        pic_type = "numeric"
    else:
        pic_type = "alphanumeric"

    return pic_type, total_length, 0, is_signed


# ---------------------------------------------------------------------------
# VALUE / 88-level parsing
# ---------------------------------------------------------------------------


def _strip_quotes(val: str) -> str:
    """Remove surrounding single quotes from a value literal."""
    val = val.strip()
    if len(val) >= 2 and val[0] == "'" and val[-1] == "'":
        return val[1:-1]
    return val


def _try_numeric(val: str) -> Union[int, float, str]:
    """Attempt to convert a string to int or float, else return the string."""
    val = val.strip()
    if val.startswith("'") and val.endswith("'"):
        return _strip_quotes(val)
    try:
        return int(val)
    except ValueError:
        pass
    try:
        return float(val)
    except ValueError:
        pass
    return val


def parse_value_clause(tokens: list[str]) -> Condition88:
    """Parse an 88-level condition from its collected tokens.

    Handles:
      VALUE 'A'.
      VALUES 1 THROUGH 12.
      VALUES 1, 3, 5, 7, 8, 10, 12.
      VALUE 'ENTER'.
      VALUES  '200', '201', ... '989'.
    """
    name = tokens[0]
    cond = Condition88(name=name)

    # Everything after VALUE/VALUES keyword
    raw = " ".join(tokens[1:])
    # Remove leading VALUE or VALUES keyword
    raw = re.sub(r"^\s*VALUES?\s+", "", raw, flags=re.IGNORECASE)
    # Remove trailing period
    raw = raw.rstrip(".")

    # Check for THROUGH / THRU ranges
    through_match = re.split(r"\s+THROUGH\s+|\s+THRU\s+", raw, flags=re.IGNORECASE)
    if len(through_match) == 2:
        lo = _try_numeric(through_match[0].strip())
        hi = _try_numeric(through_match[1].strip())
        cond.through_ranges.append((lo, hi))
        return cond

    # Split by comma, ignoring commas inside quotes
    parts: list[str] = []
    current = ""
    in_quote = False
    for ch in raw:
        if ch == "'" and not in_quote:
            in_quote = True
            current += ch
        elif ch == "'" and in_quote:
            in_quote = False
            current += ch
        elif ch == "," and not in_quote:
            parts.append(current.strip())
            current = ""
        else:
            current += ch
    if current.strip():
        parts.append(current.strip())

    for part in parts:
        part = part.strip()
        if not part:
            continue
        # Check for inline THROUGH within a comma-list (unlikely but defensive)
        through_inner = re.split(r"\s+THROUGH\s+|\s+THRU\s+", part, flags=re.IGNORECASE)
        if len(through_inner) == 2:
            lo = _try_numeric(through_inner[0].strip())
            hi = _try_numeric(through_inner[1].strip())
            cond.through_ranges.append((lo, hi))
        else:
            cond.values.append(_try_numeric(part))

    return cond


# ---------------------------------------------------------------------------
# Copybook file parser
# ---------------------------------------------------------------------------

_LEVEL_LINE_RE = re.compile(
    r"^\s*(\d{2})\s+(.+)",  # level number + rest
)


def _preprocess_lines(raw_lines: list[str]) -> list[str]:
    """Strip COBOL sequence numbers (cols 1-6), skip comments (col 7 = '*'),
    handle continuation lines (col 7 = '-'), and join multi-line statements
    ending with a period.
    """
    cleaned: list[str] = []

    for raw in raw_lines:
        if len(raw) < 7:
            continue

        indicator = raw[6] if len(raw) > 6 else " "

        # Skip comment lines
        if indicator == "*":
            continue

        # Extract text area (cols 8-72 in traditional, but many copybooks
        # use free-format — take everything after col 7)
        text = raw[7:].rstrip() if len(raw) > 7 else ""

        if indicator == "-":
            # Continuation: append to previous line (strip leading spaces)
            if cleaned:
                cleaned[-1] = cleaned[-1] + text.lstrip()
            continue

        cleaned.append(text)

    # Now join logical statements (each ending with '.')
    statements: list[str] = []
    current = ""
    for line in cleaned:
        line = line.strip()
        if not line:
            continue
        current = (current + " " + line).strip() if current else line
        if current.endswith("."):
            statements.append(current)
            current = ""
    if current:
        statements.append(current)

    return statements


def _tokenize_statement(stmt: str) -> dict:
    """Tokenize a single COBOL statement into its components."""
    result: dict = {}

    m = _LEVEL_LINE_RE.match(stmt)
    if not m:
        return result

    result["level"] = int(m.group(1))
    rest = m.group(2).strip()

    # Remove trailing period
    if rest.endswith("."):
        rest = rest[:-1].strip()

    tokens = rest.split()
    if not tokens:
        return result

    # Field name
    result["name"] = tokens[0]
    idx = 1

    # Parse remaining clauses
    while idx < len(tokens):
        token_upper = tokens[idx].upper()

        if token_upper == "PIC" or token_upper == "PICTURE":
            idx += 1
            if idx < len(tokens):
                # PIC clause may span like "PIC S9(10)V99"
                pic_str = tokens[idx]
                idx += 1
                result["pic"] = pic_str
            continue

        if token_upper == "REDEFINES":
            idx += 1
            if idx < len(tokens):
                result["redefines"] = tokens[idx]
                idx += 1
            continue

        if token_upper == "OCCURS":
            idx += 1
            if idx < len(tokens):
                try:
                    result["occurs"] = int(tokens[idx])
                except ValueError:
                    pass
                idx += 1
            # Skip optional TIMES keyword
            if idx < len(tokens) and tokens[idx].upper() == "TIMES":
                idx += 1
            continue

        if token_upper in ("VALUE", "VALUES"):
            # Collect everything from VALUE/VALUES onward
            value_tokens = tokens[idx:]
            result["value_tokens"] = value_tokens
            break

        idx += 1

    return result


def parse_copybook(file_path: str) -> list[CopybookRecord]:
    """Parse a COBOL copybook file and return structured records.

    Reads .cpy file line by line, strips sequence numbers, handles
    continuation lines, tokenizes level/field/PIC/VALUE/REDEFINES/OCCURS,
    builds hierarchical tree based on level numbers, and extracts
    88-level conditions as Condition88 attached to parent field.
    """
    path = Path(file_path)
    raw_lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    statements = _preprocess_lines(raw_lines)

    records: list[CopybookRecord] = []
    # Stack for building hierarchy: list of (level, CobolField)
    stack: list[tuple[int, CobolField]] = []
    current_record: CopybookRecord | None = None

    for stmt in statements:
        info = _tokenize_statement(stmt)
        if "level" not in info:
            continue

        level = info["level"]
        name = info.get("name", "FILLER")

        # Handle 88-level conditions
        if level == 88:
            value_tokens = info.get("value_tokens", [])
            if value_tokens and stack:
                parent_field = stack[-1][1]
                cond = parse_value_clause([name] + value_tokens)
                parent_field.conditions.append(cond)
            continue

        # Create the field
        is_filler = name.upper() == "FILLER"
        fld = CobolField(level=level, name=name, is_filler=is_filler)

        if "pic" in info:
            pic_str = info["pic"]
            fld.pic_clause = pic_str
            pic_type, length, dec, signed = parse_pic_clause(pic_str)
            fld.pic_type = pic_type
            fld.length = length
            fld.decimal_places = dec
            fld.is_signed = signed

        if "redefines" in info:
            fld.redefines = info["redefines"]

        if "occurs" in info:
            fld.occurs = info["occurs"]

        # Handle 01-level as a new record
        if level == 1 or level == 77:
            current_record = CopybookRecord(
                name=name,
                source_file=path.name,
            )
            records.append(current_record)
            stack = [(level, fld)]
            if level == 1:
                # 01-level is the record itself; fields are children
                current_record.fields = fld.children
                # Keep fld on stack so children attach to it
            continue

        # Pop stack to find parent
        while stack and stack[-1][0] >= level:
            stack.pop()

        if stack:
            parent = stack[-1][1]
            parent.children.append(fld)
        elif current_record is not None:
            current_record.fields.append(fld)

        stack.append((level, fld))

    return records
