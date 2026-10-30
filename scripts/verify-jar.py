#!/usr/bin/env python3
"""Check Gradle's deployable JAR against the source baseline (no extra packages)."""

from pathlib import Path
import sys
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
VERSION = "26.3"
MAIN = "io/github/rypofalem/armorstandeditor/ArmorStandEditorPlugin.class"


def main() -> None:
    jar = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "build/libs" / f"armorstandeditor-{VERSION}.jar"
    if not jar.is_file():
        raise SystemExit(f"Missing JAR: {jar}")

    source_resources = ROOT / "src/main/resources"
    source_java = ROOT / "src/main/java"
    resources = {p.relative_to(source_resources).as_posix(): p.read_bytes() for p in source_resources.rglob('*') if p.is_file()}
    classes = {p.relative_to(source_java).with_suffix('.class').as_posix() for p in source_java.rglob('*.java')}
    expected_descriptor = resources.pop('plugin.yml').replace(b'${project.version}', VERSION.encode('ascii'))

    with ZipFile(jar) as archive:
        names = set(archive.namelist())
        expected = set(resources) | classes | {'plugin.yml'}
        missing = expected - names
        assert not missing, f"Missing JAR entries: {sorted(missing)}"
        assert archive.read('plugin.yml') == expected_descriptor, 'Packaged plugin.yml changed unexpectedly'
        for path, value in resources.items():
            assert archive.read(path) == value, f"Packaged resource differs: {path}"
        for path in names:
            if path.endswith('.class'):
                bytecode = archive.read(path)
                assert bytecode[:4] == b'\xca\xfe\xba\xbe', f"Invalid class file: {path}"
                assert int.from_bytes(bytecode[6:8], 'big') == 69, f"Not Java 25 bytecode: {path}"
        assert MAIN in names, 'Main class missing'
        assert 'version: 26.3' in archive.read('plugin.yml').decode('utf-8')
        assert '${project.version}' not in archive.read('plugin.yml').decode('utf-8')

    print(f"PASS: {jar.name}; descriptor matches source with version resolved; all resources unchanged; Java 25 bytecode")


if __name__ == '__main__':
    main()
