# SportsPulse
### Multi-Sport Analytics & Performance Platform — Pure Java (17+)

SportsPulse is a console-based sports analytics and performance platform covering **Football, Basketball, and Cricket**, natively demonstrating 24 classic algorithms across six modules, an academic Algorithm Laboratory, PBKDF2 Carter-Wegman universal hashing, zero external libraries, and a 10/10 ANSI presentation system.

---

## 1. Quick Start & Execution Options

SportsPulse is **100% self-contained and editor-independent**. It contains ZERO hardcoded machine paths, requires no third-party libraries, and can be extracted to ANY folder on ANY computer (Windows, Linux, macOS) without code changes.

### Prerequisites
* **Java**: JDK 17+ (built and tested on JDK 17, 21, and 25 LTS).
* **Apache Ant** *(optional)*: for Ant-based automated builds.

### Option 1: 1-Click Launch Scripts (Recommended)
* **Windows CMD / PowerShell**:
  ```cmd
  run.bat
  ```
* **Linux / macOS**:
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

### Option 2: Pre-Built Runnable JAR
```bash
# Run directly from the project root:
java -jar dist/SportsPulse.jar

# Or run from ANY other directory on your machine:
java -jar /path/to/SportsPulse-Java/dist/SportsPulse.jar
```

### Option 3: Apache Ant Build
```bash
ant clean     # removes build/, dist/, bin/
ant compile   # compiles src/ into build/classes/
ant run       # executes interactive application
ant test      # executes automated 173+ assertion test suite
ant jar       # generates standalone dist/SportsPulse.jar
ant env-check # verifies host portability and environment readiness
```

### Option 4: Pure Shell / Batch Scripts (No Ant Needed)
* **Windows**:
  ```cmd
  build.bat build   # compile everything into bin/
  build.bat test    # run self-test verification suite
  build.bat run     # launch application
  build.bat jar     # package dist\SportsPulse.jar
  build.bat check   # run host portability diagnostics
  build.bat clean   # clean build artifacts
  ```
* **Linux / macOS / Git Bash**:
  ```bash
  ./build.sh build
  ./build.sh test
  ./build.sh run
  ./build.sh jar
  ./build.sh check
  ./build.sh clean
  ```

### Option 5: IDE Integration (VS Code, IntelliJ IDEA, Eclipse, NetBeans)
* **VS Code**: Open the `SportsPulse-Java` folder. The Java Language Support extension detects `src/`. Run or debug `com.sportspulse.Main`.
* **IntelliJ IDEA**: Open or Import folder as a Project. Mark `src` as *Sources Root*. Run `com.sportspulse.Main`.
* **Eclipse**: File -> Import -> Existing Projects into Workspace. The included `.project` and `.classpath` configure `src` automatically. Run as Java Application.
* **NetBeans**: File -> Open Project (Ant-compatible) or Open Folder. Run `com.sportspulse.Main`.

---

## 2. Host Portability & Directory Architecture

SportsPulse is engineered to be **location-independent, username-independent, drive-independent, and OS-conscious**:

```
SportsPulse-Java/
├── src/                          # Pure Java authoritative source code
│   └── com/sportspulse/
│       ├── Main.java             # Platform entry point & portal loop
│       ├── EnvironmentCheck.java # Automated host portability verifier
│       ├── SelfTest.java         # 173+ automated regression & security tests
│       ├── admin/                # Admin Algorithm Lab & telemetry console
│       ├── algo/                 # 24 core DSA algorithms (M1-M6)
│       ├── auth/                 # PBKDF2 & defensive credential store
│       ├── benchmark/            # Empirical complexity runner & CSV exporter
│       ├── cli/                  # 6 interactive domain menus
│       ├── data/                 # In-memory mock data (Football, Basketball, Cricket)
│       ├── hashing/              # Carter-Wegman universal hash table
│       ├── model/                # Core entities (Player, Team, Match, Facility)
│       └── util/                 # AppPaths, Ansi, TerminalRenderer, TerminalMenu, etc.
│
├── config/                       # Portable configuration
│   └── sportspulse.properties    # Environment & UI defaults
│
├── resources/                    # Packaged static assets
│   └── README.md                 # Resources guide
│
├── runtime/                      # Auto-created writable directory for output
│   ├── logs/                     # Application diagnostic logs
│   ├── reports/                  # Generated match reports & summaries
│   ├── benchmarks/               # Exported empirical performance CSVs
│   └── data/                     # Local working data
│
├── dist/                         # Distribution output
│   └── SportsPulse.jar           # Standalone runnable JAR (Main-Class configured)
│
├── build.xml                     # Standard Apache Ant build script
├── build.bat / build.sh          # Dependency-free build scripts
├── run.bat / run.sh              # 1-click cross-platform launch scripts
├── .classpath / .project         # Standard Eclipse project metadata
├── .gitignore                    # Portable exclusion patterns
└── README.md                     # Comprehensive project documentation
```

