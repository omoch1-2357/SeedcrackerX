#!/usr/bin/env python3
"""Audit the produced SeedcrackerX JAR and its nested mod surface.

The source audit catches obvious regressions before compilation. This audit
checks what is actually packaged: the client-only metadata, nested Fabric mods,
and forbidden outbound/networking symbols in SeedcrackerX's own class files.
"""

from __future__ import annotations

import io
import json
import sys
import zipfile
from pathlib import Path

FORBIDDEN_CLASS_TOKENS = (
    b"java/net/",
    b"java/net/http/",
    b"ClientPlayNetworking",
    b"ServerPlayNetworking",
    b"PayloadTypeRegistry",
    b"CustomPacketPayload",
    b"ServerboundPlayerActionPacket",
    b"joinServer",
    b"openUri",
    b"HttpClient",
    b"HttpRequest",
    b"HttpResponse",
    b"Socket",
    b"DatagramSocket",
    b"http://",
    b"https://",
)

FORBIDDEN_NESTED_MOD_IDS = {
    "cloth-config",
    "modmenu",
    "fabric-networking-api-v1",
}


def read_mod_json(zf: zipfile.ZipFile) -> dict | None:
    try:
        return json.loads(zf.read("fabric.mod.json"))
    except KeyError:
        return None


def nested_mod_ids(zf: zipfile.ZipFile) -> list[tuple[str, str]]:
    result: list[tuple[str, str]] = []
    for name in sorted(zf.namelist()):
        if not name.startswith("META-INF/jars/") or not name.endswith(".jar"):
            continue
        with zipfile.ZipFile(io.BytesIO(zf.read(name))) as nested:
            metadata = read_mod_json(nested)
            if metadata is not None:
                result.append((str(metadata.get("id", "<missing-id>")), name))
    return result


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: audit_built_jar.py <jar>", file=sys.stderr)
        return 2

    jar_path = Path(sys.argv[1])
    report_path = Path("build/reports/passive-jar-audit.txt")
    report_path.parent.mkdir(parents=True, exist_ok=True)

    errors: list[str] = []
    report: list[str] = [f"JAR: {jar_path}"]

    with zipfile.ZipFile(jar_path) as zf:
        metadata = read_mod_json(zf)
        if metadata is None:
            errors.append("outer JAR has no fabric.mod.json")
        else:
            report.append(f"mod id: {metadata.get('id')}")
            report.append(f"version: {metadata.get('version')}")
            report.append(f"environment: {metadata.get('environment')}")
            if metadata.get("environment") != "client":
                errors.append("outer mod is not declared client-only")

        nested = nested_mod_ids(zf)
        report.append("nested mod ids:")
        if nested:
            for mod_id, path in nested:
                report.append(f"  - {mod_id}: {path}")
                if mod_id in FORBIDDEN_NESTED_MOD_IDS:
                    errors.append(f"forbidden nested mod id: {mod_id} ({path})")
        else:
            report.append("  (none)")

        for name in sorted(zf.namelist()):
            if not name.startswith("kaptainwutax/seedcrackerX/") or not name.endswith(".class"):
                continue
            data = zf.read(name)
            for token in FORBIDDEN_CLASS_TOKENS:
                if token in data:
                    errors.append(f"{name}: forbidden class token {token.decode('ascii', errors='replace')}")

    if errors:
        report.append("status: FAILED")
        report.extend(f"ERROR: {error}" for error in errors)
    else:
        report.append("status: PASSED")

    report_path.write_text("\n".join(report) + "\n", encoding="utf-8")
    print(report_path.read_text(encoding="utf-8"), end="")
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
