"""OpenAPI 3.0.3 Spec Generator from parsed COBOL copybook records.

Maps CobolField trees to JSON Schema types and assembles a complete
OpenAPI specification document.
"""

from __future__ import annotations

from pathlib import Path
from typing import Any

import yaml

from .cobol_parser import CobolField, CopybookRecord


def _cobol_name_to_camel(name: str) -> str:
    """Convert COBOL-DASH-NAME to camelCase for JSON property names.

    Examples:
      'ACCT-ID'          -> 'acctId'
      'CUST-FIRST-NAME'  -> 'custFirstName'
      'FD-TIMESTAMP'     -> 'fdTimestamp'
    """
    parts = name.lower().split("-")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def _cobol_name_to_schema(name: str) -> str:
    """Convert COBOL-DASH-NAME to PascalCase for schema component names.

    Examples:
      'ACCOUNT-RECORD'  -> 'AccountRecord'
      'DALYTRAN-RECORD' -> 'DalytranRecord'
      'CC-WORK-AREAS.'  -> 'CcWorkAreas'
    """
    # Strip trailing periods that may remain from COBOL source
    clean = name.rstrip(".")
    return "".join(part.capitalize() for part in clean.lower().split("-"))


def cobol_field_to_json_schema(field: CobolField) -> dict[str, Any] | None:
    """Convert a single CobolField (and its children) to a JSON Schema object.

    Returns None for FILLER fields (they are padding and should be omitted).
    """
    if field.is_filler:
        return None

    schema: dict[str, Any] = {}

    # Group item (no PIC clause) — becomes an object with properties
    if field.pic_clause is None and field.children:
        schema["type"] = "object"
        properties: dict[str, Any] = {}
        required: list[str] = []
        for child in field.children:
            child_schema = cobol_field_to_json_schema(child)
            if child_schema is None:
                continue
            prop_name = _cobol_name_to_camel(child.name)
            properties[prop_name] = child_schema
            required.append(prop_name)
        if properties:
            schema["properties"] = properties
        if required:
            schema["required"] = required

    elif field.pic_type == "alphanumeric":
        schema["type"] = "string"
        if field.length:
            schema["maxLength"] = field.length

    elif field.pic_type == "numeric":
        schema["type"] = "integer"
        if field.pic_clause:
            schema["x-cobol-pic"] = field.pic_clause

    elif field.pic_type in ("decimal", "signed_decimal"):
        schema["type"] = "number"
        schema["format"] = "decimal"
        if field.decimal_places:
            schema["x-decimal-places"] = field.decimal_places
        if field.pic_clause:
            schema["x-cobol-pic"] = field.pic_clause

    elif field.pic_clause is None and not field.children:
        # Elementary item with no PIC and no children — treat as string
        schema["type"] = "string"

    else:
        # Fallback
        schema["type"] = "string"
        if field.pic_clause:
            schema["x-cobol-pic"] = field.pic_clause

    # Add REDEFINES extension
    if field.redefines:
        schema["x-redefines"] = field.redefines

    # Attach 88-level conditions as validation rules
    if field.conditions:
        _attach_conditions(schema, field)

    # COBOL metadata
    if field.pic_clause:
        schema["x-cobol-pic"] = field.pic_clause

    # Handle OCCURS — wrap in array
    if field.occurs is not None:
        array_schema: dict[str, Any] = {
            "type": "array",
            "items": schema,
            "minItems": field.occurs,
            "maxItems": field.occurs,
        }
        return array_schema

    return schema