### Central Path Resolution (`AppPaths.java`)
All file writes and directory references are resolved via `com.sportspulse.util.AppPaths`:
1. **Dynamic CodeSource Detection**: Automatically discovers whether the application is running from an IDE, exploded classes directory (`bin/`), or a JAR (`dist/`), anchoring all runtime paths relative to the project container.
2. **Configurable Overrides**: Can be explicitly overridden at runtime using `-Dsportspulse.home=/path/to/app` or environment variable `SPORTSPULSE_HOME`.
3. **Automatic Creation**: All runtime directories (`runtime/logs`, `runtime/reports`, `runtime/benchmarks`, `runtime/data`) are created dynamically on demand.

### Automated Environment Portability Check
Run the diagnostic check anytime to verify host environment readiness:
```bash
java -cp bin com.sportspulse.EnvironmentCheck
# or
ant env-check
```

---

## 3. Terminal Presentation System (10/10 ANSI / TrueColor Architecture)

SportsPulse includes a **10/10 pure-Java terminal presentation engine** built from first principles with zero external dependencies (no JLine, Lanterna, Jansi, or curses).

#### 1. Dual-Mode Interactive TUI & Script Fallback
* **Interactive Mode (Real TTY)**: When attached to an interactive console (`System.console() != null`), menus render with real-time arrow navigation, an active cursor selection indicator (`❯`), high-density viewport status bars, and keyboard shortcuts:
  * **Navigation**: `↑` / `↓` Arrow Keys, Vim keys (`k` / `j`), Gamer keys (`w` / `s`)
  * **Selection**: `Enter` (selects highlighted item)
  * **Direct Jump**: `1`-`9`, `0` (instant jump/select without arrow scrolling)
  * **Cancel / Exit**: `q`, `Q`, `Esc`, `0`
* **Headless / Piped Fallback**: When input is redirected, piped, or running in CI (`build.bat test`, automated test runners, or `build_dossier.py`), the engine automatically switches to standard numeric line prompts (`Select option: `). It **never polls, hangs, or crashes** on EOF.

#### 2. TrueColor (24-bit RGB) & Mathematical Fallback
* Generates 24-bit TrueColor escape codes (`ESC[38;2;r;g;bm` / `ESC[48;2;r;g;bm`) for 16.7M colors.
* If running on legacy or standard 16-color ANSI terminals, RGB triplets are mapped to the closest matching 4-bit ANSI color using **Euclidean 3D color distance**:
  $$\Delta = (r - r_i)^2 + (g - g_i)^2 + (b - b_i)^2$$
* Supports `-Dsportspulse.color=false`, `-Dsportspulse.color.mode=mono|ansi16|truecolor`, and the `NO_COLOR` standard.

#### 3. Responsive Geometry & Box Borders
* Adapts layouts across three column tiers: **NARROW** (65 cols), **STANDARD** (80 cols), and **WIDE** (120 cols).
* Responsive tables measure visible printable width via regex ANSI stripping (`Ansi.strip()`) so color codes never distort column alignment. Overlong columns are cleanly truncated with ellipsis (`...`).
* Seamlessly renders Unicode double borders (`╔═╗`, `│`, `├─┼─┤`, `╚═╝`) with automatic ASCII fallback (`+--+`, `|`).

#### 4. Terminal Safety & Cursor Restoration
* Hiding the cursor without restoration can corrupt the host terminal if a process terminates abruptly.
* `TerminalRenderer` installs an unconditional **JVM shutdown hook** (`Runtime.getRuntime().addShutdownHook`) ensuring `ESC[?25h` (show cursor) and `ESC[0m` (reset ANSI) are emitted even on `Ctrl+C` or unexpected termination.

```
+-------------------------------------------------------------+
|                 SportsPulse Terminal Engine                 |
+-------------------------------------------------------------+
                              |
      +-----------------------+-----------------------+
      |                                               |
+-----v-------------------+               +-----------v-----------+
|  TerminalInput (TUI)    |               |  TerminalRenderer     |
|  - Arrow Key Parser     |               |  - Box Headers (╔═╗)  |
|  - Vim (w/s, k/j)       |               |  - Status Bar         |
|  - Direct Numeric Jump  |               |  - JVM Safety Hook    |
|  - Piped Input Fallback |               |  - Screen Clearing    |
+-------------------------+               +-----------+-----------+
              |                                       |
              +-------------------+-------------------+
                                  |
                   +--------------v--------------+
                   |     ConsoleCapabilities     |
                   | - TrueColor / ANSI16 / Mono |
                   | - Interactive vs Headless   |
                   | - Terminal Width Tier       |
                   | - Unicode Glyph Detection   |
                   +--------------+--------------+
                                  |
                   +--------------v--------------+
                   |  Ansi & TerminalTheme       |
                   | - 24-Bit RGB (ESC[38;2;m)   |
                   | - Euclidean ANSI16 Centroid |
                   | - Accessibility Badges      |
                   +-----------------------------+
```

---

## 4. System Architecture (ASCII)

