# SportsPulse Bundled Resources

This directory contains packaged, read-only application resources such as:
- Default sport specifications
- Benchmark test fixtures
- Sample commentary text corpora for string matching algorithms (KMP, Rabin-Karp, Aho-Corasick)
- Report templates

These resources are packaged into `dist/SportsPulse.jar` during build and can be accessed via `ClassLoader.getResourceAsStream(...)`.
