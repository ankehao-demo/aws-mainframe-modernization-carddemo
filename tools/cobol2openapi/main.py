"""CLI entry point for COBOL Copybook to OpenAPI Spec generator.

Usage:
    python -m tools.cobol2openapi.main --input-dir app/cpy/ --output openapi-specs/carddemo-openapi.yaml
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from .cobol_parser import CopybookRecord, parse_copybook
from .openapi_generator import generate_openapi_spec, write_openapi_yaml


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Generate OpenAPI 3.x specs from COBOL copybook files."
    )
    parser.add_argument(
        "--input-dir",
        required=True,
        help="Directory containing .cpy copybook files.",
    )
    parser.add_argument(
        "--output",
        required=True,
        help="Output path for the generated OpenAPI YAML file.",
    )
    parser.add_argument(
        "--title",
        default="CardDemo COBOL Copybook API",
        help="Title for the OpenAPI spec (default: CardDemo COBOL Copybook API).",
    )
    parser.add_argument(
        "--version",
        default="1.0.0",
        help="API version string (default: 1.0.0).",
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Print details about each parsed copybook.",
    )

    args = parser.parse_args(argv)
    input_dir = Path(args.input_dir)

    if not input_dir.is_dir():
        print(f"Error: {input_dir} is not a directory.", file=sys.stderr)
        return 1

    cpy_files = sorted(input_dir.glob("*.cpy")) + sorted(input_dir.glob("*.CPY"))
    if not cpy_files:
        print(f"Error: No .cpy files found in {input_dir}.", file=sys.stderr)
        return 1

    print(f"Found {len(cpy_files)} copybook file(s) in {input_dir}")

    all_records: list[CopybookRecord] = []
    errors: list[str] = []

    for cpy_file in cpy_files:
        try:
            records = parse_copybook(str(cpy_file))
            for rec in records:
                if args.verbose:
                    field_count = _count_fields(rec)
                    cond_count = _count_conditions(rec)
                    print(
                        f"  {cpy_file.name}: record={rec.name}, "
                        f"fields={field_count}, conditions={cond_count}"
                    )
                all_records.append(rec)
        except Exception as exc:
            msg = f"  WARNING: Failed to parse {cpy_file.name}: {exc}"
            print(msg, file=sys.stderr)
            errors.append(msg)

    if not all_records:
        print("Error: No records parsed from any copybook.", file=sys.stderr)
        return 1

    print(f"Parsed {len(all_records)} record(s) from {len(cpy_files)} file(s)")

    spec = generate_openapi_spec(
        all_records,
        title=args.title,
        version=args.version,
    )

    write_openapi_yaml(spec, args.output)
    print(f"OpenAPI spec written to {args.output}")

    schema_count = len(spec.get("components", {}).get("schemas", {}))
    print(f"  Schemas generated: {schema_count}")

    if errors:
        print(f"\n{len(errors)} warning(s) during parsing:")
        for e in errors:
            print(e)

    return 0


def _count_fields(record: CopybookRecord) -> int:
    """Count total fields (recursively) in a record."""

    def _count(fields: list) -> int:
        total = 0
        for f in fields:
            if not f.is_filler:
                total += 1
            total += _count(f.children)
        return total

    return _count(record.fields)


def _count_conditions(record: CopybookRecord) -> int:
    """Count total 88-level conditions in a record."""

    def _count(fields: list) -> int:
        total = 0
        for f in fields:
            total += len(f.conditions)
            total += _count(f.children)
        return total

    return _count(record.fields)


if __name__ == "__main__":
    sys.exit(main())