```
                              +---------------------------+
                              |         Main.java          |
                              |   (menu loop / dispatcher) |
                              +--------------+--------------+
                                             |
        +----------------+----------------+-+--------------+----------------+----------------+
        |                |                |                |                |                |
 +------v------+  +------v------+  +------v------+  +------v------+  +------v------+  +------v------+
 |SearchMenu   |  |ReportsMenu  |  |SquadBuilder |  |FacilityMenu |  |SelectionAdv.|  |LiveMatchMenu|
 |(cli)        |  |(cli)        |  |Menu (cli)   |  |(cli)        |  |Menu (cli)   |  |(cli)        |
 +------+------+  +------+------+  +------+------+  +------+------+  +------+------+  +------+------+
        |                |                |                |                |                |
 =======+================+================+================+================+================+=======
        |  M1: STRING    |  M2: SUFFIX    |  M3: DP        |  M4: FLOW      |  M5: NP-COMPL. |  M6: RAND/PAR.
        |  MATCHING      |  STRUCTURES    |                |  & MATCHING    |  & APPROX.     |
 -------+----------------+----------------+----------------+----------------+----------------+-------
        | KMP            | SuffixArray    | EditDistance   | FlowGraph      | SATSolver      | RandomizedQuickSort
        | ZFunction      | Kasai (LCP)    |  (Levenshtein/ | FordFulkerson  | ThreeSatToClique| ReservoirSampling
        | RabinKarp      | SuffixAutomaton|   Damerau)     | EdmondsKarp    | GraphReductions | MillerRabin
        | AhoCorasick    |                | BitmaskDP      | Dinic          |  (Clique/IS/VC/ | BlellochScan
        |                |                | MatrixChain    | BipartiteMatch |   2-approx VC)  | ParallelReduce
        |                |                | SubsetSelectDP |  + Konig       |                 | BrentTheorem
 -------+----------------+----------------+----------------+----------------+----------------+-------
        |                                                                                       |
        +---------------------------------------------------------------------------------------+
                                             |
                              +--------------v--------------+
                              |          model/*.java        |
                              |  Sport, Player, Team, Match,  |
                              |  MatchEvent, MatchReport,     |
                              |  Facility                     |
                              +--------------+--------------+
                                             |
                              +--------------v--------------+
                              |     data/MockData.java       |
                              |  Football, Basketball,       |
                              |  Cricket: teams, players,     |
                              |  matches, event streams,      |
                              |  commentary reports,          |
                              |  training facilities          |
                              +------------------------------+
```

---

## 5. Feature-to-Algorithm Map

| CLI Feature | Algorithms used | Course module |
|---|---|---|
| **Search & Lookup** | KMP, Z-Function, Rabin-Karp, Aho-Corasick, Damerau-Levenshtein | M1, M3 |
| **Reports & Commentary Analysis** | Suffix Array, Kasai's LCP, Suffix Automaton | M2 |
| **Squad Builder** | Bitmask DP, DP on Subsets (knapsack), Matrix-Chain Multiplication | M3 |
| **Facility Scheduler** | Ford-Fulkerson, Edmonds-Karp (+min-cut), Dinic's, Hopcroft-Karp Bipartite Matching, König's theorem | M4 |
| **Selection Advisor** | SAT/3-SAT Solver, 3-SAT→CLIQUE reduction, Max Clique, Max Independent Set, Min Vertex Cover (exact + 2-approx) | M5 |
| **Live Match Center** | Reservoir Sampling, Blelloch Parallel Scan, Parallel Reduce (Fork/Join), Randomized QuickSort, Miller-Rabin, Brent's Theorem | M6 |
| **Admin Algorithm Lab** | Carter-Wegman Universal Hashing, Separate Chaining, PBKDF2 Hashing, Constant-Time Verify, Empirical Benchmarks | Core & M6 |

---

## 6. Troubleshooting Guide

### 1. `java` or `javac` not recognized
Ensure JDK 17 or higher is installed and added to your system `PATH`:
```bash
java -version
javac -version
```
If you have multiple Java versions installed, ensure `JAVA_HOME` points to JDK 17+.

### 2. `ant` not recognized
If Apache Ant is not installed on your system, use the bundled zero-dependency scripts:
* On Windows: `build.bat {build|test|run|jar|check|clean}` or `run.bat`
* On Linux/macOS: `./build.sh {build|test|run|jar|check|clean}` or `./run.sh`

### 3. Raw ANSI escape codes showing in terminal
If your terminal does not support ANSI formatting (e.g. classic Windows `cmd.exe` without virtual terminal support), disable ANSI styling completely:
```bash
java -Dsportspulse.color=false -jar dist/SportsPulse.jar
# or via environment variable:
set NO_COLOR=1
```

### 4. Arrow keys not responding in terminal
When running in environments that do not pass raw escape codes or line-buffer standard input, SportsPulse automatically falls back to clean numeric choices. Simply type the option number (`1`, `2`, `0`) and press `Enter`. You can also navigate using `w`/`s` or `k`/`j`.

### 5. Runtime permissions or file access issues
SportsPulse writes runtime files (exported CSV benchmarks, diagnostic logs) strictly into the relative `runtime/` folder inside the project container. Ensure the user running the process has write permission for the project directory.