def _attach_conditions(schema: dict[str, Any], field: CobolField) -> None:
    """Attach 88-level condition validation rules to a JSON Schema dict.

    - Single value → const or enum with one value
    - Multiple values → enum list
    - Single THROUGH range → minimum / maximum
    - Multiple conditions are combined into x-conditions for full detail
    """
    all_values: list = []
    all_ranges: list[tuple] = []

    for cond in field.conditions:
        all_values.extend(cond.values)
        all_ranges.extend(cond.through_ranges)

    # Store full condition details in extension
    cond_list = []
    for cond in field.conditions:
        entry: dict[str, Any] = {"name": cond.name}
        if cond.values:
            entry["values"] = cond.values
        if cond.through_ranges:
            entry["ranges"] = [{"from": r[0], "to": r[1]} for r in cond.through_ranges]
        cond_list.append(entry)
    if cond_list:
        schema["x-conditions"] = cond_list

    # If there are only discrete values, add enum
    if all_values and not all_ranges:
        # Deduplicate preserving order
        seen = set()
        unique: list = []
        for v in all_values:
            key = str(v)
            if key not in seen:
                seen.add(key)
                unique.append(v)
        if len(unique) == 1:
            schema["enum"] = unique
        else:
            schema["enum"] = unique

    # If there's exactly one THROUGH range and no discrete values,
    # use minimum/maximum
    elif all_ranges and not all_values and len(all_ranges) == 1:
        lo, hi = all_ranges[0]
        if isinstance(lo, (int, float)):
            schema["minimum"] = lo
        if isinstance(hi, (int, float)):
            schema["maximum"] = hi


def record_to_openapi_schema(record: CopybookRecord) -> dict[str, Any]:
    """Convert a CopybookRecord to an OpenAPI component schema."""
    schema: dict[str, Any] = {
        "type": "object",
        "x-source-copybook": record.source_file,
    }
    properties: dict[str, Any] = {}
    required: list[str] = []

    for field in record.fields:
        child_schema = cobol_field_to_json_schema(field)
        if child_schema is None:
            continue
        prop_name = _cobol_name_to_camel(field.name)
        properties[prop_name] = child_schema
        required.append(prop_name)

    if properties:
        schema["properties"] = properties
    if required:
        schema["required"] = required

    return schema


def generate_openapi_spec(
    records: list[CopybookRecord],
    title: str = "CardDemo COBOL Copybook API",
    version: str = "1.0.0",
    description: str | None = None,
) -> dict[str, Any]:
    """Build a complete OpenAPI 3.0.3 specification from parsed copybook records."""
    if description is None:
        description = (
            "Auto-generated OpenAPI specification from COBOL copybook "
            "data structures in the AWS CardDemo mainframe modernization "
            "application. Each schema corresponds to a COBOL 01-level "
            "record definition."
        )

    spec: dict[str, Any] = {
        "openapi": "3.0.3",
        "info": {
            "title": title,
            "version": version,
            "description": description,
        },
        "paths": {},
        "components": {
            "schemas": {},
        },
    }

    seen_names: dict[str, int] = {}
    for record in records:
        schema_name = _cobol_name_to_schema(record.name)
        # Handle duplicate schema names by appending a suffix
        if schema_name in seen_names:
            seen_names[schema_name] += 1
            schema_name = f"{schema_name}{seen_names[schema_name]}"
        else:
            seen_names[schema_name] = 1
        spec["components"]["schemas"][schema_name] = record_to_openapi_schema(record)

    return spec


def write_openapi_yaml(spec: dict[str, Any], output_path: str) -> None:
    """Write the OpenAPI spec dict to a YAML file."""
    path = Path(output_path)
    path.parent.mkdir(parents=True, exist_ok=True)

    # Custom representer to handle multiline strings and ordering
    class _Dumper(yaml.SafeDumper):
        pass

    def _str_representer(dumper: yaml.Dumper, data: str) -> Any:
        if "\n" in data:
            return dumper.represent_scalar("tag:yaml.org,2002:str", data, style="|")
        return dumper.represent_scalar("tag:yaml.org,2002:str", data)

    _Dumper.add_representer(str, _str_representer)

    yaml_str = yaml.dump(
        spec,
        Dumper=_Dumper,
        default_flow_style=False,
        sort_keys=False,
        allow_unicode=True,
        width=120,
    )
    path.write_text(yaml_str, encoding="utf-8")
